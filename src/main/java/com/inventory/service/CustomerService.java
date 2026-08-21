package com.inventory.service;

import com.inventory.entity.Customer;
import com.inventory.entity.Sale;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Long id, Customer customer);

    Customer getCustomerById(Long id);

    Customer getCustomerByPhone(String phone);

    Customer getOrCreateCustomer(String name, String phone, String address, String email);

    List<Customer> getAllCustomers();

    List<Customer> searchCustomers(String query);

    List<Sale> getCustomerSalesHistory(Long customerId);

    void deleteCustomer(Long id);
}
