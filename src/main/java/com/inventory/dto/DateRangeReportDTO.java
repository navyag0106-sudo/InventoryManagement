package com.inventory.dto;

import com.inventory.entity.Customer;
import com.inventory.entity.Purchase;
import com.inventory.entity.Sale;

import java.time.LocalDate;
import java.util.List;

public class DateRangeReportDTO {

    private LocalDate fromDate;
    private LocalDate toDate;

    private long salesCount;
    private double totalSalesAmount;

    private long purchaseCount;
    private double totalPurchaseAmount;

    private double totalProfit;
    private double grossProfit;

    private double totalDiscountAmount;
    private long totalProductsSold;
    private long totalProductsPurchased;

    private long customerCount;

    private List<Sale> salesList;
    private List<Purchase> purchasesList;
    private List<Customer> customersList;

    public DateRangeReportDTO() {
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(long salesCount) {
        this.salesCount = salesCount;
    }

    public double getTotalSalesAmount() {
        return totalSalesAmount;
    }

    public void setTotalSalesAmount(double totalSalesAmount) {
        this.totalSalesAmount = totalSalesAmount;
    }

    public long getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(long purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public double getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public void setTotalPurchaseAmount(double totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public double getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(double totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public long getTotalProductsSold() {
        return totalProductsSold;
    }

    public void setTotalProductsSold(long totalProductsSold) {
        this.totalProductsSold = totalProductsSold;
    }

    public long getTotalProductsPurchased() {
        return totalProductsPurchased;
    }

    public void setTotalProductsPurchased(long totalProductsPurchased) {
        this.totalProductsPurchased = totalProductsPurchased;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(long customerCount) {
        this.customerCount = customerCount;
    }

    public List<Sale> getSalesList() {
        return salesList;
    }

    public void setSalesList(List<Sale> salesList) {
        this.salesList = salesList;
    }

    public List<Purchase> getPurchasesList() {
        return purchasesList;
    }

    public void setPurchasesList(List<Purchase> purchasesList) {
        this.purchasesList = purchasesList;
    }

    public List<Customer> getCustomersList() {
        return customersList;
    }

    public void setCustomersList(List<Customer> customersList) {
        this.customersList = customersList;
    }
}
