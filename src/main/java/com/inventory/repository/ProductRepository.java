package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Product> findByProductNameContainingIgnoreCase(String name);

    List<Product> findByCategory_CategoryId(Long categoryId);

    List<Product> findBySupplier_SupplierId(Long supplierId);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.minimumStockLevel AND p.quantity > 0")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.quantity = 0")
    List<Product> findOutOfStockProducts();
}
