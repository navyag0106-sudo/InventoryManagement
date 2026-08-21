package com.inventory.controller;

import com.inventory.entity.Product;
import com.inventory.entity.StockTransaction;
import com.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllStock() {
        return ResponseEntity.ok(stockService.getAllStock());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getStockByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getStockByProductId(productId));
    }

    @GetMapping("/low")
    public ResponseEntity<List<Product>> getLowStock() {
        return ResponseEntity.ok(stockService.getLowStockProducts());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStock() {
        return ResponseEntity.ok(stockService.getOutOfStockProducts());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<StockTransaction>> getAllTransactions() {
        return ResponseEntity.ok(stockService.getAllTransactions());
    }

    @GetMapping("/transactions/{productId}")
    public ResponseEntity<List<StockTransaction>> getTransactionHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getTransactionHistory(productId));
    }
}
