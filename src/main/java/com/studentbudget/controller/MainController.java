package com.studentbudget.controller;

import com.studentbudget.model.Category;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionStatus;
import com.studentbudget.model.TransactionType;
import com.studentbudget.service.CategoryService;
import com.studentbudget.service.TransactionService;
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
    private final TransactionService transactionService;
    private final CategoryService categoryService;
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
    
    @FXML private PieChart expenseChart;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<TransactionStatus> statusFilter;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    
    @FXML private TableView<Transaction> allTransactionsTable;
    @FXML private TableColumn<Transaction, LocalDateTime> allDateColumn;
    @FXML private TableColumn<Transaction, TransactionType> allTypeColumn;
    @FXML private TableColumn<Transaction, Category> allCategoryColumn;
    @FXML private TableColumn<Transaction, BigDecimal> allAmountColumn;
    @FXML private TableColumn<Transaction, String> allDescriptionColumn;
    @FXML private TableColumn<Transaction, TransactionStatus> allStatusColumn;
    @FXML private TableColumn<Transaction, Void> actionsColumn;

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, String> categoryNameColumn;
    @FXML private TableColumn<Category, String> categoryDescriptionColumn;
    @FXML private TableColumn<Category, Void> categoryActionsColumn;

    public MainController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableColumns();
        initializeFilters();
        initializeCategoryTable();
        updateDashboard();
    }

    private void initializeTableColumns() {
        // Initialize columns for both tables
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add cell factory for category column to display category name
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

        allDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        allTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        allCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        allAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        allDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add cell factory for all transactions category column
        allCategoryColumn.setCellFactory(column -> new TableCell<>() {
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

        // Format date cells
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

        allDateColumn.setCellFactory(column -> new TableCell<>() {
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

        // Format amount cells
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

        allAmountColumn.setCellFactory(column -> new TableCell<>() {
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

        // Add action buttons to the actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Изменить");
            private final Button deleteButton = new Button("Удалить");

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                editButton.setOnAction(event -> handleEditTransaction(getTableRow().getItem()));
                deleteButton.setOnAction(event -> handleDeleteTransaction(getTableRow().getItem()));
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
    }

    private void initializeCategoryTable() {
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Add action buttons to the category actions column
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

    private void updateDashboard() {
        updateSummary();
        updateTransactionTables();
        updateExpenseChart();
        updateCategoryTable();
    }

    private void updateSummary() {
        totalIncomeLabel.setText(formatAmount(transactionService.getTotalIncome()));
        totalExpensesLabel.setText(formatAmount(transactionService.getTotalExpenses()));
        updateBalanceLabel();
    }

    private void updateTransactionTables() {
        transactionsTable.setItems(FXCollections.observableArrayList(transactionService.getAllTransactions()));
        allTransactionsTable.setItems(FXCollections.observableArrayList(transactionService.getAllTransactions()));
    }

    private void updateExpenseChart() {
        expenseChart.getData().clear();
        Map<Category, Double> distribution = transactionService.getExpenseDistribution();
        
        System.out.println("Updating expense chart with distribution: " + distribution);
        
        if (distribution != null && !distribution.isEmpty()) {
            // Sort categories by percentage for consistent display
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
            
            // Add legend if not present
            expenseChart.setLegendVisible(true);
        } else {
            System.out.println("No expense distribution data available");
            // Add placeholder when no data
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

    @FXML
    private void handleSearch() {
        LocalDateTime start = startDate.getValue() != null ? startDate.getValue().atStartOfDay() : null;
        LocalDateTime end = endDate.getValue() != null ? endDate.getValue().atTime(23, 59, 59) : null;
        
        if (start != null && end != null) {
            allTransactionsTable.setItems(FXCollections.observableArrayList(
                transactionService.getTransactionsByDateRange(start, end)));
        }
        
        if (!searchField.getText().isEmpty()) {
            allTransactionsTable.setItems(FXCollections.observableArrayList(
                transactionService.searchTransactions(searchField.getText())));
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
            
            // Get all categories and log them
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
                                // Показываем диалог выбора новой категории
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
                        // Если у категории нет транзакций, просто удаляем её
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
} 