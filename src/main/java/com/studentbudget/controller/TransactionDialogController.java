package com.studentbudget.controller;

import com.studentbudget.model.Category;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionStatus;
import com.studentbudget.model.TransactionType;
import com.studentbudget.model.User;
import com.studentbudget.model.UserRole;
import com.studentbudget.service.AuthService;
import com.studentbudget.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Контроллер диалогового окна для создания и редактирования транзакций.
 * Обеспечивает интерфейс для ввода данных транзакции с учетом прав пользователя.
 */
public class TransactionDialogController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TransactionDialogController.class);

    // Компоненты пользовательского интерфейса
    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TransactionStatus> statusComboBox;
    @FXML private ComboBox<User> userComboBox;
    @FXML private Label userLabel;

    // Данные и сервисы
    private Transaction transaction;
    private boolean saveClicked = false;
    private final AuthService authService;
    private final UserService userService;

    /**
     * Конструктор контроллера диалога транзакций.
     * @param authService сервис аутентификации для проверки прав пользователя
     * @param userService сервис управления пользователями для работы со списком пользователей
     */
    public TransactionDialogController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Инициализация контроллера.
     * Настраивает компоненты интерфейса и устанавливает начальные значения.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("Инициализация TransactionDialogController начата");
        
        // Проверяем, что все FXML-компоненты были инициализированы
        if (typeComboBox == null) logger.error("typeComboBox не инициализирован");
        if (amountField == null) logger.error("amountField не инициализирован");
        if (categoryComboBox == null) logger.error("categoryComboBox не инициализирован");
        if (descriptionArea == null) logger.error("descriptionArea не инициализирован");
        if (datePicker == null) logger.error("datePicker не инициализирован");
        if (statusComboBox == null) logger.error("statusComboBox не инициализирован");
        if (userComboBox == null) logger.error("userComboBox не инициализирован");
        if (userLabel == null) logger.error("userLabel не инициализирован");
        
        initializeComboBoxes();
        logger.debug("ComboBoxes инициализированы");
        
        // Показывать выбор пользователя только для администраторов
        boolean isAdmin = authService.getCurrentUser().getRole() == UserRole.ADMIN;
        logger.debug("Текущий пользователь {} администратор", isAdmin ? "является" : "не является");
        
        userComboBox.setVisible(isAdmin);
        userLabel.setVisible(isAdmin);
        
        if (isAdmin) {
            initializeUserComboBox();
            logger.debug("UserComboBox инициализирован для администратора");
        }
        
        logger.debug("Инициализация TransactionDialogController завершена");
    }

    /**
     * Инициализация выпадающих списков с конвертерами для корректного отображения значений.
     */
    private void initializeComboBoxes() {
        logger.debug("Начало инициализации ComboBoxes");
        
        // Инициализация списка типов транзакций
        typeComboBox.getItems().setAll(TransactionType.values());
        typeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TransactionType type) {
                if (type == null) return "";
                return switch (type) {
                    case INCOME -> "Доход";
                    case EXPENSE -> "Расход";
                };
            }

            @Override
            public TransactionType fromString(String string) {
                return null; // Не используется для ComboBox
            }
        });
        logger.debug("TypeComboBox инициализирован");

        // Инициализация списка статусов
        statusComboBox.getItems().setAll(TransactionStatus.values());
        statusComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TransactionStatus status) {
                if (status == null) return "";
                return switch (status) {
                    case ACTIVE -> "Активна";
                    case CANCELLED -> "Отменена";
                    case PENDING -> "Ожидает";
                    case REJECTED -> "Отклонена";
                };
            }

            @Override
            public TransactionStatus fromString(String string) {
                return null; // Не используется для ComboBox
            }
        });
        logger.debug("StatusComboBox инициализирован");

        // Инициализация списка категорий
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public Category fromString(String string) {
                return null; // Не используется для ComboBox
            }
        });
        logger.debug("CategoryComboBox инициализирован");
        
        logger.debug("Завершение инициализации ComboBoxes");
    }

    /**
     * Инициализация выпадающего списка пользователей для администраторов.
     * Заполняет список всеми пользователями системы.
     */
    private void initializeUserComboBox() {
        List<User> users = userService.getAllUsers();
        userComboBox.getItems().setAll(users);
        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getUsername() + " (" + user.getFirstName() + " " + user.getLastName() + ")";
            }

            @Override
            public User fromString(String string) {
                return null; // Не используется для ComboBox
            }
        });

        // Установка текущего пользователя по умолчанию
        userComboBox.setValue(authService.getCurrentUser());
    }

    /**
     * Устанавливает транзакцию для редактирования.
     * Заполняет поля формы данными из транзакции.
     * @param transaction транзакция для редактирования или null для создания новой
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        if (transaction != null) {
            typeComboBox.setValue(transaction.getType());
            if (transaction.getAmount() != null) {
                amountField.setText(transaction.getAmount().toString());
            } else {
                amountField.setText("");
            }
            categoryComboBox.setValue(transaction.getCategory());
            descriptionArea.setText(transaction.getDescription());
            
            // Initialize date with current date for new transactions
            if (transaction.getDate() == null) {
                transaction.setDate(LocalDateTime.now());
            }
            datePicker.setValue(transaction.getDate().toLocalDate());
            
            statusComboBox.setValue(transaction.getStatus());
            
            if (userComboBox != null) {
                userComboBox.setValue(transaction.getUser());
            }
        } else {
            typeComboBox.setValue(TransactionType.EXPENSE);
            amountField.setText("");
            datePicker.setValue(LocalDate.now());
            statusComboBox.setValue(TransactionStatus.ACTIVE);
        }
    }

    /**
     * Устанавливает список доступных категорий для транзакции.
     * @param categories список категорий для выбора
     */
    public void setCategories(List<Category> categories) {
        categoryComboBox.getItems().setAll(categories);
    }

    /**
     * Обработчик нажатия кнопки "Сохранить".
     * Проверяет введенные данные и сохраняет транзакцию.
     */
    @FXML
    private void handleSave() {
        logger.debug("Начало обработки сохранения");
        if (!validateInput()) {
            logger.debug("Валидация не пройдена");
            return;
        }

        if (transaction == null) {
            transaction = new Transaction();
            logger.debug("Создана новая транзакция");
        }

        try {
            transaction.setType(typeComboBox.getValue());
            transaction.setAmount(new BigDecimal(amountField.getText().trim()));
            transaction.setCategory(categoryComboBox.getValue());
            transaction.setDescription(descriptionArea.getText());
            transaction.setDate(LocalDateTime.of(datePicker.getValue(), LocalDateTime.now().toLocalTime()));
            transaction.setStatus(statusComboBox.getValue());
            
            if (userComboBox.isVisible()) {
                transaction.setUser(userComboBox.getValue());
            } else {
                transaction.setUser(authService.getCurrentUser());
            }
            
            logger.debug("Данные транзакции установлены успешно");
            
            saveClicked = true;
            closeDialog();
        } catch (Exception e) {
            logger.error("Ошибка при сохранении транзакции", e);
            showError("Ошибка", "Ошибка при сохранении", e.getMessage());
        }
    }

    /**
     * Проверяет корректность введенных данных.
     * @return true если все данные корректны, false если есть ошибки
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        if (typeComboBox.getValue() == null) {
            errorMessage.append("Выберите тип транзакции\n");
        }

        if (amountField.getText().trim().isEmpty()) {
            errorMessage.append("Введите сумму\n");
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage.append("Сумма должна быть больше нуля\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Неверный формат суммы\n");
            }
        }

        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Выберите категорию\n");
        }

        if (datePicker.getValue() == null) {
            errorMessage.append("Выберите дату\n");
        }

        if (userComboBox.isVisible() && userComboBox.getValue() == null) {
            errorMessage.append("Выберите пользователя\n");
        }

        if (errorMessage.length() > 0) {
            showError("Ошибка валидации", "Исправьте следующие ошибки:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Обработчик нажатия кнопки "Отмена".
     * Закрывает диалоговое окно без сохранения изменений.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Отмена создания/редактирования транзакции");
        saveClicked = false;
        closeDialog();
    }

    /**
     * Закрывает диалоговое окно.
     */
    private void closeDialog() {
        logger.debug("Закрытие диалога");
        Stage stage = (Stage) typeComboBox.getScene().getWindow();
        stage.close();
    }

    /**
     * Отображает диалоговое окно с ошибкой.
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * @return true если была нажата кнопка "Сохранить"
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * @return редактируемая транзакция
     */
    public Transaction getTransaction() {
        return transaction;
    }
} 