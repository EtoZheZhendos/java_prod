package com.studentbudget.dao;

import com.studentbudget.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    Category save(Category category);
    Category update(Category category);
    void deleteById(Long id);
    Optional<Category> findById(Long id);
    List<Category> findAll();
    Category findByName(String name);
} 