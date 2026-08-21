package com.inventory.service.impl;

import com.inventory.entity.Product;
import com.inventory.entity.StockTransaction;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockTransactionRepository;
import com.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Autowired
    public StockServiceImpl(ProductRepository productRepository,
                             StockTransactionRepository stockTransactionRepository) {
        this.productRepository = productRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    @Override
    public List<Product> getAllStock() {
        return productRepository.findAll();
    }

    @Override
    public Product getStockByProductId(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    }

    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    @Override
    public List<StockTransaction> getTransactionHistory(Long productId) {
        return stockTransactionRepository.findByProduct_ProductIdOrderByTransactionDateDesc(productId);
    }

    @Override
    public List<StockTransaction> getAllTransactions() {
        return stockTransactionRepository.findAllByOrderByTransactionDateDesc();
    }

    /**
     * quantityDelta: positive for PURCHASE (stock increase),
     *                negative for SALE (stock decrease).
     * Runs inside the caller's transaction (Purchase/SaleService) so the
     * stock update and the stock transaction record are atomic together
     * with the purchase/sale save.
     */
    @Override
    @Transactional
    public StockTransaction adjustStock(Product product, String transactionType, int quantityDelta, Long referenceId) {
        int previousStock = product.getQuantity();
        int updatedStock = previousStock + quantityDelta;

        if (updatedStock < 0) {
            throw new com.inventory.exception.InsufficientStockException(
                    "Insufficient stock available for product: " + product.getProductName());
        }

        product.setQuantity(updatedStock);
        productRepository.save(product);

        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(Math.abs(quantityDelta));
        transaction.setPreviousStock(previousStock);
        transaction.setUpdatedStock(updatedStock);
        transaction.setReferenceId(referenceId);

        return stockTransactionRepository.save(transaction);
    }
}
