package com.egls.server.command.model.type;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基础类型
 *
 * @author mayer
 * @version 1.0
 */
public class FiledType {

    public static final Map<String, FiledType> FIELD_TYPES = new LinkedHashMap<>();

    static {
        FIELD_TYPES.put("String", new FiledType("String", "String", "readString", "writeString"));
        FIELD_TYPES.put("LString", new FiledType("String", "String", "readLongString", "writeLongString"));
        FIELD_TYPES.put("byte", new FiledType("byte", "Byte", "readByte", "writeByte"));
        FIELD_TYPES.put("boolean", new FiledType("boolean", "Boolean", "readBoolean", "writeBoolean"));
        FIELD_TYPES.put("char", new FiledType("char", "Character", "readChar", "writeChar"));
        FIELD_TYPES.put("short", new FiledType("short", "Short", "readShort", "writeShort"));
        FIELD_TYPES.put("int", new FiledType("int", "Integer", "readInt", "writeInt"));
        FIELD_TYPES.put("long", new FiledType("long", "Long", "readLong", "writeLong"));
        FIELD_TYPES.put("float", new FiledType("float", "Float", "readFloat", "writeFloat"));
        FIELD_TYPES.put("double", new FiledType("double", "Double", "readDouble", "writeDouble"));

        FIELD_TYPES.put("bytes", new FiledType("byte[]", "", "readBytes", "writeBytes"));//整个指令包的所有剩余字节.
    }

    public static FiledType get(String type) {
        return FIELD_TYPES.get(type);
    }

    protected final String type;

    protected final String boxType;

    protected final String read;

    protected final String write;

    public FiledType(String type, String boxType, String read, String write) {
        this.type = type;
        this.boxType = boxType;
        this.read = read;
        this.write = write;
    }

    public String getType() {
        return type;
    }

    public String getBoxType() {
        return boxType;
    }

    public String generateRead(String fieldName) {
        return String.format("this.%s = netDataPacket.%s();\n", fieldName, read);
    }

    public String generateCollectionRead() {
        return String.format("packet.%s();", read);
    }

    public String generateWrite(String fieldName) {
        return String.format("netDataPacket.%s(this.%s);\n", write, fieldName);
    }

    public String generateCollectionWrite(String fieldName) {
        return String.format("packet.%s(%s)", write, fieldName);
    }
}
