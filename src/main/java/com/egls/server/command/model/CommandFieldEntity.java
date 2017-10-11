package com.egls.server.command.model;

import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.CompoundFieldType;
import com.egls.server.command.model.type.FiledType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
@XmlType(propOrder = {
        "collectionType",
        "type",
        "name",
        "comment",
})
@XmlAccessorType(XmlAccessType.NONE)
public class CommandFieldEntity {

    //集合类型
    private CollectionType collectionType = CollectionType.none;
    //元素类型
    private String type;

    private FiledType fieldType;

    //类型全名，普通类型：String， Item， 集合类型：List<Integer>
    private StringProperty fullType = new SimpleStringProperty();

    private StringProperty name = new SimpleStringProperty();
    private StringProperty comment = new SimpleStringProperty();

    public CommandFieldEntity() {
    }

    public CommandFieldEntity(CollectionType collectionType, String type, String name, String comment) {
        setType(type);
        setCollectionType(collectionType);
        setName(name);
        setComment(comment);
    }

    public String getFullTypeName() {
        return StringUtils.isBlank(type) ? "" : collectionType.generateTypeName(getFieldType());
    }

    @XmlElement
    public CollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
        this.setFullType(getFullTypeName());
    }

    public FiledType getFieldType() {
        return this.fieldType;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.fieldType = FiledType.get(type);
        if (this.fieldType == null) {
            this.fieldType = new CompoundFieldType(type);
        }

        this.setFullType(getFullTypeName());
    }


    public String getFullType() {
        return fullType.get();
    }

    public StringProperty fullTypeProperty() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType.set(fullType);
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
}
