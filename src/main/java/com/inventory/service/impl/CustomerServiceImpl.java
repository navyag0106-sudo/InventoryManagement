package com.inventory.service.impl;

import com.inventory.entity.Customer;
import com.inventory.entity.Sale;
import com.inventory.exception.CustomerNotFoundException;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            Optional<Customer> existing = customerRepository.findByPhone(customer.getPhone().trim());
            if (existing.isPresent()) {
                Customer c = existing.get();
                c.setName(customer.getName());
                if (customer.getAddress() != null) c.setAddress(customer.getAddress());
                if (customer.getEmail() != null) c.setEmail(customer.getEmail());
                return customerRepository.save(c);
            }
        }
        customer.setStatus("ACTIVE");
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        customer.setName(customerDetails.getName());
        customer.setPhone(customerDetails.getPhone());
        customer.setEmail(customerDetails.getEmail());
        customer.setAddress(customerDetails.getAddress());
        if (customerDetails.getStatus() != null) {
            customer.setStatus(customerDetails.getStatus());
        }
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    @Override
    public Customer getCustomerByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        return customerRepository.findByPhone(phone.trim()).orElse(null);
    }

    @Override
    @Transactional
    public Customer getOrCreateCustomer(String name, String phone, String address, String email) {
        String trimmedPhone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null;
        if (trimmedPhone != null) {
            Optional<Customer> existing = customerRepository.findByPhone(trimmedPhone);
            if (existing.isPresent()) {
                Customer c = existing.get();
                if (name != null && !name.trim().isEmpty()) c.setName(name.trim());
                if (address != null && !address.trim().isEmpty()) c.setAddress(address.trim());
                if (email != null && !email.trim().isEmpty()) c.setEmail(email.trim());
                return customerRepository.save(c);
            }
        } else {
            Optional<Customer> walkInOpt = customerRepository.findByPhone("WALKIN-CUSTOMER");
            if (walkInOpt.isPresent()) {
                return walkInOpt.get();
            }
            trimmedPhone = "WALKIN-CUSTOMER";
        }
        Customer newCustomer = new Customer();
        newCustomer.setName(name != null && !name.trim().isEmpty() ? name.trim() : "Walk-in Customer");
        newCustomer.setPhone(trimmedPhone);
        newCustomer.setAddress(address);
        newCustomer.setEmail(email);
        newCustomer.setStatus("ACTIVE");
        return customerRepository.save(newCustomer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query.trim(), query.trim());
    }

    @Override
    public List<Sale> getCustomerSalesHistory(Long customerId) {
        return saleRepository.findByCustomerCustomerIdOrderBySaleDateDesc(customerId);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
}
