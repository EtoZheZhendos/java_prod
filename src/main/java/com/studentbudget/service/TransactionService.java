package com.studentbudget.service;

import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import com.studentbudget.model.User;
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
    
    // Методы для работы с транзакциями текущего пользователя
    List<Transaction> getCurrentUserTransactions();
    List<Transaction> getCurrentUserTransactionsByType(TransactionType type);
    List<Transaction> getCurrentUserTransactionsByCategory(Category category);
    List<Transaction> getCurrentUserTransactionsByDateRange(LocalDateTime start, LocalDateTime end);
    List<Transaction> getCurrentUserTransactionsByDateRange(LocalDate startDate, LocalDate endDate);
    List<Transaction> getCurrentUserTransactionsByStatus(String status);
    
    // Методы для администратора
    List<Transaction> getAllUsersTransactions();
    List<Transaction> getTransactionsByUser(User user);
    List<Transaction> getTransactionsByUserAndType(User user, TransactionType type);
    List<Transaction> getTransactionsByUserAndCategory(User user, Category category);
    List<Transaction> getTransactionsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
    
    List<Transaction> searchTransactions(String searchTerm);
    List<Transaction> searchTransactions(String query, Category category, 
                                      LocalDate startDate, LocalDate endDate);
    
    // Методы для получения финансовой статистики
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses();
    BigDecimal getCurrentBalance();
    
    // Методы для получения статистики по категориям
    Map<Category, BigDecimal> getExpensesByCategory();
    Map<Category, Double> getExpenseDistribution();
    
    // Методы для администратора по работе со статистикой
    Map<User, BigDecimal> getTotalIncomeByUser();
    Map<User, BigDecimal> getTotalExpensesByUser();
    Map<User, Map<Category, BigDecimal>> getExpensesByCategoryAndUser();
    
    void updateTransactionStatus(Long id, String newStatus);
    List<Transaction> getTransactionsByStatus(String status);
    void moveTransactions(Category fromCategory, Category toCategory);
} 