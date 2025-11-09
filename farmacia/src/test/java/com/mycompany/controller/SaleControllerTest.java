package com.mycompany.controller;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Product;
import com.mycompany.model.entity.ProductBatch;
import com.mycompany.model.entity.SaleDetail;
import com.mycompany.model.entity.User;
import com.mycompany.repository.BranchRepository;
import com.mycompany.service.ICashRegisterService;
import com.mycompany.service.IProductService;
import com.mycompany.service.ISaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SaleController - specifically for the calculateChange() functionality
 * Tests automatic change calculation based on cash received and cart total
 * @author ramir
 */
@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock
    private ISaleService saleService;

    @Mock
    private IProductService productService;

    @Mock
    private ICashRegisterService cashRegisterService;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserController userController;

    private SaleController saleController;
    private List<SaleDetail> testCartItems;
    private Product testProduct;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize controller
        saleController = new SaleController();

        // Manually inject mocked dependencies using reflection
        injectMock(saleController, "saleService", saleService);
        injectMock(saleController, "productService", productService);
        injectMock(saleController, "cashRegisterService", cashRegisterService);
        injectMock(saleController, "branchRepository", branchRepository);
        injectMock(saleController, "userController", userController);

        // Create test product
        testProduct = Product.builder()
                .productId(1L)
                .commercialName("Test Product")
                .brand("Test Brand")
                .requiresPrescription(false)
                .build();

        // Create test cart items with known prices
        testCartItems = new ArrayList<>();

        // Initialize controller fields
        saleController.setCartItems(testCartItems);
        saleController.setCashReceived(BigDecimal.ZERO);
        saleController.setChangeGiven(BigDecimal.ZERO);
    }

    /**
     * Helper method to inject mocks using reflection
     */
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    /**
     * Helper method to add items to cart
     */
    private void addItemToCart(BigDecimal unitPrice, Integer quantity) {
        SaleDetail detail = SaleDetail.builder()
                .product(testProduct)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .unitCost(unitPrice.multiply(new BigDecimal("0.7"))) // 70% cost
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .lineTotal(unitPrice.multiply(new BigDecimal(quantity)))
                .requiresPrescription(false)
                .build();
        testCartItems.add(detail);
    }

    @Test
    void testCalculateChange_WithSufficientCash_CalculatesPositiveChange() {
        // Given: Cart with total Q100, cash received Q150
        addItemToCart(new BigDecimal("50.00"), 2); // 2 items x Q50 = Q100
        saleController.setCashReceived(new BigDecimal("150.00"));

        // When
        saleController.calculateChange();

        // Then: Change should be Q50.00
        BigDecimal expectedChange = new BigDecimal("50.00");
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should be Q50.00 when cash received (Q150) exceeds total (Q100)");
    }

    @Test
    void testCalculateChange_WithExactCash_CalculatesZeroChange() {
        // Given: Cart with total Q100, cash received Q100 (exact amount)
        addItemToCart(new BigDecimal("25.00"), 4); // 4 items x Q25 = Q100
        saleController.setCashReceived(new BigDecimal("100.00"));

        // When
        saleController.calculateChange();

        // Then: Change should be Q0.00
        BigDecimal expectedChange = BigDecimal.ZERO;
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should be Q0.00 when cash received equals total");
    }

    @Test
    void testCalculateChange_WithInsufficientCash_CalculatesNegativeChange() {
        // Given: Cart with total Q100, cash received Q80 (insufficient)
        addItemToCart(new BigDecimal("100.00"), 1); // 1 item x Q100 = Q100
        saleController.setCashReceived(new BigDecimal("80.00"));

        // When
        saleController.calculateChange();

        // Then: Change should be Q-20.00 (negative)
        BigDecimal expectedChange = new BigDecimal("-20.00");
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should be Q-20.00 when cash received (Q80) is less than total (Q100)");
    }

    @Test
    void testCalculateChange_WithNullCashReceived_SetsChangeToZero() {
        // Given: Cart with products, but cashReceived is null
        addItemToCart(new BigDecimal("50.00"), 2); // Total Q100
        saleController.setCashReceived(null);

        // When
        saleController.calculateChange();

        // Then: Change should be Q0.00
        BigDecimal expectedChange = BigDecimal.ZERO;
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should be Q0.00 when cash received is null");
    }

    @Test
    void testCalculateChange_WithZeroCashReceived_SetsChangeToZero() {
        // Given: Cart with products, cash received is Q0
        addItemToCart(new BigDecimal("50.00"), 2); // Total Q100
        saleController.setCashReceived(BigDecimal.ZERO);

        // When
        saleController.calculateChange();

        // Then: Change should be Q0.00
        BigDecimal expectedChange = BigDecimal.ZERO;
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should be Q0.00 when cash received is zero");
    }

    @Test
    void testCalculateChange_WithEmptyCart_CalculatesChangeFromZeroTotal() {
        // Given: Empty cart (total Q0), cash received Q50
        // testCartItems is already empty from setUp
        saleController.setCashReceived(new BigDecimal("50.00"));

        // When
        saleController.calculateChange();

        // Then: Change should be Q50.00 (all the cash received, since total is 0)
        BigDecimal expectedChange = new BigDecimal("50.00");
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should equal cash received when cart is empty (total = Q0)");
    }

    @Test
    void testCalculateChange_WithDecimalAmounts_CalculatesPreciseChange() {
        // Given: Cart total Q99.99, cash received Q100.00
        addItemToCart(new BigDecimal("33.33"), 3); // 3 items x Q33.33 = Q99.99
        saleController.setCashReceived(new BigDecimal("100.00"));

        // When
        saleController.calculateChange();

        // Then: Change should be Q0.01 (one cent)
        BigDecimal expectedChange = new BigDecimal("0.01");
        assertEquals(0, expectedChange.compareTo(saleController.getChangeGiven()),
                "Change should be Q0.01 when cash received (Q100.00) exceeds total (Q99.99) by one cent");
    }

    @Test
    void testCalculateSubtotal_WithMultipleItems_SumsLineTotal() {
        // Given: Multiple items in cart
        addItemToCart(new BigDecimal("10.00"), 2); // Q20.00
        addItemToCart(new BigDecimal("15.50"), 3); // Q46.50
        addItemToCart(new BigDecimal("8.25"), 1);  // Q8.25

        // When
        BigDecimal subtotal = saleController.calculateSubtotal();

        // Then: Subtotal should be Q74.75
        BigDecimal expectedSubtotal = new BigDecimal("74.75");
        assertEquals(0, expectedSubtotal.compareTo(subtotal),
                "Subtotal should correctly sum all line totals");
    }

    @Test
    void testCalculateSubtotal_WithEmptyCart_ReturnsZero() {
        // Given: Empty cart
        // testCartItems is already empty from setUp

        // When
        BigDecimal subtotal = saleController.calculateSubtotal();

        // Then: Subtotal should be Q0.00
        assertEquals(0, BigDecimal.ZERO.compareTo(subtotal),
                "Subtotal should be Q0.00 for empty cart");
    }

    @Test
    void testCalculateTotal_ReturnsSubtotal() {
        // Given: Cart with items
        addItemToCart(new BigDecimal("50.00"), 2); // Q100.00

        // When
        BigDecimal total = saleController.calculateTotal();

        // Then: Total should equal subtotal (no tax/discount yet)
        BigDecimal expectedTotal = new BigDecimal("100.00");
        assertEquals(0, expectedTotal.compareTo(total),
                "Total should equal subtotal when no tax/discount applied");
    }

    @Test
    void testCalculateChange_MultipleCallsWithDifferentAmounts_UpdatesChangeCorrectly() {
        // Given: Cart with total Q100
        addItemToCart(new BigDecimal("100.00"), 1);

        // When: First calculation with Q150
        saleController.setCashReceived(new BigDecimal("150.00"));
        saleController.calculateChange();
        BigDecimal firstChange = saleController.getChangeGiven();

        // Then: First change should be Q50.00
        assertEquals(0, new BigDecimal("50.00").compareTo(firstChange),
                "First calculation should return Q50.00");

        // When: Second calculation with Q200
        saleController.setCashReceived(new BigDecimal("200.00"));
        saleController.calculateChange();
        BigDecimal secondChange = saleController.getChangeGiven();

        // Then: Second change should be Q100.00
        assertEquals(0, new BigDecimal("100.00").compareTo(secondChange),
                "Second calculation should return Q100.00");

        // Verify that change was updated, not accumulated
        assertNotEquals(firstChange, secondChange,
                "Change should be recalculated, not accumulated");
    }

    @Test
    void testFormatCurrency_WithValidAmount_FormatsCorrectly() {
        // Given: Valid BigDecimal amount
        BigDecimal amount = new BigDecimal("123.456");

        // When
        String formatted = saleController.formatCurrency(amount);

        // Then: Should format as Q123.46 (rounded to 2 decimals)
        assertEquals("Q123.46", formatted,
                "Currency should be formatted with Q prefix and 2 decimal places");
    }

    @Test
    void testFormatCurrency_WithNull_ReturnsDefaultValue() {
        // Given: Null amount
        BigDecimal amount = null;

        // When
        String formatted = saleController.formatCurrency(amount);

        // Then: Should return Q0.00
        assertEquals("Q0.00", formatted,
                "Null amount should return Q0.00");
    }
}
