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
    
    List<Transaction> getCurrentUserTransactions();
    List<Transaction> getCurrentUserTransactionsByType(TransactionType type);
    List<Transaction> getCurrentUserTransactionsByCategory(Category category);
    List<Transaction> getCurrentUserTransactionsByDateRange(LocalDateTime start, LocalDateTime end);
    List<Transaction> getCurrentUserTransactionsByDateRange(LocalDate startDate, LocalDate endDate);
    List<Transaction> getCurrentUserTransactionsByStatus(String status);
    
    List<Transaction> getAllUsersTransactions();
    List<Transaction> getTransactionsByUser(User user);
    List<Transaction> getTransactionsByUserAndType(User user, TransactionType type);
    List<Transaction> getTransactionsByUserAndCategory(User user, Category category);
    List<Transaction> getTransactionsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
    
    List<Transaction> searchTransactions(String searchTerm);
    List<Transaction> searchTransactions(String query, Category category, 
                                      LocalDate startDate, LocalDate endDate);
    
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses();
    BigDecimal getCurrentBalance();
    
    Map<Category, BigDecimal> getExpensesByCategory();
    Map<Category, Double> getExpenseDistribution();
    
    Map<User, BigDecimal> getTotalIncomeByUser();
    Map<User, BigDecimal> getTotalExpensesByUser();
    Map<User, Map<Category, BigDecimal>> getExpensesByCategoryAndUser();
    
    void updateTransactionStatus(Long id, String newStatus);
    List<Transaction> getTransactionsByStatus(String status);
    void moveTransactions(Category fromCategory, Category toCategory);

    // Новые методы для администратора

    // Пакетные операции
    void batchUpdateStatus(List<Long> transactionIds, String newStatus);
    void batchDeleteTransactions(List<Long> transactionIds);
    void batchMoveTransactions(List<Long> transactionIds, Category toCategory);

    // Расширенная статистика
    Map<User, Map<String, BigDecimal>> getUserStatistics(LocalDate startDate, LocalDate endDate);
    Map<String, BigDecimal> getSystemStatistics(LocalDate startDate, LocalDate endDate);
    List<Transaction> getAnomalousTransactions(BigDecimal threshold);

    // Управление лимитами пользователей
    void setUserTransactionLimit(User user, TransactionType type, BigDecimal limit);
    void removeUserTransactionLimit(User user, TransactionType type);
    Map<User, Map<TransactionType, BigDecimal>> getAllUserLimits();
    boolean checkTransactionLimit(Transaction transaction);

    // Рабочий процесс утверждения
    void requireApprovalForTransactions(BigDecimal threshold);
    List<Transaction> getPendingApprovalTransactions();
    void approveTransaction(Long transactionId);
    void rejectTransaction(Long transactionId, String reason);
    List<Transaction> getRejectedTransactions();
} 