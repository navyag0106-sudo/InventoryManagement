package com.inventory.repository;

import com.inventory.entity.OnlinePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnlinePaymentRepository extends JpaRepository<OnlinePayment, Long> {
    List<OnlinePayment> findAllByOrderByIdDesc();
}
