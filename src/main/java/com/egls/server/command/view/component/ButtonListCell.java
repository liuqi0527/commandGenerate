package com.egls.server.command.view.component;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


/**
 * 表格的格子，带有一系列功能按钮
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
public class ButtonListCell<S> extends TableCell<S, Boolean> {

    private HBox box;

    public ButtonListCell(Triple<String, String, Consumer<Integer>> triple) {
        this(Collections.singletonList(triple));
    }

    public ButtonListCell(List<Triple<String, String, Consumer<Integer>>> btnList) {
        box = new HBox();
        for (Triple<String, String, Consumer<Integer>> triple : btnList) {
            Button button = new Button(triple.getLeft());
            if (StringUtils.isNotBlank(triple.getMiddle())) {
                Image imageDecline = new Image(getClass().getResourceAsStream(triple.getMiddle()), 18, 20, false, false);
                button.setGraphic(new ImageView(imageDecline));
                button.setStyle("-fx-background-color: transparent;");
            }
            button.setOnAction(event -> triple.getRight().accept(getTableRow().getIndex()));
            box.getChildren().add(button);
        }
    }

    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setGraphic(box);
        } else {
            setGraphic(null);
        }
    }
}
