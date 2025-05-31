package com.studentbudget.controller;

import com.studentbudget.service.AuthService;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.TransactionService;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import javafx.collections.FXCollections;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private final AuthService authService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @FXML private TabPane loginTabPane;
    @FXML private Tab loginTab;
    @FXML private Tab registerTab;
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
   
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<UserRole> roleComboBox;
    
    @FXML private ToggleButton themeToggle;

    public LoginController(AuthService authService, TransactionService transactionService, CategoryService categoryService) {
        this.authService = authService;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize() {
        themeToggle.setOnAction(event -> toggleTheme());
        errorLabel.setVisible(false);
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.setValue(UserRole.STUDENT);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }

        try {
            User user = authService.authenticate(username, password);
            logger.info("User logged in successfully: {}", username);
            showMainWindow();
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", username, e.getMessage());
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        UserRole role = roleComboBox.getValue();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || 
            firstName.isEmpty() || lastName.isEmpty()) {
            showError("Пожалуйста, заполните все обязательные поля");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Пароли не совпадают");
            return;
        }

        try {
            User user = authService.register(username, password, email, firstName, lastName, role);
            logger.info("User registered successfully: {}", username);
            
            // Show success message and switch to login tab
            showInfo("Регистрация успешна", 
                    "Вы успешно зарегистрировались. Теперь вы можете войти в систему.");
            loginTabPane.getSelectionModel().select(loginTab);
            
            // Clear registration fields
            clearRegistrationFields();
            
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            showError(e.getMessage());
        }
    }

    private void showMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            MainController controller = new MainController(transactionService, categoryService, authService);
            loader.setController(controller);
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Student Budget Manager");
            currentStage.setMaximized(true);
            
        } catch (IOException e) {
            logger.error("Error loading main window: ", e);
            showError("Не удалось загрузить главное окно: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearRegistrationFields() {
        regUsernameField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        emailField.clear();
        firstNameField.clear();
        lastNameField.clear();
        roleComboBox.setValue(UserRole.STUDENT);
    }

    private void toggleTheme() {
        Scene scene = usernameField.getScene();
        boolean isDarkTheme = scene.getStylesheets().contains(getClass().getResource("/css/dark-theme.css").toExternalForm());
        
        scene.getStylesheets().clear();
        if (isDarkTheme) {
            scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
            themeToggle.setText("🌙");
        } else {
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            themeToggle.setText("☀️");
        }
    }
} 