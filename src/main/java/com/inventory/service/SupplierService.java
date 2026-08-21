package com.inventory.service;

import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import java.util.List;

public interface SupplierService {
    Supplier addSupplier(Supplier supplier);
    Supplier updateSupplier(Long id, Supplier supplier);
    void deleteSupplier(Long id);
    Supplier getSupplierById(Long id);
    List<Supplier> getAllSuppliers();
    List<Supplier> searchSuppliers(String name);
    List<Product> getSupplierProducts(Long supplierId);
}
