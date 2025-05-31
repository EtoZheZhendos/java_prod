package com.studentbudget.service.impl;

import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import com.studentbudget.model.TransactionStatus;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.TransactionService;
import com.studentbudget.service.AuthService;
import com.studentbudget.util.HibernateTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionDao transactionDao;
    private final HibernateTransactionManager transactionManager;
    private final AuthService authService;

    public TransactionServiceImpl(TransactionDao transactionDao, HibernateTransactionManager transactionManager, AuthService authService) {
        this.transactionDao = transactionDao;
        this.transactionManager = transactionManager;
        this.authService = authService;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        logger.debug("Creating new transaction: {}", transaction);
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        
        if (transaction.getUser() == null) {
            transaction.setUser(authService.getCurrentUser());
        }
        
        if (!isAdminOrOwner(transaction.getUser())) {
            throw new SecurityException("Недостаточно прав для создания транзакции от имени другого пользователя");
        }
        
        return transactionManager.executeInTransaction(session -> transactionDao.save(transaction));
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        logger.debug("Updating transaction with id {}: {}", transaction.getId(), transaction);
        transactionManager.executeInTransactionWithoutResult(session -> {
            Transaction existing = transactionDao.findById(transaction.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transaction.getId()));
            
            if (!isAdminOrOwner(existing.getUser())) {
                throw new SecurityException("Недостаточно прав для редактирования этой транзакции");
            }
            
            transactionDao.update(transaction);
        });
    }

    @Override
    public void deleteTransaction(Long id) {
        logger.debug("Deleting transaction with id: {}", id);
        transactionManager.executeInTransactionWithoutResult(session -> {
            Transaction existing = transactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
            
            if (!isAdminOrOwner(existing.getUser())) {
                throw new SecurityException("Недостаточно прав для удаления этой транзакции");
            }
            
            transactionDao.deleteById(id);
        });
    }

    @Override
    public Transaction getTransactionById(Long id) {
        logger.debug("Fetching transaction with id: {}", id);
        return transactionManager.executeInTransaction(session -> {
            Transaction transaction = transactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
            
            if (!isAdminOrOwner(transaction.getUser())) {
                throw new SecurityException("Недостаточно прав для просмотра этой транзакции");
            }
            
            return transaction;
        });
    }

    @Override
    public List<Transaction> getAllTransactions() {
        logger.debug("Fetching all transactions");
        return isAdmin() ? getAllUsersTransactions() : getCurrentUserTransactions();
    }

    @Override
    public List<Transaction> getCurrentUserTransactions() {
        logger.debug("Fetching current user transactions");
        User currentUser = authService.getCurrentUser();
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findAll().stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getCurrentUserTransactionsByType(TransactionType type) {
        logger.debug("Fetching current user transactions by type: {}", type);
        User currentUser = authService.getCurrentUser();
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByType(type).stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getCurrentUserTransactionsByCategory(Category category) {
        logger.debug("Fetching current user transactions by category: {}", category.getName());
        User currentUser = authService.getCurrentUser();
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByCategory(category).stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getCurrentUserTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.debug("Fetching current user transactions between {} and {}", start, end);
        User currentUser = authService.getCurrentUser();
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByDateRange(start, end).stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getCurrentUserTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return getCurrentUserTransactionsByDateRange(start, end);
    }

    @Override
    public List<Transaction> getCurrentUserTransactionsByStatus(String status) {
        logger.debug("Fetching current user transactions by status: {}", status);
        User currentUser = authService.getCurrentUser();
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByStatus(status).stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getAllUsersTransactions() {
        logger.debug("Fetching all users transactions");
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать все транзакции");
        }
        return transactionManager.executeInTransaction(session -> transactionDao.findAll());
    }

    @Override
    public List<Transaction> getTransactionsByUser(User user) {
        logger.debug("Fetching transactions for user: {}", user.getUsername());
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать транзакции других пользователей");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getTransactionsByUserAndType(User user, TransactionType type) {
        logger.debug("Fetching transactions for user {} by type: {}", user.getUsername(), type);
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать транзакции других пользователей");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByType(type).stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getTransactionsByUserAndCategory(User user, Category category) {
        logger.debug("Fetching transactions for user {} by category: {}", user.getUsername(), category.getName());
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать транзакции других пользователей");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByCategory(category).stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<Transaction> getTransactionsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end) {
        logger.debug("Fetching transactions for user {} between {} and {}", user.getUsername(), start, end);
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать транзакции других пользователей");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findByDateRange(start, end).stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList())
        );
    }

    @Override
    public BigDecimal getTotalIncome() {
        logger.debug("Calculating total income");
        List<Transaction> transactions = isAdmin() ? getAllUsersTransactions() : getCurrentUserTransactions();
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalExpenses() {
        logger.debug("Calculating total expenses");
        List<Transaction> transactions = isAdmin() ? getAllUsersTransactions() : getCurrentUserTransactions();
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<Category, BigDecimal> getExpensesByCategory() {
        logger.debug("Calculating expenses by category");
        List<Transaction> transactions = isAdmin() ? getAllUsersTransactions() : getCurrentUserTransactions();
        return transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    Transaction::getAmount,
                    BigDecimal::add
                )
            ));
    }

    @Override
    public Map<User, BigDecimal> getTotalIncomeByUser() {
        logger.debug("Calculating total income by user");
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать статистику по всем пользователям");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findAll().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                    Transaction::getUser,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Transaction::getAmount,
                        BigDecimal::add
                    )
                ))
        );
    }

    @Override
    public Map<User, BigDecimal> getTotalExpensesByUser() {
        logger.debug("Calculating total expenses by user");
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать статистику по всем пользователям");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findAll().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                    Transaction::getUser,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Transaction::getAmount,
                        BigDecimal::add
                    )
                ))
        );
    }

    @Override
    public Map<User, Map<Category, BigDecimal>> getExpensesByCategoryAndUser() {
        logger.debug("Calculating expenses by category and user");
        if (!isAdmin()) {
            throw new SecurityException("Только администратор может просматривать статистику по всем пользователям");
        }
        return transactionManager.executeInTransaction(session -> 
            transactionDao.findAll().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                    Transaction::getUser,
                    Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                            BigDecimal.ZERO,
                            Transaction::getAmount,
                            BigDecimal::add
                        )
                    )
                ))
        );
    }

    @Override
    public BigDecimal getCurrentBalance() {
        logger.debug("Calculating current balance");
        return getTotalIncome().subtract(getTotalExpenses());
    }

    @Override
    public Map<Category, Double> getExpenseDistribution() {
        logger.debug("Calculating expense distribution");
        return transactionManager.executeInTransaction(session -> {

            List<Transaction> allExpenses = transactionDao.findByType(TransactionType.EXPENSE);
            logger.debug("Total expense transactions found: {}", allExpenses.size());
            
            allExpenses.forEach(t -> logger.debug("Expense transaction: amount={}, category={}, status={}", 
                t.getAmount(), t.getCategory().getName(), t.getStatus()));
            
            List<Transaction> expenseTransactions = allExpenses.stream()
                .filter(t -> t.getStatus() == TransactionStatus.ACTIVE)
                .toList();
            
            logger.debug("Active expense transactions after filtering: {}", expenseTransactions.size());
            
            expenseTransactions.forEach(t -> logger.debug("Active expense: amount={}, category={}", 
                t.getAmount(), t.getCategory().getName()));
            
            Map<Category, BigDecimal> expensesByCategory = expenseTransactions.stream()
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Transaction::getAmount,
                        BigDecimal::add
                    )
                ));
            
            logger.debug("Expenses grouped by category: {}", expensesByCategory);
            
            BigDecimal totalExpenses = expenseTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            logger.debug("Total expenses amount: {}", totalExpenses);
            
            if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
                logger.debug("No expenses found, returning empty distribution");
                return new HashMap<>();
            }

            Map<Category, Double> distribution = expensesByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> {
                        Double percentage = e.getValue()
                            .divide(totalExpenses, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                        logger.debug("Category {} percentage: {}%", e.getKey().getName(), percentage);
                        return percentage;
                    }
                ));
                
            logger.debug("Final distribution: {}", distribution);
            return distribution;
        });
    }

    private boolean isAdmin() {
        return authService.getCurrentUser().getRole() == UserRole.ADMIN;
    }

    private boolean isAdminOrOwner(User user) {
        User currentUser = authService.getCurrentUser();
        return currentUser.getRole() == UserRole.ADMIN || 
               currentUser.getId().equals(user.getId());
    }

    @Override
    public List<Transaction> searchTransactions(String searchTerm) {
        List<Transaction> results = transactionManager.executeInTransaction(session -> 
            transactionDao.searchByDescription(searchTerm)
        );
        
        if (!isAdmin()) {
            User currentUser = authService.getCurrentUser();
            results = results.stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        return results;
    }

    @Override
    public List<Transaction> searchTransactions(String query, Category category, 
                                              LocalDate startDate, LocalDate endDate) {
        List<Transaction> results = transactionManager.executeInTransaction(session -> 
            getAllTransactions().stream()
                .filter(t -> (query == null || query.isEmpty() || 
                            t.getDescription().toLowerCase().contains(query.toLowerCase())))
                .filter(t -> (category == null || t.getCategory().equals(category)))
                .filter(t -> (startDate == null || !t.getDate().toLocalDate().isBefore(startDate)))
                .filter(t -> (endDate == null || !t.getDate().toLocalDate().isAfter(endDate)))
                .collect(Collectors.toList())
        );
        
        if (!isAdmin()) {
            User currentUser = authService.getCurrentUser();
            results = results.stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        return results;
    }

    @Override
    public void updateTransactionStatus(Long id, String newStatus) {
        logger.debug("Updating transaction status with id: {} to: {}", id, newStatus);
        transactionManager.executeInTransactionWithoutResult(session -> {
            Transaction transaction = getTransactionById(id);
            transaction.setStatus(TransactionStatus.valueOf(newStatus.toUpperCase()));
            transactionDao.update(transaction);
        });
    }

    @Override
    public List<Transaction> getTransactionsByStatus(String status) {
        logger.debug("Fetching transactions by status: {}", status);
        return transactionManager.executeInTransaction(session -> transactionDao.findByStatus(status));
    }

    @Override
    public void moveTransactions(Category fromCategory, Category toCategory) {
        logger.debug("Moving transactions from category {} to category {}", fromCategory.getName(), toCategory.getName());
        transactionManager.executeInTransactionWithoutResult(session -> {
            List<Transaction> transactions = transactionDao.findByCategory(fromCategory);
            for (Transaction transaction : transactions) {
                transaction.setCategory(toCategory);
                transactionDao.update(transaction);
            }
        });
    }
} 