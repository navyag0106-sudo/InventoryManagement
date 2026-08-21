package com.inventory.dto;

import com.inventory.entity.Product;
import java.util.List;

public class DashboardResponse {

    private long totalProducts;
    private long totalSuppliers;
    private long totalPurchases;
    private long totalSales;
    private int totalStock;
    private long lowStockCount;
    private double todaysSalesAmount;
    private double todaysPurchasesAmount;
    private List<Product> lowStockProducts;

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalSuppliers() {
        return totalSuppliers;
    }

    public void setTotalSuppliers(long totalSuppliers) {
        this.totalSuppliers = totalSuppliers;
    }

    public long getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(long totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public double getTodaysSalesAmount() {
        return todaysSalesAmount;
    }

    public void setTodaysSalesAmount(double todaysSalesAmount) {
        this.todaysSalesAmount = todaysSalesAmount;
    }

    public double getTodaysPurchasesAmount() {
        return todaysPurchasesAmount;
    }

    public void setTodaysPurchasesAmount(double todaysPurchasesAmount) {
        this.todaysPurchasesAmount = todaysPurchasesAmount;
    }

    public List<Product> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<Product> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }
}
