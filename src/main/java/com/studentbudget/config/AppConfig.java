package com.studentbudget.config;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.dao.impl.HibernateCategoryDao;
import com.studentbudget.dao.impl.HibernateTransactionDao;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.TransactionService;
import com.studentbudget.service.impl.CategoryServiceImpl;
import com.studentbudget.service.impl.TransactionServiceImpl;
import com.studentbudget.util.HibernateTransactionManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();
    private final SessionFactory sessionFactory;
    private final HibernateTransactionManager transactionManager;
    private final TransactionDao transactionDao;
    private final CategoryDao categoryDao;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    private AppConfig() {
        // Initialize Hibernate
        this.sessionFactory = new Configuration()
                .configure() // Load hibernate.cfg.xml
                .buildSessionFactory();

        // Initialize Transaction Manager
        this.transactionManager = new HibernateTransactionManager(sessionFactory);

        // Initialize DAOs
        this.transactionDao = new HibernateTransactionDao(sessionFactory);
        this.categoryDao = new HibernateCategoryDao(sessionFactory);

        // Initialize Services
        this.transactionService = new TransactionServiceImpl(transactionDao, transactionManager);
        this.categoryService = new CategoryServiceImpl(categoryDao, transactionManager);
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public HibernateTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
} 