# Схема базы данных Student Budget Manager

```plantuml
@startuml

!define table(x) class x << (T,#FFAAAA) >>
!define primary_key(x) <b>x</b>
!define foreign_key(x) <i>x</i>

table(users) {
    primary_key(id): BIGINT
    username: VARCHAR(50)
    email: VARCHAR(100)
    password: VARCHAR(255)
    first_name: VARCHAR(50)
    last_name: VARCHAR(50)
    role: VARCHAR(20)
    created_at: TIMESTAMP
    updated_at: TIMESTAMP
}

table(categories) {
    primary_key(id): BIGINT
    name: VARCHAR(50)
    description: VARCHAR(255)
    type: VARCHAR(20)
    created_at: TIMESTAMP
    updated_at: TIMESTAMP
}

table(transactions) {
    primary_key(id): BIGINT
    foreign_key(user_id): BIGINT
    foreign_key(category_id): BIGINT
    amount: DECIMAL(10,2)
    type: VARCHAR(20)
    description: VARCHAR(255)
    date: TIMESTAMP
    status: VARCHAR(20)
    created_at: TIMESTAMP
    updated_at: TIMESTAMP
}

table(budgets) {
    primary_key(id): BIGINT
    foreign_key(user_id): BIGINT
    foreign_key(category_id): BIGINT
    amount: DECIMAL(10,2)
    period: VARCHAR(20)
    start_date: DATE
    end_date: DATE
    created_at: TIMESTAMP
    updated_at: TIMESTAMP
}

users "1" -- "many" transactions
categories "1" -- "many" transactions
users "1" -- "many" budgets
categories "1" -- "many" budgets

@enduml
```

## Описание таблиц

### Users (Пользователи)
- **id**: Уникальный идентификатор пользователя
- **username**: Логин пользователя
- **email**: Email пользователя
- **password**: Хешированный пароль
- **first_name**: Имя пользователя
- **last_name**: Фамилия пользователя
- **role**: Роль пользователя (USER/ADMIN)
- **created_at**: Дата создания записи
- **updated_at**: Дата обновления записи

### Categories (Категории)
- **id**: Уникальный идентификатор категории
- **name**: Название категории
- **description**: Описание категории
- **type**: Тип категории (INCOME/EXPENSE)
- **created_at**: Дата создания записи
- **updated_at**: Дата обновления записи

### Transactions (Транзакции)
- **id**: Уникальный идентификатор транзакции
- **user_id**: Идентификатор пользователя (внешний ключ)
- **category_id**: Идентификатор категории (внешний ключ)
- **amount**: Сумма транзакции
- **type**: Тип транзакции (INCOME/EXPENSE)
- **description**: Описание транзакции
- **date**: Дата транзакции
- **status**: Статус транзакции (PENDING/COMPLETED/CANCELLED)
- **created_at**: Дата создания записи
- **updated_at**: Дата обновления записи

### Budgets (Бюджеты)
- **id**: Уникальный идентификатор бюджета
- **user_id**: Идентификатор пользователя (внешний ключ)
- **category_id**: Идентификатор категории (внешний ключ)
- **amount**: Сумма бюджета
- **period**: Период бюджета (MONTHLY/WEEKLY/YEARLY)
- **start_date**: Дата начала периода
- **end_date**: Дата окончания периода
- **created_at**: Дата создания записи
- **updated_at**: Дата обновления записи

## Индексы

### Users
- PRIMARY KEY (id)
- UNIQUE INDEX idx_users_username (username)
- UNIQUE INDEX idx_users_email (email)

### Categories
- PRIMARY KEY (id)
- UNIQUE INDEX idx_categories_name (name)

### Transactions
- PRIMARY KEY (id)
- INDEX idx_transactions_user (user_id)
- INDEX idx_transactions_category (category_id)
- INDEX idx_transactions_date (date)

### Budgets
- PRIMARY KEY (id)
- INDEX idx_budgets_user (user_id)
- INDEX idx_budgets_category (category_id)
- INDEX idx_budgets_period (period)

## Ограничения

### Внешние ключи
- transactions.user_id -> users.id (ON DELETE CASCADE)
- transactions.category_id -> categories.id (ON DELETE RESTRICT)
- budgets.user_id -> users.id (ON DELETE CASCADE)
- budgets.category_id -> categories.id (ON DELETE RESTRICT)

### Проверки
- transactions.amount > 0
- budgets.amount >= 0
- transactions.date <= CURRENT_TIMESTAMP
- budgets.start_date < budgets.end_date 