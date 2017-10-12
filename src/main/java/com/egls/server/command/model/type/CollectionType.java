package com.egls.server.command.model.type;

import com.egls.server.command.model.CommandFieldEntity;
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
        public String read(int index, CommandFieldEntity fieldEntity) {
            String fieldName = fieldEntity.getName();
            FieldType fieldType = fieldEntity.getSingleFieldType();

            return String.format(
                    "\n       %s = netDataPacket.readArrayList(packet -> {  \n" +
                            "     %s localVar = %s                          \n" +
                            "     return localVar;                          \n" +
                            " });                                           \n",
                    fieldName, fieldType.getType(), fieldType.generateCollectionRead());
        }

        @Override
        public String write(int index, CommandFieldEntity fieldEntity) {
            String fieldName = fieldEntity.getName();
            FieldType fieldType = fieldEntity.getSingleFieldType();

            return String.format("netDataPacket.writeCollection(%s, (packet, loopVar) -> %s);\n",
                    fieldName, fieldType.generateCollectionWrite("loopVar"));
        }

        @Override
        public String generateTypeName(CommandFieldEntity fieldEntity) {
            FieldType fieldType = fieldEntity.getSingleFieldType();
            return String.format("Collection<%s>", fieldType instanceof CompoundFieldType ? fieldType.getName() : fieldType.getBoxType());
        }
    },

    Map {
        @Override
        public String read(int index, CommandFieldEntity fieldEntity) {
            String fieldName = fieldEntity.getName();
            FieldType keyType = fieldEntity.getFieldTypes().get(0);
            FieldType valueType = fieldEntity.getFieldTypes().get(1);

            return String.format(
                    "\n       %s = netDataPacket.readLinkedHashMap(packet -> {  \n" +
                            "     %s localVar = %s                          \n" +
                            "     return localVar;                          \n" +
                            " }, packet -> {                                \n" +
                            "     %s localVar = %s                          \n" +
                            "     return localVar;                          \n" +
                            " });                                           \n",
                    fieldName, keyType.getType(), keyType.generateCollectionRead(), valueType.getType(), valueType.generateCollectionRead());
        }

        @Override
        public String write(int index, CommandFieldEntity fieldEntity) {
            String fieldName = fieldEntity.getName();
            FieldType keyType = fieldEntity.getFieldTypes().get(0);
            FieldType valueType = fieldEntity.getFieldTypes().get(1);

            return String.format("netDataPacket.writeMap(%s, (packet, key) -> %s ,(packet, value) -> %s );\n",
                    fieldName, keyType.generateCollectionWrite("key"), valueType.generateCollectionWrite("value"));
        }

        @Override
        public String generateTypeName(CommandFieldEntity fieldEntity) {
            java.util.List<FieldType> fieldTypes = fieldEntity.getFieldTypes();
            String keyType = fieldTypes.isEmpty() ? "null" : fieldTypes.get(0).getBoxType();
            String valueType = fieldTypes.size() <= 1 ? "null" : fieldTypes.get(1).getBoxType();
            return String.format("Map<%s, %s>", keyType, valueType);
        }
    };

    public static ObservableList<String> nameList;

    static {
        nameList = FXCollections.observableArrayList(none.name(), List.name(), Map.name());
    }

    public static CollectionType getType(String type) {
        for (CollectionType collectionType : CollectionType.values()) {
            if (StringUtils.equals(type, collectionType.name())) {
                return collectionType;
            }
        }
        return none;
    }


    public String read(int index, CommandFieldEntity fieldEntity) {
        return fieldEntity.getSingleFieldType().generateRead(fieldEntity.getName());
    }

    public String write(int index, CommandFieldEntity fieldEntity) {
        return fieldEntity.getSingleFieldType().generateWrite(fieldEntity.getName());
    }

    public String generateTypeName(CommandFieldEntity fieldEntity) {
        return fieldEntity.getSingleFieldType().getType();
    }
}
