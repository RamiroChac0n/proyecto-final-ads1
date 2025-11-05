package com.mycompany.service;

import com.mycompany.model.entity.Sale;
import jakarta.ejb.Local;
import java.io.File;

/**
 * Service for generating invoice XML documents
 * @author ramir
 */
@Local
public interface IInvoiceGenerationService {

    /**
     * Generates an invoice XML file from a sale
     * @param sale The sale to generate invoice for
     * @return The generated XML file
     * @throws Exception if generation fails
     */
    File generateInvoiceXML(Sale sale) throws Exception;

    /**
     * Gets the invoice file path for a sale
     * @param sale The sale
     * @return The expected file path
     */
    String getInvoiceFilePath(Sale sale);
}
