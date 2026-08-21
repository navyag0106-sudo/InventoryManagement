package com.inventory.service.impl;

import com.inventory.dto.DashboardResponse;
import com.inventory.entity.Product;
import com.inventory.entity.Purchase;
import com.inventory.entity.Sale;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public DashboardServiceImpl(ProductRepository productRepository,
                                 SupplierRepository supplierRepository,
                                 PurchaseRepository purchaseRepository,
                                 SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public DashboardResponse getDashboardData() {
        DashboardResponse response = new DashboardResponse();

        List<Product> allProducts = productRepository.findAll();
        List<Purchase> allPurchases = purchaseRepository.findAll();
        List<Sale> allSales = saleRepository.findAll();
        LocalDate today = LocalDate.now();

        response.setTotalProducts(allProducts.size());
        response.setTotalSuppliers(supplierRepository.count());
        response.setTotalPurchases(allPurchases.size());
        response.setTotalSales(allSales.size());
        response.setTotalStock(allProducts.stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum());

        List<Product> lowStock = productRepository.findLowStockProducts();
        response.setLowStockCount(lowStock != null ? lowStock.size() : 0);
        response.setLowStockProducts(lowStock);

        double todaysSales = allSales.stream()
                .filter(s -> s.getSaleDate() != null && s.getSaleDate().toLocalDate().isEqual(today))
                .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0)
                .sum();
        response.setTodaysSalesAmount(todaysSales);

        double todaysPurchases = allPurchases.stream()
                .filter(p -> p.getPurchaseDate() != null && p.getPurchaseDate().toLocalDate().isEqual(today))
                .mapToDouble(p -> p.getTotalAmount() != null ? p.getTotalAmount() : 0.0)
                .sum();
        response.setTodaysPurchasesAmount(todaysPurchases);

        return response;
    }
}
