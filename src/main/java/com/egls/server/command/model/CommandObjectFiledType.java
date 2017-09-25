package com.egls.server.command.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created on 2017-09-06 17:13
 *
 * @author mayer
 * @version 1.0
 */
public class CommandObjectFiledType {

    public static final Map<String, CommandObjectFiledType> FIELD_TYPES = new LinkedHashMap<>();

    static {
        FIELD_TYPES.put("String", new CommandObjectFiledType("String", "readString", "writeString"));
        FIELD_TYPES.put("LString", new CommandObjectFiledType("String", "readLongString", "writeLongString"));
        FIELD_TYPES.put("byte", new CommandObjectFiledType("byte", "readByte", "writeByte"));
        FIELD_TYPES.put("boolean", new CommandObjectFiledType("boolean", "readBoolean", "writeBoolean"));
        FIELD_TYPES.put("char", new CommandObjectFiledType("char", "readChar", "writeChar"));
        FIELD_TYPES.put("short", new CommandObjectFiledType("short", "readShort", "writeShort"));
        FIELD_TYPES.put("int", new CommandObjectFiledType("int", "readInt", "writeInt"));
        FIELD_TYPES.put("long", new CommandObjectFiledType("long", "readLong", "writeLong"));
        FIELD_TYPES.put("float", new CommandObjectFiledType("float", "readFloat", "writeFloat"));
        FIELD_TYPES.put("double", new CommandObjectFiledType("double", "readDouble", "writeDouble"));
        //
        FIELD_TYPES.put("String[]", new CommandObjectFiledType("String[]", "readStringArray", "writeStringArray"));
        FIELD_TYPES.put("LString[]", new CommandObjectFiledType("String[]", "readLongStringArray", "writeLongStringArray"));
        FIELD_TYPES.put("byte[]", new CommandObjectFiledType("byte[]", "readByteArray", "writeByteArray"));
        FIELD_TYPES.put("boolean[]", new CommandObjectFiledType("boolean[]", "readBooleanArray", "writeBooleanArray"));
        FIELD_TYPES.put("char[]", new CommandObjectFiledType("char[]", "readCharArray", "writeCharArray"));
        FIELD_TYPES.put("short[]", new CommandObjectFiledType("short[]", "readShortArray", "writeShortArray"));
        FIELD_TYPES.put("int[]", new CommandObjectFiledType("int[]", "readIntArray", "writeIntArray"));
        FIELD_TYPES.put("long[]", new CommandObjectFiledType("long[]", "readLongArray", "writeLongArray"));
        FIELD_TYPES.put("float[]", new CommandObjectFiledType("float[]", "readFloatArray", "writeFloatArray"));
        FIELD_TYPES.put("double[]", new CommandObjectFiledType("double[]", "readDoubleArray", "writeDoubleArray"));
        //
        FIELD_TYPES.put("bytes", new CommandObjectFiledType("byte[]", "readBytes", "writeBytes"));//整个指令包的所有剩余字节.
    }

    private final String type;

    private final String read;

    private final String write;

    public CommandObjectFiledType(String type, String read, String write) {
        this.type = type;
        this.read = read;
        this.write = write;
    }

    public String generateDeclaration(String fieldName, String fieldComment) {
        return "/**\n" + "* " + fieldComment + "\n*/\n" + String.format("private %s %s;\n", type, fieldName);
    }

    public String generateGetter(String fieldName) {
        char[] uppercaseFiledNameChars = fieldName.toCharArray();
        uppercaseFiledNameChars[0] = Character.toUpperCase(uppercaseFiledNameChars[0]);
        return String.format("public %s get%s() {\nreturn %s;\n}\n", type, new String(uppercaseFiledNameChars), fieldName);
    }

    public String generateSetter(String fieldName) {
        char[] uppercaseFiledNameChars = fieldName.toCharArray();
        uppercaseFiledNameChars[0] = Character.toUpperCase(uppercaseFiledNameChars[0]);
        return String.format("public void set%s(%s %s) {\nthis.%s = %s;\n}\n", new String(uppercaseFiledNameChars), type, fieldName, fieldName, fieldName);
    }

    public String generateRead(String fieldName) {
        return String.format("this.%s = netDataPacket.%s();\n", fieldName, read);
    }

    public String generateWrite(String fieldName) {
        return String.format("netDataPacket.%s(this.%s);\n", write, fieldName);
    }

}
