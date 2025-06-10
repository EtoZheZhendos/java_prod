package com.studentbudget.service;

import com.studentbudget.dao.UserDao;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.impl.AuthServiceImpl;
import com.studentbudget.util.HibernateTransactionManager;
import com.studentbudget.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private HibernateTransactionManager transactionManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(userDao, transactionManager);

        // Настраиваем мок транзакционного менеджера для методов, возвращающих значения
        lenient().when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            HibernateTransactionManager.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.execute(null);
        });

        // Настраиваем мок транзакционного менеджера для void методов
        lenient().doAnswer(invocation -> {
            HibernateTransactionManager.VoidTransactionCallback callback = invocation.getArgument(0);
            callback.execute(null);
            return null;
        }).when(transactionManager).executeInTransactionWithoutResult(any());
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnUser() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = PasswordUtils.generateSecurePassword(password);
        
        User mockUser = new User(username, hashedPassword, "test@test.com", "Test", "User", UserRole.STUDENT);
        mockUser.setActive(true);
        
        when(userDao.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        User authenticatedUser = authService.authenticate(username, password);

        // Assert
        assertNotNull(authenticatedUser);
        assertEquals(username, authenticatedUser.getUsername());
        assertTrue(authService.isAuthenticated());
    }

    @Test
    void authenticate_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = PasswordUtils.generateSecurePassword(correctPassword);
        
        User mockUser = new User(username, hashedPassword, "test@test.com", "Test", "User", UserRole.STUDENT);
        mockUser.setActive(true);
        
        when(userDao.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.authenticate(username, wrongPassword)
        );
    }

    @Test
    void authenticate_WithInactiveUser_ShouldThrowException() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = PasswordUtils.generateSecurePassword(password);
        
        User mockUser = new User(username, hashedPassword, "test@test.com", "Test", "User", UserRole.STUDENT);
        mockUser.setActive(false);
        
        when(userDao.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.authenticate(username, password)
        );
    }

    @Test
    void register_WithValidData_ShouldCreateUser() {
        // Arrange
        String username = "newuser";
        String password = "password123";
        String email = "new@test.com";
        String firstName = "New";
        String lastName = "User";
        UserRole role = UserRole.STUDENT;

        when(userDao.existsByUsername(username)).thenReturn(false);
        when(userDao.existsByEmail(email)).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        User registeredUser = authService.register(username, password, email, firstName, lastName, role);

        // Assert
        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(firstName, registeredUser.getFirstName());
        assertEquals(lastName, registeredUser.getLastName());
        assertEquals(role, registeredUser.getRole());
        verify(userDao).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Arrange
        String existingUsername = "existinguser";
        when(userDao.existsByUsername(existingUsername)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.register(existingUsername, "password", "email@test.com", "First", "Last", UserRole.STUDENT)
        );
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        String existingEmail = "existing@test.com";
        when(userDao.existsByUsername(any())).thenReturn(false);
        when(userDao.existsByEmail(existingEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.register("newuser", "password", existingEmail, "First", "Last", UserRole.STUDENT)
        );
    }

    @Test
    void logout_ShouldClearCurrentUser() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = PasswordUtils.generateSecurePassword(password);
        
        User mockUser = new User(username, hashedPassword, "test@test.com", "Test", "User", UserRole.STUDENT);
        mockUser.setActive(true);
        
        when(userDao.findByUsername(username)).thenReturn(Optional.of(mockUser));
        
        // First authenticate
        authService.authenticate(username, password);
        assertTrue(authService.isAuthenticated());

        // Act
        authService.logout();

        // Assert
        assertFalse(authService.isAuthenticated());
        assertNull(authService.getCurrentUser());
    }

    @Test
    void changePassword_WithValidOldPassword_ShouldUpdatePassword() {
        // Arrange
        String username = "testuser";
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        String oldHashedPassword = PasswordUtils.generateSecurePassword(oldPassword);
        
        User mockUser = new User(username, oldHashedPassword, "test@test.com", "Test", "User", UserRole.STUDENT);
        mockUser.setActive(true);
        
        when(userDao.findByUsername(username)).thenReturn(Optional.of(mockUser));
        
        // First authenticate
        authService.authenticate(username, oldPassword);

        // Act
        authService.changePassword(oldPassword, newPassword);

        // Assert
        verify(userDao).update(mockUser);
        String newHashedPassword = mockUser.getPasswordHash();
        assertTrue(PasswordUtils.verifySecurePassword(newPassword, newHashedPassword));
        assertFalse(PasswordUtils.verifySecurePassword(oldPassword, newHashedPassword));
    }

    @Test
    void changePassword_WithInvalidOldPassword_ShouldThrowException() {
        // Arrange
        String username = "testuser";
        String correctPassword = "correctpassword";
        String wrongPassword = "wrongpassword";
        String newPassword = "newpassword";
        String hashedPassword = PasswordUtils.generateSecurePassword(correctPassword);
        
        User mockUser = new User(username, hashedPassword, "test@test.com", "Test", "User", UserRole.STUDENT);
        mockUser.setActive(true);
        
        when(userDao.findByUsername(username)).thenReturn(Optional.of(mockUser));
        
        // First authenticate
        authService.authenticate(username, correctPassword);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            authService.changePassword(wrongPassword, newPassword)
        );
        verify(userDao, never()).update(any());
        assertEquals(hashedPassword, mockUser.getPasswordHash());
    }
} 