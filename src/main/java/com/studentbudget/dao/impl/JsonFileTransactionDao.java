package com.studentbudget.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonFileTransactionDao implements TransactionDao {
    private final File storageFile;
    private final ObjectMapper objectMapper;
    private List<Transaction> transactions;

    public JsonFileTransactionDao(String filePath) {
        this.storageFile = new File(filePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.transactions = loadTransactions();
    }

    private List<Transaction> loadTransactions() {
        try {
            if (!storageFile.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(storageFile, new TypeReference<List<Transaction>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveTransactions() {
        try {
            objectMapper.writeValue(storageFile, transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Transaction save(Transaction entity) {
        if (entity.getId() == null) {
            entity.setId(generateId());
        }
        transactions.add(entity);
        saveTransactions();
        return entity;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return transactions.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions);
    }

    public void delete(Transaction entity) {
        transactions.removeIf(t -> t.getId().equals(entity.getId()));
        saveTransactions();
    }

    @Override
    public void deleteById(Long id) {
        transactions.removeIf(t -> t.getId().equals(id));
        saveTransactions();
    }

    @Override
    public Transaction update(Transaction entity) {
        deleteById(entity.getId());
        transactions.add(entity);
        saveTransactions();
        return entity;
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        return transactions.stream()
                .filter(t -> t.getCategory().getId().equals(category.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByStatus(String status) {
        return transactions.stream()
                .filter(t -> t.getStatus().name().equals(status))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> searchByDescription(String searchTerm) {
        return transactions.stream()
                .filter(t -> t.getDescription() != null &&
                        t.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }

    private Long generateId() {
        return transactions.stream()
                .mapToLong(Transaction::getId)
                .max()
                .orElse(0) + 1;
    }
} 