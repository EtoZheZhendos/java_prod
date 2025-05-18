package com.studentbudget.util;

import com.studentbudget.model.Category;
import com.studentbudget.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final CategoryService categoryService;

    public DatabaseInitializer(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void initializeCategories() {
        logger.info("Starting database initialization...");
        
        // First, check existing categories
        List<Category> existingCategories = categoryService.getAllCategories();
        logger.info("Found {} existing categories", existingCategories.size());
        existingCategories.forEach(category -> 
            logger.info("Existing category: {} (ID: {})", category.getName(), category.getId())
        );

        // Create default categories if none exist
        if (existingCategories.isEmpty()) {
            logger.info("No categories found, creating defaults...");
            
            createCategoryIfNotExists("Продукты", "Расходы на продукты питания");
            createCategoryIfNotExists("Транспорт", "Расходы на общественный транспорт и такси");
            createCategoryIfNotExists("Развлечения", "Расходы на кино, театры, концерты");
            createCategoryIfNotExists("Образование", "Расходы на учебные материалы и курсы");
            createCategoryIfNotExists("Здоровье", "Расходы на медицину и лекарства");
            createCategoryIfNotExists("Одежда", "Расходы на одежду и обувь");
            createCategoryIfNotExists("Связь", "Расходы на телефон и интернет");
            createCategoryIfNotExists("Хобби", "Расходы на хобби и увлечения");
            createCategoryIfNotExists("Подработка", "Доходы от подработки");
            createCategoryIfNotExists("Стипендия", "Доходы от стипендии");
            
            // Verify categories were created
            List<Category> newCategories = categoryService.getAllCategories();
            logger.info("Created {} new categories", newCategories.size());
            newCategories.forEach(category -> 
                logger.info("New category: {} (ID: {})", category.getName(), category.getId())
            );
        }
        
        logger.info("Database initialization completed");
    }

    private void createCategoryIfNotExists(String name, String description) {
        try {
            Category existing = categoryService.getCategoryByName(name);
            if (existing == null) {
                Category category = new Category(name, description);
                Category created = categoryService.createCategory(category);
                logger.info("Created new category: {} (ID: {})", created.getName(), created.getId());
            } else {
                logger.debug("Category already exists: {} (ID: {})", existing.getName(), existing.getId());
            }
        } catch (Exception e) {
            logger.error("Error creating category {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to create category: " + name, e);
        }
    }
} 