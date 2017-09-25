package com.egls.server.command.view.component;

import com.egls.server.command.CommandManager;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;

import java.util.Collection;

/**
 * 表格的格子，带有下拉菜单编辑功能
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/12
 */
public class TypeEditCell<S> extends TableCell<S, String> {

    private ChoiceBox<String> typeBox;

    public TypeEditCell(Collection<String> list) {
        typeBox = new ChoiceBox<>();
        typeBox.setItems(FXCollections.observableArrayList(list));
        typeBox.setOnAction(e -> {
            commitEdit(typeBox.getValue());
            CommandManager.save();
        });
        typeBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();

        setGraphic(typeBox);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        typeBox.getSelectionModel().select(getString());
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
            setContentDisplay(ContentDisplay.TEXT_ONLY);

        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }
}
