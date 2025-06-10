package com.studentbudget.service;

import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.*;
import com.studentbudget.service.impl.TransactionServiceImpl;
import com.studentbudget.util.HibernateTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private HibernateTransactionManager transactionManager;

    @Mock
    private AuthService authService;

    private TransactionService transactionService;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(transactionDao, transactionManager, authService);

        // Setup test users
        testUser = new User("testuser", "hashedpass", "test@test.com", "Test", "User", UserRole.STUDENT);
        testUser.setId(1L);
        
        adminUser = new User("admin", "hashedpass", "admin@test.com", "Admin", "User", UserRole.ADMIN);
        adminUser.setId(2L);

        // Настраиваем мок транзакционного менеджера для методов, возвращающих значения
        lenient().when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            HibernateTransactionManager.TransactionCallback<?> callback = invocation.getArgument(0);
            if (callback != null) {
                return callback.execute(null);
            }
            return null;
        });

        // Настраиваем мок транзакционного менеджера для void методов
        lenient().doAnswer(invocation -> {
            HibernateTransactionManager.VoidTransactionCallback callback = invocation.getArgument(0);
            if (callback != null) {
                callback.execute(null);
            }
            return null;
        }).when(transactionManager).executeInTransactionWithoutResult(any());
    }

    @Test
    void createTransaction_AsStudent_ShouldCreateForSelf() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.INCOME);
        transaction.setDescription("Test transaction");
        transaction.setUser(testUser);
        
        when(transactionDao.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(transactionDao).save(transaction);
    }

    @Test
    void createTransaction_AsAdmin_ShouldCreateForAnyUser() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.INCOME);
        transaction.setDescription("Test transaction");
        transaction.setUser(testUser);
        
        when(transactionDao.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Transaction result = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(transactionDao).save(transaction);
    }

    @Test
    void createTransaction_AsStudent_ShouldNotCreateForOthers() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        
        User otherUser = new User("other", "pass", "other@test.com", "Other", "User", UserRole.STUDENT);
        otherUser.setId(3L);
        
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.INCOME);
        transaction.setDescription("Test transaction");
        transaction.setUser(otherUser);

        // Act & Assert
        assertThrows(SecurityException.class, () -> 
            transactionService.createTransaction(transaction)
        );
        verify(transactionDao, never()).save(any());
    }

    @Test
    void getAllTransactions_AsAdmin_ShouldReturnAllTransactions() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        
        Transaction t1 = new Transaction();
        t1.setUser(testUser);
        Transaction t2 = new Transaction();
        t2.setUser(adminUser);
        
        List<Transaction> allTransactions = Arrays.asList(t1, t2);
        when(transactionDao.findAll()).thenReturn(allTransactions);

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(allTransactions));
    }

    @Test
    void getAllTransactions_AsStudent_ShouldReturnOnlyOwnTransactions() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        
        Transaction t1 = new Transaction();
        t1.setUser(testUser);
        Transaction t2 = new Transaction();
        t2.setUser(adminUser);
        
        List<Transaction> allTransactions = Arrays.asList(t1, t2);
        when(transactionDao.findAll()).thenReturn(allTransactions);

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    void deleteTransaction_AsAdmin_ShouldDeleteAnyTransaction() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(adminUser);
        
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(testUser);
        
        when(transactionDao.findById(1L)).thenReturn(Optional.of(transaction));

        // Act
        transactionService.deleteTransaction(1L);

        // Assert
        verify(transactionDao).deleteById(1L);
    }

    @Test
    void deleteTransaction_AsStudent_ShouldOnlyDeleteOwnTransaction() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        
        Transaction ownTransaction = new Transaction();
        ownTransaction.setId(1L);
        ownTransaction.setUser(testUser);
        
        Transaction otherTransaction = new Transaction();
        otherTransaction.setId(2L);
        otherTransaction.setUser(adminUser);
        
        when(transactionDao.findById(1L)).thenReturn(Optional.of(ownTransaction));
        when(transactionDao.findById(2L)).thenReturn(Optional.of(otherTransaction));
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0, HibernateTransactionManager.TransactionCallback.class).execute(null);
        });

        // Act & Assert
        // Should be able to delete own transaction
        transactionService.deleteTransaction(1L);
        verify(transactionDao).deleteById(1L);

        // Should not be able to delete other's transaction
        assertThrows(SecurityException.class, () -> 
            transactionService.deleteTransaction(2L)
        );
        verify(transactionDao, never()).deleteById(2L);
    }

    @Test
    void getCurrentUserTransactions_ShouldReturnOnlyUserTransactions() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        
        Transaction t1 = new Transaction();
        t1.setUser(testUser);
        Transaction t2 = new Transaction();
        t2.setUser(adminUser);
        
        List<Transaction> allTransactions = Arrays.asList(t1, t2);
        when(transactionDao.findAll()).thenReturn(allTransactions);

        // Act
        List<Transaction> result = transactionService.getCurrentUserTransactions();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    void getTotalIncome_ShouldCalculateCorrectly() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        
        Transaction t1 = new Transaction();
        t1.setUser(testUser);
        t1.setType(TransactionType.INCOME);
        t1.setAmount(new BigDecimal("100.00"));
        
        Transaction t2 = new Transaction();
        t2.setUser(testUser);
        t2.setType(TransactionType.INCOME);
        t2.setAmount(new BigDecimal("50.00"));
        
        Transaction t3 = new Transaction();
        t3.setUser(testUser);
        t3.setType(TransactionType.EXPENSE);
        t3.setAmount(new BigDecimal("30.00"));
        
        List<Transaction> transactions = Arrays.asList(t1, t2, t3);
        when(transactionDao.findAll()).thenReturn(transactions);

        // Act
        BigDecimal totalIncome = transactionService.getTotalIncome();

        // Assert
        assertEquals(new BigDecimal("150.00"), totalIncome);
    }
} 