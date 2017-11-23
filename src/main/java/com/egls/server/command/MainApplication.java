package com.egls.server.command;

import com.egls.server.utils.file.FileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainApplication extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        LocalProperties.init();
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
                if (CommandManager.load(file)) {
                    showMainUI();
                }
                break;
            case open:
                if (CommandManager.load(file)) {
                    showMainUI();
                }
                break;
            case save:
                LocalProperties.setConfigPath(file.getAbsolutePath());
                CommandManager.save();
                break;
        }
    }

    public static void showMainUI() {
        try {
            Parent root = FXMLLoader.load(MainApplication.class.getResource("/CommandFrame.fxml"));
            primaryStage.setTitle("Command Edit");
            primaryStage.setScene(new Scene(root, 1435, 812));
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
