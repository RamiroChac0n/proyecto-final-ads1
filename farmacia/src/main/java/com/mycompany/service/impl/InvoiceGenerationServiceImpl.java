package com.mycompany.service.impl;

import com.mycompany.model.entity.Branch;
import com.mycompany.model.entity.Sale;
import com.mycompany.model.entity.SaleDetail;
import com.mycompany.service.IInvoiceGenerationService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.ejb.Stateless;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of invoice generation service using Freemarker templates
 * @author ramir
 */
@Stateless
public class InvoiceGenerationServiceImpl implements IInvoiceGenerationService {

    private static final Logger LOGGER = Logger.getLogger(InvoiceGenerationServiceImpl.class.getName());

    private static final String INVOICE_BASE_DIR = System.getProperty("user.home") + File.separator + "pharmacy-invoices";

    private Configuration freemarkerConfig;

    /**
     * Initializes Freemarker configuration
     */
    private Configuration getFreemarkerConfiguration() throws IOException {
        if (freemarkerConfig == null) {
            freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);

            freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
            freemarkerConfig.setDefaultEncoding("UTF-8");
        }
        return freemarkerConfig;
    }

    @Override
    public File generateInvoiceXML(Sale sale) throws Exception {
        LOGGER.log(Level.INFO, "Generating invoice for sale: {0}", sale.getSaleNumber());

        try {

            if (sale.getSaleDetails() == null || sale.getSaleDetails().isEmpty()) {
                throw new IllegalArgumentException("Sale has no details");
            }


            Configuration config = getFreemarkerConfiguration();
            Template template = config.getTemplate("factura-template.ftl");


            Map<String, Object> dataModel = prepareDataModel(sale);


            String filePath = getInvoiceFilePath(sale);
            File outputFile = new File(filePath);
            outputFile.getParentFile().mkdirs();

            try (Writer fileWriter = new FileWriter(outputFile)) {
                template.process(dataModel, fileWriter);
            }

            LOGGER.log(Level.INFO, "Invoice generated successfully: {0}", filePath);
            return outputFile;

        } catch (IOException | TemplateException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate invoice for sale: " + sale.getSaleNumber(), e);
            throw new Exception("Failed to generate invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public String getInvoiceFilePath(Sale sale) {

        Date saleDate = sale.getSaleDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(saleDate);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        return INVOICE_BASE_DIR + File.separator +
                year + File.separator +
                String.format("%02d", month) + File.separator +
                sanitizeFileName(sale.getSaleNumber()) + ".xml";
    }

    /**
     * Prepares the data model for Freemarker template
     */
    private Map<String, Object> prepareDataModel(Sale sale) {
        Map<String, Object> model = new HashMap<>();

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        String fechaEmision = isoFormat.format(sale.getSaleDate());

        model.put("fechaEmision", fechaEmision);
        model.put("fechaCertificacion", fechaEmision);

        Branch branch = sale.getBranch();
        model.put("codigoEstablecimiento", branch.getBranchId().toString());
        model.put("correoEmisor", ""); 
        model.put("nitEmisor", "12345678");
        model.put("nombreComercial", branch.getBranchName());
        model.put("nombreEmisor", branch.getBranchName());
        model.put("direccionEmisor", branch.getAddress() != null ? branch.getAddress() : "Guatemala");
        model.put("codigoPostal", "00000");
        model.put("municipio", "Guatemala");
        model.put("departamento", "Guatemala");

        model.put("correoReceptor", "");
        model.put("idReceptor", sale.getCustomerNit() != null ? sale.getCustomerNit() : "CF");
        model.put("nombreReceptor", sale.getCustomerName() != null ? sale.getCustomerName() : "CONSUMIDOR FINAL");
        model.put("direccionReceptor", sale.getCustomerAddress());

        List<Map<String, Object>> items = new ArrayList<>();
        int lineNumber = 1;
        for (SaleDetail detail : sale.getSaleDetails()) {
            Map<String, Object> item = new HashMap<>();
            item.put("numeroLinea", lineNumber++);
            item.put("cantidad", detail.getQuantity());
            item.put("descripcion", detail.getProduct().getCommercialName());
            item.put("precioUnitario", formatBigDecimal(detail.getUnitPrice()));

            BigDecimal precio = detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity()));
            item.put("precio", formatBigDecimal(precio));

            item.put("descuento", formatBigDecimal(detail.getDiscountAmount()));
            item.put("total", formatBigDecimal(detail.getLineTotal()));

            items.add(item);
        }
        model.put("items", items);


        model.put("granTotal", formatBigDecimal(sale.getTotalAmount()));

        model.put("numeroAutorizacion", generateRandomNumber(9));
        model.put("serieAutorizacion", generateRandomHex(8).toUpperCase());
        model.put("uuidAutorizacion", UUID.randomUUID().toString().toUpperCase());

        return model;
    }

    /**
     * Formats BigDecimal for XML (no thousands separator, 2 decimals)
     */
    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * Generates a random number as string
     */
    private String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Generates random hexadecimal string
     */
    private String generateRandomHex(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString();
    }

    /**
     * Sanitizes filename to remove invalid characters
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "invoice";
        }
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
