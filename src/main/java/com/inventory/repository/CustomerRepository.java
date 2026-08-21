package com.inventory.repository;

import com.inventory.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    List<Customer> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    List<Customer> findByStatus(String status);

    List<Customer> findByCreatedDateBetweenOrderByCreatedDateDesc(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
