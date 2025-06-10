package com.studentbudget.service;

import com.studentbudget.dao.CategoryDao;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.model.Category;
import com.studentbudget.model.Transaction;
import com.studentbudget.service.impl.CategoryServiceImpl;
import com.studentbudget.util.HibernateTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryDao categoryDao;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private HibernateTransactionManager transactionManager;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryServiceImpl(categoryDao, transactionDao, transactionManager);

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
    void createCategory_WithValidData_ShouldCreateCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        
        when(categoryDao.findByName(category.getName())).thenReturn(null);
        when(categoryDao.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Category result = categoryService.createCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(categoryDao).save(category);
    }

    @Test
    void createCategory_WithExistingName_ShouldThrowException() {
        // Arrange
        Category category = new Category();
        category.setName("Existing Category");
        
        when(categoryDao.findByName(category.getName())).thenReturn(category);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            categoryService.createCategory(category)
        );
        verify(categoryDao, never()).save(any());
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        Category c1 = new Category();
        c1.setName("Category 1");
        Category c2 = new Category();
        c2.setName("Category 2");
        
        List<Category> categories = Arrays.asList(c1, c2);
        when(categoryDao.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll(categories));
    }

    @Test
    void deleteCategory_WithNoTransactions_ShouldDelete() {
        // Arrange
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        
        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(category));
        when(transactionDao.findByCategory(category)).thenReturn(Arrays.asList());
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0, HibernateTransactionManager.TransactionCallback.class).execute(null);
        });

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryDao).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WithTransactions_ShouldThrowException() {
        // Arrange
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        
        Transaction transaction = new Transaction();
        transaction.setCategory(category);
        
        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(category));
        when(transactionDao.findByCategory(category)).thenReturn(Arrays.asList(transaction));
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0, HibernateTransactionManager.TransactionCallback.class).execute(null);
        });

        // First check if category has transactions
        assertTrue(categoryService.hasTransactions(categoryId));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            categoryService.deleteCategory(categoryId)
        );
        verify(categoryDao, never()).deleteById(categoryId);
    }

    @Test
    void deleteCategoryWithTransactions_ShouldMoveTransactionsAndDelete() {
        // Arrange
        Long oldCategoryId = 1L;
        Category oldCategory = new Category();
        oldCategory.setId(oldCategoryId);
        oldCategory.setName("Old Category");
        
        Long newCategoryId = 2L;
        Category newCategory = new Category();
        newCategory.setId(newCategoryId);
        newCategory.setName("New Category");
        
        Transaction t1 = new Transaction();
        t1.setCategory(oldCategory);
        Transaction t2 = new Transaction();
        t2.setCategory(oldCategory);
        
        List<Transaction> transactions = Arrays.asList(t1, t2);
        
        when(categoryDao.findById(oldCategoryId)).thenReturn(Optional.of(oldCategory));
        when(transactionDao.findByCategory(oldCategory)).thenReturn(transactions);
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0, HibernateTransactionManager.TransactionCallback.class).execute(null);
        });

        // Act
        categoryService.deleteCategoryWithTransactions(oldCategoryId, newCategory);

        // Assert
        for (Transaction t : transactions) {
            verify(transactionDao).update(t);
            assertSame(newCategory, t.getCategory());
        }
        
        verify(categoryDao).deleteById(oldCategoryId);
    }

    @Test
    void getCategoryByName_WithExistingName_ShouldReturnCategory() {
        // Arrange
        String categoryName = "Test Category";
        Category category = new Category();
        category.setName(categoryName);
        
        when(categoryDao.findByName(categoryName)).thenReturn(category);

        // Act
        Category result = categoryService.getCategoryByName(categoryName);

        // Assert
        assertNotNull(result);
        assertEquals(categoryName, result.getName());
    }

    @Test
    void isCategoryNameUnique_WithNewName_ShouldReturnTrue() {
        // Arrange
        String newName = "New Category";
        when(categoryDao.findByName(newName)).thenReturn(null);

        // Act
        boolean result = categoryService.isCategoryNameUnique(newName);

        // Assert
        assertTrue(result);
    }

    @Test
    void isCategoryNameUnique_WithExistingName_ShouldReturnFalse() {
        // Arrange
        String existingName = "Existing Category";
        Category existingCategory = new Category();
        existingCategory.setName(existingName);
        
        when(categoryDao.findByName(existingName)).thenReturn(existingCategory);

        // Act
        boolean result = categoryService.isCategoryNameUnique(existingName);

        // Assert
        assertFalse(result);
    }
} 