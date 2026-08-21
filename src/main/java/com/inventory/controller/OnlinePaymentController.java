package com.inventory.controller;

import com.inventory.entity.OnlinePayment;
import com.inventory.repository.OnlinePaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/online-payments")
public class OnlinePaymentController {

    private final OnlinePaymentRepository onlinePaymentRepository;

    @Autowired
    public OnlinePaymentController(OnlinePaymentRepository onlinePaymentRepository) {
        this.onlinePaymentRepository = onlinePaymentRepository;
    }

    @GetMapping
    public ResponseEntity<List<OnlinePayment>> getAllOnlinePayments() {
        return ResponseEntity.ok(onlinePaymentRepository.findAllByOrderByIdDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnlinePayment> getById(@PathVariable Long id) {
        return onlinePaymentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
