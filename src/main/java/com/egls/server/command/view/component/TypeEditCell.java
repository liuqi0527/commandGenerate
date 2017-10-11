package com.egls.server.command.view.component;

import com.egls.server.command.CommandManager;
import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.CompoundFieldType;
import com.egls.server.command.model.type.FiledType;
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

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 表格的格子，带有下拉菜单编辑功能
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/12
 */
public class TypeEditCell extends TableCell<CommandFieldEntity, String> {

    private boolean isPrimaryType;

    private HBox hBox;
    private ChoiceBox<String> collectionBox;
    private ChoiceBox<String> typeBox;
    private ChoiceBox<String> itemBox;
    private Button switchBtn;
    private boolean isEdit;

    public TypeEditCell() {
        collectionBox = new ChoiceBox<>();
        collectionBox.setPrefWidth(55);
        collectionBox.setMinWidth(55);

        typeBox = new ChoiceBox<>();
        typeBox.setPrefWidth(65);
        typeBox.getSelectionModel().selectedItemProperty().addListener(this::commit);

        itemBox = new ChoiceBox<>();
        itemBox.setPrefWidth(65);
        itemBox.getSelectionModel().selectedItemProperty().addListener(this::commit);

        switchBtn = new Button();
        switchBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icon/switch.png"), 18, 20, false, false)));
        switchBtn.setStyle("-fx-background-color: transparent;");
        switchBtn.setOnAction(e -> switchBox());

        hBox = new HBox();
        hBox.getChildren().add(collectionBox);
        hBox.getChildren().add(typeBox);
        hBox.getChildren().add(itemBox);
        hBox.getChildren().add(switchBtn);

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (event.getCode() == KeyCode.ENTER) {
                commit(null, null, null);
            }
        });
    }

    private void commit(ObservableValue<?> observable, String oldValue, String newValue) {
        if (!isEdit) {
            return;
        }

        System.out.println(String.format("%s: %s   %s --> %s", LocalDateTime.now(), isEdit, oldValue, newValue));

        CommandFieldEntity fieldEntity = getFieldEntity();
        if (fieldEntity != null) {
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

            fieldEntity.setCollectionType(collectionType);
            fieldEntity.setType(type);
            commitEdit(fieldEntity.getFullTypeName());
            CommandManager.save();
            isEdit = false;
        } else {
            cancelEdit();
        }
    }

    private void init() {
        CommandFieldEntity fieldEntity = getFieldEntity();

        collectionBox.setItems(CollectionType.nameList);
        collectionBox.getSelectionModel().select(fieldEntity.getCollectionType().name());

        typeBox.setItems(FXCollections.observableArrayList(FiledType.FIELD_TYPES.keySet()));
        itemBox.setItems(FXCollections.observableArrayList(CommandManager.getInstance().itemList.stream().map(CommandObjectEntity::getName).collect(Collectors.toList())));

        if (fieldEntity.getFieldType() instanceof CompoundFieldType) {
            itemBox.getSelectionModel().select(fieldEntity.getType());
            typeBox.getSelectionModel().select(0);
            isPrimaryType = false;
        } else {
            typeBox.getSelectionModel().select(fieldEntity.getType());
            itemBox.getSelectionModel().select(0);
            isPrimaryType = true;
        }
        updateUI();
    }

    private void switchBox() {
        isPrimaryType = !isPrimaryType;

        isEdit = false;
        updateUI();
        isEdit = true;
    }

    private void updateUI() {
        ChoiceBox<String> removeChild = isPrimaryType ? itemBox : typeBox;
        ChoiceBox<String> addChild = isPrimaryType ? typeBox : itemBox;

        hBox.getChildren().remove(removeChild);
        if (!hBox.getChildren().contains(addChild)) {
            hBox.getChildren().add(1, isPrimaryType ? typeBox : itemBox);
            CommandFieldEntity fieldEntity = getFieldEntity();
            if (addChild.getItems().contains(fieldEntity.getType())) {
                addChild.getSelectionModel().select(fieldEntity.getType());
            } else {
                addChild.getSelectionModel().clearSelection();
            }
        }
//        typeBox.setVisible(isPrimaryType);
//        itemBox.setVisible(!isPrimaryType);
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
        isEdit = true;
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        isEdit = false;
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
