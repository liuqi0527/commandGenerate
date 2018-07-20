package com.egls.server.command.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.CompoundFieldType;
import com.egls.server.command.model.type.FieldType;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

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
    private static final String SEPARATOR = ",";

    //集合类型
    private CollectionType collectionType = CollectionType.none;

    //类型全名，普通类型：String， Item， 集合类型：List<Integer>, Map<Integer, Item>
    private StringProperty fullType = new SimpleStringProperty();

    private StringProperty name = new SimpleStringProperty();
    private StringProperty comment = new SimpleStringProperty();


    //元素类型，基础类型或者符合类型的名称
    private List<FieldType> fieldTypes = new ArrayList<>();
    //存储用
    private String type;

    public CommandFieldEntity() {
    }

    public CommandFieldEntity(CollectionType collectionType, String name, String comment) {
        setCollectionType(collectionType);
        setName(name);
        setComment(comment);
    }

    public String getFullTypeName() {
        return fieldTypes.isEmpty() ? "" : collectionType.generateTypeName(this);
    }

    @XmlElement
    public CollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
        this.setFullType(getFullTypeName());
    }

    public FieldType getSingleFieldType() {
        return this.fieldTypes.stream().findFirst().orElse(null);
    }


    public void addFieldType(String typeName, boolean clear) {
        FieldType type = FieldType.get(typeName);
        type = type != null ? type : new CompoundFieldType(typeName);
        if (clear) {
            fieldTypes.clear();
        }
        fieldTypes.add(type);

        StringBuilder sb = new StringBuilder();
        for (FieldType fieldType : fieldTypes) {
            sb.append(fieldType.getName()).append(SEPARATOR);
        }
        this.type = sb.toString();
        resetFullName();
    }

    public List<FieldType> getFieldTypes() {
        return fieldTypes;
    }

    public void resetFullName() {
        setFullType(getFullTypeName());
    }

    //存储用
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
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

        fieldTypes.clear();
        for (String typeStr : StringUtils.split(type, SEPARATOR)) {
            if (StringUtils.isNotBlank(typeStr)) {
                addFieldType(typeStr, false);
            }
        }
    }

    /**
     * 模板使用的方法
     */
    public String getCapitalizeName() {
        return StringUtils.capitalize(getName());
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
