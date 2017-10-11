package com.egls.server.command.model.type;

/**
 * 复合类型
 *
 * @author LiuQi
 * @version 1.0 Create on  2017/9/26
 */

public class CompoundFieldType extends FiledType {

    public CompoundFieldType(String type) {
        super(type, type, null, null);
    }

    public String generateRead(String fieldName) {
        return String.format("this.%s = new %s();\n   this.%s.read(netDataPacket);\n", fieldName, type, fieldName);
    }

    public String generateCollectionRead() {
        return String.format(" new %s();\n   localVar.read(packet);", type);
    }

    public String generateWrite(String fieldName) {
        return "this." + fieldName + ".write(netDataPacket);\n";
    }

    public String generateCollectionWrite(String collectionName) {
        return String.format("%s.write(packet)", collectionName);
    }

}