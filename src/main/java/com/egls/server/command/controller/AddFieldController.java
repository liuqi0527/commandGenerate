package com.egls.server.command.controller;

import java.util.stream.Collectors;

import com.egls.server.command.CommandManager;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.FieldType;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
public class AddFieldController {

    @FXML
    private ChoiceBox<String> collectionBox;

    @FXML
    private Pane typePanel;
    @FXML
    private ChoiceBox<String> typeBox;
    @FXML
    private ChoiceBox<String> itemBox;


    @FXML
    private Pane mapPanel;
    @FXML
    private ChoiceBox<String> keyBox;
    @FXML
    private ChoiceBox<String> valueTypeBox;
    @FXML
    private ChoiceBox<String> valueItemBox;


    @FXML
    private Button switchBtn;


    @FXML
    private TextField nameField;
    @FXML
    private TextField desField;

    private boolean isMapType;
    private boolean isPrimaryType = true;
    private boolean isPrimaryValueType = true;

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
            stage.show();

            AddFieldController controller = loader.getController();
            controller.init(stage, messageViewController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        //fxml文件完成载入时被自动调用. 所有的FXML属性都已被初始化.

        collectionBox.setItems(CollectionType.nameList);
        collectionBox.getSelectionModel().select(0);
        collectionBox.getSelectionModel().selectedItemProperty().addListener(this::onCollectionChange);

        initBox(typeBox, 1);
        initBox(itemBox, 3);
        initBox(keyBox, 2);
        initBox(valueTypeBox, 2);
        initBox(valueItemBox, 3);

        updateTypeBox();
        updateValueBox();
    }

    private void init(Stage stage, CommandController commandController) {
        this.stage = stage;
        this.messageViewController = commandController;

        CommandObjectEntity message = messageViewController.getSelectMsg();
        boolean isUICommand = message != null && message.isUiCommand();
        collectionBox.setDisable(isUICommand);
        switchBtn.setDisable(isUICommand);
        nameField.requestFocus();

        stage.getScene().setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            } else if (event.getCode() == KeyCode.ENTER) {
                create();
            }
        });
    }

    private void initBox(ChoiceBox<String> box, int type) {
        switch (type) {
            case 1:
                box.setItems(FXCollections.observableArrayList(FieldType.ALL_FIELD_TYPES.keySet()));
                break;
            case 2:
                box.setItems(FXCollections.observableArrayList(FieldType.SIMPLE_FIELD_TYPES.keySet()));
                break;
            case 3:
                box.setItems(FXCollections.observableArrayList(CommandManager.getInstance().itemList.stream().map(CommandObjectEntity::getName).collect(Collectors.toList())));
                break;
        }
        box.getSelectionModel().select(0);
    }

    @FXML
    private void create() {
        CommandObjectEntity message = messageViewController.getSelectMsg();
        if (message == null) {
            ConfirmController.show("请选择一个指令进行编辑");
            stage.close();
            return;
        }

        if (message.isUiCommand() && (CollectionType.getType(collectionBox.getValue()) != CollectionType.none ||
                !isPrimaryType || StringUtils.equals(typeBox.getValue(), "bytes"))) {
            ConfirmController.show("UI类型消息，只能使用基础类型");
            return;
        }


        if (!message.isFieldNameValid(null, nameField.getText())) {
            return;
        }

        if (isMapType ? addMapField() : addSimpleField()) {
            desField.setText(null);
            nameField.setText(null);
            nameField.requestFocus();
        }
    }

    private boolean addMapField() {
        CollectionType collectionType = getCollectionType();
        String keyType = keyBox.getValue();
        String valueType = isPrimaryValueType ? valueTypeBox.getValue() : valueItemBox.getValue();
        if (StringUtils.isBlank(valueType)) {
            ConfirmController.show("请选择Value的类型");
            return false;
        }

        CommandFieldEntity field = new CommandFieldEntity(collectionType, nameField.getText(), desField.getText());
        field.addFieldType(keyType, true);
        field.addFieldType(valueType, false);
        messageViewController.getSelectMsg().getFields().add(field);
        CommandManager.save();
        return true;
    }

    private boolean addSimpleField() {
        CollectionType collectionType = getCollectionType();
        String type = isPrimaryType ? typeBox.getValue() : itemBox.getValue();
        if (StringUtils.isBlank(type)) {
            ConfirmController.show("请选择属性类型");
            return false;
        }
        if (collectionType != CollectionType.none && StringUtils.equals("bytes", type)) {
            ConfirmController.show("该类型无法作为集合元素使用");
            return false;
        }

        CommandFieldEntity field = new CommandFieldEntity(collectionType, nameField.getText(), desField.getText());
        field.addFieldType(type, true);
        messageViewController.getSelectMsg().getFields().add(field);
        CommandManager.save();
        return true;
    }

    private CollectionType getCollectionType() {
        return CollectionType.getType(collectionBox.getValue());
    }

    //
    @FXML
    private void switchType() {
        isPrimaryType = !isPrimaryType;
        updateTypeBox();
    }

    @FXML
    private void switchMapValue() {
        isPrimaryValueType = !isPrimaryValueType;
        updateValueBox();
    }

    private void updateTypeBox() {
        typeBox.setVisible(isPrimaryType);
        itemBox.setVisible(!isPrimaryType);
    }

    private void updateValueBox() {
        valueTypeBox.setVisible(isPrimaryValueType);
        valueItemBox.setVisible(!isPrimaryValueType);
    }

    private void onCollectionChange(ObservableValue<?> observable, String oldValue, String newValue) {
        boolean oldIsMap = isMap(oldValue);
        boolean newIsMap = isMap(newValue);
        if (oldIsMap == newIsMap) {
            return;
        }

        typePanel.setVisible(!newIsMap);
        mapPanel.setVisible(newIsMap);
        isMapType = newIsMap;
    }

    private boolean isMap(String type) {
        return StringUtils.equals(type, CollectionType.Map.name());
    }
}
