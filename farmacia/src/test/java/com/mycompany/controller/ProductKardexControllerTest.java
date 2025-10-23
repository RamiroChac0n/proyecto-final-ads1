package com.mycompany.controller;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.MovementType;
import com.mycompany.service.IInventoryMovementService;
import com.mycompany.service.IProductService;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductKardexController
 * Tests KARDEX calculation logic and view controller behavior
 * @author ramir
 */
public class ProductKardexControllerTest {

    private ProductKardexController controller;

    @Mock
    private IProductService productService;

    @Mock
    private IInventoryMovementService inventoryMovementService;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ExternalContext externalContext;

    private Map<String, String> requestParameterMap;
    private Map<String, Object> sessionMap;
    private MockedStatic<FacesContext> facesContextMockedStatic;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        controller = new ProductKardexController();

        requestParameterMap = new HashMap<>();
        sessionMap = new HashMap<>();

        // Start static mocking of FacesContext
        facesContextMockedStatic = Mockito.mockStatic(FacesContext.class);
        facesContextMockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

        // Mock the behavior of the FacesContext instance
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestParameterMap()).thenReturn(requestParameterMap);
        when(externalContext.getSessionMap()).thenReturn(sessionMap);

        // Inject mocks using reflection
        injectField("productService", productService);
        injectField("inventoryMovementService", inventoryMovementService);
    }

    @AfterEach
    void tearDown() {
        // Stop static mocking
        if (facesContextMockedStatic != null) {
            facesContextMockedStatic.close();
        }
    }

    @Test
    @DisplayName("Should initialize with valid productId and load product and movements")
    void testInit_WithValidProductId_LoadsProductAndMovements() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId);
        List<InventoryMovement> movements = createTestMovements(product);

        requestParameterMap.put("productId", productId.toString());
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(productId)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(movements);

        // When
        controller.init();

        // Then
        assertEquals(product, controller.getProduct());
        assertEquals(productId, controller.getProductId());
        assertNotNull(controller.getMovements());
        assertEquals(3, controller.getMovements().size());
        assertNotNull(controller.getKardexRows());
        assertEquals(3, controller.getKardexRows().size());
    }

    @Test
    @DisplayName("Should calculate running balance correctly with IN movements")
    void testCalculateKardex_WithINMovements_IncreasesBalance() {
        // Given
        Product product = createTestProduct(1L);
        InventoryMovement movement1 = createMovement(product, MovementType.IN, 10, LocalDateTime.now().minusDays(2), new BigDecimal("10.00"));
        InventoryMovement movement2 = createMovement(product, MovementType.IN, 20, LocalDateTime.now().minusDays(1), new BigDecimal("12.00"));
        InventoryMovement movement3 = createMovement(product, MovementType.IN, 15, LocalDateTime.now(), new BigDecimal("15.00"));

        List<InventoryMovement> movements = Arrays.asList(movement3, movement2, movement1);

        requestParameterMap.put("productId", "1");
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(movements);

        // When
        controller.init();
        List<ProductKardexController.KardexRow> kardexRows = controller.getKardexRows();

        // Then
        assertEquals(3, kardexRows.size());
        // Newest first - verify quantities
        assertEquals(45, kardexRows.get(0).getBalanceQuantity()); // 10 + 20 + 15
        assertEquals(30, kardexRows.get(1).getBalanceQuantity()); // 10 + 20
        assertEquals(10, kardexRows.get(2).getBalanceQuantity()); // 10

        // Verify ENTRADAS are populated correctly
        assertEquals(15, kardexRows.get(0).getInputQuantity());
        assertEquals(20, kardexRows.get(1).getInputQuantity());
        assertEquals(10, kardexRows.get(2).getInputQuantity());

        // Verify SALIDAS are null for IN movements
        assertNull(kardexRows.get(0).getOutputQuantity());
        assertNull(kardexRows.get(1).getOutputQuantity());
        assertNull(kardexRows.get(2).getOutputQuantity());
    }

    @Test
    @DisplayName("Should calculate running balance correctly with OUT movements")
    void testCalculateKardex_WithOUTMovements_DecreasesBalance() {
        // Given
        Product product = createTestProduct(1L);
        InventoryMovement movement1 = createMovement(product, MovementType.IN, 50, LocalDateTime.now().minusDays(3), new BigDecimal("10.00"));
        InventoryMovement movement2 = createMovement(product, MovementType.OUT, 15, LocalDateTime.now().minusDays(2), new BigDecimal("10.00"));
        InventoryMovement movement3 = createMovement(product, MovementType.OUT, 10, LocalDateTime.now().minusDays(1), new BigDecimal("10.00"));

        List<InventoryMovement> movements = Arrays.asList(movement3, movement2, movement1);

        requestParameterMap.put("productId", "1");
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(movements);

        // When
        controller.init();
        List<ProductKardexController.KardexRow> kardexRows = controller.getKardexRows();

        // Then
        assertEquals(3, kardexRows.size());
        // Newest first - verify quantities
        assertEquals(25, kardexRows.get(0).getBalanceQuantity()); // 50 - 15 - 10
        assertEquals(35, kardexRows.get(1).getBalanceQuantity()); // 50 - 15
        assertEquals(50, kardexRows.get(2).getBalanceQuantity()); // 50

        // Verify SALIDAS are populated correctly for OUT movements
        assertEquals(10, kardexRows.get(0).getOutputQuantity());
        assertEquals(15, kardexRows.get(1).getOutputQuantity());

        // Verify ENTRADAS are populated correctly for IN movement
        assertEquals(50, kardexRows.get(2).getInputQuantity());

        // Verify SALIDAS are null for IN movement
        assertNull(kardexRows.get(2).getOutputQuantity());
    }

    @Test
    @DisplayName("Should calculate running balance correctly with ADJUSTMENT movements")
    void testCalculateKardex_WithADJUSTMENTMovements_AdjustsBalance() {
        // Given
        Product product = createTestProduct(1L);
        InventoryMovement movement1 = createMovement(product, MovementType.IN, 30, LocalDateTime.now().minusDays(2), new BigDecimal("10.00"));
        InventoryMovement movement2 = createMovement(product, MovementType.ADJUSTMENT, 5, LocalDateTime.now().minusDays(1), new BigDecimal("10.00"));
        InventoryMovement movement3 = createMovement(product, MovementType.ADJUSTMENT, -3, LocalDateTime.now(), new BigDecimal("10.00"));

        List<InventoryMovement> movements = Arrays.asList(movement3, movement2, movement1);

        requestParameterMap.put("productId", "1");
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(movements);

        // When
        controller.init();
        List<ProductKardexController.KardexRow> kardexRows = controller.getKardexRows();

        // Then
        assertEquals(3, kardexRows.size());
        assertEquals(32, kardexRows.get(0).getBalanceQuantity()); // 30 + 5 - 3
        assertEquals(35, kardexRows.get(1).getBalanceQuantity()); // 30 + 5
        assertEquals(30, kardexRows.get(2).getBalanceQuantity()); // 30

        // Verify positive ADJUSTMENT goes to ENTRADAS
        assertEquals(5, kardexRows.get(1).getInputQuantity());
        assertNull(kardexRows.get(1).getOutputQuantity());

        // Verify negative ADJUSTMENT goes to SALIDAS
        assertEquals(3, kardexRows.get(0).getOutputQuantity());
        assertNull(kardexRows.get(0).getInputQuantity());
    }

    @Test
    @DisplayName("Should calculate running balance correctly with mixed movements")
    void testCalculateKardex_MixedMovements_CalculatesCorrectRunningBalance() {
        // Given - Realistic scenario
        Product product = createTestProduct(1L);
        InventoryMovement m1 = createMovement(product, MovementType.IN, 100, LocalDateTime.now().minusDays(5), new BigDecimal("10.00"));
        InventoryMovement m2 = createMovement(product, MovementType.OUT, 25, LocalDateTime.now().minusDays(4), new BigDecimal("10.00"));
        InventoryMovement m3 = createMovement(product, MovementType.IN, 50, LocalDateTime.now().minusDays(3), new BigDecimal("12.00"));
        InventoryMovement m4 = createMovement(product, MovementType.OUT, 30, LocalDateTime.now().minusDays(2), new BigDecimal("10.00"));
        InventoryMovement m5 = createMovement(product, MovementType.ADJUSTMENT, -5, LocalDateTime.now().minusDays(1), new BigDecimal("10.00"));
        InventoryMovement m6 = createMovement(product, MovementType.OUT, 10, LocalDateTime.now(), new BigDecimal("10.00"));

        List<InventoryMovement> movements = Arrays.asList(m6, m5, m4, m3, m2, m1);

        requestParameterMap.put("productId", "1");
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(movements);

        // When
        controller.init();
        List<ProductKardexController.KardexRow> kardexRows = controller.getKardexRows();

        // Then - Verify progressive balance calculation
        assertEquals(6, kardexRows.size());
        assertEquals(80, kardexRows.get(0).getBalanceQuantity());  // Final: 100 - 25 + 50 - 30 - 5 - 10 = 80
        assertEquals(90, kardexRows.get(1).getBalanceQuantity());  // After adjustment: 100 - 25 + 50 - 30 - 5 = 90
        assertEquals(95, kardexRows.get(2).getBalanceQuantity());  // After OUT 30: 100 - 25 + 50 - 30 = 95
        assertEquals(125, kardexRows.get(3).getBalanceQuantity()); // After IN 50: 100 - 25 + 50 = 125
        assertEquals(75, kardexRows.get(4).getBalanceQuantity());  // After OUT 25: 100 - 25 = 75
        assertEquals(100, kardexRows.get(5).getBalanceQuantity()); // Initial IN: 100
    }

    @Test
    @DisplayName("Should return empty kardex when product has no movements")
    void testCalculateKardex_EmptyMovements_ReturnsEmptyKardex() {
        // Given
        Product product = createTestProduct(1L);

        requestParameterMap.put("productId", "1");
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(Collections.emptyList());

        // When
        controller.init();

        // Then
        assertNotNull(controller.getKardexRows());
        assertTrue(controller.getKardexRows().isEmpty());
    }

    @Test
    @DisplayName("Should display newest movements first in kardex")
    void testCalculateKardex_OrdersNewestFirst() {
        // Given
        Product product = createTestProduct(1L);
        InventoryMovement oldest = createMovement(product, MovementType.IN, 10, LocalDateTime.now().minusDays(3), new BigDecimal("10.00"));
        InventoryMovement middle = createMovement(product, MovementType.IN, 20, LocalDateTime.now().minusDays(2), new BigDecimal("10.00"));
        InventoryMovement newest = createMovement(product, MovementType.IN, 30, LocalDateTime.now(), new BigDecimal("10.00"));

        List<InventoryMovement> movements = Arrays.asList(newest, middle, oldest);

        requestParameterMap.put("productId", "1");
        sessionMap.put("user", createTestUser());

        Mockito.when(productService.findById(1L)).thenReturn(product);
        Mockito.when(inventoryMovementService.findByProduct(product)).thenReturn(movements);

        // When
        controller.init();
        List<ProductKardexController.KardexRow> kardexRows = controller.getKardexRows();

        // Then - Verify order (newest first)
        assertEquals(30, kardexRows.get(0).getMovement().getQuantity());
        assertEquals(20, kardexRows.get(1).getMovement().getQuantity());
        assertEquals(10, kardexRows.get(2).getMovement().getQuantity());
    }

    @Test
    @DisplayName("Should return correct icon for each movement type")
    void testGetMovementTypeIcon_AllTypes() {
        assertEquals("pi pi-arrow-down", controller.getMovementTypeIcon(MovementType.IN));
        assertEquals("pi pi-arrow-up", controller.getMovementTypeIcon(MovementType.OUT));
        assertEquals("pi pi-sync", controller.getMovementTypeIcon(MovementType.ADJUSTMENT));
        assertEquals("pi pi-question", controller.getMovementTypeIcon(null));
    }

    @Test
    @DisplayName("Should return correct CSS class for each movement type")
    void testGetMovementTypeClass_AllTypes() {
        assertEquals("text-success", controller.getMovementTypeClass(MovementType.IN));
        assertEquals("text-danger", controller.getMovementTypeClass(MovementType.OUT));
        assertEquals("text-warning", controller.getMovementTypeClass(MovementType.ADJUSTMENT));
        assertEquals("", controller.getMovementTypeClass(null));
    }

    @Test
    @DisplayName("Should return correct severity for each movement type")
    void testGetMovementTypeSeverity_AllTypes() {
        assertEquals("success", controller.getMovementTypeSeverity(MovementType.IN));
        assertEquals("danger", controller.getMovementTypeSeverity(MovementType.OUT));
        assertEquals("warning", controller.getMovementTypeSeverity(MovementType.ADJUSTMENT));
        assertEquals("info", controller.getMovementTypeSeverity(null));
    }

    @Test
    @DisplayName("Should format movement date correctly")
    void testFormatMovementDate() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 14, 30, 45);
        InventoryMovement movement = InventoryMovement.builder()
                .movementDate(dateTime)
                .build();

        // When
        String formatted = controller.formatMovementDate(movement);

        // Then
        assertEquals("15/01/2025 14:30:45", formatted);
    }

    @Test
    @DisplayName("Should return N/A when movement date is null")
    void testFormatMovementDate_NullDate() {
        // Given
        InventoryMovement movement = InventoryMovement.builder().build();

        // When
        String formatted = controller.formatMovementDate(movement);

        // Then
        assertEquals("N/A", formatted);
    }

    @Test
    @DisplayName("Should get batch number when batch exists")
    void testGetBatchNumber_WithBatch() {
        // Given
        ProductBatch batch = ProductBatch.builder()
                .batchNumber("BATCH-001")
                .build();
        InventoryMovement movement = InventoryMovement.builder()
                .batch(batch)
                .build();

        // When
        String batchNumber = controller.getBatchNumber(movement);

        // Then
        assertEquals("BATCH-001", batchNumber);
    }

    @Test
    @DisplayName("Should return N/A when batch is null")
    void testGetBatchNumber_WithoutBatch() {
        // Given
        InventoryMovement movement = InventoryMovement.builder().build();

        // When
        String batchNumber = controller.getBatchNumber(movement);

        // Then
        assertEquals("N/A", batchNumber);
    }

    @Test
    @DisplayName("Should get branch name when branch exists")
    void testGetBranchName_WithBranch() {
        // Given
        Branch branch = Branch.builder()
                .branchName("Main Branch")
                .build();
        InventoryMovement movement = InventoryMovement.builder()
                .branch(branch)
                .build();

        // When
        String branchName = controller.getBranchName(movement);

        // Then
        assertEquals("Main Branch", branchName);
    }

    @Test
    @DisplayName("Should return N/A when branch is null")
    void testGetBranchName_WithoutBranch() {
        // Given
        InventoryMovement movement = InventoryMovement.builder().build();

        // When
        String branchName = controller.getBranchName(movement);

        // Then
        assertEquals("N/A", branchName);
    }

    @Test
    @DisplayName("Should get user name when user exists")
    void testGetUserName_WithUser() {
        // Given
        User user = createTestUser();
        InventoryMovement movement = InventoryMovement.builder()
                .user(user)
                .build();

        // When
        String userName = controller.getUserName(movement);

        // Then
        assertEquals("John Doe", userName);
    }

    @Test
    @DisplayName("Should return N/A when user is null")
    void testGetUserName_WithoutUser() {
        // Given
        InventoryMovement movement = InventoryMovement.builder().build();

        // When
        String userName = controller.getUserName(movement);

        // Then
        assertEquals("N/A", userName);
    }

    @Test
    @DisplayName("Should populate KardexRow fields correctly for ENTRADAS")
    void testKardexRow_PopulatesEntradasFields() {
        // Given
        ProductKardexController.KardexRow row = new ProductKardexController.KardexRow();

        // When - Set ENTRADAS fields
        row.setInputQuantity(10);
        row.setInputUnitCost(new BigDecimal("15.50"));
        row.setInputTotalCost(new BigDecimal("155.00"));
        row.setBalanceQuantity(10);
        row.setBalanceTotalCost(new BigDecimal("155.00"));

        // Then
        assertEquals(10, row.getInputQuantity());
        assertEquals(new BigDecimal("15.50"), row.getInputUnitCost());
        assertEquals(new BigDecimal("155.00"), row.getInputTotalCost());
        assertNull(row.getOutputQuantity());
        assertNull(row.getOutputUnitCost());
        assertNull(row.getOutputTotalCost());
    }

    @Test
    @DisplayName("Should populate KardexRow fields correctly for SALIDAS")
    void testKardexRow_PopulatesSalidasFields() {
        // Given
        ProductKardexController.KardexRow row = new ProductKardexController.KardexRow();

        // When - Set SALIDAS fields
        row.setOutputQuantity(5);
        row.setOutputUnitCost(new BigDecimal("15.50"));
        row.setOutputTotalCost(new BigDecimal("77.50"));
        row.setBalanceQuantity(5);
        row.setBalanceTotalCost(new BigDecimal("77.50"));

        // Then
        assertNull(row.getInputQuantity());
        assertNull(row.getInputUnitCost());
        assertNull(row.getInputTotalCost());
        assertEquals(5, row.getOutputQuantity());
        assertEquals(new BigDecimal("15.50"), row.getOutputUnitCost());
        assertEquals(new BigDecimal("77.50"), row.getOutputTotalCost());
    }

    @Test
    @DisplayName("Should format currency correctly with null value")
    void testFormatCurrency_WithNull() {
        String result = controller.formatCurrency(null);
        assertEquals("Q 0.00", result);
    }

    @Test
    @DisplayName("Should format currency correctly with valid amount")
    void testFormatCurrency_WithValidAmount() {
        String result = controller.formatCurrency(new BigDecimal("1234.56"));
        assertEquals("Q 1,234.56", result);
    }

    @Test
    @DisplayName("Should format currency with thousands separator")
    void testFormatCurrency_WithThousandsSeparator() {
        String result = controller.formatCurrency(new BigDecimal("1234567.89"));
        assertEquals("Q 1,234,567.89", result);
    }

    // Helper methods
    private void injectField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = controller.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private Product createTestProduct(Long id) {
        return Product.builder()
                .productId(id)
                .commercialName("Test Product")
                .manufacturer("Test Manufacturer")
                .build();
    }

    private User createTestUser() {
        User user = new User();
        user.setId("1234567890123");
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }

    private List<InventoryMovement> createTestMovements(Product product) {
        InventoryMovement m1 = createMovement(product, MovementType.IN, 10, LocalDateTime.now().minusDays(2), new BigDecimal("10.00"));
        InventoryMovement m2 = createMovement(product, MovementType.OUT, 5, LocalDateTime.now().minusDays(1), new BigDecimal("10.00"));
        InventoryMovement m3 = createMovement(product, MovementType.IN, 15, LocalDateTime.now(), new BigDecimal("12.00"));

        return Arrays.asList(m3, m2, m1);
    }

    private InventoryMovement createMovement(Product product, MovementType type, int quantity, LocalDateTime date, BigDecimal unitCost) {
        ProductBatch batch = ProductBatch.builder()
                .batchNumber("BATCH-" + System.currentTimeMillis())
                .unitCost(unitCost)
                .salePrice(unitCost.multiply(new BigDecimal("1.3")))
                .build();

        return InventoryMovement.builder()
                .product(product)
                .batch(batch)
                .movementType(type)
                .quantity(quantity)
                .movementDate(date)
                .reason("Test movement")
                .build();
    }
}
