package com.egls.server.command.model.type;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

/**
 * Created on 2017-09-06 17:13
 *
 * @author mayer
 * @version 1.0
 */
public enum CollectionType {

    none,
    List {
        @Override
        public String read(int index, FiledType fieldType, String fieldName) {
            return String.format(
                    "\n       %s = netDataPacket.readArrayList(packet -> {  \n" +
                            "     %s localVar = %s                          \n" +
                            "     return localVar;                          \n" +
                            " });                                           \n",
                    fieldName, fieldType.getType(), fieldType.generateCollectionRead());
        }

        @Override
        public String write(int index, FiledType fieldType, String fieldName) {
            return String.format("netDataPacket.writeCollection(%s, (packet, loopVar) -> %s);\n",
                    fieldName, fieldType.generateCollectionWrite("loopVar"));
        }

        @Override
        public String generateTypeName(FiledType fieldType) {
            return String.format("Collection<%s>", fieldType instanceof CompoundFieldType ? fieldType.getType() : fieldType.getBoxType());
        }
    },;

    public static ObservableList<String> nameList;

    static {
        nameList = FXCollections.observableArrayList(none.name(), List.name());
    }

    public static CollectionType getType(String type) {
        for (CollectionType collectionType : CollectionType.values()) {
            if (StringUtils.equals(type, collectionType.name())) {
                return collectionType;
            }
        }
        return none;
    }


    public String read(int index, FiledType fieldType, String fieldName) {
        return fieldType.generateRead(fieldName);
    }

    public String write(int index, FiledType fieldType, String fieldName) {
        return fieldType.generateWrite(fieldName);
    }

    public String generateTypeName(FiledType fieldType) {
        return fieldType.getType();
    }
}
