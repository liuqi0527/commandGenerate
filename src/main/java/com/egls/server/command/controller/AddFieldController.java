package com.egls.server.command.controller;

import com.egls.server.command.CommandManager;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.FiledType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
public class AddFieldController {

    @FXML
    private ChoiceBox<String> collectionBox;

    @FXML
    private Pane typePanel;

    @FXML
    private ChoiceBox<String> typeBox;

    @FXML
    private Pane itemPanel;

    @FXML
    private ChoiceBox<String> itemBox;

    @FXML
    private TextField nameField;

    @FXML
    private TextField desField;

    private boolean isPrimaryType = true;

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

        collectionBox.setItems(CollectionType.nameList);
        collectionBox.getSelectionModel().select(0);

        typeBox.setItems(FXCollections.observableArrayList(FiledType.FIELD_TYPES.keySet()));
        typeBox.getSelectionModel().select(0);

        itemBox.setItems(FXCollections.observableArrayList(CommandManager.getInstance().itemList.stream().map(CommandObjectEntity::getName).collect(Collectors.toList())));
        itemBox.getSelectionModel().select(0);

        updateUI();
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

        CollectionType collectionType = CollectionType.getType(collectionBox.getValue());
        String type = isPrimaryType ? typeBox.getValue() : itemBox.getValue();
        if (StringUtils.isBlank(type)) {
            ConfirmController.show("请选择属性类型");
            return;
        }
        if (collectionType != CollectionType.none && StringUtils.equals("bytes", type)) {
            ConfirmController.show("该类型无法作为集合元素使用");
            return;
        }

        CommandFieldEntity field = new CommandFieldEntity(collectionType, type, name, desField.getText());
        message.getFields().add(field);
        CommandManager.save();

        nameField.setText(null);
        desField.setText(null);
    }

    @FXML
    private void switchType() {
        isPrimaryType = !isPrimaryType;
        updateUI();
    }

    private void updateUI() {
        typePanel.setVisible(isPrimaryType);
        itemPanel.setVisible(!isPrimaryType);
    }
}
