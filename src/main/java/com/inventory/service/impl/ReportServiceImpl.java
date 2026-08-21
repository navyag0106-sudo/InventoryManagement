package com.inventory.service.impl;

import com.inventory.dto.DateRangeReportDTO;
import com.inventory.entity.Customer;
import com.inventory.entity.Product;
import com.inventory.entity.Purchase;
import com.inventory.entity.PurchaseItem;
import com.inventory.entity.Sale;
import com.inventory.entity.SaleItem;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseItemRepository;
import com.inventory.repository.PurchaseRepository;
import com.inventory.repository.SaleItemRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SaleItemRepository saleItemRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public ReportServiceImpl(ProductRepository productRepository,
                              PurchaseRepository purchaseRepository,
                              SaleRepository saleRepository,
                              PurchaseItemRepository purchaseItemRepository,
                              SaleItemRepository saleItemRepository,
                              CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.saleRepository = saleRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.saleItemRepository = saleItemRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Product> getProductReport() {
        return productRepository.findAll();
    }

    @Override
    public List<Purchase> getPurchaseReport() {
        return purchaseRepository.findAll();
    }

    @Override
    public List<Sale> getSalesReport() {
        return saleRepository.findAll();
    }

    /**
     * Stock report: for every product show opening stock, purchased qty,
     * sold qty, and current stock.
     * Opening Stock = Current Stock - TotalPurchased + TotalSold
     */
    @Override
    public List<Map<String, Object>> getStockReport() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Product product : products) {
            int purchasedQty = purchaseItemRepository.findByProduct_ProductId(product.getProductId())
                    .stream().mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0).sum();
            int soldQty = saleItemRepository.findByProduct_ProductId(product.getProductId())
                    .stream().mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0).sum();
            int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
            int openingStock = currentStock - purchasedQty + soldQty;

            Map<String, Object> row = new HashMap<>();
            row.put("product", product.getProductName());
            row.put("productId", product.getProductId());
            row.put("openingStock", openingStock);
            row.put("purchasedQuantity", purchasedQty);
            row.put("soldQuantity", soldQty);
            row.put("currentStock", currentStock);
            report.add(row);
        }
        return report;
    }

    @Override
    public List<Product> getLowStockReport() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public DateRangeReportDTO getDateRangeReport(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusMonths(1);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startDateTime, endDateTime);
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetweenOrderByPurchaseDateDesc(startDateTime, endDateTime);
        List<Customer> customers = customerRepository.findByCreatedDateBetweenOrderByCreatedDateDesc(startDateTime, endDateTime);

        // Map all products to their unit price (unit cost)
        Map<Long, Double> productCostMap = new HashMap<>();
        for (Product p : productRepository.findAll()) {
            if (p.getProductId() != null) {
                productCostMap.put(p.getProductId(), p.getUnitPrice() != null ? p.getUnitPrice() : 0.0);
            }
        }

        double totalSalesAmount = sales.stream()
                .mapToDouble(s -> s.getTotalAmount() != null ? s.getTotalAmount() : 0.0)
                .sum();

        double totalPurchaseAmount = purchases.stream()
                .mapToDouble(p -> p.getTotalAmount() != null ? p.getTotalAmount() : 0.0)
                .sum();

        double totalDiscountAmount = 0.0;
        long totalProductsSold = 0;
        double totalCostOfGoodsSold = 0.0;
        double totalProfitFromUnitCost = 0.0;

        for (Sale s : sales) {
            double saleDiscount = s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0;
            if (saleDiscount > 0) {
                totalDiscountAmount += saleDiscount;
            }

            if (s.getSaleItems() != null) {
                for (SaleItem item : s.getSaleItems()) {
                    int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                    totalProductsSold += qty;

                    double sellingPricePerUnit = item.getSellingPrice() != null ? item.getSellingPrice() : 0.0;

                    if (saleDiscount == 0 && item.getOriginalPrice() != null && item.getSellingPrice() != null) {
                        totalDiscountAmount += (item.getOriginalPrice() - item.getSellingPrice()) * qty;
                    }

                    // Look up Unit Cost from Product map
                    Long pId = (item.getProduct() != null) ? item.getProduct().getProductId() : null;
                    double unitCost = 0.0;
                    if (pId != null && productCostMap.containsKey(pId)) {
                        unitCost = productCostMap.get(pId);
                    } else if (item.getProduct() != null && item.getProduct().getUnitPrice() != null) {
                        unitCost = item.getProduct().getUnitPrice();
                    }

                    totalCostOfGoodsSold += (unitCost * qty);

                    // Profit = (Selling Price charged per unit - Unit Cost) * Quantity
                    double itemProfit = (sellingPricePerUnit - unitCost) * qty;
                    totalProfitFromUnitCost += itemProfit;
                }
            }
        }

        double totalProfit = Math.round(totalProfitFromUnitCost * 100.0) / 100.0;
        double grossProfit = totalProfit;

        long totalProductsPurchased = purchases.stream()
                .filter(p -> p.getPurchaseItems() != null)
                .flatMap(p -> p.getPurchaseItems().stream())
                .mapToLong(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();

        DateRangeReportDTO dto = new DateRangeReportDTO();
        dto.setFromDate(fromDate);
        dto.setToDate(toDate);
        dto.setSalesCount(sales.size());
        dto.setTotalSalesAmount(totalSalesAmount);
        dto.setPurchaseCount(purchases.size());
        dto.setTotalPurchaseAmount(totalPurchaseAmount);
        dto.setTotalProfit(totalProfit);
        dto.setGrossProfit(grossProfit);
        dto.setTotalDiscountAmount(Math.round(totalDiscountAmount * 100.0) / 100.0);
        dto.setTotalProductsSold(totalProductsSold);
        dto.setTotalProductsPurchased(totalProductsPurchased);
        dto.setCustomerCount(customers.size());
        dto.setSalesList(sales);
        dto.setPurchasesList(purchases);
        dto.setCustomersList(customers);

        return dto;
    }
}
