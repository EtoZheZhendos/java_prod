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

    public MainController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableColumns();
        initializeFilters();
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

        allDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        allTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        allCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        allAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        allDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

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

        // Add action buttons to the actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
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

    private void updateDashboard() {
        // Update summary labels
        totalIncomeLabel.setText(formatAmount(transactionService.getTotalIncome()));
        totalExpensesLabel.setText(formatAmount(transactionService.getTotalExpenses()));
        balanceLabel.setText(formatAmount(transactionService.getBalance()));

        // Update transactions table
        transactionsTable.setItems(FXCollections.observableArrayList(transactionService.getAllTransactions()));
        allTransactionsTable.setItems(FXCollections.observableArrayList(transactionService.getAllTransactions()));

        // Update pie chart
        updateExpenseChart();
    }

    private void updateExpenseChart() {
        expenseChart.getData().clear();
        Map<Category, Double> distribution = transactionService.getExpenseDistribution();
        distribution.forEach((category, percentage) -> 
            expenseChart.getData().add(new PieChart.Data(category.getName(), percentage))
        );
    }

    @FXML
    private void handleAddTransaction() {
        Transaction transaction = showTransactionDialog(null);
        if (transaction != null) {
            transactionService.addTransaction(transaction);
            updateDashboard();
        }
    }

    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryDialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Category");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(totalIncomeLabel.getScene().getWindow());
            dialogStage.setScene(scene);

            CategoryDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                updateDashboard();
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
        Transaction editedTransaction = showTransactionDialog(transaction);
        if (editedTransaction != null) {
            transactionService.updateTransaction(editedTransaction);
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

    private Transaction showTransactionDialog(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transaction-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage dialogStage = new Stage();
            dialogStage.setTitle(transaction == null ? "Add Transaction" : "Edit Transaction");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(totalIncomeLabel.getScene().getWindow());
            dialogStage.setScene(scene);

            TransactionDialogController controller = loader.getController();
            controller.setCategories(categoryService.getAllCategories());
            controller.setTransaction(transaction);

            dialogStage.showAndWait();

            return controller.isSaveClicked() ? controller.getTransaction() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("$%.2f", amount);
    }
} 