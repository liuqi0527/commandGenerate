package com.egls.server.command.controller;

import com.egls.server.command.MainApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/14
 */

public class ConfirmController {

    @FXML
    private Label msgLabel;

    private Stage stage;
    private Runnable runnable;

    public static void show(String msg) {
        show(msg, null);
    }

    public static void show(String msg, Runnable runnable) {
        try {
            System.out.println("tips :" + msg);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddCommandController.class.getResource("/ConfirmFrame.fxml"));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(loader.load()));


            ConfirmController confirmController = loader.getController();
            confirmController.stage = stage;
            confirmController.msgLabel.setText(msg);
            confirmController.runnable = runnable;

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void confirm() {
        if (runnable != null) {
            runnable.run();
        }
        stage.close();
    }

    @FXML
    private void cancel() {
        stage.close();
    }
}
