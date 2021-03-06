package com.egls.server.command.controller;

import com.egls.server.command.CommandManager;
import com.egls.server.command.Constant;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CommandType;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;


/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
public class AddCommandController {

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField desField;

    @FXML
    private Pane commandPanel;

    @FXML
    private CheckBox uiCommandCheckBox;

    private CommandController messageViewController;
    private Stage stage;
    private CommandType type;

    @FXML
    private void initialize() {
        //fxml文件完成载入时被自动调用. 所有的FXML属性都已被初始化.
    }

    public static void show(CommandController messageViewController, CommandType type) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddCommandController.class.getResource("/AddCommandFrame.fxml"));

            Stage stage = new Stage();
            stage.setTitle("new Command");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(loader.load()));
            stage.show();

            AddCommandController newMessageFrame = loader.getController();
            newMessageFrame.init(stage, type, messageViewController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(Stage stage, CommandType commandType, CommandController messageViewController) {
        this.stage = stage;
        this.type = commandType;
        this.messageViewController = messageViewController;
        this.commandPanel.setVisible(type.isCommand());
        this.stage.getScene().setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            } else if (event.getCode() == KeyCode.ENTER) {
                create();
            }
        });


        if (messageViewController.getSelectMsg() != null) {
            int defaultCommandId = messageViewController.getSelectMsg().getIntId() + 1;
            while (CommandManager.containsId(defaultCommandId)) {
                defaultCommandId += 1;
            }

            String defaultCommand = "0x" + StringUtils.upperCase(StringUtils.leftPad(Integer.toHexString(defaultCommandId), 4, '0'));
            idField.setText(defaultCommand);
            nameField.requestFocus();
        }
    }

    @FXML
    private void create() {
        String id = idField.getText();
        if (type.isCommand() && !Constant.isCommandValid(id)) {
            return;
        }

        String name = nameField.getText();
        if (!Constant.isClassNameValid(name)) {
            return;
        }

        CommandObjectEntity message = new CommandObjectEntity(type, name, desField.getText());
        if (type.isCommand()) {
            message.setId(id);
            message.setUiCommand(uiCommandCheckBox.isSelected());
        }
        messageViewController.addMessageToTable(message);
        CommandManager.addCommand(message);

        stage.close();
    }
}
