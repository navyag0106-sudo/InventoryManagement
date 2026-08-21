package com.inventory.service;

import com.inventory.entity.Product;
import java.util.List;

public interface ProductService {
    Product addProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    Product getProductById(Long id);
    List<Product> getAllProducts();
    List<Product> searchProducts(String name);
    List<Product> filterByCategory(Long categoryId);
    List<Product> filterBySupplier(Long supplierId);
}
