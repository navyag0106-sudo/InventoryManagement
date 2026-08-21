package com.inventory.repository;

import com.inventory.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCustomerNameContainingIgnoreCase(String customerName);

    List<Sale> findByCustomerCustomerIdOrderBySaleDateDesc(Long customerId);

    List<Sale> findByCustomerPhoneOrderBySaleDateDesc(String customerPhone);

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
