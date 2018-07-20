package com.egls.server.command.model.type;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基础类型
 *
 * @author mayer
 * @version 1.0
 */
public class FieldType {

    public static final Map<String, FieldType> ALL_FIELD_TYPES = new LinkedHashMap<>();
    public static final Map<String, FieldType> SIMPLE_FIELD_TYPES = new LinkedHashMap<>();

    static {
        addField(new FieldType("String", "String", "String", "readString", "writeString"));
        addField(new FieldType("LString", "String", "String", "readLongString", "writeLongString"));
        addField(new FieldType("byte", "byte", "Byte", "readByte", "writeByte"));
        addField(new FieldType("boolean", "boolean", "Boolean", "readBoolean", "writeBoolean"));
        addField(new FieldType("char", "char", "Character", "readChar", "writeChar"));
        addField(new FieldType("short", "short", "Short", "readShort", "writeShort"));
        addField(new FieldType("int", "int", "Integer", "readInt", "writeInt"));
        addField(new FieldType("long", "long", "Long", "readLong", "writeLong"));
        addField(new FieldType("float", "float", "Float", "readFloat", "writeFloat"));
        addField(new FieldType("double", "double", "Double", "readDouble", "writeDouble"));

        //整个指令包的所有剩余字节.
        ALL_FIELD_TYPES.put("bytes", new FieldType("bytes", "byte[]", "", "readBytes", "writeBytes"));
    }

    private static void addField(FieldType fieldType) {
        ALL_FIELD_TYPES.put(fieldType.getName(), fieldType);
        SIMPLE_FIELD_TYPES.put(fieldType.getName(), fieldType);
    }

    public static FieldType get(String type) {
        return ALL_FIELD_TYPES.get(type);
    }

    protected final String name;

    protected final String type;

    protected final String boxType;

    protected final String read;

    protected final String write;

    public FieldType(String name, String type, String boxType, String read, String write) {
        this.name = name;
        this.type = type;
        this.boxType = boxType;
        this.read = read;
        this.write = write;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getBoxType() {
        return boxType;
    }

    public String getRead() {
        return read;
    }

    public String getWrite() {
        return write;
    }
}
