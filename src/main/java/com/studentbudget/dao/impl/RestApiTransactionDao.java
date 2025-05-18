package com.studentbudget.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.Category;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestApiTransactionDao implements TransactionDao {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RestApiTransactionDao(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Transaction save(Transaction entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), Transaction.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save transaction", e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/" + id))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(response.body(), Transaction.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find transaction by id", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all transactions", e);
        }
    }

    @Override
    public void delete(Transaction entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/" + id))
                    .DELETE()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    @Override
    public Transaction update(Transaction entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/" + entity.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), Transaction.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/type/" + type))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to find transactions by type", e);
        }
    }

    @Override
    public List<Transaction> findByCategory(Category category) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/category/" + category.getId()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to find transactions by category", e);
        }
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            String url = String.format("%s/transactions/dateRange?start=%s&end=%s",
                    baseUrl, start.toString(), end.toString());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to find transactions by date range", e);
        }
    }

    @Override
    public List<Transaction> findByStatus(String status) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/status/" + status))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to find transactions by status", e);
        }
    }

    @Override
    public List<Transaction> searchByDescription(String searchTerm) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/transactions/search?term=" + searchTerm))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to search transactions by description", e);
        }
    }
} 