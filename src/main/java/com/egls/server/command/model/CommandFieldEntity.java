package com.egls.server.command.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlType;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
@XmlType(propOrder = {
        "type",
        "name",
        "comment",
})
public class CommandFieldEntity {

    private StringProperty type = new SimpleStringProperty();
    private StringProperty name = new SimpleStringProperty();
    private StringProperty comment = new SimpleStringProperty();

    public CommandFieldEntity() {
    }

    public CommandFieldEntity(String type, String name, String comment) {
        this.type.set(type);
        this.name.set(name);
        this.comment.set(comment);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }
}
