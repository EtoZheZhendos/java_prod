package com.studentbudget;

import com.studentbudget.config.AppConfig;
import com.studentbudget.controller.LoginController;
import com.studentbudget.dao.*;
import com.studentbudget.dao.impl.*;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.*;
import com.studentbudget.service.impl.*;
import com.studentbudget.util.DatabaseInitializer;
import com.studentbudget.util.HibernateTransactionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс приложения Student Budget Manager.
 * Инициализирует компоненты приложения, настраивает подключение к базе данных
 * и запускает пользовательский интерфейс.
 */
public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private SessionFactory sessionFactory;
    private HibernateTransactionManager transactionManager;

    /**
     * Метод запуска приложения.
     * Инициализирует базу данных, сервисы и отображает окно входа.
     * @param primaryStage главное окно приложения
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            transactionManager = new HibernateTransactionManager(sessionFactory);

            UserDao userDao = new UserDaoImpl(sessionFactory);
            CategoryDao categoryDao = new CategoryDaoImpl(sessionFactory);
            TransactionDao transactionDao = new TransactionDaoImpl(sessionFactory);

            AuthService authService = new AuthServiceImpl(userDao, transactionManager);
            UserService userService = new UserServiceImpl(userDao, transactionManager);
            CategoryService categoryService = new CategoryServiceImpl(categoryDao, transactionDao, transactionManager);
            TransactionService transactionService = new TransactionServiceImpl(transactionDao, transactionManager, authService);

            DatabaseInitializer initializer = new DatabaseInitializer(categoryService, userService, sessionFactory);
            initializer.initialize();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            LoginController controller = new LoginController(authService, userService, transactionService, categoryService);
            loader.setController(controller);
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Student Budget Manager - Login");
            primaryStage.show();
            
        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения: ", e);
            System.exit(1);
        }
    }

    /**
     * Освобождает ресурсы при закрытии приложения.
     * Закрывает соединение с базой данных.
     */
    @Override
    public void stop() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    /**
     * Точка входа в приложение.
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch(args);
    }
} 