package com.inventory.service;

import com.inventory.dto.DateRangeReportDTO;
import com.inventory.entity.Product;
import com.inventory.entity.Purchase;
import com.inventory.entity.Sale;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    List<Product> getProductReport();
    List<Purchase> getPurchaseReport();
    List<Sale> getSalesReport();
    List<Map<String, Object>> getStockReport();
    List<Product> getLowStockReport();
    DateRangeReportDTO getDateRangeReport(LocalDate fromDate, LocalDate toDate);
}
