package com.studentbudget.service;

import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction);
    void updateTransaction(Transaction transaction);
    void deleteTransaction(Long id);
    Transaction getTransactionById(Long id);
    List<Transaction> getAllTransactions();
    
    List<Transaction> getTransactionsByType(TransactionType type);
    List<Transaction> getTransactionsByCategory(Category category);
    List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end);
    List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);
    List<Transaction> getTransactionsByStatus(String status);
    
    List<Transaction> searchTransactions(String searchTerm);
    List<Transaction> searchTransactions(String query, Category category, 
                                      LocalDate startDate, LocalDate endDate);
    
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses();
    BigDecimal getCurrentBalance();
    
    Map<Category, BigDecimal> getExpensesByCategory();
    Map<Category, Double> getExpenseDistribution();
    
    void updateTransactionStatus(Long id, String newStatus);
    void moveTransactions(Category fromCategory, Category toCategory);
} 