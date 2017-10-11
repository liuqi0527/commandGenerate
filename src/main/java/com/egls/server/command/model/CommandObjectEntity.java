package com.egls.server.command.model;

import com.egls.server.command.CommandManager;
import com.egls.server.command.Constant;
import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.CommandType;
import com.egls.server.command.model.type.FiledType;
import com.egls.server.net.tcp.spi.NetCommandObject;
import com.egls.server.net.tcp.spi.NetDataPacket;
import com.egls.server.utils.date.DateTimeFormatterType;
import com.egls.server.utils.date.DateTimeUtil;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Auto Created on 2017-09-05 14:27
 */
@XmlType(propOrder = {
        "type",
        "id",
        "name",
        "comment",
        "fields"
})
public class CommandObjectEntity {

    private CommandType type = CommandType.command;
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

    public String toJavaCode() throws FormatterException {
        String sourceCode = type == CommandType.command ? generateCommandCode() : generateItemCode();
        try {
            return javaSourceCodeFormatter.formatSource(sourceCode);
        } catch (Exception e) {
            e.printStackTrace();
            return sourceCode;
        }
    }

    private String generateCommandCode() {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(CommandManager.getPackageName())) {
            stringBuilder.append(String.format("package %s;\n", CommandManager.getPackageName()));
        }

        stringBuilder.append(String.format("import %s;\n", NetCommandObject.class.getName()));
        stringBuilder.append(String.format("import %s;\n", NetDataPacket.class.getName()));
        if (fields.stream().anyMatch(field -> field.getCollectionType() != CollectionType.none)) {
            stringBuilder.append("import java.util.*;\n");
        }

        stringBuilder.append("\n/**\n");
        if (StringUtils.isNotBlank(getComment())) {
            stringBuilder.append(String.format(" * %s\n", getComment()));
        }
        stringBuilder.append(String.format(" * Automatically Created on %s\n", DateTimeUtil.toString(LocalDateTime.now(), DateTimeFormatterType.date_time_millis_zone)));
        stringBuilder.append(" */\n");

        stringBuilder.append(String.format("public final class %s implements NetCommandObject {\n", getName()));
        //生成属性声明
        fields.forEach(fieldTriple -> stringBuilder.append(generateDeclaration(fieldTriple)));

        //生成getter
        fields.forEach(fieldTriple -> stringBuilder.append(generateGetter(fieldTriple)));
        //生成setter
        fields.forEach(fieldTriple -> stringBuilder.append(generateSetter(fieldTriple)));

        //生成read方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public void read(NetDataPacket netDataPacket) {\n");
        fields.forEach(field -> stringBuilder.append(generateRead(field)));
        stringBuilder.append("}\n");

        //生成write方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public NetDataPacket write() {\n");
        stringBuilder.append("NetDataPacket netDataPacket = NetDataPacket.build(getCommand());\n");
        fields.forEach(field -> stringBuilder.append(generateWrite(field)));
        stringBuilder.append("return netDataPacket;\n");
        stringBuilder.append("}\n");

        //生成命令方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public short getCommand() {\n");
        stringBuilder.append(String.format("return %s;\n", generateCommand()));
        stringBuilder.append("}\n");

        //生成属性列表
        stringBuilder.append("@Override\n");
        stringBuilder.append("public   Object[] getAllFields(){\n");
        stringBuilder.append(String.format("Object[] allFields = new Object[%d];\n", fields.size()));
        for (int i = 0; i < fields.size(); i++) {
            stringBuilder.append(String.format("allFields[%d] = %s;\n", i, fields.get(i).getName()));
        }
        stringBuilder.append("return allFields;\n");
        stringBuilder.append("}\n");

        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private String generateItemCode() throws FormatterException {
        //包
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(CommandManager.getPackageName())) {
            stringBuilder.append(String.format("package %s;\n", CommandManager.getPackageName()));
        }

