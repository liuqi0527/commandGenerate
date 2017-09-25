package com.egls.server.command.view.component;

import com.egls.server.command.CommandManager;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.function.BiPredicate;

/**
 * 表格的格子，带有文本编辑功能
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/12
 */
public class TextEditCell<S> extends TableCell<S, String> {

    private TextField textField;
    private BiPredicate<S, String> validTest;

    public TextEditCell() {
    }

    public TextEditCell(BiPredicate<S, String> validTest) {
        this.validTest = validTest;
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        textField.setText(getString());

        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.positionCaret(0);
        textField.selectNextWord();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(String.valueOf(getItem()));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    private void createTextField() {
        textField = new TextField();
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (validTest == null || validTest.test(getRowItem(), textField.getText())) {
                    commitEdit(textField.getText());
                    CommandManager.save();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private S getRowItem() {
        return getTableView().getItems().get(getTableRow().getIndex());
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }
}
