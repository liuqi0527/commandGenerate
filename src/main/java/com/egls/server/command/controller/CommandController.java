package com.egls.server.command.controller;

import com.egls.server.command.CommandManager;
import com.egls.server.command.Constant;
import com.egls.server.command.FileOperationType;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.CommandObjectFiledType;
import com.egls.server.command.view.component.ButtonListCell;
import com.egls.server.command.view.component.TextEditCell;
import com.egls.server.command.view.component.TypeEditCell;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/12
 */
public class CommandController {

    @FXML
    private TableView<CommandObjectEntity> messageTable;
    @FXML
    private TableColumn<CommandObjectEntity, String> messageIdColumn;
    @FXML
    private TableColumn<CommandObjectEntity, String> messageNameColumn;
    @FXML
    private TableColumn<CommandObjectEntity, String> messageDesColumn;
    @FXML
    private TableColumn<CommandObjectEntity, Boolean> messageEditColumn;

    @FXML
    private TableView<CommandFieldEntity> detailTable;
    @FXML
    private TableColumn<CommandFieldEntity, String> detailTypeColumn;
    @FXML
    private TableColumn<CommandFieldEntity, String> detailNameColumn;
    @FXML
    private TableColumn<CommandFieldEntity, String> detailDesColumn;
    @FXML
    private TableColumn<CommandFieldEntity, Boolean> detailEditColumn;

    @FXML
    private TextField groupField;
    @FXML
    private TextField artifactField;
    @FXML
    private TextField versionField;
    @FXML
    private TextField packageField;

    private CommandObjectEntity selectMsg;

    @FXML
    private void initialize() {
        //fxml文件完成载入时被自动调用. 所有的FXML属性都已被初始化.

        //初始化个人设置
        initTextField(groupField, CommandManager::getGroupId, CommandManager::setGroupId);
        initTextField(artifactField, CommandManager::getArtifactId, CommandManager::setArtifactId);
        initTextField(versionField, CommandManager::getVersion, CommandManager::setVersion);
        initTextField(packageField, CommandManager::getPackageName, CommandManager::setPackageName);

        //初始化消息表
        messageTable.setItems(CommandManager.getInstance().commandList);
        messageTable.getSelectionModel().selectedItemProperty().addListener(this::messageSelect);

        messageIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        messageIdColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> Constant.isCommandValid(v)));

        messageNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        messageNameColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> StringUtils.equals(m.getName(), v) || Constant.isClassNameValid(v)));

        messageDesColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());
        messageDesColumn.setCellFactory(column -> new TextEditCell<>());

        messageEditColumn.setCellFactory(column -> new ButtonListCell<CommandObjectEntity>(Triple.of("", "/icon/icon_del1.png", this::messageDelete)));


        //初始化消息内容表
        detailTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        detailTypeColumn.setCellFactory(column -> new TypeEditCell<>(CommandObjectFiledType.FIELD_TYPES.keySet()));

        detailNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        detailNameColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> selectMsg != null && selectMsg.isFieldNameValid(m, v)));

        detailDesColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());
        detailDesColumn.setCellFactory(column -> new TextEditCell<>());

        detailEditColumn.setCellFactory(column -> new ButtonListCell<CommandFieldEntity>(Arrays.asList(
                Triple.of("", "/icon/arrow_triangle-up.png", this::fieldUp),
                Triple.of("", "/icon/arrow_triangle-down.png", this::fieldDown),
                Triple.of("", "/icon/icon_del1.png", this::fieldDelete)
        )));
    }

    private void initTextField(TextField textField, Supplier<String> textSupplier, Consumer<String> textConsumer) {
        textField.setText(textSupplier.get());
        textField.textProperty().addListener(e -> textConsumer.accept(textField.getText()));
    }

    private void fieldUp(int rowIndex) {
        if (selectMsg != null && selectMsg.getFields().size() > rowIndex) {
            CommandFieldEntity filed = selectMsg.getFields().remove(rowIndex);
            int insertIndex = Math.max(0, rowIndex - 1);
            selectMsg.getFields().add(insertIndex, filed);
            detailTable.getSelectionModel().select(insertIndex);
            CommandManager.save();
        }
    }

    private void fieldDown(int rowIndex) {
        if (selectMsg != null && selectMsg.getFields().size() > rowIndex) {
            CommandFieldEntity filed = selectMsg.getFields().remove(rowIndex);
            int insertIndex = Math.min(selectMsg.getFields().size(), rowIndex + 1);
            selectMsg.getFields().add(insertIndex, filed);
            detailTable.getSelectionModel().select(insertIndex);
            CommandManager.save();
        }
    }

    private void fieldDelete(int rowIndex) {
        if (selectMsg != null && selectMsg.getFields().size() > rowIndex) {
            selectMsg.getFields().remove(rowIndex);
            CommandManager.save();
        }
    }

    private void messageDelete(int rowIndex) {
        CommandObjectEntity message = messageTable.getItems().get(rowIndex);
        if (message != null) {
            ConfirmController.show(String.format("确认删除消息：%s -- %s ？", message.getId(), message.getName()), () -> {
                messageTable.getItems().remove(message);
                CommandManager.removeCommand(message);
            });
        }
    }

    private void messageSelect(ObservableValue<?> observable, CommandObjectEntity oldValue, CommandObjectEntity newValue) {
        selectMsg = newValue;
        detailTable.setItems(newValue != null ? newValue.getFields() : null);
    }

    public CommandObjectEntity getSelectMsg() {
        return selectMsg;
    }

    public void addMessageToTable(CommandObjectEntity message) {
        messageTable.getItems().add(message);
        messageTable.getSelectionModel().select(message);
        showCreateFieldUI();
    }

    @FXML
    private void installJar() {
        GenerateController.showAndGenerate(GenerateController.INSTALL);
    }

    @FXML
    private void deployJar() {
        GenerateController.showAndGenerate(GenerateController.DEPLOY);
    }

    @FXML
    private void saveAs() {
        MainApplication.showConfigChooseUI(FileOperationType.save);
    }

    @FXML
    private void createConfigFile() {
        MainApplication.showConfigChooseUI(FileOperationType.create);
    }

    @FXML
    public void openConfigFile() {
        MainApplication.showConfigChooseUI(FileOperationType.open);
    }

    @FXML
    private void showCreateMsgUI() {
        AddCommandController.show(this);
    }

    @FXML
    private void showCreateFieldUI() {
        if (selectMsg == null) {
            ConfirmController.show("请先选择一个指令进行编辑");
            return;
        }
        AddFieldController.show(this);
    }

    @FXML
    private void showHelpUI() {
        try {
            Parent root = FXMLLoader.load(MainApplication.class.getResource("/HelpFrame.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Help");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
