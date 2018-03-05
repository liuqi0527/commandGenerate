package com.egls.server.command;

import java.io.File;

import com.egls.server.command.controller.AddCommandController;
import com.egls.server.command.controller.CommandController;
import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.shortcut.ShortcutManager;
import com.egls.server.utils.file.FileUtil;
import com.egls.server.utils.resource.file.ResourceWatcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static CommandController primaryController;

    public static void main(String[] args) {
        ResourceWatcher.startup();
        LocalProperties.init();
        ShortcutManager.load();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApplication.primaryStage = primaryStage;

        File file = new File(LocalProperties.getConfigPath());
        if (file.isFile()) {
            CommandManager.load(file);
        }

        showMainUI();

        if (file.isDirectory() || !file.exists()) {
            showConfigChooseUI(FileOperationType.open);
        }
    }

    public static void showConfigChooseUI(FileOperationType type) {
        File configFile = new File(LocalProperties.getConfigPath());
        File configDir;
        if (configFile.exists()) {
            configDir = configFile.isFile() ? configFile.getParentFile() : configFile;
        } else {
            configDir = new File(System.getProperty("user.dir"));
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(type.getTitle());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        fileChooser.setInitialDirectory(configDir);

        File file;
        if (type != FileOperationType.open) {
            fileChooser.setInitialFileName("command");
            file = fileChooser.showSaveDialog(MainApplication.getPrimaryStage());
        } else {
            file = fileChooser.showOpenDialog(MainApplication.getPrimaryStage());
        }

        if (file == null || file.isDirectory()) {
            return;
        }

        switch (type) {
            case create:
                try {
                    FileUtil.createFileOnNoExists(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadAndShow(file);
                break;
            case open:
                loadAndShow(file);
                break;
            case save:
                LocalProperties.setConfigPath(file.getAbsolutePath());
                CommandManager.save();
                break;
        }
    }

    public static void loadAndShow(File file) {
        if (CommandManager.load(file)) {
            LocalProperties.setConfigPath(file.getAbsolutePath());
            showMainUI();
        }
    }

    public static void showMainUI() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddCommandController.class.getResource("/CommandFrame.fxml"));

            primaryStage.setTitle("Command Edit");
            primaryStage.setScene(new Scene(loader.load(), getValue(primaryStage.getWidth(), 1435), getValue(primaryStage.getHeight(), 812)));
            primaryStage.setMaximized(true);
            primaryStage.getScene().setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    ConfirmController.show("确定退出程序？", () -> System.exit(0));
                } else {
                    ShortcutManager.onGlobalKeyPress(event);
                }
            });
            primaryStage.show();
            primaryController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static double getValue(double value, double defaultValue) {
        return Double.isNaN(value) ? defaultValue : primaryStage.getWidth();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static CommandController getPrimaryController() {
        return primaryController;
    }
}
