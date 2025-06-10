package com.studentbudget.service.impl;

import com.studentbudget.dao.UserDao;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.UserService;
import com.studentbudget.util.HibernateTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDao userDao;
    private final HibernateTransactionManager transactionManager;

    public UserServiceImpl(UserDao userDao, HibernateTransactionManager transactionManager) {
        this.userDao = userDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return transactionManager.executeInTransaction(session -> userDao.findAll());
    }

    @Override
    public User getUserById(Long id) {
        logger.debug("Fetching user with id: {}", id);
        return transactionManager.executeInTransaction(session -> 
            userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id))
        );
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        logger.debug("Fetching users with role: {}", role);
        return transactionManager.executeInTransaction(session -> 
            userDao.findAll().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList())
        );
    }

    @Override
    public User createUser(User user) {
        logger.debug("Creating new user: {}", user.getUsername());
        if (userExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        return transactionManager.executeInTransaction(session -> userDao.save(user));
    }

    @Override
    public void updateUser(User user) {
        logger.debug("Updating user with id: {}", user.getId());
        transactionManager.executeInTransactionWithoutResult(session -> {
            User existing = getUserById(user.getId());
            if (!existing.getUsername().equals(user.getUsername()) && userExists(user.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }
            if (!existing.getEmail().equals(user.getEmail()) && emailExists(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
            userDao.update(user);
        });
    }

    @Override
    public void deleteUser(Long id) {
        logger.debug("Deleting user with id: {}", id);
        transactionManager.executeInTransactionWithoutResult(session -> {
            User user = getUserById(id);
            userDao.deleteById(id);
        });
    }

    @Override
    public void deactivateUser(Long id) {
        logger.debug("Deactivating user with id: {}", id);
        transactionManager.executeInTransactionWithoutResult(session -> {
            User user = getUserById(id);
            user.setActive(false);
            userDao.update(user);
        });
    }

    @Override
    public void activateUser(Long id) {
        logger.debug("Activating user with id: {}", id);
        transactionManager.executeInTransactionWithoutResult(session -> {
            User user = getUserById(id);
            user.setActive(true);
            userDao.update(user);
        });
    }

    @Override
    public boolean userExists(String username) {
        logger.debug("Checking if username exists: {}", username);
        return transactionManager.executeInTransaction(session -> userDao.existsByUsername(username));
    }

    @Override
    public boolean emailExists(String email) {
        logger.debug("Checking if email exists: {}", email);
        return transactionManager.executeInTransaction(session -> userDao.existsByEmail(email));
    }
} 