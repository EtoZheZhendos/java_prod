package com.studentbudget.dao;

import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserDao extends GenericDao<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findActiveUsers();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
} 