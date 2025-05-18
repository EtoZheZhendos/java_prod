package com.studentbudget.controller;

import com.studentbudget.model.Category;
import com.studentbudget.model.Transaction;
import com.studentbudget.model.TransactionStatus;
import com.studentbudget.model.TransactionType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionDialogController implements Initializable {
    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<TransactionStatus> statusComboBox;

    private Transaction transaction;
    private boolean saveClicked = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeComboBox.getItems().addAll(TransactionType.values());
        statusComboBox.getItems().addAll(TransactionStatus.values());

        // Set up category combo box converter
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });

        // Add validators and formatters for amount field
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldValue);
            }
        });

        // Format amount with ruble symbol when focus is lost
        amountField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // focus lost
                try {
                    BigDecimal amount = new BigDecimal(amountField.getText().replace("₽", "").trim());
                    amountField.setText(String.format("%.2f ₽", amount));
                } catch (NumberFormatException e) {
                    // Keep the original text if it's not a valid number
                }
            } else { // focus gained
                String text = amountField.getText().replace("₽", "").trim();
                amountField.setText(text);
            }
        });
    }

    public void setCategories(List<Category> categories) {
        categoryComboBox.getItems().addAll(categories);
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;

        if (transaction != null) {
            typeComboBox.setValue(transaction.getType());
            if (transaction.getAmount() != null) {
                amountField.setText(String.format("%.2f ₽", transaction.getAmount()));
            }
            categoryComboBox.setValue(transaction.getCategory());
            descriptionArea.setText(transaction.getDescription());
            datePicker.setValue(transaction.getDate() != null ? transaction.getDate().toLocalDate() : LocalDate.now());
            statusComboBox.setValue(transaction.getStatus() != null ? transaction.getStatus() : TransactionStatus.ACTIVE);
        } else {
            typeComboBox.setValue(TransactionType.EXPENSE);
            amountField.setText("0.00 ₽");
            datePicker.setValue(LocalDate.now());
            statusComboBox.setValue(TransactionStatus.ACTIVE);
        }
    }

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        if (transaction == null) {
            transaction = new Transaction();
        }

        transaction.setType(typeComboBox.getValue());
        transaction.setAmount(new BigDecimal(amountField.getText().replace("₽", "").trim()));
        transaction.setCategory(categoryComboBox.getValue());
        transaction.setDescription(descriptionArea.getText());
        transaction.setDate(datePicker.getValue().atStartOfDay());
        transaction.setStatus(statusComboBox.getValue());

        saveClicked = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (typeComboBox.getValue() == null) {
            errorMessage += "Please select a transaction type!\n";
        }
        if (amountField.getText() == null || amountField.getText().trim().isEmpty()) {
            errorMessage += "Please enter an amount!\n";
        }
        if (categoryComboBox.getValue() == null) {
            errorMessage += "Please select a category!\n";
        }
        if (datePicker.getValue() == null) {
            errorMessage += "Please select a date!\n";
        }
        if (statusComboBox.getValue() == null) {
            errorMessage += "Please select a status!\n";
        }

        if (errorMessage.isEmpty()) {
            try {
                new BigDecimal(amountField.getText().replace("₽", "").trim());
                return true;
            } catch (NumberFormatException e) {
                errorMessage += "Invalid amount format!\n";
            }
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Fields");
        alert.setHeaderText("Please correct invalid fields");
        alert.setContentText(errorMessage);
        alert.showAndWait();

        return false;
    }

    private void closeDialog() {
        ((Stage) typeComboBox.getScene().getWindow()).close();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public Transaction getTransaction() {
        return transaction;
    }
} 