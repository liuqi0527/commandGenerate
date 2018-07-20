package com.egls.server.command.model;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.egls.server.command.Constant;
import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.model.type.CommandType;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

/**
 * Auto Created on 2017-09-05 14:27
 */
@XmlType(propOrder = {
        "type",
        "id",
        "uiCommand",
        "name",
        "comment",
        "fields"
})
public class CommandObjectEntity {

    private CommandType type = CommandType.command;
    private boolean uiCommand;
    private StringProperty id = new SimpleStringProperty();
    private StringProperty name = new SimpleStringProperty();
    private StringProperty comment = new SimpleStringProperty();

    private ObservableList<CommandFieldEntity> fields = FXCollections.observableArrayList();

    public CommandObjectEntity() {
    }

    public CommandObjectEntity(CommandType type, String name, String comment) {
        setType(type);
        setName(name);
        setComment(comment);
    }

    public boolean isFieldNameValid(CommandFieldEntity field, String name) {
        if (!Constant.isNameValid(name)) {
            return false;
        }
        if (fields.stream().filter(f -> !Objects.equals(f, field)).anyMatch(f -> StringUtils.equals(f.getName(), name))) {
            ConfirmController.show("名称重复");
            return false;
        }
        return true;
    }

    public boolean containsKeyword(String keyword) {
        return StringUtils.containsIgnoreCase(id.get(), keyword)
                || StringUtils.containsIgnoreCase(name.get(), keyword)
                || StringUtils.containsIgnoreCase(comment.get(), keyword);
    }

    /**
     * 模板使用的方法
     */
    public String getTextCommand() {
        return "0x" + StringUtils.upperCase(StringUtils.leftPad(Integer.toHexString(getIntId()), 4, '0'));
    }

    public int getIntId() {
        return Integer.decode(getId());
    }

    @XmlElement
    public String getId() {
        return id.get() == null ? "" : id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(StringUtils.leftPad(id, 4, "0"));
    }

    @XmlElement
    public boolean isUiCommand() {
        return uiCommand;
    }

    public void setUiCommand(boolean uiCommand) {
        this.uiCommand = uiCommand;
    }

    @XmlElement
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ObservableList<CommandFieldEntity> getFields() {
        return fields;
    }

    public void setFields(ObservableList<CommandFieldEntity> fields) {
        this.fields = fields;
    }

    @XmlElement
    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    @XmlElement
    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  