        //导入
        stringBuilder.append(String.format("import %s;\n", NetDataPacket.class.getName()));
        stringBuilder.append(String.format("import %s;\n", NetCommandObject.class.getName()));
        if (fields.stream().anyMatch(field -> field.getCollectionType() != CollectionType.none)) {
            stringBuilder.append("import java.util.*;\n");
        }

        //注释
        stringBuilder.append("\n/**\n");
        if (StringUtils.isNotBlank(getComment())) {
            stringBuilder.append(String.format(" * %s\n", getComment()));
        }
        stringBuilder.append(String.format(" * Automatically Created on %s\n", DateTimeUtil.toString(LocalDateTime.now(), DateTimeFormatterType.date_time_millis_zone)));
        stringBuilder.append(" */\n");

        //类名
        stringBuilder.append(String.format("public final class %s implements NetCommandObject.CompoundType {\n", getName()));

        //生成属性声明
        fields.forEach(fieldTriple -> stringBuilder.append(generateDeclaration(fieldTriple)));

        //生成getter
        fields.forEach(fieldTriple -> stringBuilder.append(generateGetter(fieldTriple)));
        //生成setter
        fields.forEach(fieldTriple -> stringBuilder.append(generateSetter(fieldTriple)));

        //生成read方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public void read(NetDataPacket netDataPacket) {\n");
        fields.forEach(fieldTriple -> stringBuilder.append(generateRead(fieldTriple)));
        stringBuilder.append("}\n");

        //生成write方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public void write(NetDataPacket netDataPacket) {\n");
        fields.forEach(fieldTriple -> stringBuilder.append(generateWrite(fieldTriple)));
        stringBuilder.append("}\n");

        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private String generateDeclaration(CommandFieldEntity fieldEntity) {
        String fieldName = fieldEntity.getName();
        String fieldComment = StringUtils.isBlank(fieldEntity.getComment()) ? fieldName : fieldEntity.getComment();
        return "/**\n" + "* " + fieldComment + "\n*/\n" + String.format("private %s %s;\n", fieldEntity.getFullTypeName(), fieldName);
    }

    private String generateGetter(CommandFieldEntity fieldEntity) {
        String fieldName = fieldEntity.getName();
        return String.format("public %s get%s() {\nreturn this.%s;\n}\n", fieldEntity.getFullTypeName(), StringUtils.capitalize(fieldName), fieldName);
    }

    private String generateSetter(CommandFieldEntity fieldEntity) {
        String fieldName = fieldEntity.getName();
        return String.format("public void set%s(%s %s) {\nthis.%s = %s;\n}\n", StringUtils.capitalize(fieldName), fieldEntity.getFullTypeName(),
                fieldName, fieldName, fieldName);
    }

    private String generateRead(CommandFieldEntity fieldEntity) {
        CollectionType collectionType = fieldEntity.getCollectionType();
        FiledType fieldType = fieldEntity.getFieldType();
        return collectionType.read(fields.indexOf(fieldEntity), fieldType, fieldEntity.getName());
    }

    private String generateWrite(CommandFieldEntity fieldEntity) {
        CollectionType collectionType = fieldEntity.getCollectionType();
        FiledType fieldType = fieldEntity.getFieldType();
        return collectionType.write(fields.indexOf(fieldEntity), fieldType, fieldEntity.getName());
    }

    private String generateCommand() {
        return "0x" + StringUtils.upperCase(StringUtils.leftPad(Integer.toHexString(getIntId()), 4, '0'));
    }

    public int getIntId() {
        return Integer.decode(getId());
    }

    @XmlElement
    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(StringUtils.leftPad(id, 4, "0"));
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

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    private static final Formatter javaSourceCodeFormatter = new Formatter(new CustomJavaFormatterOptions());

    private static final class CustomJavaFormatterOptions extends JavaFormatterOptions {

        CustomJavaFormatterOptions() {
            super(JavaFormatterOptions.JavadocFormatter.NONE, Style.AOSP, JavaFormatterOptions.SortImports.NO);
        }

        @Override
        public int maxLineLength() {
            return 240;
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  