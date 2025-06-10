package com.studentbudget.service;

import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import java.util.List;

/**
 * Интерфейс сервиса для управления пользователями.
 * Предоставляет методы для работы с пользователями системы:
 * создание, обновление, удаление и поиск пользователей.
 */
public interface UserService {
    /**
     * Получает список всех пользователей системы.
     * @return список всех пользователей
     */
    List<User> getAllUsers();

    /**
     * Получает пользователя по его идентификатору.
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    User getUserById(Long id);

    /**
     * Получает список пользователей с указанной ролью.
     * @param role роль пользователя
     * @return список пользователей с указанной ролью
     */
    List<User> getUsersByRole(UserRole role);

    /**
     * Создает нового пользователя.
     * @param user данные нового пользователя
     * @return созданный пользователь
     * @throws IllegalArgumentException если пользователь с таким именем или email уже существует
     */
    User createUser(User user);

    /**
     * Обновляет данные существующего пользователя.
     * @param user обновленные данные пользователя
     * @throws IllegalArgumentException если пользователь не найден или новые данные конфликтуют с существующими
     */
    void updateUser(User user);

    /**
     * Удаляет пользователя по идентификатору.
     * @param id идентификатор пользователя
     * @throws IllegalArgumentException если пользователь не найден
     */
    void deleteUser(Long id);

    /**
     * Деактивирует учетную запись пользователя.
     * @param id идентификатор пользователя
     * @throws IllegalArgumentException если пользователь не найден
     */
    void deactivateUser(Long id);

    /**
     * Активирует учетную запись пользователя.
     * @param id идентификатор пользователя
     * @throws IllegalArgumentException если пользователь не найден
     */
    void activateUser(Long id);

    /**
     * Проверяет существование пользователя с указанным именем.
     * @param username имя пользователя
     * @return true если пользователь существует, false в противном случае
     */
    boolean userExists(String username);

    /**
     * Проверяет существование пользователя с указанным email.
     * @param email email пользователя
     * @return true если email уже используется, false в противном случае
     */
    boolean emailExists(String email);
} 