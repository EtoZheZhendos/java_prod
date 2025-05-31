package com.studentbudget;

import com.studentbudget.controller.LoginController;
import com.studentbudget.service.AuthService;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.TransactionService;
import com.studentbudget.service.impl.AuthServiceImpl;
import com.studentbudget.service.impl.CategoryServiceImpl;
import com.studentbudget.service.impl.TransactionServiceImpl;
import com.studentbudget.dao.UserDao;
import com.studentbudget.dao.CategoryDao;
import com.studentbudget.dao.TransactionDao;
import com.studentbudget.dao.impl.UserDaoImpl;
import com.studentbudget.dao.impl.CategoryDaoImpl;
import com.studentbudget.dao.impl.TransactionDaoImpl;
import com.studentbudget.util.HibernateTransactionManager;
import com.studentbudget.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private SessionFactory sessionFactory;
    private HibernateTransactionManager transactionManager;

    @Override
    public void start(Stage primaryStage) {
        try {
        
            sessionFactory = new Configuration().configure().buildSessionFactory();
            transactionManager = new HibernateTransactionManager(sessionFactory);
            

            UserDao userDao = new UserDaoImpl(sessionFactory);
            CategoryDao categoryDao = new CategoryDaoImpl(sessionFactory);
            TransactionDao transactionDao = new TransactionDaoImpl(sessionFactory);

        
            AuthService authService = new AuthServiceImpl(userDao, transactionManager);
            CategoryService categoryService = new CategoryServiceImpl(categoryDao, transactionDao, transactionManager);
            TransactionService transactionService = new TransactionServiceImpl(transactionDao, transactionManager, authService);

        
            DatabaseInitializer initializer = new DatabaseInitializer(categoryService, sessionFactory);
            initializer.initialize();

        
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            LoginController controller = new LoginController(authService, transactionService, categoryService);
            loader.setController(controller);
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("Student Budget Manager - Login");
            primaryStage.show();
            
        } catch (Exception e) {
            logger.error("Error starting application: ", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 