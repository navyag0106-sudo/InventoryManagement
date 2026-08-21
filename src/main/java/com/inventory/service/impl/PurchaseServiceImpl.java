package com.inventory.service.impl;

import com.inventory.dto.PurchaseItemRequest;
import com.inventory.dto.PurchaseRequest;
import com.inventory.entity.Product;
import com.inventory.entity.Purchase;
import com.inventory.entity.PurchaseItem;
import com.inventory.entity.Supplier;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.exception.PurchaseNotFoundException;
import com.inventory.exception.SupplierNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.PurchaseService;
import com.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                                SupplierRepository supplierRepository,
                                ProductRepository productRepository,
                                StockService stockService) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
    }

    /**
     * PurchaseService workflow:
     *   Save Purchase -> Save Purchase Items -> Update Product Stock -> Create Stock Transaction
     * The whole flow is wrapped in a single @Transactional so it is atomic:
     * if any step fails, everything rolls back (Rule 8).
     */
    @Override
    @Transactional
    public Purchase createPurchase(PurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException(
                        "Supplier not found with id: " + request.getSupplierId()));

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setPaymentStatus("PENDING");

        double total = 0.0;

        // First save the purchase to get an ID (needed as stock transaction reference)
        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (PurchaseItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product not found with id: " + itemReq.getProductId()));

            double subtotal = itemReq.getPurchasePrice() * itemReq.getQuantity();

            PurchaseItem item = new PurchaseItem();
            item.setPurchase(savedPurchase);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPurchasePrice(itemReq.getPurchasePrice());
            item.setSubtotal(subtotal);
            savedPurchase.getPurchaseItems().add(item);

            total += subtotal;

            // Purchase increases stock: New Stock = Existing Stock + Purchased Quantity
            stockService.adjustStock(product, "PURCHASE", itemReq.getQuantity(), savedPurchase.getPurchaseId());
        }

        savedPurchase.setTotalAmount(total);
        savedPurchase.setPaymentStatus("COMPLETED");

        return purchaseRepository.save(savedPurchase);
    }

    @Override
    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException("Purchase not found with id: " + id));
    }

    @Override
    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAll();
    }

    @Override
    public List<Purchase> getPurchasesBySupplier(Long supplierId) {
        return purchaseRepository.findBySupplier_SupplierId(supplierId);
    }
}
