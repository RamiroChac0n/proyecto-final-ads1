-- =========================================================
-- SCRIPT PARA BORRAR TODAS LAS TABLAS (orden correcto)
-- =========================================================
DROP TABLE IF EXISTS inventory_movements CASCADE;
DROP TABLE IF EXISTS product_batches CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS dosage_forms CASCADE;
DROP TABLE IF EXISTS active_principles CASCADE;
DROP TABLE IF EXISTS product_categories CASCADE;
DROP TABLE IF EXISTS product_types CASCADE;
DROP TABLE IF EXISTS concentration_units CASCADE;
DROP TABLE IF EXISTS branches CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP SEQUENCE IF EXISTS product_sequence;

-- =========================================================
-- CREACIÓN DE TABLAS
-- =========================================================

-- Usuarios
CREATE TABLE users (
    id VARCHAR(13) PRIMARY KEY, -- DPI guatemalteco
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(8),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Para hash BCrypt
    role VARCHAR(20) NOT NULL,
    
    -- Constraints adicionales
    CONSTRAINT chk_id_length CHECK (LENGTH(id) = 13),
    CONSTRAINT chk_phone_length CHECK (phone_number IS NULL OR LENGTH(phone_number) = 8),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Tipos de producto
CREATE TABLE product_types (
    type_code VARCHAR(3) PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categorías de producto
CREATE TABLE product_categories (
    category_code VARCHAR(3) PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    requires_prescription BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Principios activos
CREATE TABLE active_principles (
    principle_code VARCHAR(3) PRIMARY KEY,
    inn_name VARCHAR(200) NOT NULL,
    therapeutic_action TEXT,
    contraindications TEXT,
    requires_prescription BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Formas farmacéuticas
CREATE TABLE dosage_forms (
    form_code VARCHAR(3) PRIMARY KEY,
    form_name VARCHAR(50) NOT NULL,
    route_administration VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Concentraciones
CREATE TABLE concentration_units (
    unit_code VARCHAR(3) PRIMARY KEY,   -- ej. MG, ML, UI
    unit_name VARCHAR(50) NOT NULL,      -- ej. Miligramos, Mililitros, Unidades Internacionales
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Productos
CREATE TABLE products (
    -- Llave primaria numérica para performance
    product_id SERIAL PRIMARY KEY,
    
    -- Componentes del código (para búsquedas y reconstrucción)
    product_type VARCHAR(3) NOT NULL REFERENCES product_types(type_code),
    category_code VARCHAR(3) NOT NULL REFERENCES product_categories(category_code),
    principle_code VARCHAR(3) NOT NULL REFERENCES active_principles(principle_code),
    concentration VARCHAR(10) NOT NULL,
    concentration_unit VARCHAR(3) NOT NULL REFERENCES concentration_units(unit_code),
    dosage_form VARCHAR(3) NOT NULL REFERENCES dosage_forms(form_code),
    sequence_number INTEGER DEFAULT 0,
	
    commercial_name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    manufacturer VARCHAR(200) NOT NULL,
    requires_prescription BOOLEAN DEFAULT false,
    min_stock INTEGER DEFAULT 0,
    max_stock INTEGER DEFAULT 1000,
    current_stock INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices
    CONSTRAINT uk_product_components UNIQUE (
        product_type, category_code, principle_code, 
        concentration, dosage_form, sequence_number
    )
);

-- Función que maneja la lógica del sequence_number
CREATE OR REPLACE FUNCTION handle_product_sequence()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo procesar si sequence_number no fue especificado explícitamente (es 0 o NULL)
    IF NEW.sequence_number IS NULL OR NEW.sequence_number = 0 THEN
        -- Buscar el máximo sequence_number para esta combinación de componentes
        SELECT COALESCE(MAX(sequence_number), -1) + 1
        INTO NEW.sequence_number
        FROM products 
        WHERE product_type = NEW.product_type
            AND category_code = NEW.category_code
            AND principle_code = NEW.principle_code
            AND concentration = NEW.concentration
            AND dosage_form = NEW.dosage_form
            AND (product_id != NEW.product_id OR NEW.product_id IS NULL); -- Excluir el registro actual en UPDATEs
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear el trigger que se ejecuta antes de INSERT
DROP TRIGGER IF EXISTS trg_product_sequence_insert ON products;
CREATE TRIGGER trg_product_sequence_insert
    BEFORE INSERT ON products
    FOR EACH ROW
    EXECUTE FUNCTION handle_product_sequence();

-- Crear el trigger que se ejecuta antes de UPDATE (versión simplificada)
DROP TRIGGER IF EXISTS trg_product_sequence_update ON products;
CREATE TRIGGER trg_product_sequence_update
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION handle_product_sequence();

-- Lotes de productos
CREATE TABLE product_batches (
    batch_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(product_id),
    batch_number VARCHAR(50) NOT NULL,
    quantity_received INTEGER NOT NULL,
    quantity_available INTEGER NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    sale_price DECIMAL(10,2) NOT NULL,
    manufacture_date DATE,
    expiration_date DATE NOT NULL,
    received_date DATE DEFAULT CURRENT_DATE,
    is_active BOOLEAN DEFAULT true,
    is_expired BOOLEAN DEFAULT false,
    days_until_expiration INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_batch_number UNIQUE (product_id, batch_number),
    CONSTRAINT chk_dates CHECK (expiration_date > manufacture_date),
    CONSTRAINT chk_quantity CHECK (quantity_available >= 0 AND quantity_available <= quantity_received)
);

-- Función para actualizar campos relacionados con la expiración
CREATE OR REPLACE FUNCTION update_batch_expiry_fields()
RETURNS TRIGGER AS $$
BEGIN
    NEW.is_expired := (NEW.expiration_date < CURRENT_DATE);
    NEW.days_until_expiration := (NEW.expiration_date - CURRENT_DATE);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar campos relacionados con la expiración
CREATE TRIGGER trg_update_batch_expiry
BEFORE INSERT OR UPDATE ON product_batches
FOR EACH ROW
EXECUTE FUNCTION update_batch_expiry_fields();

-- Sucursales
CREATE TABLE branches (
    branch_id SERIAL PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT true
);

-- Movimientos de inventario
CREATE TABLE inventory_movements (
    movement_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(product_id),
    batch_id INT REFERENCES product_batches(batch_id),
    branch_id INT REFERENCES branches(branch_id),
    movement_type VARCHAR(20) NOT NULL CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT')),
    quantity INTEGER NOT NULL,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    user_id VARCHAR(13) REFERENCES users(id)
);

select * from users;
select * from product_types;
select * from product_categories;
select * from active_principles;
select * from dosage_forms;
select * from concentration_units;

-- =====================================================
-- 1. TIPOS DE PRODUCTOS (product_types)
-- =====================================================
INSERT INTO product_types (type_code, type_name, description, is_active) VALUES
('MED', 'Medicamento', 'Productos farmacéuticos con principio activo terapéutico', true),
('SUP', 'Suplemento', 'Suplementos vitamínicos y nutricionales', true),
('DM', 'Dispositivo Médico', 'Equipos y materiales médicos', true),
('COS', 'Cosmético', 'Productos de cuidado personal y belleza', true),
('HIG', 'Higiene', 'Productos de higiene y limpieza personal', true),
('INF', 'Infantil', 'Productos especializados para bebés y niños', true),
('HOM', 'Homeopático', 'Medicamentos homeopáticos y naturales', true),
('VET', 'Veterinario', 'Productos de uso veterinario', true);

-- =====================================================
-- 2. CATEGORÍAS DE PRODUCTOS (product_categories)
-- =====================================================
INSERT INTO product_categories (category_code, category_name, description, requires_prescription, is_active) VALUES
('ANL', 'Analgésicos', 'Medicamentos para aliviar el dolor', false, true),
('ANT', 'Antibióticos', 'Medicamentos antimicrobianos', true, true),
('ANF', 'Antiinflamatorios', 'Medicamentos para reducir la inflamación', false, true),
('ANH', 'Antihistamínicos', 'Medicamentos para alergias', false, true),
('CAR', 'Cardiovascular', 'Medicamentos para el sistema cardiovascular', true, true),
('DIG', 'Digestivos', 'Medicamentos para el sistema digestivo', false, true),
('RES', 'Respiratorios', 'Medicamentos para el sistema respiratorio', false, true),
('NEU', 'Neurológicos', 'Medicamentos para el sistema nervioso', true, true),
('PSI', 'Psiquiátricos', 'Medicamentos psiquiátricos', true, true),
('END', 'Endocrinos', 'Medicamentos hormonales y endocrinos', true, true),
('VIT', 'Vitaminas', 'Suplementos vitamínicos', false, true),
('MIN', 'Minerales', 'Suplementos minerales', false, true),
('DER', 'Dermatológicos', 'Productos para la piel', false, true),
('OFT', 'Oftálmicos', 'Productos para los ojos', false, true),
('OTO', 'Otológicos', 'Productos para los oídos', false, true);

-- =====================================================
-- 3. PRINCIPIOS ACTIVOS (active_principles)
-- =====================================================
INSERT INTO active_principles (principle_code, inn_name, therapeutic_action, contraindications, requires_prescription) VALUES
('PAR', 'Paracetamol', 'Analgésico y antipirético', 'Hipersensibilidad al paracetamol, insuficiencia hepática grave', false),
('IBU', 'Ibuprofeno', 'Antiinflamatorio no esteroideo, analgésico y antipirético', 'Úlcera péptica activa, insuficiencia renal o hepática grave', false),
('AMX', 'Amoxicilina', 'Antibiótico betalactámico de amplio espectro', 'Hipersensibilidad a penicilinas', true),
('AZI', 'Azitromicina', 'Antibiótico macrólido', 'Hipersensibilidad a macrólidos', true),
('OME', 'Omeprazol', 'Inhibidor de la bomba de protones', 'Hipersensibilidad al omeprazol', false),
('LOR', 'Loratadina', 'Antihistamínico H1 no sedante', 'Hipersensibilidad a loratadina', false),
('MET', 'Metformina', 'Antidiabético oral', 'Insuficiencia renal, acidosis metabólica', true),
('ATE', 'Atenolol', 'Betabloqueante cardioselectivo', 'Bradicardia severa, bloqueo cardíaco', true),
('SIM', 'Simvastatina', 'Hipolipemiante, inhibidor de HMG-CoA reductasa', 'Enfermedad hepática activa, embarazo', true),
('ALP', 'Alprazolam', 'Ansiolítico benzodiazepínico', 'Miastenia gravis, glaucoma de ángulo cerrado', true),
('CLO', 'Clopidogrel', 'Antiagregante plaquetario', 'Sangrado patológico activo', true),
('SAL', 'Salbutamol', 'Broncodilatador beta-2 agonista', 'Hipersensibilidad al salbutamol', false),
('DIC', 'Diclofenaco', 'Antiinflamatorio no esteroideo', 'Úlcera péptica, insuficiencia renal grave', false),
('CET', 'Cetirizina', 'Antihistamínico H1', 'Hipersensibilidad a cetirizina', false),
('AMC', 'Amoxicilina + Ácido Clavulánico', 'Antibiótico de amplio espectro', 'Hipersensibilidad a betalactámicos', true);

-- =====================================================
-- 4. UNIDADES DE CONCENTRACIÓN (concentration_units)
-- =====================================================
INSERT INTO concentration_units (unit_code, unit_name) VALUES
('MG', 'Miligramos'),
('G', 'Gramos'),
('MCG', 'Microgramos'),
('ML', 'Mililitros'),
('L', 'Litros'),
('UI', 'Unidades Internacionales'),
('MUI', 'Millones de Unidades Internacionales'),
('%', 'Porcentaje'),
('MG/', 'Miligramos/Mililitro'),
('G/L', 'Gramos/Litro'),
('MCM', 'Microgramos/Mililitro'),
('MEQ', 'Miliequivalentes'),
('PPM', 'Partes por millón'),
('U', 'Unidades'),
('MM', 'Milimolar');

-- =====================================================
-- 5. FORMAS FARMACÉUTICAS (dosage_forms)
-- =====================================================
INSERT INTO dosage_forms (form_code, form_name, route_administration) VALUES
('TAB', 'Tableta', 'Oral'),
('CAP', 'Cápsula', 'Oral'),
('JBE', 'Jarabe', 'Oral'),
('SUS', 'Suspensión', 'Oral'),
('SOL', 'Solución', 'Oral'),
('GOT', 'Gotas', 'Oral'),
('INY', 'Inyectable', 'Parenteral'),
('CRM', 'Crema', 'Tópica'),
('GEL', 'Gel', 'Tópica'),
('UNG', 'Ungüento', 'Tópica'),
('LOC', 'Loción', 'Tópica'),
('OVU', 'Óvulo', 'Vaginal'),
('SUP', 'Supositorio', 'Rectal'),
('COL', 'Colirio', 'Oftálmica'),
('NEB', 'Nebulización', 'Inhalatoria'),
('INH', 'Inhalador', 'Inhalatoria'),
('PAR', 'Parche', 'Transdérmica'),
('EFE', 'Efervescente', 'Oral'),
('SPR', 'Spray', 'Tópica/Nasal'),
('AMP', 'Ampolla', 'Parenteral');

INSERT INTO products (
    product_type, category_code, principle_code,
    concentration, concentration_unit, dosage_form,
    commercial_name, brand, manufacturer, requires_prescription,
    min_stock, max_stock, current_stock, is_active
) VALUES (
    'MED', 'ANL', 'PAR', 
    '500', 'MG', 'TAB',
    'Paracetamol 500mg', 'BAYER', 'Farmacéutica Nacional', false,
    50, 1000, 200, true
);



SELECT * FROM products;