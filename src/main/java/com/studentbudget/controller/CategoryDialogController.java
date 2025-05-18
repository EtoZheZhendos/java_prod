package com.studentbudget.controller;

import com.studentbudget.config.AppConfig;
import com.studentbudget.model.Category;
import com.studentbudget.service.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CategoryDialogController {
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField descriptionField;

    private Stage dialogStage;
    private Category category;
    private boolean okClicked = false;
    private final CategoryService categoryService;

    public CategoryDialogController() {
        this.categoryService = AppConfig.getInstance().getCategoryService();
    }

    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCategory(Category category) {
        this.category = category;

        if (category != null) {
            nameField.setText(category.getName());
            descriptionField.setText(category.getDescription());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (category == null) {
                category = new Category();
            }
            
            category.setName(nameField.getText());
            category.setDescription(descriptionField.getText());

            try {
                if (category.getId() == null) {
                    categoryService.addCategory(category);
                } else {
                    categoryService.updateCategory(category);
                }
                okClicked = true;
                dialogStage.close();
            } catch (Exception e) {
                // Handle error
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "Category name is required!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show error message
            return false;
        }
    }
} 