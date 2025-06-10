package com.studentbudget.controller;

import com.studentbudget.config.AppConfig;
import com.studentbudget.model.*;
import com.studentbudget.service.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final UserService userService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label balanceLabel;
    
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, LocalDateTime> dateColumn;
    @FXML private TableColumn<Transaction, TransactionType> typeColumn;
    @FXML private TableColumn<Transaction, Category> categoryColumn;
    @FXML private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, TransactionStatus> statusColumn;
    @FXML private TableColumn<Transaction, User> userColumn;
    
    @FXML private PieChart expenseChart;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<TransactionStatus> statusFilter;
    @FXML private ComboBox<User> userFilter;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    
    @FXML private TabPane mainTabPane;
    @FXML private Tab adminTab;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, UserRole> roleColumn;
    @FXML private TableColumn<User, LocalDateTime> createdAtColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, BigDecimal> userBalanceColumn;

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, String> categoryNameColumn;
    @FXML private TableColumn<Category, String> categoryDescriptionColumn;
    @FXML private TableColumn<Category, Void> categoryActionsColumn;

    @FXML private Label currentUserLabel;

    @FXML private ToggleButton themeToggle;
    private Scene scene;
    private static final String LIGHT_THEME = "/css/light-theme.css";
    private static final String DARK_THEME = "/css/dark-theme.css";

    public MainController(TransactionService transactionService, CategoryService categoryService, AuthService authService, UserService userService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableColumns();
        initializeFilters();
        initializeCategoryTable();
        initializeThemeToggle();
        
        User currentUser = authService.getCurrentUser();
        currentUserLabel.setText(String.format("%s (%s)", 
            currentUser.getUsername(), 
            currentUser.getRole().toString()));
        
        if (isAdmin()) {
            initializeAdminComponents();
        } else {

            if (adminTab != null && mainTabPane != null) {
                mainTabPane.getTabs().remove(adminTab);
            }
            if (userColumn != null) {
                userColumn.setVisible(false);
            }
            if (userFilter != null) {
                userFilter.setVisible(false);
            }
        }
        
        updateDashboard();
    }

    private void initializeTableColumns() {

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        if (userColumn != null) {
            userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
            userColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getUsername());
                    }
                }
            });
        }

        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.getName());
                }
            }
        });

        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(item));
                }
            }
        });

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₽", item));
                }
            }
        });
    }

    private void initializeFilters() {
        categoryFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });

        statusFilter.setItems(FXCollections.observableArrayList(TransactionStatus.values()));
        
        if (userFilter != null && isAdmin()) {
            userFilter.setConverter(new StringConverter<>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getUsername() : "";
                }

                @Override
                public User fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            userFilter.setItems(FXCollections.observableArrayList(authService.getAllUsers()));
        }
    }

    private void initializeAdminComponents() {
        if (usersTable != null) {

            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
            createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
            activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
            
            userBalanceColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        User user = getTableRow().getItem();
                        if (user != null) {
                            BigDecimal balance = calculateUserBalance(user);
                            setText(String.format("%.2f ₽", balance));
                        } else {
                            setText(null);
                        }
                    }
                }
            });
            
            updateUsersTable();
        }
    }

    private void updateUsersTable() {
        if (usersTable != null && isAdmin()) {
            usersTable.setItems(FXCollections.observableArrayList(authService.getAllUsers()));
        }
    }

    private BigDecimal calculateUserBalance(User user) {
        if (!isAdmin()) return BigDecimal.ZERO;
        
        List<Transaction> userTransactions = transactionService.getTransactionsByUser(user);
        BigDecimal income = userTransactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal expenses = userTransactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return income.subtract(expenses);
    }

    private void updateDashboard() {
        updateSummary();
        updateTransactionTables();
        updateExpenseChart();
        updateCategoryTable();
        if (isAdmin()) {
            updateUsersTable();
        }
    }

    private void updateSummary() {
        totalIncomeLabel.setText(formatAmount(transactionService.getTotalIncome()));
        totalExpensesLabel.setText(formatAmount(transactionService.getTotalExpenses()));
        updateBalanceLabel();
    }

    private void updateTransactionTables() {
        List<Transaction> transactions;
        if (isAdmin()) {
            User selectedUser = userFilter.getValue();
            if (selectedUser != null) {
                transactions = transactionService.getTransactionsByUser(selectedUser);
            } else {
                transactions = transactionService.getAllUsersTransactions();
            }
        } else {
            transactions = transactionService.getCurrentUserTransactions();
        }
        transactionsTable.setItems(FXCollections.observableArrayList(transactions));
    }

    @FXML
    private void handleSearch() {
        LocalDateTime start = startDate.getValue() != null ? startDate.getValue().atStartOfDay() : null;
        LocalDateTime end = endDate.getValue() != null ? endDate.getValue().atTime(23, 59, 59) : null;
        
        List<Transaction> results = new ArrayList<>();
        
        if (isAdmin()) {
            User selectedUser = userFilter.getValue();
            if (selectedUser != null) {
                if (start != null && end != null) {
                    results = transactionService.getTransactionsByUserAndDateRange(selectedUser, start, end);
                } else {
                    results = transactionService.getTransactionsByUser(selectedUser);
                }
            } else {
                if (start != null && end != null) {
                    results = transactionService.getAllUsersTransactions().stream()
                        .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                        .toList();
                } else {
                    results = transactionService.getAllUsersTransactions();
                }
            }
        } else {
            if (start != null && end != null) {
                results = transactionService.getCurrentUserTransactionsByDateRange(start, end);
            } else {
                results = transactionService.getCurrentUserTransactions();
            }
        }
        
        if (!searchField.getText().isEmpty()) {
            String searchTerm = searchField.getText().toLowerCase();
            results = results.stream()
                .filter(t -> t.getDescription().toLowerCase().contains(searchTerm))
                .toList();
        }
        
        if (categoryFilter.getValue() != null) {
            Category category = categoryFilter.getValue();
            results = results.stream()
                .filter(t -> t.getCategory().equals(category))
                .toList();
        }
        
        if (statusFilter.getValue() != null) {
            TransactionStatus status = statusFilter.getValue();
            results = results.stream()
                .filter(t -> t.getStatus() == status)
                .toList();
        }
        
        transactionsTable.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    private void handleUserFilterChange() {
        if (isAdmin()) {
            updateTransactionTables();
            updateExpenseChart();
        }
    }

    private boolean isAdmin() {
        return authService.getCurrentUser().getRole() == UserRole.ADMIN;
    }

    private void initializeCategoryTable() {
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        categoryActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Изменить");
            private final Button deleteButton = new Button("Удалить");

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> handleEditCategory(getTableRow().getItem()));
                deleteButton.setOnAction(event -> handleDeleteCategory(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    var container = new javafx.scene.layout.HBox(5);
                    container.getChildren().addAll(editButton, deleteButton);
                    setGraphic(container);
                }
            }
        });

        updateCategoryTable();
    }

    private void updateCategoryTable() {
        categoriesTable.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
    }

    private void updateExpenseChart() {
        expenseChart.getData().clear();
        Map<Category, Double> distribution = transactionService.getExpenseDistribution();
        
        System.out.println("Updating expense chart with distribution: " + distribution);
        
        if (distribution != null && !distribution.isEmpty()) {

            List<Map.Entry<Category, Double>> sortedEntries = new ArrayList<>(distribution.entrySet());
            sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            
            System.out.println("Sorted entries for pie chart: " + sortedEntries.size());
            
            double totalPercentage = 0.0;
            for (Map.Entry<Category, Double> entry : sortedEntries) {
                Category category = entry.getKey();
                Double percentage = entry.getValue();
                String name = category != null ? category.getName() : "Без категории";
                
                System.out.println(String.format("Adding category to chart: %s with %.2f%%", name, percentage));
                
                PieChart.Data slice = new PieChart.Data(
                    String.format("%s (%.1f%%)", name, percentage),
                    percentage
                );
                expenseChart.getData().add(slice);
                totalPercentage += percentage;
            }
            
            System.out.println(String.format("Total percentage in pie chart: %.2f%%", totalPercentage));
            
            expenseChart.setLegendVisible(true);
        } else {
            System.out.println("No expense distribution data available");

            expenseChart.getData().add(new PieChart.Data("Нет расходов", 100));
        }
    }

    private void updateBalanceLabel() {
        BigDecimal balance = transactionService.getCurrentBalance();
        balanceLabel.setText(formatAmount(balance));
    }

    @FXML
    private void handleAddTransaction() {
        Transaction transaction = new Transaction();
        boolean okClicked = showTransactionDialog(transaction, "Добавить Транзакцию");
        
        if (okClicked) {
            try {
                transactionService.createTransaction(transaction);
                updateDashboard();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Ошибка при создании транзакции");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryDialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Добавить Категорию");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(totalIncomeLabel.getScene().getWindow());
            dialogStage.setScene(scene);

            CategoryDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                updateDashboard();
                updateCategoryTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEditTransaction(Transaction transaction) {
        boolean okClicked = showTransactionDialog(transaction, "Редактировать Транзакцию");
        if (okClicked) {
            transactionService.updateTransaction(transaction);
            updateDashboard();
        }
    }

    private void handleDeleteTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this transaction?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                transactionService.deleteTransaction(transaction.getId());
                updateDashboard();
            }
        });
    }

    private boolean showTransactionDialog(Transaction transaction, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(totalIncomeLabel.getScene().getWindow());
            dialogStage.setScene(scene);

            TransactionDialogController controller = loader.getController();
            
            List<Category> categories = categoryService.getAllCategories();
            System.out.println("Loading categories for transaction dialog: " + categories.size());
            categories.forEach(category -> 
                System.out.println("Category: " + category.getName() + " (ID: " + category.getId() + ")")
            );
            
            controller.setCategories(categories);
            controller.setTransaction(transaction);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                transaction = controller.getTransaction();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка при открытии диалога");
            alert.setContentText("Не удалось открыть диалог транзакции: " + e.getMessage());
            alert.showAndWait();
            return false;
        }
    }

    private void handleEditCategory(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryDialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактировать Категорию");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(totalIncomeLabel.getScene().getWindow());
            dialogStage.setScene(scene);

            CategoryDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCategory(category);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                updateDashboard();
                updateCategoryTable();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteCategory(Category category) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Вы уверены, что хотите удалить эту категорию?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Удаление категории");
        confirmAlert.setHeaderText("Подтверждение удаления");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (categoryService.hasTransactions(category.getId())) {
                        // Если у категории есть транзакции, предложим переместить их
                        Alert moveAlert = new Alert(Alert.AlertType.CONFIRMATION,
                                "У этой категории есть транзакции. Хотите переместить их в другую категорию?",
                                ButtonType.YES, ButtonType.NO);
                        moveAlert.setTitle("Перемещение транзакций");
                        moveAlert.setHeaderText("Обнаружены связанные транзакции");
                        
                        moveAlert.showAndWait().ifPresent(moveResponse -> {
                            if (moveResponse == ButtonType.YES) {

                                List<Category> categories = categoryService.getAllCategories();
                                categories.remove(category);
                                
                                if (categories.isEmpty()) {
                                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                    errorAlert.setTitle("Ошибка");
                                    errorAlert.setHeaderText("Нет доступных категорий");
                                    errorAlert.setContentText("Создайте новую категорию перед удалением текущей.");
                                    errorAlert.showAndWait();
                                    return;
                                }
                                
                                ChoiceDialog<Category> dialog = new ChoiceDialog<>(categories.get(0), categories);
                                dialog.setTitle("Выбор категории");
                                dialog.setHeaderText("Выберите категорию для перемещения транзакций");
                                dialog.setContentText("Категория:");
                                
                                dialog.getDialogPane().lookupAll(".combo-box").forEach(node -> {
                                    if (node instanceof ComboBox) {
                                        @SuppressWarnings("unchecked")
                                        ComboBox<Category> comboBox = (ComboBox<Category>) node;
                                        comboBox.setConverter(new StringConverter<>() {
                                            @Override
                                            public String toString(Category cat) {
                                                return cat != null ? cat.getName() : "";
                                            }
                                            
                                            @Override
                                            public Category fromString(String string) {
                                                return null;
                                            }
                                        });
                                    }
                                });
                                
                                dialog.showAndWait().ifPresent(newCategory -> {
                                    try {
                                        categoryService.deleteCategoryWithTransactions(category.getId(), newCategory);
                                        updateDashboard();
                                    } catch (Exception ex) {
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                        errorAlert.setTitle("Ошибка");
                                        errorAlert.setHeaderText("Ошибка при удалении категории");
                                        errorAlert.setContentText(ex.getMessage());
                                        errorAlert.showAndWait();
                                    }
                                });
                            }
                        });
                    } else {

                        categoryService.deleteCategory(category.getId());
                        updateDashboard();
                    }
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Ошибка при удалении категории");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%.2f ₽", amount);
    }

    @FXML
    private void handleLogout() {
        try {
            authService.logout();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            LoginController controller = new LoginController(authService, userService, transactionService, categoryService);
            loader.setController(controller);
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            Stage currentStage = (Stage) currentUserLabel.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Student Budget Manager - Login");
            currentStage.setMaximized(false);
            currentStage.sizeToScene();
            currentStage.centerOnScreen();
            
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            showError("Ошибка", "Не удалось выйти из системы: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void initializeThemeToggle() {
        themeToggle.setSelected(true); // По умолчанию темная тема
        themeToggle.setText(themeToggle.isSelected() ? "🌙" : "☀️");
        
        themeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            scene = themeToggle.getScene();
            if (scene != null) {
                if (newValue) { // Темная тема
                    scene.getStylesheets().remove(LIGHT_THEME);
                    scene.getStylesheets().add(DARK_THEME);
                    themeToggle.setText("🌙");
                } else { // Светлая тема
                    scene.getStylesheets().remove(DARK_THEME);
                    scene.getStylesheets().add(LIGHT_THEME);
                    themeToggle.setText("☀️");
                }
            }
        });
    }
} 