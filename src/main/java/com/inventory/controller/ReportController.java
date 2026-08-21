package com.inventory.controller;

import com.inventory.dto.DateRangeReportDTO;
import com.inventory.entity.Product;
import com.inventory.entity.Purchase;
import com.inventory.entity.Sale;
import com.inventory.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> productReport() {
        return ResponseEntity.ok(reportService.getProductReport());
    }

    @GetMapping("/purchases")
    public ResponseEntity<List<Purchase>> purchaseReport() {
        return ResponseEntity.ok(reportService.getPurchaseReport());
    }

    @GetMapping("/sales")
    public ResponseEntity<List<Sale>> salesReport() {
        return ResponseEntity.ok(reportService.getSalesReport());
    }

    @GetMapping("/stock")
    public ResponseEntity<List<Map<String, Object>>> stockReport() {
        return ResponseEntity.ok(reportService.getStockReport());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> lowStockReport() {
        return ResponseEntity.ok(reportService.getLowStockReport());
    }

    @GetMapping("/date-range")
    public ResponseEntity<DateRangeReportDTO> dateRangeReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getDateRangeReport(fromDate, toDate));
    }
}
