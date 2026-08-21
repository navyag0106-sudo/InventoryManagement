package com.inventory.service.impl;

import com.inventory.entity.OnlinePayment;
import com.inventory.repository.OnlinePaymentRepository;
import com.inventory.dto.SaleItemRequest;
import com.inventory.dto.SaleRequest;
import com.inventory.entity.Customer;
import com.inventory.entity.Product;
import com.inventory.entity.Sale;
import com.inventory.entity.SaleItem;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.exception.SaleNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.service.CustomerService;
import com.inventory.service.SaleService;
import com.inventory.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final CustomerService customerService;
    private final OnlinePaymentRepository onlinePaymentRepository;

    @Autowired
    public SaleServiceImpl(SaleRepository saleRepository,
                            ProductRepository productRepository,
                            StockService stockService,
                            CustomerService customerService,
                            OnlinePaymentRepository onlinePaymentRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
        this.customerService = customerService;
        this.onlinePaymentRepository = onlinePaymentRepository;
    }

    @Override
    @Transactional
    public Sale createSale(SaleRequest request) {
        Sale sale = new Sale();
        sale.setCustomerName(request.getCustomerName());
        sale.setCustomerPhone(request.getCustomerPhone());
        sale.setCustomerAddress(request.getCustomerAddress());
        sale.setPaymentStatus("PAID");

        String pMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().trim().isEmpty())
                ? request.getPaymentMethod().trim().toUpperCase() : "CASH";
        sale.setPaymentMethod(pMethod);

        // Link or auto-create Customer entity
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty()) {
            Customer customer = customerService.getOrCreateCustomer(
                    request.getCustomerName(),
                    request.getCustomerPhone(),
                    request.getCustomerAddress(),
                    request.getCustomerEmail()
            );
            sale.setCustomer(customer);
        }

        double overallDiscountPercent = (request.getDiscountPercentage() != null && request.getDiscountPercentage() >= 0)
                ? request.getDiscountPercentage() : 0.0;
        sale.setDiscountPercentage(overallDiscountPercent);

        Sale savedSale = saleRepository.save(sale);

        double total = 0.0;
        double totalOriginal = 0.0;

        for (SaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product not found with id: " + itemReq.getProductId()));

            int itemQty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
            int availableQty = product.getQuantity() != null ? product.getQuantity() : 0;

            // Rule 5: Sale cannot exceed available stock.
            if (itemQty > availableQty) {
                throw new InsufficientStockException(
                        "Insufficient stock available. Product: " + product.getProductName()
                                + ", Available: " + availableQty
                                + ", Requested: " + itemQty);
            }

            double originalPrice = product.getSellingPrice() != null ? product.getSellingPrice() : 0.0;
            double itemDiscount = (itemReq.getDiscountPercentage() != null && itemReq.getDiscountPercentage() >= 0)
                    ? itemReq.getDiscountPercentage() : overallDiscountPercent;

            double discountedPrice = originalPrice * ((100.0 - itemDiscount) / 100.0);
            discountedPrice = Math.round(discountedPrice * 100.0) / 100.0; // round to 2 decimal places

            double subtotal = discountedPrice * itemReq.getQuantity();

            SaleItem item = new SaleItem();
            item.setSale(savedSale);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setOriginalPrice(originalPrice);
            item.setDiscountPercentage(itemDiscount);
            item.setSellingPrice(discountedPrice);
            item.setSubtotal(subtotal);
            savedSale.getSaleItems().add(item);

            total += subtotal;
            totalOriginal += (originalPrice * itemReq.getQuantity());

            // Sale decreases stock: New Stock = Existing Stock - Sold Quantity
            stockService.adjustStock(product, "SALE", -itemReq.getQuantity(), savedSale.getSaleId());
        }

        savedSale.setTotalAmount(Math.round(total * 100.0) / 100.0);
        savedSale.setDiscountAmount(Math.round((totalOriginal - total) * 100.0) / 100.0);

        Sale finalSale = saleRepository.save(savedSale);

        // Store transaction data in online_payments table if paymentMethod is ONLINE
        if ("ONLINE".equalsIgnoreCase(finalSale.getPaymentMethod())) {
            OnlinePayment op = new OnlinePayment();
            op.setCustomer(finalSale.getCustomerName());
            op.setMobileNumber(finalSale.getCustomerPhone());
            op.setAmountPaid(finalSale.getTotalAmount());
            op.setPaymentTime(finalSale.getSaleDate() != null ? finalSale.getSaleDate() : LocalDateTime.now());
            op.setSaleId(finalSale.getSaleId());
            op.setTransactionRef("UPI-" + finalSale.getSaleId() + "-" + (System.currentTimeMillis() % 100000));
            onlinePaymentRepository.save(op);
        }

        return finalSale;
    }

    @Override
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found with id: " + id));
    }

    @Override
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> searchSalesByCustomer(String customerName) {
        return saleRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }
}
