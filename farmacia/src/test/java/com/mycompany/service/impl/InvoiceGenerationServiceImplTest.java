package com.mycompany.service.impl;

import com.mycompany.model.entity.*;
import com.mycompany.model.entity.enums.Role;
import com.mycompany.model.entity.enums.SaleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvoiceGenerationServiceImpl
 * @author Claude Code
 */
class InvoiceGenerationServiceImplTest {

    private InvoiceGenerationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InvoiceGenerationServiceImpl();
    }

    @Test
    void generateInvoiceXML_ValidSale_GeneratesXMLFile() throws Exception {
        // Given - Create test sale with details
        Sale sale = createTestSale();

        // When
        File invoiceFile = service.generateInvoiceXML(sale);

        // Then
        assertNotNull(invoiceFile, "Invoice file should not be null");
        assertTrue(invoiceFile.exists(), "Invoice file should exist");
        assertTrue(invoiceFile.getName().endsWith(".xml"), "Invoice file should be XML");

        // Verify XML content
        String xmlContent = Files.readString(invoiceFile.toPath());
        assertTrue(xmlContent.contains("<?xml version=\"1.0\" encoding=\"UTF-8\""), "Should have XML declaration");
        assertTrue(xmlContent.contains("<dte:GTDocumento"), "Should have root element");
        assertTrue(xmlContent.contains("<dte:Descripcion>Test Product</dte:Descripcion>"), "Should contain product name");
        assertTrue(xmlContent.contains("<dte:GranTotal>100.00</dte:GranTotal>"), "Should contain total amount");

        // Cleanup
        invoiceFile.delete();
    }

    @Test
    void generateInvoiceXML_SaleWithMultipleItems_GeneratesAllItems() throws Exception {
        // Given - Sale with multiple items
        Sale sale = createTestSaleWithMultipleItems();

        // When
        File invoiceFile = service.generateInvoiceXML(sale);

        // Then
        String xmlContent = Files.readString(invoiceFile.toPath());
        assertTrue(xmlContent.contains("NumeroLinea=\"1\""), "Should have first item");
        assertTrue(xmlContent.contains("NumeroLinea=\"2\""), "Should have second item");
        assertTrue(xmlContent.contains("<dte:Descripcion>Test Product</dte:Descripcion>"), "Should have first product");
        assertTrue(xmlContent.contains("<dte:Descripcion>Test Product 2</dte:Descripcion>"), "Should have second product");

        // Cleanup
        invoiceFile.delete();
    }

    @Test
    void generateInvoiceXML_SaleWithNoDetails_ThrowsException() {
        // Given - Sale without details
        Sale sale = createTestSale();
        sale.setSaleDetails(new ArrayList<>());

        // When/Then
        Exception exception = assertThrows(Exception.class, () -> {
            service.generateInvoiceXML(sale);
        });

        assertTrue(exception.getMessage().contains("no details") ||
                   exception.getMessage().contains("Failed to generate invoice"));
    }

    @Test
    void getInvoiceFilePath_ValidSale_ReturnsCorrectPath() {
        // Given
        Sale sale = createTestSale();

        // When
        String filePath = service.getInvoiceFilePath(sale);

        // Then
        assertNotNull(filePath);
        assertTrue(filePath.contains("SALE-001.xml"), "Should contain sale number");
        assertTrue(filePath.contains(File.separator + "2025" + File.separator), "Should contain year directory");
        assertTrue(filePath.endsWith(".xml"), "Should end with .xml");
    }

    @Test
    void generateInvoiceXML_SaleWithCustomerData_IncludesCustomerInfo() throws Exception {
        // Given - Sale with customer data
        Sale sale = createTestSale();
        sale.setCustomerName("Juan Perez");
        sale.setCustomerNit("12345678-9");

        // When
        File invoiceFile = service.generateInvoiceXML(sale);

        // Then
        String xmlContent = Files.readString(invoiceFile.toPath());
        assertTrue(xmlContent.contains("Juan Perez"), "Should contain customer name");
        assertTrue(xmlContent.contains("12345678-9"), "Should contain customer NIT");

        // Cleanup
        invoiceFile.delete();
    }

    // Helper methods

    private Sale createTestSale() {
        // Create branch
        Branch branch = Branch.builder()
                .branchId(1)
                .branchName("Farmacia Central")
                .address("1ra Calle 2-34 Zona 1")
                .phone("22334455")
                .build();

        // Create user
        User user = User.builder()
                .id("1234567890123")
                .firstName("John")
                .lastName("Doe")
                .userName("johnDoe")
                .email("john@example.com")
                .role(Role.CASHIER)
                .build();

        // Create product
        Product product = Product.builder()
                .productId(1L)
                .commercialName("Test Product")
                .build();

        // Create batch
        ProductBatch batch = ProductBatch.builder()
                .batchId(1)
                .batchNumber("BATCH-001")
                .salePrice(new BigDecimal("50.00"))
                .unitCost(new BigDecimal("30.00"))
                .expirationDate(new Date())
                .build();

        // Create sale detail
        SaleDetail detail = SaleDetail.builder()
                .detailId(1)
                .product(product)
                .batch(batch)
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .unitCost(new BigDecimal("30.00"))
                .lineTotal(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .build();

        List<SaleDetail> details = new ArrayList<>();
        details.add(detail);

        // Create sale
        Sale sale = Sale.builder()
                .saleId(1)
                .saleNumber("SALE-001")
                .saleDate(new Date())
                .branch(branch)
                .user(user)
                .customerName("CONSUMIDOR FINAL")
                .customerNit("C/F")
                .subtotal(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("100.00"))
                .saleStatus(SaleStatus.COMPLETED)
                .saleDetails(details)
                .build();

        return sale;
    }

    private Sale createTestSaleWithMultipleItems() {
        Sale sale = createTestSale();

        // Create second product
        Product product2 = Product.builder()
                .productId(2L)
                .commercialName("Test Product 2")
                .build();

        // Create second batch
        ProductBatch batch2 = ProductBatch.builder()
                .batchId(2)
                .batchNumber("BATCH-002")
                .salePrice(new BigDecimal("75.00"))
                .unitCost(new BigDecimal("45.00"))
                .expirationDate(new Date())
                .build();

        // Create second detail
        SaleDetail detail2 = SaleDetail.builder()
                .detailId(2)
                .product(product2)
                .batch(batch2)
                .quantity(1)
                .unitPrice(new BigDecimal("75.00"))
                .unitCost(new BigDecimal("45.00"))
                .lineTotal(new BigDecimal("75.00"))
                .discountAmount(BigDecimal.ZERO)
                .build();

        sale.getSaleDetails().add(detail2);
        sale.setTotalAmount(new BigDecimal("175.00"));

        return sale;
    }
}
