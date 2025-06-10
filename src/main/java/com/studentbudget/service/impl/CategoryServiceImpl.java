package com.studentbudget.service.impl;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Category;
import com.studentbudget.model.Transaction;
import com.studentbudget.service.CategoryService;
import com.studentbudget.util.HibernateTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryDao categoryDao;
    private final TransactionDao transactionDao;
    private final HibernateTransactionManager transactionManager;

    public CategoryServiceImpl(CategoryDao categoryDao, TransactionDao transactionDao, HibernateTransactionManager transactionManager) {
        this.categoryDao = categoryDao;
        this.transactionDao = transactionDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public Category createCategory(Category category) {
        logger.debug("Creating new category: {}", category);
        return transactionManager.executeInTransaction(session -> {
            Category existingCategory = categoryDao.findByName(category.getName());
            if (existingCategory != null) {
                throw new IllegalArgumentException("Category name already exists: " + category.getName());
            }
            return categoryDao.save(category);
        });
    }

    @Override
    public void updateCategory(Category category) {
        logger.debug("Updating category with id {}: {}", category.getId(), category);
        transactionManager.executeInTransactionWithoutResult(session -> {
            Category existingCategory = categoryDao.findById(category.getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + category.getId()));
            
            if (!existingCategory.getName().equals(category.getName()) && 
                !isCategoryNameUnique(category.getName())) {
                throw new IllegalArgumentException("Category name already exists: " + category.getName());
            }
            
            categoryDao.update(category);
        });
    }

    @Override
    public void deleteCategory(Long id) {
        logger.debug("Deleting category with id: {}", id);
        transactionManager.executeInTransactionWithoutResult(session -> {
            Category category = categoryDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
            
            List<Transaction> transactions = transactionDao.findByCategory(category);
            if (!transactions.isEmpty()) {
                throw new IllegalStateException("Cannot delete category with existing transactions");
            }
            
            categoryDao.deleteById(id);
        });
    }

    @Override
    public Category getCategoryById(Long id) {
        logger.debug("Fetching category with id: {}", id);
        return transactionManager.executeInTransaction(session -> 
            categoryDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id))
        );
    }

    @Override
    public List<Category> getAllCategories() {
        logger.debug("Fetching all categories");
        return transactionManager.executeInTransaction(session -> categoryDao.findAll());
    }

    @Override
    public Category getCategoryByName(String name) {
        logger.debug("Fetching category by name: {}", name);
        return transactionManager.executeInTransaction(session -> categoryDao.findByName(name));
    }

    @Override
    public boolean isCategoryNameUnique(String name) {
        logger.debug("Checking if category name is unique: {}", name);
        return transactionManager.executeInTransaction(session -> categoryDao.findByName(name) == null);
    }

    @Override
    public boolean hasTransactions(Long categoryId) {
        logger.debug("Checking if category has transactions: {}", categoryId);
        return transactionManager.executeInTransaction(session -> {
            Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
            List<Transaction> transactions = transactionDao.findByCategory(category);
            return !transactions.isEmpty();
        });
    }

    public void deleteCategoryWithTransactions(Long categoryId, Category newCategory) {
        logger.debug("Deleting category {} and moving transactions to category {}", categoryId, newCategory.getName());
        transactionManager.executeInTransactionWithoutResult(session -> {
            Category oldCategory = categoryDao.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
            
            List<Transaction> transactions = transactionDao.findByCategory(oldCategory);
            for (Transaction transaction : transactions) {
                transaction.setCategory(newCategory);
                transactionDao.update(transaction);
            }
            

            categoryDao.deleteById(categoryId);
        });
    }
} 