package com.studentbudget.util;

import com.studentbudget.model.Category;
import com.studentbudget.service.CategoryService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final CategoryService categoryService;
    private final SessionFactory sessionFactory;

    public DatabaseInitializer(CategoryService categoryService, SessionFactory sessionFactory) {
        this.categoryService = categoryService;
        this.sessionFactory = sessionFactory;
    }

    public void initialize() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                initializeCategories();
                transaction.commit();
                logger.info("Database initialized successfully");
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Error initializing database: ", e);
                throw e;
            }
        }
    }

    private void initializeCategories() {
        List<Category> defaultCategories = Arrays.asList(
            new Category("Продукты", "Расходы на продукты питания"),
            new Category("Транспорт", "Расходы на общественный транспорт и такси"),
            new Category("Учебные материалы", "Книги, канцтовары и другие учебные принадлежности"),
            new Category("Развлечения", "Кино, театры, концерты и другие развлечения"),
            new Category("Стипендия", "Доходы от стипендии"),
            new Category("Подработка", "Доходы от частичной занятости"),
            new Category("Помощь родителей", "Финансовая поддержка от родителей")
        );

        for (Category category : defaultCategories) {
            if (!categoryExists(category.getName())) {
                categoryService.createCategory(category);
                logger.info("Created default category: {}", category.getName());
            }
        }
    }

    private boolean categoryExists(String name) {
        return categoryService.getAllCategories().stream()
                .anyMatch(c -> c.getName().equals(name));
    }
} 