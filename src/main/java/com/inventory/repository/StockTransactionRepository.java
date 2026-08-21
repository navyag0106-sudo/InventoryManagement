package com.inventory.repository;

import com.inventory.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProduct_ProductIdOrderByTransactionDateDesc(Long productId);
    List<StockTransaction> findAllByOrderByTransactionDateDesc();
}
