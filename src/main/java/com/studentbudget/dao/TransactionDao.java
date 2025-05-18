package com.studentbudget.dao;

import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionDao extends GenericDao<Transaction> {
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByCategory(Category category);
    List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<Transaction> findByStatus(String status);
    List<Transaction> searchByDescription(String searchTerm);
} 