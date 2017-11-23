package com.egls.server.command.view.component;

import com.egls.server.command.CommandManager;
import com.egls.server.command.model.CommandObjectEntity;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


/**
 * 表格的格子，带有一系列功能按钮
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
public class CheckBoxCell extends TableCell<CommandObjectEntity, Boolean> {

    private Pane stackPane;
    private CheckBox checkBox;

    public CheckBoxCell() {
        checkBox = new CheckBox();
        checkBox.setOnAction(this::onSelect);


        stackPane = new StackPane();
        stackPane.getChildren().add(checkBox);
    }

    private void onSelect(ActionEvent event) {
        getCommandObject().setUiCommand(checkBox.isSelected());
        CommandManager.save();
    }

    private CommandObjectEntity getCommandObject() {
        return getTableView().getItems().get(getTableRow().getIndex());
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            CommandObjectEntity commandObject = getCommandObject();
            checkBox.setSelected(commandObject != null && commandObject.isUiCommand());
            setGraphic(stackPane);
        } else {
            setGraphic(null);
        }
    }
}
