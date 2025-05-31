package com.studentbudget;

import com.studentbudget.config.AppConfig;
import com.studentbudget.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Инициализация конфигурации
            AppConfig appConfig = AppConfig.getInstance();
            
            // Загрузка FXML для окна авторизации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            
            // Создание контроллера с внедрением зависимостей
            LoginController controller = new LoginController(
                appConfig.getAuthService(),
                appConfig.getTransactionService(),
                appConfig.getCategoryService()
            );
            loader.setController(controller);
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("Student Budget Manager - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Закрытие ресурсов при завершении приложения
        AppConfig.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 