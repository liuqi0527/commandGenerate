package com.egls.server.command.controller;

import com.egls.server.command.CommandManager;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.CommandObjectFiledType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
public class AddFieldController {

    @FXML
    private ChoiceBox<String> typeBox;

    @FXML
    private TextField nameField;

    @FXML
    private TextField desField;

    private Stage stage;
    private CommandController messageViewController;

    public static void show(CommandController messageViewController) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddFieldController.class.getResource("/AddFieldFrame.fxml"));

            Stage stage = new Stage();
            stage.setTitle("new Field");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(loader.load()));

            AddFieldController controller = loader.getController();
            controller.stage = stage;
            controller.messageViewController = messageViewController;

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        //fxml文件完成载入时被自动调用. 所有的FXML属性都已被初始化.

        typeBox.setItems(FXCollections.observableArrayList(CommandObjectFiledType.FIELD_TYPES.keySet()));
        typeBox.getSelectionModel().select(0);
    }

    @FXML
    private void create() {
        CommandObjectEntity message = messageViewController.getSelectMsg();
        if (message == null) {
            ConfirmController.show("请选择一个指令进行编辑");
            stage.close();
            return;
        }

        String name = nameField.getText();
        if (!message.isFieldNameValid(null, name)) {
            return;
        }

        CommandFieldEntity field = new CommandFieldEntity(typeBox.getValue(), name, desField.getText());
        message.getFields().add(field);
        CommandManager.save();

        nameField.setText(null);
        desField.setText(null);
    }
}
