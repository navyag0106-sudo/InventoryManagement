package com.inventory.service.impl;

import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.CategoryNotFoundException;
import com.inventory.exception.DuplicateProductCodeException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.exception.SupplierNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional
    public Product addProduct(Product product) {
        if (productRepository.existsByProductCode(product.getProductCode())) {
            throw new DuplicateProductCodeException("Product code already exists: " + product.getProductCode());
        }
        resolveRelations(product);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existing = getProductById(id);

        // Only check duplication if product code actually changed
        if (product.getProductCode() != null
                && (existing.getProductCode() == null || !existing.getProductCode().equalsIgnoreCase(product.getProductCode()))
                && productRepository.existsByProductCode(product.getProductCode())) {
            throw new DuplicateProductCodeException("Product code already exists: " + product.getProductCode());
        }

        existing.setProductName(product.getProductName());
        existing.setProductCode(product.getProductCode());
        existing.setDescription(product.getDescription());
        existing.setUnitPrice(product.getUnitPrice());
        existing.setSellingPrice(product.getSellingPrice());
        existing.setQuantity(product.getQuantity());
        existing.setMinimumStockLevel(product.getMinimumStockLevel());
        existing.setStatus(product.getStatus());
        existing.setDiscount(product.getDiscount());

        if (product.getCategory() != null && product.getCategory().getCategoryId() != null) {
            existing.setCategory(fetchCategory(product.getCategory().getCategoryId()));
        } else {
            existing.setCategory(null);
        }
        if (product.getSupplier() != null && product.getSupplier().getSupplierId() != null) {
            existing.setSupplier(fetchSupplier(product.getSupplier().getSupplierId()));
        } else {
            existing.setSupplier(null);
        }

        return productRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product existing = getProductById(id);
        productRepository.delete(existing);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> searchProducts(String name) {
        return productRepository.findByProductNameContainingIgnoreCase(name);
    }

    @Override
    public List<Product> filterByCategory(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    @Override
    public List<Product> filterBySupplier(Long supplierId) {
        return productRepository.findBySupplier_SupplierId(supplierId);
    }

    private void resolveRelations(Product product) {
        if (product.getCategory() != null && product.getCategory().getCategoryId() != null) {
            product.setCategory(fetchCategory(product.getCategory().getCategoryId()));
        } else {
            product.setCategory(null);
        }
        if (product.getSupplier() != null && product.getSupplier().getSupplierId() != null) {
            product.setSupplier(fetchSupplier(product.getSupplier().getSupplierId()));
        } else {
            product.setSupplier(null);
        }
    }

    private Category fetchCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
    }

    private Supplier fetchSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));
    }
}
