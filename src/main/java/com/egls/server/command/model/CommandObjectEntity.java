package com.egls.server.command.model;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.egls.server.command.CommandManager;
import com.egls.server.command.Constant;
import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.model.type.CollectionType;
import com.egls.server.command.model.type.CommandType;
import com.egls.server.command.model.type.FieldType;
import com.egls.server.net.tcp.spi.NetCommandObject;
import com.egls.server.net.tcp.spi.NetDataPacket;
import com.egls.server.net.tcp.spi.TextNetCommandObject;
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

    public String toJavaCode() throws FormatterException {
        String sourceCode;
        if (type == CommandType.item) {
            sourceCode = generateItemCode();
        } else if (isUiCommand()) {
            sourceCode = generateUICommandCode();
        } else {
            sourceCode = generateCommandCode();
        }

        try {
            return javaSourceCodeFormatter.formatSource(sourceCode);
        } catch (Exception e) {
            e.printStackTrace();
            return sourceCode;
        }
    }

    public boolean containsKeyword(String keyword) {
        return StringUtils.containsIgnoreCase(id.get(), keyword)
                || StringUtils.containsIgnoreCase(name.get(), keyword)
                || StringUtils.containsIgnoreCase(comment.get(), keyword);
    }

    private String generateCommandCode() {
        //包
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(CommandManager.getPackageName())) {
            stringBuilder.append(String.format("package %s;\n", CommandManager.getPackageName()));
        }

        //导入
        stringBuilder.append(String.format("import %s;\n", NetCommandObject.class.getName()));
        stringBuilder.append(String.format("import %s;\n", NetDataPacket.class.getName()));
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
        stringBuilder.append(String.format("public final class %s implements NetCommandObject {\n", getName()));
        //指令号
        stringBuilder.append(String.format("public static final short command = %s;\n", generateCommand()));

        //生成属性声明
        fields.forEach(fieldTriple -> stringBuilder.append(generateDeclaration(fieldTriple)));

        //生成getter
        fields.forEach(fieldTriple -> stringBuilder.append(generateGetter(fieldTriple)));
        //生成setter
        fields.forEach(fieldTriple -> stringBuilder.append(generateSetter(fieldTriple)));

        //build方法
        generateBuildMethod(stringBuilder);

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

        //生成命令号方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public short getCommand() {\n");
        stringBuilder.append("return command;\n");
        stringBuilder.append("}\n");

        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private String generateUICommandCode() {
        //包
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(CommandManager.getPackageName())) {
            stringBuilder.append(String.format("package %s;\n", CommandManager.getPackageName()));
        }

        //导入
        stringBuilder.append(String.format("import %s;\n", TextNetCommandObject.class.getName()));
        stringBuilder.append(String.format("import %s;\n", NetDataPacket.class.getName()));
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
        stringBuilder.append(String.format("public final class %s extends TextNetCommandObject {\n", getName()));
        //指令号
        stringBuilder.append(String.format("public static final short command = %s;\n", generateCommand()));

        //生成属性声明
        fields.forEach(field -> stringBuilder.append(generateDeclaration(field)));

        //生成getter
        fields.forEach(field -> stringBuilder.append(generateGetter(field)));
        //生成setter
        fields.forEach(field -> stringBuilder.append(generateSetter(field)));

        //build方法
        generateBuildMethod(stringBuilder);

        //生成parse方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public void parse(String[] strings) {\n");
        for (int i = 0; i < fields.size(); i++) {
            CommandFieldEntity field = fields.get(i);
            FieldType fieldType = field.getSingleFieldType();
            if (StringUtils.equals(fieldType.getType(), "String")) {
                stringBuilder.append(String.format("%s = strings[%d];\n", field.getName(), i));
            } else if (StringUtils.equals(fieldType.getType(), "char")) {
                stringBuilder.append(String.format("%s = strings[%d].charAt(0);\n", field.getName(), i));
            } else {
                stringBuilder.append(String.format("%s = %s.valueOf(strings[%d]);\n", field.getName(), fieldType.getBoxType(), i));
            }
        }
        stringBuilder.append("}\n");

        //生成splice方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public String splice() {\n");
        if (!fields.isEmpty()) {
            stringBuilder.append("StringBuilder stringBuilder = new StringBuilder();\n");
            fields.forEach(field -> stringBuilder.append(String.format("stringBuilder.append(%s).append(separator());\n", field.getName())));
            stringBuilder.append("return stringBuilder.toString();\n");
        } else {
            stringBuilder.append("return \"\";\n");
        }

        stringBuilder.append("}\n");

        //生成separator方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public String separator() {\n");
        stringBuilder.append("return \":\";\n");
        stringBuilder.append("}\n");

        //生成命令号方法
        stringBuilder.append("@Override\n");
        stringBuilder.append("public short getCommand() {\n");
        stringBuilder.append("return command;\n");
        stringBuilder.append("}\n");

        stringBuilder.append("}\n");

        return stringBuilder.toString();
    }

    private String generateItemCode() {
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

        //build方法
        generateBuildMethod(stringBuilder);

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

    private void generateBuildMethod(StringBuilder stringBuilder) {
        String paramList = StringUtils.join(fields.stream().map(this::generateParam).toArray(), " ,");
        stringBuilder.append(String.format("public static %s build(%s) {\n", getName(), paramList));
        stringBuilder.append(String.format("%s cmdObject = new %s();\n", getName(), getName()));
        fields.forEach(field -> stringBuilder.append(String.format("cmdObject.%s = %s;\n", field.getName(), field.getName())));
        stringBuilder.append("return cmdObject;\n");
        stringBuilder.append("};\n");
    }

    private String generateParam(CommandFieldEntity fieldEntity) {
        return fieldEntity.getFullTypeName() + " " + fieldEntity.getName();
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
        return String.format("public %s set%s(%s %s) {\nthis.%s = %s;\n return this;\n}\n", getName(), StringUtils.capitalize(fieldName), fieldEntity.getFullTypeName(),
                fieldName, fieldName, fieldName);
    }

    private String generateRead(CommandFieldEntity fieldEntity) {
        CollectionType collectionType = fieldEntity.getCollectionType();
        return collectionType.read(fields.indexOf(fieldEntity), fieldEntity);
    }

    private String generateWrite(CommandFieldEntity fieldEntity) {
        CollectionType collectionType = fieldEntity.getCollectionType();
        return collectionType.write(fields.indexOf(fieldEntity), fieldEntity);
    }

    private String generateCommand() {
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
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  