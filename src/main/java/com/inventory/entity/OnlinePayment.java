package com.inventory.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "online_payments")
public class OnlinePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer", length = 100)
    private String customer;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "amount_paid", precision = 14, scale = 2)
    private Double amountPaid = 0.0;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "sale_id")
    private Long saleId;

    @Column(name = "transaction_ref", length = 50)
    private String transactionRef;

    public OnlinePayment() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.paymentTime == null) {
            this.paymentTime = LocalDateTime.now();
        }
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }
}
