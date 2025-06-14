# Student Budget Manager - JavaDoc Documentation

## 1. Основные пакеты

### 1.1. com.studentbudget
Корневой пакет приложения, содержащий основной класс.

```java
/**
 * Главный класс приложения Student Budget Manager.
 * Инициализирует компоненты приложения, настраивает подключение к базе данных
 * и запускает пользовательский интерфейс.
 */
public class Main extends Application { ... }
```

### 1.2. com.studentbudget.model
Пакет с моделями данных приложения.

#### User
```java
/**
 * Модель пользователя системы.
 * Представляет собой сущность пользователя с основными атрибутами и ролью.
 */
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 1.3. com.studentbudget.service
Пакет сервисов, реализующих бизнес-логику.

#### AuthService
```java
/**
 * Сервис аутентификации и авторизации пользователей.
 * Предоставляет методы для входа, регистрации и управления сессиями пользователей.
 */
public interface AuthService {
    /**
     * Аутентифицирует пользователя по логину и паролю
     * @param username имя пользователя
     * @param password пароль
     * @return аутентифицированный пользователь
     * @throws IllegalArgumentException если данные неверны
     */
    User authenticate(String username, String password);
    
    // ... другие методы
}
```

#### TransactionService
```java
/**
 * Сервис управления финансовыми транзакциями.
 * Обеспечивает создание, обновление, удаление и поиск транзакций,
 * а также расчет финансовой статистики.
 */
public interface TransactionService {
    /**
     * Создает новую транзакцию
     * @param transaction данные транзакции
     * @return созданная транзакция
     */
    Transaction createTransaction(Transaction transaction);
    
    /**
     * Рассчитывает общий доход пользователя
     * @return сумма всех доходов
     */
    BigDecimal getTotalIncome();
    
    // ... другие методы
}
```

### 1.4. com.studentbudget.dao
Пакет для работы с хранилищем данных.

#### GenericDao
```java
/**
 * Базовый интерфейс для доступа к данным.
 * Определяет основные операции CRUD для сущностей.
 */
public interface GenericDao<T> {
    T save(T entity);
    T update(T entity);
    void deleteById(Long id);
    Optional<T> findById(Long id);
    List<T> findAll();
}
```

### 1.5. com.studentbudget.controller
Пакет контроллеров пользовательского интерфейса.

#### MainController
```java
/**
 * Главный контроллер приложения.
 * Управляет основным интерфейсом программы, отображает транзакции,
 * статистику и обрабатывает действия пользователя.
 */
public class MainController implements Initializable {
    // ... поля и методы
}
```

#### LoginController
```java
/**
 * Контроллер окна авторизации.
 * Обрабатывает вход пользователей в систему и регистрацию новых пользователей.
 */
public class LoginController {
    // ... поля и методы
}
```

### 1.6. com.studentbudget.util
Пакет утилитарных классов.

#### DatabaseInitializer
```java
/**
 * Инициализатор базы данных.
 * Создает начальные данные при первом запуске приложения:
 * - Стандартные категории транзакций
 * - Учетные записи администратора и тестового пользователя
 */
public class DatabaseInitializer {
    // ... поля и методы
}
```

## 2. Основные функциональные возможности

### 2.1. Управление пользователями
- Регистрация новых пользователей
- Аутентификация существующих пользователей
- Управление ролями (ADMIN, STUDENT)
- Изменение пароля и личных данных

### 2.2. Управление транзакциями
- Создание доходов и расходов
- Категоризация транзакций
- Поиск и фильтрация транзакций
- Формирование отчетов

### 2.3. Управление категориями
- Создание пользовательских категорий
- Редактирование существующих категорий
- Объединение категорий
- Статистика по категориям

### 2.4. Статистика и отчеты
- Общий баланс
- Доходы и расходы по категориям
- Графики и диаграммы
- Экспорт данных

## 3. Технические особенности

### 3.1. Архитектура
- Многоуровневая архитектура (MVC)
- Разделение на слои (DAO, Service, Controller)
- Использование паттернов проектирования

### 3.2. Безопасность
- Хеширование паролей
- Разграничение прав доступа
- Валидация входных данных

### 3.3. Работа с данными
- Использование Hibernate ORM
- Транзакционность операций
- Кэширование данных

### 3.4. Пользовательский интерфейс
- JavaFX компоненты
- Темная и светлая темы
- Адаптивный дизайн 