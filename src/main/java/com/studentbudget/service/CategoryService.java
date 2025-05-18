package com.studentbudget.service;

import com.studentbudget.model.Category;
import java.util.List;

public interface CategoryService {
    Category addCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Long id);
    Category getCategory(Long id);
    List<Category> getAllCategories();
    Category getCategoryByName(String name);
} 