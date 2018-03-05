package com.egls.server.command.controller;

import com.egls.server.command.MainApplication;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/22
 */

public class GenerateController {
    public static final String INSTALL = "install";
    public static final String DEPLOY = "deploy";

    @FXML
    private TextArea consoleField;

    @FXML
    private Button closeBtn;

    private Stage stage;

    private String buildCmd;

    public static void showAndGenerate(String buildCmd) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddCommandController.class.getResource("/GenerateConsole.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Console");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(loader.load()));
            stage.show();

            GenerateController generateController = loader.getController();
            generateController.init(stage, buildCmd);
            generateController.generate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        consoleField.setText("");
        closeBtn.setVisible(false);
    }

    @FXML
    private void close() {
        stage.close();
    }

    private void init(Stage stage, String buildCmd) {
        this.stage = stage;
        this.buildCmd = buildCmd;
        stage.getScene().setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE && closeBtn.isVisible()) {
                close();
            }
        });
    }

    private void generate() {
        CodeGenerate codeGenerate = new CodeGenerate(buildCmd, this::printMessage, () -> Platform.runLater(() -> closeBtn.setVisible(true)));
        codeGenerate.start();
    }

    private void printMessage(String message) {
        System.out.println(message);

        Platform.runLater(() -> {
            consoleField.appendText(message);
            consoleField.setScrollLeft(0D);
            consoleField.setScrollTop(Double.MAX_VALUE);
        });
    }
}
