# Student Budget Manager

## О проекте
JavaFX-приложение для управления личным бюджетом студента с возможностью отслеживания доходов и расходов, категоризации трат и визуализации финансовой статистики.

## Структура проекта
```
student-budget-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/studentbudget/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       ├── dao/
│   │   │       └── util/
│   │   └── resources/
│   │       ├── css/
│   │       ├── fxml/
│   │       └── hibernate.cfg.xml
│   └── test/
├── pom.xml
├── README.md
└── .gitignore
```

## Требования
- Java 17 или выше
- Maven 3.8+
- H2 Database
- JavaFX 17+

## Быстрый старт
```bash
git clone https://github.com/yourusername/student-budget-manager.git
cd student-budget-manager
mvn clean install
mvn javafx:run
```

## Основные возможности
- Регистрация и авторизация пользователей
- Управление доходами и расходами
- Категоризация транзакций
- Визуализация финансовой статистики
- Фильтрация и поиск транзакций
- Поддержка светлой и темной темы
- Административный интерфейс

## Технологии
- Java 17
- JavaFX
- Hibernate
- H2 Database
- Maven
- CSS
- FXML

## Разработка

### Предварительные требования
1. JDK 17
2. Maven 3.8+
3. Любая IDE с поддержкой Java (рекомендуется IntelliJ IDEA)

### Настройка окружения
1. Клонируйте репозиторий
2. Импортируйте проект как Maven-проект
3. Убедитесь, что все зависимости успешно загружены
4. Запустите `mvn clean install`

### Запуск приложения
```bash
mvn javafx:run
```

## Техническая документация

Полное техническое задание и документация доступны в файле [TECHNICAL_REQUIREMENTS.md](TECHNICAL_REQUIREMENTS.md) 