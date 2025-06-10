package com.studentbudget.config;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.dao.UserDao;
import com.studentbudget.dao.impl.CategoryDaoImpl;
import com.studentbudget.dao.impl.TransactionDaoImpl;
import com.studentbudget.dao.impl.UserDaoImpl;
import com.studentbudget.service.AuthService;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.TransactionService;
import com.studentbudget.service.UserService;
import com.studentbudget.service.impl.AuthServiceImpl;
import com.studentbudget.service.impl.CategoryServiceImpl;
import com.studentbudget.service.impl.TransactionServiceImpl;
import com.studentbudget.service.impl.UserServiceImpl;
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
    private final UserDao userDao;
    private final CategoryDao categoryDao;
    private final TransactionDao transactionDao;
    private final AuthService authService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final UserService userService;

    private AppConfig() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            transactionManager = new HibernateTransactionManager(sessionFactory);

            userDao = new UserDaoImpl(sessionFactory);
            categoryDao = new CategoryDaoImpl(sessionFactory);
            transactionDao = new TransactionDaoImpl(sessionFactory);

            userService = new UserServiceImpl(userDao, transactionManager);
            authService = new AuthServiceImpl(userDao, transactionManager);
            categoryService = new CategoryServiceImpl(categoryDao, transactionDao, transactionManager);
            transactionService = new TransactionServiceImpl(transactionDao, transactionManager, authService);

            DatabaseInitializer databaseInitializer = new DatabaseInitializer(
                categoryService,
                userService,
                sessionFactory
            );
            databaseInitializer.initialize();

        } catch (Exception e) {
            logger.error("Failed to initialize application configuration", e);
            throw new RuntimeException("Failed to initialize application configuration", e);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public UserService getUserService() {
        return userService;
    }
} 