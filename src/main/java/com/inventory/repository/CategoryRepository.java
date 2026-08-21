package com.inventory.repository;

import com.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
    List<Category> findByCategoryNameContainingIgnoreCase(String name);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
