package com.studentbudget.controller;

import com.studentbudget.service.AuthService;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.TransactionService;
import com.studentbudget.service.UserService;
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

/**
 * Контроллер окна авторизации и регистрации пользователей.
 * Обеспечивает функционал входа в систему и регистрации новых пользователей.
 * Также поддерживает переключение между светлой и темной темами интерфейса.
 */
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private final AuthService authService;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final UserService userService;

    // Компоненты пользовательского интерфейса для авторизации
    @FXML private TabPane loginTabPane;
    @FXML private Tab loginTab;
    @FXML private Tab registerTab;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    // Компоненты пользовательского интерфейса для регистрации
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private ToggleButton themeToggle;

    /**
     * Конструктор контроллера окна авторизации.
     * @param authService сервис аутентификации
     * @param userService сервис управления пользователями
     * @param transactionService сервис управления транзакциями
     * @param categoryService сервис управления категориями
     */
    public LoginController(AuthService authService, UserService userService, 
                         TransactionService transactionService, CategoryService categoryService) {
        this.authService = authService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    /**
     * Инициализация контроллера.
     * Настраивает начальное состояние компонентов интерфейса.
     */
    @FXML
    private void initialize() {
        themeToggle.setOnAction(event -> toggleTheme());
        errorLabel.setVisible(false);
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.setValue(UserRole.STUDENT);
    }

    /**
     * Обработчик нажатия кнопки входа.
     * Проверяет учетные данные и выполняет вход в систему.
     */
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
            logger.info("Пользователь успешно вошел в систему: {}", username);
            showMainView(user);
        } catch (Exception e) {
            logger.error("Ошибка входа для пользователя {}: {}", username, e.getMessage());
            showError(e.getMessage());
        }
    }

    /**
     * Обработчик нажатия кнопки регистрации.
     * Проверяет введенные данные и регистрирует нового пользователя.
     */
    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        UserRole role = roleComboBox.getValue();

        if (!validateRegistrationInput(username, password, confirmPassword, email, firstName, lastName)) {
            return;
        }

        try {
            User user = authService.register(username, password, email, firstName, lastName, role);
            logger.info("Пользователь успешно зарегистрирован: {}", username);
            
            showInfo("Регистрация успешна", 
                    "Вы успешно зарегистрировались. Теперь вы можете войти в систему.");
            loginTabPane.getSelectionModel().select(loginTab);
            
            clearRegistrationFields();
            
        } catch (Exception e) {
            logger.error("Ошибка регистрации: {}", e.getMessage());
            showError(e.getMessage());
        }
    }

    /**
     * Проверяет корректность введенных данных при регистрации.
     * @return true если все данные корректны, false если есть ошибки
     */
    private boolean validateRegistrationInput(String username, String password, String confirmPassword, 
                                           String email, String firstName, String lastName) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showError("Ошибка валидации", "Все поля обязательны для заполнения");
            return false;
        }

        if (password.length() < 6) {
            showError("Ошибка валидации", "Пароль должен содержать не менее 6 символов");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Ошибка валидации", "Пароли не совпадают");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Ошибка валидации", "Некорректный формат email");
            return false;
        }

        return true;
    }

    /**
     * Открывает главное окно приложения для авторизованного пользователя.
     * @param user авторизованный пользователь
     */
    private void showMainView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            MainController controller = new MainController(
                transactionService,
                categoryService,
                authService,
                userService
            );
            loader.setController(controller);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Student Budget Manager - " + user.getUsername());
            stage.show();
        } catch (IOException e) {
            logger.error("Ошибка загрузки главного окна: ", e);
            showError("Ошибка", "Не удалось загрузить главное окно", e.getMessage());
        }
    }

    /**
     * Отображает диалоговое окно с ошибкой.
     */
    private void showError(String message) {
        showError("Ошибка", null, message);
    }

    private void showError(String title, String message) {
        showError(title, null, message);
    }

    private void showError(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Отображает информационное диалоговое окно.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Очищает поля формы регистрации.
     */
    private void clearRegistrationFields() {
        regUsernameField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        emailField.clear();
        firstNameField.clear();
        lastNameField.clear();
        roleComboBox.setValue(UserRole.STUDENT);
    }

    /**
     * Переключает тему оформления между светлой и темной.
     */
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