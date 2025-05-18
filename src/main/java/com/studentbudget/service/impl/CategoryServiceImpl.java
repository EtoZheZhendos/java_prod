package com.studentbudget.service.impl;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.model.Category;
import com.studentbudget.service.CategoryService;
import com.studentbudget.util.HibernateTransactionManager;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryDao categoryDao;
    private final HibernateTransactionManager transactionManager;

    public CategoryServiceImpl(CategoryDao categoryDao, HibernateTransactionManager transactionManager) {
        this.categoryDao = categoryDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public Category addCategory(Category category) {
        return transactionManager.executeInTransaction(session -> categoryDao.save(category));
    }

    @Override
    public Category updateCategory(Category category) {
        return transactionManager.executeInTransaction(session -> {
            if (!categoryDao.findById(category.getId()).isPresent()) {
                throw new IllegalArgumentException("Category not found with id: " + category.getId());
            }
            return categoryDao.update(category);
        });
    }

    @Override
    public void deleteCategory(Long id) {
        transactionManager.executeInTransactionWithoutResult(session -> categoryDao.deleteById(id));
    }

    @Override
    public Category getCategory(Long id) {
        return transactionManager.executeInTransaction(session -> 
            categoryDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id)));
    }

    @Override
    public List<Category> getAllCategories() {
        return transactionManager.executeInTransaction(session -> categoryDao.findAll());
    }

    @Override
    public Category getCategoryByName(String name) {
        return transactionManager.executeInTransaction(session -> categoryDao.findByName(name));
    }
} 