package com.egls.server.command.view.component;

import com.egls.server.command.CommandManager;
import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.CompoundFieldType;
import com.egls.server.command.model.type.FieldType;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

/**
 * 表格的格子，带有下拉菜单编辑功能
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/12
 */
public class TypeEditCell extends TableCell<CommandFieldEntity, String> {

    private boolean isPrimaryType;
    private boolean isMap;

    private HBox hBox;
    private ChoiceBox<String> collectionBox;
    private ChoiceBox<String> keyBox;
    private ChoiceBox<String> typeBox;
    private ChoiceBox<String> itemBox;
    private Button switchBtn;
    private Button confirmBtn;

    public TypeEditCell() {
        collectionBox = new ChoiceBox<>();
        collectionBox.setPrefWidth(55);
        collectionBox.setMinWidth(55);
        collectionBox.getSelectionModel().selectedItemProperty().addListener(this::collectionTypeChange);

        keyBox = new ChoiceBox<>();
        keyBox.setPrefWidth(65);

        typeBox = new ChoiceBox<>();
        typeBox.setPrefWidth(65);

        itemBox = new ChoiceBox<>();
        itemBox.setPrefWidth(65);

        switchBtn = new Button();
        switchBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icon/switch.png"), 17, 19, false, false)));
        switchBtn.setStyle("-fx-background-color: transparent;");
        switchBtn.setOnAction(e -> switchBox());

        confirmBtn = new Button();
        confirmBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icon/icon_confirm.png"), 17, 19, false, false)));
        confirmBtn.setStyle("-fx-background-color: transparent;");
        confirmBtn.setOnAction(e -> commit());

        hBox = new HBox();
        hBox.getChildren().add(collectionBox);
        hBox.getChildren().add(typeBox);
        hBox.getChildren().add(itemBox);
        hBox.getChildren().add(switchBtn);
        hBox.getChildren().add(confirmBtn);

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (event.getCode() == KeyCode.ENTER) {
                commit();
            }
        });
    }

    private void collectionTypeChange(ObservableValue<?> observable, String oldValue, String newValue) {
        boolean oldIsMap = isMapType(oldValue);
        boolean newIsMap = isMapType(newValue);
        if (oldIsMap == newIsMap) {
            return;
        }

        isMap = newIsMap;
        if (isMap) {
            hBox.getChildren().add(1, keyBox);
        } else {
            hBox.getChildren().remove(keyBox);
        }
        updateUI();
    }

    private boolean isMapType(String type) {
        return StringUtils.equals(type, CollectionType.Map.name());
    }

    private void commit() {
        CommandFieldEntity fieldEntity = getFieldEntity();
        if (fieldEntity != null && (isMap ? editMapField(fieldEntity) : editSimpleField(fieldEntity))) {
            commitEdit(fieldEntity.getFullTypeName());
            CommandManager.save();
        } else {
            cancelEdit();
        }
    }


    private boolean editMapField(CommandFieldEntity fieldEntity) {
        CollectionType collectionType = CollectionType.getType(collectionBox.getValue());
        String keyType = keyBox.getValue();
        String valueType = isPrimaryType ? typeBox.getValue() : itemBox.getValue();
        if (StringUtils.isBlank(valueType)) {
            ConfirmController.show("请选择Value的类型");
            return false;
        }
        if (collectionType != CollectionType.none && StringUtils.equals("bytes", valueType)) {
            ConfirmController.show("bytes类型无法作为集合元素使用");
            return false;
        }

        fieldEntity.setCollectionType(collectionType);
        fieldEntity.addFieldType(keyType, true);
        fieldEntity.addFieldType(valueType, false);
        return true;
    }

    private boolean editSimpleField(CommandFieldEntity fieldEntity) {
        CollectionType collectionType = CollectionType.getType(collectionBox.getValue());
        String type = isPrimaryType ? typeBox.getValue() : itemBox.getValue();
        if (StringUtils.isBlank(type)) {
            ConfirmController.show("请选择属性类型");
            return false;
        }
        if (collectionType != CollectionType.none && StringUtils.equals("bytes", type)) {
            ConfirmController.show("bytes类型无法作为集合元素使用");
            return false;
        }

        fieldEntity.setCollectionType(collectionType);
        fieldEntity.addFieldType(type, true);
        return true;
    }

    private void init() {
        CommandFieldEntity fieldEntity = getFieldEntity();

        collectionBox.setItems(CollectionType.nameList);
        collectionBox.getSelectionModel().select(fieldEntity.getCollectionType().name());

        keyBox.setItems(FXCollections.observableArrayList(FieldType.SIMPLE_FIELD_TYPES.keySet()));
        typeBox.setItems(FXCollections.observableArrayList(FieldType.ALL_FIELD_TYPES.keySet()));
        itemBox.setItems(FXCollections.observableArrayList(CommandManager.getInstance().itemList.stream().map(CommandObjectEntity::getName).collect(Collectors.toList())));

        if (fieldEntity.getCollectionType() == CollectionType.Map) {
            keyBox.getSelectionModel().select(fieldEntity.getFieldTypes().get(0).getName());
            initValueBox(fieldEntity.getFieldTypes().get(1));
            isMap = true;
        } else {
            keyBox.getSelectionModel().select(0);
            initValueBox(fieldEntity.getSingleFieldType());
            isMap = false;
        }

        updateUI();
    }

    private void initValueBox(FieldType type) {
        if (type instanceof CompoundFieldType) {
            itemBox.getSelectionModel().select(type.getName());
            typeBox.getSelectionModel().select(0);
            isPrimaryType = false;
        } else {
            typeBox.getSelectionModel().select(type.getName());
            itemBox.getSelectionModel().select(0);
            isPrimaryType = true;
        }
    }

    private void switchBox() {
        isPrimaryType = !isPrimaryType;
        updateUI();
    }

    private void updateUI() {
        hBox.getChildren().clear();
        hBox.getChildren().add(collectionBox);
        if (isMap) {
            hBox.getChildren().add(keyBox);
        }
        hBox.getChildren().add(isPrimaryType ? typeBox : itemBox);
        hBox.getChildren().add(switchBtn);
        hBox.getChildren().add(confirmBtn);

        CommandFieldEntity fieldEntity = getFieldEntity();

//
//        hBox.getChildren().remove(removeChild);
//        if (!hBox.getChildren().contains(addChild)) {
//            hBox.getChildren().add(1, isPrimaryType ? typeBox : itemBox);
//            if (addChild.getItems().contains(fieldEntity.getType())) {
//                addChild.getSelectionModel().select(fieldEntity.getType());
//            } else {
//                addChild.getSelectionModel().clearSelection();
//            }
//        }
////        typeBox.setVisible(isPrimaryType);
////        itemBox.setVisible(!isPrimaryType);
    }

    private CommandFieldEntity getFieldEntity() {
        return getTableView().getItems().get(getTableRow().getIndex());
    }

    @Override
    public void startEdit() {
        init();
        super.startEdit();


        setGraphic(hBox);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else if (!isEditing()) {
            setText(getString());
            setGraphic(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);

        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }
}
