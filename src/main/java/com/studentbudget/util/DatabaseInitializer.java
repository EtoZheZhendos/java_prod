package com.studentbudget.util;

import com.studentbudget.model.Category;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.UserService;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Утилитарный класс для инициализации базы данных начальными данными.
 * Создает стандартные категории транзакций и учетные записи пользователей по умолчанию.
 */
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final CategoryService categoryService;
    private final UserService userService;
    private final SessionFactory sessionFactory;

    /**
     * Конструктор инициализатора базы данных.
     * @param categoryService сервис для работы с категориями
     * @param userService сервис для работы с пользователями
     * @param sessionFactory фабрика сессий Hibernate
     */
    public DatabaseInitializer(CategoryService categoryService, UserService userService, SessionFactory sessionFactory) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Инициализирует базу данных начальными данными.
     * Создает стандартные категории и пользователей по умолчанию.
     * Если данные уже существуют, они не будут созданы повторно.
     */
    public void initialize() {
        logger.info("Инициализация базы данных...");
        try {
            initializeCategories();
            initializeUsers();
            logger.info("Инициализация базы данных успешно завершена.");
        } catch (Exception e) {
            logger.error("Ошибка при инициализации базы данных", e);
            throw new RuntimeException("Не удалось инициализировать базу данных", e);
        }
    }

    /**
     * Создает стандартные категории транзакций.
     * Категории включают основные типы расходов студента.
     */
    private void initializeCategories() {
        logger.info("Инициализация стандартных категорий...");
        
        List<String> defaultCategories = List.of(
            "Питание",
            "Транспорт",
            "Учебные материалы",
            "Развлечения",
            "Медицина",
            "Одежда",
            "Спорт",
            "Прочее"
        );

        for (String categoryName : defaultCategories) {
            if (!categoryService.isCategoryNameUnique(categoryName)) {
                logger.debug("Категория {} уже существует, пропускаем...", categoryName);
                continue;
            }

            Category category = new Category();
            category.setName(categoryName);
            category.setDescription("Категория для " + categoryName.toLowerCase());
            
            try {
                categoryService.createCategory(category);
                logger.debug("Создана категория: {}", categoryName);
            } catch (Exception e) {
                logger.error("Не удалось создать категорию: " + categoryName, e);
            }
        }
    }

    /**
     * Создает учетные записи пользователей по умолчанию.
     * Создает администратора (admin/admin) и тестового студента (student/student).
     */
    private void initializeUsers() {
        logger.info("Инициализация пользователей по умолчанию...");
        
        // Создание администратора, если не существует
        if (!userService.userExists("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPasswordHash(PasswordUtils.generateSecurePassword("admin"));
            adminUser.setEmail("admin@example.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole(UserRole.ADMIN);
            
            try {
                userService.createUser(adminUser);
                logger.debug("Создан пользователь-администратор");
            } catch (Exception e) {
                logger.error("Не удалось создать пользователя-администратора", e);
            }
        }

        // Создание тестового студента, если не существует
        if (!userService.userExists("student")) {
            User studentUser = new User();
            studentUser.setUsername("student");
            studentUser.setPasswordHash(PasswordUtils.generateSecurePassword("student"));
            studentUser.setEmail("student@example.com");
            studentUser.setFirstName("Test");
            studentUser.setLastName("Student");
            studentUser.setRole(UserRole.STUDENT);
            
            try {
                userService.createUser(studentUser);
                logger.debug("Создан тестовый пользователь-студент");
            } catch (Exception e) {
                logger.error("Не удалось создать тестового пользователя-студента", e);
            }
        }
    }
} 