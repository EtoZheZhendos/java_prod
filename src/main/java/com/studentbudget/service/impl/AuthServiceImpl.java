package com.studentbudget.service.impl;

import com.studentbudget.dao.UserDao;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.AuthService;
import com.studentbudget.util.HibernateTransactionManager;
import com.studentbudget.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserDao userDao;
    private final HibernateTransactionManager transactionManager;
    private User currentUser;

    public AuthServiceImpl(UserDao userDao, HibernateTransactionManager transactionManager) {
        this.userDao = userDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public User authenticate(String username, String password) {
        logger.debug("Attempting to authenticate user: {}", username);
        
        return transactionManager.executeInTransaction(session -> {
            User user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

            if (!user.isActive()) {
                throw new IllegalArgumentException("Учетная запись отключена");
            }

            if (!PasswordUtils.verifySecurePassword(password, user.getPasswordHash())) {
                throw new IllegalArgumentException("Неверный пароль");
            }

            this.currentUser = user;
            logger.info("User authenticated successfully: {}", username);
            return user;
        });
    }

    @Override
    public User register(String username, String password, String email, 
                        String firstName, String lastName, UserRole role) {
        logger.debug("Attempting to register new user: {}", username);
        
        return transactionManager.executeInTransaction(session -> {
            if (!isUsernameAvailable(username)) {
                throw new IllegalArgumentException("Пользователь с таким именем уже существует");
            }
            if (!isEmailAvailable(email)) {
                throw new IllegalArgumentException("Email уже используется");
            }

            String securePassword = PasswordUtils.generateSecurePassword(password);
            User newUser = new User(username, securePassword, email, firstName, lastName, role);
            
            userDao.save(newUser);
            logger.info("New user registered successfully: {}", username);
            return newUser;
        });
    }

    @Override
    public void logout() {
        logger.debug("Logging out current user: {}", 
            currentUser != null ? currentUser.getUsername() : "none");
        currentUser = null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        logger.debug("Attempting to change password for user: {}", 
            currentUser != null ? currentUser.getUsername() : "none");
        
        if (currentUser == null) {
            throw new IllegalStateException("Нет активной сессии пользователя");
        }

        transactionManager.executeInTransactionWithoutResult(session -> {
            if (!PasswordUtils.verifySecurePassword(oldPassword, currentUser.getPasswordHash())) {
                throw new IllegalArgumentException("Неверный текущий пароль");
            }

            String newSecurePassword = PasswordUtils.generateSecurePassword(newPassword);
            currentUser.setPasswordHash(newSecurePassword);
            userDao.update(currentUser);
            logger.info("Password changed successfully for user: {}", currentUser.getUsername());
        });
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return transactionManager.executeInTransaction(session -> 
            !userDao.existsByUsername(username));
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return transactionManager.executeInTransaction(session -> 
            !userDao.existsByEmail(email));
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return transactionManager.executeInTransaction(session -> userDao.findAll());
    }
} 