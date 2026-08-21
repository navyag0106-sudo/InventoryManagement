package com.inventory.service;

import com.inventory.entity.Product;
import com.inventory.entity.StockTransaction;
import java.util.List;

public interface StockService {
    List<Product> getAllStock();
    Product getStockByProductId(Long productId);
    List<Product> getLowStockProducts();
    List<Product> getOutOfStockProducts();
    List<StockTransaction> getTransactionHistory(Long productId);
    List<StockTransaction> getAllTransactions();

    /**
     * Central method used by both Purchase and Sale flows to adjust stock
     * and write a StockTransaction record atomically with the caller's transaction.
     */
    StockTransaction adjustStock(Product product, String transactionType, int quantityDelta, Long referenceId);
}
