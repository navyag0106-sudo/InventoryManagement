package com.inventory.service;

import com.inventory.dto.PurchaseRequest;
import com.inventory.entity.Purchase;
import java.util.List;

public interface PurchaseService {
    Purchase createPurchase(PurchaseRequest request);
    Purchase getPurchaseById(Long id);
    List<Purchase> getAllPurchases();
    List<Purchase> getPurchasesBySupplier(Long supplierId);
}
