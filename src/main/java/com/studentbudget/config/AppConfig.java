package com.studentbudget.config;

import com.studentbudget.dao.*;
import com.studentbudget.dao.impl.*;
import com.studentbudget.service.*;
import com.studentbudget.service.impl.*;
import com.studentbudget.util.DatabaseInitializer;
import com.studentbudget.util.HibernateTransactionManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static AppConfig instance;
    private final SessionFactory sessionFactory;
    private final HibernateTransactionManager transactionManager;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final SettingsService settingsService;

    private AppConfig() {
        logger.debug("Initializing application configuration");
        
        try {
            // Initialize Hibernate
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();

            // Initialize Transaction Manager
            transactionManager = new HibernateTransactionManager(sessionFactory);

            // Initialize DAOs
            TransactionDao transactionDao = new TransactionDaoImpl(sessionFactory);
            CategoryDao categoryDao = new CategoryDaoImpl(sessionFactory);
            UserDao userDao = new UserDaoImpl(sessionFactory);

            // Initialize Services
            this.authService = new AuthServiceImpl(userDao, transactionManager);
            this.transactionService = new TransactionServiceImpl(transactionDao, transactionManager, authService);
            this.categoryService = new CategoryServiceImpl(categoryDao, transactionDao, transactionManager);
            this.settingsService = new SettingsServiceImpl();
            
            // Initialize database with default data if needed
            logger.info("Starting database initialization");
            DatabaseInitializer databaseInitializer = new DatabaseInitializer(categoryService);
            databaseInitializer.initialize();
            
            logger.info("Application configuration initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize application configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize application configuration", e);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public HibernateTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void shutdown() {
        logger.info("Shutting down application");
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
} 