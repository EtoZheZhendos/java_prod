package com.studentbudget.service;

import com.studentbudget.model.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    void updateCategory(Category category);
    void deleteCategory(Long id);
    Category getCategoryById(Long id);
    List<Category> getAllCategories();
    Category getCategoryByName(String name);
    boolean isCategoryNameUnique(String name);
    boolean hasTransactions(Long categoryId);
    void deleteCategoryWithTransactions(Long categoryId, Category newCategory);
} 