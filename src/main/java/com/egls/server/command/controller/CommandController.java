package com.egls.server.command.controller;

import com.egls.server.command.CommandManager;
import com.egls.server.command.Constant;
import com.egls.server.command.FileOperationType;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandFieldEntity;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CommandType;
import com.egls.server.command.view.component.ButtonListCell;
import com.egls.server.command.view.component.TextEditCell;
import com.egls.server.command.view.component.TypeEditCell;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private TableView<CommandObjectEntity> itemTable;
    @FXML
    private TableColumn<CommandObjectEntity, String> itemNameColumn;
    @FXML
    private TableColumn<CommandObjectEntity, String> itemDesColumn;
    @FXML
    private TableColumn<CommandObjectEntity, Boolean> itemEditColumn;

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

    @FXML
    private TextField searchField;

    @FXML
    private Button createCommandBtn;

    @FXML
    private TabPane tabPanel;

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
        messageTable.sortPolicyProperty().set(table -> {
            FXCollections.sort(table.getItems(), Comparator.comparingInt(CommandObjectEntity::getIntId).thenComparing(CommandObjectEntity::getName));
            return true;
        });

        messageIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        messageIdColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> Constant.isCommandValid(v)));

        messageNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        messageNameColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> StringUtils.equals(m.getName(), v) || Constant.isClassNameValid(v)));

        messageDesColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());
        messageDesColumn.setCellFactory(column -> new TextEditCell<>());

        messageEditColumn.setCellFactory(column -> new ButtonListCell<CommandObjectEntity>(Triple.of("", "/icon/icon_del1.png", index -> this.commandDelete(index, messageTable))));

        //
        itemTable.setItems(CommandManager.getInstance().itemList);
        itemTable.getSelectionModel().selectedItemProperty().addListener(this::messageSelect);
        itemTable.sortPolicyProperty().set(table -> {
            FXCollections.sort(table.getItems(), Comparator.comparing(CommandObjectEntity::getName));
            return true;
        });

        itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        itemNameColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> StringUtils.equals(m.getName(), v) || Constant.isClassNameValid(v)));

        itemDesColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());
        itemDesColumn.setCellFactory(column -> new TextEditCell<>());

        itemEditColumn.setCellFactory(column -> new ButtonListCell<CommandObjectEntity>(Triple.of("", "/icon/icon_del1.png", index -> this.commandDelete(index, itemTable))));

        //初始化消息内容表
        detailTypeColumn.setCellValueFactory(cellData -> cellData.getValue().fullTypeProperty());
        detailTypeColumn.setCellFactory(column -> new TypeEditCell());

        detailNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        detailNameColumn.setCellFactory(column -> new TextEditCell<>((m, v) -> selectMsg != null && selectMsg.isFieldNameValid(m, v)));

        detailDesColumn.setCellValueFactory(cellData -> cellData.getValue().commentProperty());
        detailDesColumn.setCellFactory(column -> new TextEditCell<>());

        detailEditColumn.setCellFactory(column -> new ButtonListCell<CommandFieldEntity>(Arrays.asList(
                Triple.of("", "/icon/arrow_triangle-up.png", this::fieldUp),
                Triple.of("", "/icon/arrow_triangle-down.png", this::fieldDown),
                Triple.of("", "/icon/icon_del1.png", this::fieldDelete)
        )));


        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchCommand();
            }
        });

        tabPanel.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> tabSelectChange());
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

    private void commandDelete(int rowIndex, TableView<CommandObjectEntity> tableView) {
        CommandObjectEntity command = tableView.getItems().get(rowIndex);
        if (command == null) {
            return;
        }

        if (command.getType().isItem()) {
            String commandName = getUseItemCommand(CommandManager.getInstance().commandList, command.getName());
            if (StringUtils.isBlank(commandName)) {
                getUseItemCommand(CommandManager.getInstance().itemList, command.getName());
            }
            if (StringUtils.isNotBlank(commandName)) {
                ConfirmController.show(String.format("该消息被 %s 引用，无法删除", commandName));
                return;
            }
        }

        ConfirmController.show(String.format("确认删除消息：%s   %s ？", command.getId(), command.getName()), () -> {
            tableView.getItems().remove(command);
            CommandManager.removeCommand(command);
        });
    }

    private String getUseItemCommand(Collection<CommandObjectEntity> list, String itemName) {
        return list.stream()
                .filter(command -> command.getFields().stream()
                        .anyMatch(field -> field.getFieldTypes().stream()
                                .anyMatch(type -> StringUtils.equals(itemName, type.getName()))))
                .map(CommandObjectEntity::getName)
                .findFirst()
                .orElse(null);
    }

    private void tabSelectChange() {
        if (tabPanel.getSelectionModel().getSelectedIndex() == 0) {
            messageSelect(null, null, messageTable.getSelectionModel().getSelectedItem());
            createCommandBtn.setText("+ 指令");
        } else {
            messageSelect(null, null, itemTable.getSelectionModel().getSelectedItem());
            createCommandBtn.setText("+ 结构体");
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
        TableView<CommandObjectEntity> table = message.getType().isCommand() ? messageTable : itemTable;
        table.getItems().add(message);
        table.sort();
        table.getSelectionModel().select(message);
        showCreateFieldUI();
    }

    @FXML
    private void searchCommand() {
        if (tabPanel.getSelectionModel().getSelectedIndex() == 0) {
            filterCommand(CommandManager.getInstance().commandList, searchField.getText(), messageTable);
        } else {
            filterCommand(CommandManager.getInstance().itemList, searchField.getText(), itemTable);
        }
    }

    private void filterCommand(ObservableList<CommandObjectEntity> list, String text, TableView<CommandObjectEntity> tableView) {
        ObservableList<CommandObjectEntity> itemList;
        if (StringUtils.isBlank(text)) {
            itemList = list;
        } else {
            itemList = FXCollections.observableArrayList(list.stream()
                    .filter(command -> StringUtils.contains(command.getName(), text) || StringUtils.contains(command.getId(), text))
                    .collect(Collectors.toList()));
        }
        tableView.setItems(itemList);
        tableView.sort();
        if (list.contains(selectMsg)) {
            tableView.getSelectionModel().select(selectMsg);
        } else {
            tableView.getSelectionModel().selectFirst();
        }
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
    private void showCreateCommandUI() {
        AddCommandController.show(this, tabPanel.getSelectionModel().getSelectedIndex() == 0 ? CommandType.command : CommandType.item);
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
