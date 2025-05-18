package com.studentbudget;

import com.studentbudget.config.AppConfig;
import com.studentbudget.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main-view.fxml"));
        
        // Set controller factory to use our services
        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == MainController.class) {
                return new MainController(
                    AppConfig.getInstance().getTransactionService(),
                    AppConfig.getInstance().getCategoryService()
                );
            }
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle("Student Budget Manager");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Cleanup resources
        AppConfig.getInstance().shutdown();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch();
    }
} 