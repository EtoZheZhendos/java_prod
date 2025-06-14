# Отчет по проекту Student Budget Manager

## Общая информация
- **Название проекта:** Student Budget Manager
- **Тип проекта:** Веб-приложение для управления финансами
- **Назначение:** Помощь студентам в управлении личным бюджетом
- **Статус проекта:** В разработке

## Описание проекта

### Цель проекта
Разработка удобного и функционального приложения для управления личными финансами, ориентированного на студентов. Система позволяет отслеживать доходы и расходы, создавать категории трат, анализировать финансовые привычки и планировать бюджет.

### Текущие возможности
1. **Управление транзакциями**
   - Создание и просмотр транзакций
   - Базовая категоризация доходов и расходов
   - Возможность добавления описания

2. **Базовая аналитика**
   - Просмотр списка транзакций
   - Фильтрация по категориям
   - Отображение текущего баланса

3. **Административные функции**
   - Базовое управление пользователями
   - Просмотр транзакций пользователей

## Технический стек

### Backend
- Java (базовая версия)
- Встроенная база данных
- REST API

### Инфраструктура
- GitHub для версионного контроля
- Локальный сервер разработки

## Архитектура проекта

### Компоненты системы
1. **Клиентская часть**
   - Модуль аутентификации
   - Базовый интерфейс управления транзакциями
   - Простой интерфейс администратора

2. **Серверная часть**
   - Базовый API для работы с транзакциями
   - Сервис аутентификации
   - Работа с локальным хранилищем

3. **Хранение данных**
   - Таблица пользователей
   - Таблица транзакций
   - Таблица категорий

## Реализованные функции

### Пользовательские функции
- Базовая регистрация и авторизация
- Просмотр списка транзакций
- Создание новых транзакций
- Выбор категории для транзакции

### Административные функции
- Просмотр списка пользователей
- Просмотр транзакций пользователей
- Базовые настройки системы

## Тестирование

### Текущее тестирование
1. **Ручное тестирование**
   - Проверка основных функций
   - Тестирование пользовательского интерфейса
   - Проверка работы авторизации

2. **Отладка**
   - Исправление ошибок в реальном времени
   - Проверка корректности работы API
   - Тестирование на локальном окружении

## Развертывание

### Требования к системе
- Java Runtime Environment
- Веб-браузер
- Локальный сервер

### Процесс развертывания
1. Клонирование репозитория
2. Настройка локального окружения
3. Запуск сервера
4. Открытие приложения в браузере

## Текущие ограничения
1. **Функциональные**
   - Отсутствие сложной аналитики
   - Базовая система категорий
   - Ограниченные административные возможности

2. **Технические**
   - Локальное хранение данных
   - Отсутствие внешних интеграций
   - Ограниченная масштабируемость

## Дальнейшее развитие

### Ближайшие планы
1. **Функциональные улучшения**
   - Расширение системы категорий
   - Добавление базовой аналитики
   - Улучшение пользовательского интерфейса

2. **Технические улучшения**
   - Переход на PostgreSQL
   - Добавление Spring Framework
   - Улучшение безопасности

### Долгосрочные перспективы
- Добавление мобильной версии
- Интеграция с банковскими API
- Расширение аналитических возможностей

## Заключение

Проект Student Budget Manager находится на начальной стадии разработки. Реализован базовый функционал управления транзакциями и категориями. Система требует дальнейшей доработки и улучшения, но уже сейчас предоставляет основные возможности для управления студенческим бюджетом.

## Приложения

### Скриншоты интерфейса
- [Окно входа](images/login.png)
- [Главное окно](images/main-window.png)
- [Диалог транзакции](images/transaction-dialog.png)
- [Окно категорий](images/categories-window.png)
- [Панель администратора](images/admin-panel.png)

### Документация
- [Руководство пользователя](USER_MANUAL.md) 