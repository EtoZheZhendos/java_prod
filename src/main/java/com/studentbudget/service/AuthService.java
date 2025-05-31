package com.studentbudget.service;

import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import java.util.List;

public interface AuthService {
    /**
     * Аутентифицирует пользователя по логину и паролю
     * @return User если аутентификация успешна, иначе null
     * @throws IllegalArgumentException если пользователь не найден или пароль неверный
     */
    User authenticate(String username, String password);
    
    /**
     * Регистрирует нового пользователя
     * @throws IllegalArgumentException если пользователь с таким username или email уже существует
     */
    User register(String username, String password, String email, String firstName, String lastName, UserRole role);
    
    /**
     * Завершает текущую сессию пользователя
     */
    void logout();
    
    /**
     * @return текущего аутентифицированного пользователя или null
     */
    User getCurrentUser();
    
    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    boolean isAuthenticated();
    
    /**
     * Изменяет пароль пользователя
     * @throws IllegalArgumentException если старый пароль неверный
     */
    void changePassword(String oldPassword, String newPassword);
    
    /**
     * Проверяет, существует ли пользователь с указанным именем
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Проверяет, существует ли пользователь с указанным email
     */
    boolean isEmailAvailable(String email);
    
    /**
     * @return список всех пользователей
     */
    List<User> getAllUsers();
} 