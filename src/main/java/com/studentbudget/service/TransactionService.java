package com.studentbudget.service;

import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    Transaction addTransaction(Transaction transaction);
    Transaction updateTransaction(Transaction transaction);
    void deleteTransaction(Long id);
    Transaction getTransaction(Long id);
    List<Transaction> getAllTransactions();
    
    List<Transaction> getTransactionsByType(TransactionType type);
    List<Transaction> getTransactionsByCategory(Category category);
    List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end);
    List<Transaction> searchTransactions(String searchTerm);
    
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses();
    BigDecimal getBalance();
    
    Map<Category, BigDecimal> getExpensesByCategory();
    Map<Category, Double> getExpenseDistribution();
    
    void updateTransactionStatus(Long id, String newStatus);
    List<Transaction> getTransactionsByStatus(String status);
} 