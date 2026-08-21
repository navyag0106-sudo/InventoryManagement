package com.inventory.service;

import com.inventory.dto.SaleRequest;
import com.inventory.entity.Sale;
import java.util.List;

public interface SaleService {
    Sale createSale(SaleRequest request);
    Sale getSaleById(Long id);
    List<Sale> getAllSales();
    List<Sale> searchSalesByCustomer(String customerName);
}
