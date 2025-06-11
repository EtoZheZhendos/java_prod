# Примеры использования Student Budget Manager

## 1. Регистрация и вход

### 1.1 Регистрация нового пользователя
```java
// Создание нового пользователя
User user = new User();
user.setUsername("student123");
user.setEmail("student@university.com");
user.setPassword("securePassword123");
user.setFirstName("Иван");
user.setLastName("Петров");

// Регистрация пользователя
userService.register(user);
```

### 1.2 Вход в систему
```java
// Аутентификация пользователя
String token = authService.login("student123", "securePassword123");

// Использование токена для последующих запросов
authService.validateToken(token);
```

## 2. Управление транзакциями

### 2.1 Создание новой транзакции
```java
// Создание расходной транзакции
Transaction expense = new Transaction();
expense.setAmount(new BigDecimal("500.00"));
expense.setType(TransactionType.EXPENSE);
expense.setCategory(categoryService.findByName("Books"));
expense.setDescription("Учебники по Java");
expense.setDate(LocalDateTime.now());

// Сохранение транзакции
transactionService.createTransaction(expense);
```

### 2.2 Получение списка транзакций
```java
// Получение всех транзакций пользователя
List<Transaction> transactions = transactionService.findByUser(currentUser);

// Получение транзакций за период
LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
LocalDateTime endDate = LocalDateTime.now();
List<Transaction> monthlyTransactions = 
    transactionService.findByDateBetween(startDate, endDate);
```

## 3. Работа с категориями

### 3.1 Создание категории
```java
// Создание новой категории расходов
Category category = new Category();
category.setName("Транспорт");
category.setType(TransactionType.EXPENSE);
category.setDescription("Расходы на проезд");

// Сохранение категории
categoryService.createCategory(category);
```

### 3.2 Получение категорий
```java
// Получение всех категорий расходов
List<Category> expenseCategories = 
    categoryService.findByType(TransactionType.EXPENSE);

// Получение всех категорий
List<Category> allCategories = categoryService.findAll();
```

## 4. Управление бюджетом

### 4.1 Создание бюджета
```java
// Создание месячного бюджета
Budget budget = new Budget();
budget.setCategory(categoryService.findByName("Питание"));
budget.setAmount(new BigDecimal("3000.00"));
budget.setPeriod(Period.MONTHLY);
budget.setStartDate(LocalDate.now().withDayOfMonth(1));
budget.setEndDate(LocalDate.now().withDayOfMonth(1).plusMonths(1));

// Сохранение бюджета
budgetService.createBudget(budget);
```

### 4.2 Проверка бюджета
```java
// Проверка текущего состояния бюджета
BigDecimal remaining = budgetService.getRemainingAmount(budget);
boolean isExceeded = budgetService.isBudgetExceeded(budget);

// Получение процента использования
double usagePercentage = budgetService.getUsagePercentage(budget);
```

## 5. Формирование отчетов

### 5.1 Месячный отчет
```java
// Получение отчета за текущий месяц
MonthlyReport report = reportService.generateMonthlyReport(
    currentUser,
    LocalDate.now().getMonth()
);

// Вывод основных показателей
System.out.println("Доходы: " + report.getTotalIncome());
System.out.println("Расходы: " + report.getTotalExpense());
System.out.println("Баланс: " + report.getBalance());
```

### 5.2 Отчет по категориям
```java
// Получение отчета по категориям
CategoryReport categoryReport = reportService.generateCategoryReport(currentUser);

// Вывод расходов по категориям
for (CategorySummary summary : categoryReport.getCategories()) {
    System.out.println(summary.getName() + ": " + summary.getAmount());
}
```

## 6. Экспорт данных

### 6.1 Экспорт в CSV
```java
// Экспорт транзакций в CSV
List<Transaction> transactions = transactionService.findByUser(currentUser);
String csv = exportService.exportToCSV(transactions);

// Сохранение в файл
Files.write(Paths.get("transactions.csv"), csv.getBytes());
```

### 6.2 Экспорт в JSON
```java
// Экспорт данных в JSON
UserData userData = new UserData(currentUser, transactions, budgets);
String json = exportService.exportToJSON(userData);

// Сохранение в файл
Files.write(Paths.get("user_data.json"), json.getBytes());
```

## 7. Обработка ошибок

### 7.1 Обработка исключений
```java
try {
    transactionService.createTransaction(transaction);
} catch (InsufficientFundsException e) {
    System.err.println("Недостаточно средств: " + e.getMessage());
} catch (ValidationException e) {
    System.err.println("Ошибка валидации: " + e.getMessage());
} catch (DatabaseException e) {
    System.err.println("Ошибка базы данных: " + e.getMessage());
}
```

### 7.2 Валидация данных
```java
// Валидация транзакции
try {
    transactionService.validateTransaction(transaction);
} catch (ValidationException e) {
    List<String> errors = e.getValidationErrors();
    for (String error : errors) {
        System.err.println("Ошибка: " + error);
    }
}
```

## 8. Утилиты

### 8.1 Форматирование данных
```java
// Форматирование суммы
String formattedAmount = MoneyUtil.format(transaction.getAmount());

// Форматирование даты
String formattedDate = DateUtil.format(transaction.getDate());
```

### 8.2 Валидация данных
```java
// Валидация email
if (!ValidationUtil.isValidEmail(email)) {
    throw new ValidationException("Неверный формат email");
}

// Валидация пароля
if (!ValidationUtil.isValidPassword(password)) {
    throw new ValidationException("Пароль не соответствует требованиям");
}
``` 