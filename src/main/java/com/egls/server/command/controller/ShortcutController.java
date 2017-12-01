package com.egls.server.command.controller;

import java.util.HashMap;
import java.util.Map;

import com.egls.server.command.MainApplication;
import com.egls.server.command.shortcut.FunctionKey;
import com.egls.server.command.shortcut.Shortcut;
import com.egls.server.command.shortcut.ShortcutManager;
import com.egls.server.command.shortcut.ShortcutType;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author LiuQi - [Created on 2017-11-25]
 */
public class ShortcutController {

    @FXML
    private TextField commandFunctionText;

    @FXML
    private TextField commandCodeText;

    @FXML
    private TextField fieldFunctionText;

    @FXML
    private TextField fieldCodeText;

    @FXML
    private TextField searchFunctionText;

    @FXML
    private TextField searchCodeText;

    @FXML
    private TextField installFunctionText;

    @FXML
    private TextField installCodeText;

    @FXML
    private TextField deployFunctionText;

    @FXML
    private TextField deployCodeText;

    private Stage stage;

    private Map<ShortcutType, Pair> map = new HashMap<>();

    public static void show() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddCommandController.class.getResource("/ShortcutFrame.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Shortcut");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(loader.load()));
            stage.show();

            ShortcutController controller = loader.getController();
            controller.init(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        map.put(ShortcutType.Command, new Pair(commandFunctionText, commandCodeText));
        map.put(ShortcutType.Field, new Pair(fieldFunctionText, fieldCodeText));
        map.put(ShortcutType.Search, new Pair(searchFunctionText, searchCodeText));
        map.put(ShortcutType.Install, new Pair(installFunctionText, installCodeText));
        map.put(ShortcutType.Deploy, new Pair(deployFunctionText, deployCodeText));

        for (Map.Entry<ShortcutType, Pair> entry : map.entrySet()) {
            Shortcut shortcut = ShortcutManager.getShortcut(entry.getKey());
            entry.getValue().functionText.setText(shortcut.getKeyType().name());
            entry.getValue().codeText.setText(shortcut.getCode().name());
        }
    }

    private void init(Stage stage) {
        this.stage = stage;
        this.stage.getScene().setOnKeyPressed(this::onKeyPress);
    }

    private void onKeyPress(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case ENTER:
                save();
                break;
            case ESCAPE:
                close();
                break;
            default:
                updateKey(keyEvent.getCode());
                break;
        }
    }

    private void updateKey(KeyCode keyCode) {
        for (Map.Entry<ShortcutType, Pair> entry : map.entrySet()) {
            Pair pair = entry.getValue();
            if (pair.functionText.isFocused() || pair.codeText.isFocused()) {
                FunctionKey functionKey = FunctionKey.search(keyCode);
                if (functionKey != null) {
                    pair.functionText.setText(functionKey.name());
                } else {
                    pair.codeText.setText(keyCode.name());
                }
                break;
            }
        }
    }

    @FXML
    private void save() {
        long distinctCount = map.values().stream().map(pair -> pair.functionText.getText() + "_" + pair.codeText.getText()).distinct().count();
        if (distinctCount < map.size()) {
            ConfirmController.show("快捷键重复，请修改");
            return;
        }

        for (Map.Entry<ShortcutType, Pair> entry : map.entrySet()) {
            Pair pair = entry.getValue();
            FunctionKey key = FunctionKey.valueOf(pair.functionText.getText());
            KeyCode code = KeyCode.valueOf(pair.codeText.getText());
            ShortcutManager.update(entry.getKey(), key, code);
        }
        close();
        ConfirmController.show("保存成功");
    }

    private void close() {
        stage.close();
    }


    private static class Pair {
        private TextField functionText;
        private TextField codeText;

        Pair(TextField functionText, TextField codeText) {
            this.functionText = functionText;
            this.codeText = codeText;
        }
    }
}
