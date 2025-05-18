package com.studentbudget.service.impl;

import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import com.studentbudget.model.TransactionStatus;
import com.studentbudget.service.TransactionService;
import com.studentbudget.util.HibernateTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionServiceImpl implements TransactionService {
    private final TransactionDao transactionDao;
    private final HibernateTransactionManager transactionManager;

    public TransactionServiceImpl(TransactionDao transactionDao, HibernateTransactionManager transactionManager) {
        this.transactionDao = transactionDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public Transaction addTransaction(Transaction transaction) {
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        return transactionManager.executeInTransaction(session -> transactionDao.save(transaction));
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        return transactionManager.executeInTransaction(session -> {
            if (!transactionDao.findById(transaction.getId()).isPresent()) {
                throw new IllegalArgumentException("Transaction not found with id: " + transaction.getId());
            }
            return transactionDao.update(transaction);
        });
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionManager.executeInTransactionWithoutResult(session -> transactionDao.deleteById(id));
    }

    @Override
    public Transaction getTransaction(Long id) {
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id)));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionManager.executeInTransaction(session -> transactionDao.findAll());
    }

    @Override
    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactionManager.executeInTransaction(session -> transactionDao.findByType(type));
    }

    @Override
    public List<Transaction> getTransactionsByCategory(Category category) {
        return transactionManager.executeInTransaction(session -> transactionDao.findByCategory(category));
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactionManager.executeInTransaction(session -> transactionDao.findByDateRange(start, end));
    }

    @Override
    public List<Transaction> searchTransactions(String searchTerm) {
        return transactionManager.executeInTransaction(session -> transactionDao.searchByDescription(searchTerm));
    }

    @Override
    public BigDecimal getTotalIncome() {
        return transactionManager.executeInTransaction(session -> {
            List<Transaction> incomeTransactions = transactionDao.findByType(TransactionType.INCOME);
            return incomeTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        });
    }

    @Override
    public BigDecimal getTotalExpenses() {
        return transactionManager.executeInTransaction(session -> {
            List<Transaction> expenseTransactions = transactionDao.findByType(TransactionType.EXPENSE);
            return expenseTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        });
    }

    @Override
    public BigDecimal getBalance() {
        return transactionManager.executeInTransaction(session -> {
            List<Transaction> allTransactions = transactionDao.findAll();
            BigDecimal income = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expenses = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return income.subtract(expenses);
        });
    }

    @Override
    public Map<Category, BigDecimal> getExpensesByCategory() {
        return transactionManager.executeInTransaction(session -> {
            List<Transaction> expenseTransactions = transactionDao.findByType(TransactionType.EXPENSE);
            return expenseTransactions.stream()
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Transaction::getAmount,
                        BigDecimal::add
                    )
                ));
        });
    }

    @Override
    public Map<Category, Double> getExpenseDistribution() {
        return transactionManager.executeInTransaction(session -> {
            List<Transaction> expenseTransactions = transactionDao.findByType(TransactionType.EXPENSE);
            Map<Category, BigDecimal> expensesByCategory = expenseTransactions.stream()
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Transaction::getAmount,
                        BigDecimal::add
                    )
                ));
            
            BigDecimal totalExpenses = expenseTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
                return new HashMap<>();
            }

            return expensesByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().divide(totalExpenses, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue()
                ));
        });
    }

    @Override
    public void updateTransactionStatus(Long id, String newStatus) {
        transactionManager.executeInTransactionWithoutResult(session -> {
            Transaction transaction = transactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
            transaction.setStatus(TransactionStatus.valueOf(newStatus.toUpperCase()));
            transactionDao.update(transaction);
        });
    }

    @Override
    public List<Transaction> getTransactionsByStatus(String status) {
        return transactionManager.executeInTransaction(session -> transactionDao.findByStatus(status));
    }
} 