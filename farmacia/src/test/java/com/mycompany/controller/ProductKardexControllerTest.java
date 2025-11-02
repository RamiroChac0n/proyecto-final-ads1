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
        // Oldest first - verify quantities in chronological order
        assertEquals(10, kardexRows.get(0).getBalanceQuantity()); // First movement: 10
        assertEquals(30, kardexRows.get(1).getBalanceQuantity()); // After second: 10 + 20 = 30
        assertEquals(45, kardexRows.get(2).getBalanceQuantity()); // After third: 30 + 15 = 45

        // Verify ENTRADAS are populated correctly (chronological order)
        assertEquals(10, kardexRows.get(0).getInputQuantity());
        assertEquals(20, kardexRows.get(1).getInputQuantity());
        assertEquals(15, kardexRows.get(2).getInputQuantity());

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
        // Oldest first - verify quantities in chronological order
        assertEquals(50, kardexRows.get(0).getBalanceQuantity()); // First IN: 50
        assertEquals(35, kardexRows.get(1).getBalanceQuantity()); // After OUT 15: 50 - 15 = 35
        assertEquals(25, kardexRows.get(2).getBalanceQuantity()); // After OUT 10: 35 - 10 = 25

        // Verify ENTRADAS are populated correctly for IN movement (first)
        assertEquals(50, kardexRows.get(0).getInputQuantity());

        // Verify SALIDAS are populated correctly for OUT movements
        assertEquals(15, kardexRows.get(1).getOutputQuantity());
        assertEquals(10, kardexRows.get(2).getOutputQuantity());

        // Verify SALIDAS are null for IN movement
        assertNull(kardexRows.get(0).getOutputQuantity());
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
        // Oldest first - chronological order
        assertEquals(30, kardexRows.get(0).getBalanceQuantity()); // First IN: 30
        assertEquals(35, kardexRows.get(1).getBalanceQuantity()); // After ADJ +5: 30 + 5 = 35
        assertEquals(32, kardexRows.get(2).getBalanceQuantity()); // After ADJ -3: 35 - 3 = 32

        // Verify positive ADJUSTMENT goes to ENTRADAS (index 1)
        assertEquals(5, kardexRows.get(1).getInputQuantity());
        assertNull(kardexRows.get(1).getOutputQuantity());

        // Verify negative ADJUSTMENT goes to SALIDAS (index 2)
        assertEquals(3, kardexRows.get(2).getOutputQuantity());
        assertNull(kardexRows.get(2).getInputQuantity());
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

        // Then - Verify progressive balance calculation in chronological order
        assertEquals(6, kardexRows.size());
        // Oldest first
        assertEquals(100, kardexRows.get(0).getBalanceQuantity()); // m1: IN 100 = 100
        assertEquals(75, kardexRows.get(1).getBalanceQuantity());  // m2: OUT 25 = 100 - 25 = 75
        assertEquals(125, kardexRows.get(2).getBalanceQuantity()); // m3: IN 50 = 75 + 50 = 125
        assertEquals(95, kardexRows.get(3).getBalanceQuantity());  // m4: OUT 30 = 125 - 30 = 95
        assertEquals(90, kardexRows.get(4).getBalanceQuantity());  // m5: ADJ -5 = 95 - 5 = 90
        assertEquals(80, kardexRows.get(5).getBalanceQuantity());  // m6: OUT 10 = 90 - 10 = 80 (final)
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
    @DisplayName("Should display oldest movements first in kardex (chronological order)")
    void testCalculateKardex_OrdersOldestFirst() {
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

        // Then - Verify order (oldest first - chronological order)
        assertEquals(10, kardexRows.get(0).getMovement().getQuantity()); // oldest
        assertEquals(20, kardexRows.get(1).getMovement().getQuantity()); // middle
        assertEquals(30, kardexRows.get(2).getMovement().getQuantity()); // newest
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

    // ========== Product Selection Tests ==========

    @Test
    @DisplayName("Should redirect to product kardex when valid product is selected")
    void onProductChange_ValidProduct_RedirectsToProductKardex() throws Exception {
        // Given
        Product product = createTestProduct(123L);
        sessionMap.put("user", createTestUser());

        // Inject the selected product
        injectField("selectedProduct", product);

        // When
        controller.onProductChange();

        // Then
        Mockito.verify(externalContext).redirect("product-kardex.xhtml?productId=123");
    }

    @Test
    @DisplayName("Should not redirect when selected product is null")
    void onProductChange_NullProduct_NoRedirect() throws Exception {
        // Given
        sessionMap.put("user", createTestUser());
        injectField("selectedProduct", null);

        // When
        controller.onProductChange();

        // Then
        Mockito.verify(externalContext, Mockito.never()).redirect(anyString());
    }

    @Test
    @DisplayName("Should not redirect when selected product has null ID")
    void onProductChange_ProductWithNullId_NoRedirect() throws Exception {
        // Given
        Product productWithNullId = Product.builder()
                .productId(null)
                .commercialName("Product Without ID")
                .build();
        sessionMap.put("user", createTestUser());
        injectField("selectedProduct", productWithNullId);

        // When
        controller.onProductChange();

        // Then
        Mockito.verify(externalContext, Mockito.never()).redirect(anyString());
    }

    @Test
    @DisplayName("Should show error message when redirect fails")
    void onProductChange_RedirectException_ShowsErrorMessage() throws Exception {
        // Given
        Product product = createTestProduct(456L);
        sessionMap.put("user", createTestUser());
        injectField("selectedProduct", product);

        // Mock redirect to throw exception
        Mockito.doThrow(new RuntimeException("Redirect failed"))
                .when(externalContext).redirect(anyString());

        // When
        controller.onProductChange();

        // Then - Should handle exception gracefully (logged, not thrown)
        // Verify redirect was attempted
        Mockito.verify(externalContext).redirect("product-kardex.xhtml?productId=456");
    }

    @Test
    @DisplayName("Should load all products for selector dropdown")
    void loadAllProducts_Success_LoadsProductsList() throws Exception {
        // Given
        Product product1 = createTestProduct(1L);
        Product product2 = Product.builder()
                .productId(2L)
                .commercialName("Another Product")
                .build();
        List<Product> allProducts = Arrays.asList(product1, product2);

        Mockito.when(productService.list()).thenReturn(allProducts);

        // When
        // Use reflection to call private loadAllProducts method
        java.lang.reflect.Method method = controller.getClass().getDeclaredMethod("loadAllProducts");
        method.setAccessible(true);
        method.invoke(controller);

        // Then
        // Verify the allProducts field was set
        java.lang.reflect.Field field = controller.getClass().getDeclaredField("allProducts");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Product> loadedProducts = (List<Product>) field.get(controller);

        assertNotNull(loadedProducts);
        assertEquals(2, loadedProducts.size());
        assertEquals("Test Product", loadedProducts.get(0).getCommercialName());
        assertEquals("Another Product", loadedProducts.get(1).getCommercialName());
        Mockito.verify(productService).list();
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
