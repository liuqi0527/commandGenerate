package com.egls.server.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.egls.server.command.controller.ConfirmController;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CommandType;
import com.egls.server.utils.Md5Util;
import com.egls.server.utils.resource.file.ResourceKindMonitor;
import com.egls.server.utils.resource.file.ResourceWatcher;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.NONE)
public class CommandManager implements ResourceKindMonitor {

    private static CommandManager instance = new CommandManager();

    @XmlElement
    private String groupId = "com.egls.server";

    @XmlElement
    private String artifactId = "net-command-test";

    @XmlElement
    private String version = "1.0-SNAPSHOT";

    @XmlElement
    private String packageName = "com.egls.server.command";

    @XmlElement
    public ObservableList<CommandObjectEntity> commandList = FXCollections.observableArrayList();

    @XmlElement
    public ObservableList<CommandObjectEntity> itemList = FXCollections.observableArrayList();

    private static boolean isSave;
    private static String md5Record;

    public static CommandManager getInstance() {
        return instance;
    }

    public static boolean load(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        try {
            loadFile(file);
            recordMd5(file);
            ResourceWatcher.registerFile(file, instance);
            errorCheck();
        } catch (Exception e) {
            System.err.println("read config fail");
        }
        return true;
    }

    public static boolean loadFile(File file) {
        try {
            CommandManager.instance = new CommandManager();

            JAXBContext jaxbContext = JAXBContext.newInstance(CommandManager.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            CommandManager.instance = (CommandManager) jaxbUnmarshaller.unmarshal(file);
            return true;
        } catch (Exception e) {
            System.err.println("read config fail");
            return false;
        }
    }

    @Override
    public void onModify(File file) throws Exception {
        if (!isSave && !StringUtils.equals(getMd5(file), md5Record) && StringUtils.equals(file.getAbsolutePath(), LocalProperties.getConfigPath())) {
            recordMd5(file);
            Platform.runLater(() -> MainApplication.loadAndShow(file));
        }
    }

    public static void save() {
        try {
            isSave = true;
            File file = new File(LocalProperties.getConfigPath());
            JAXBContext jaxbContext = JAXBContext.newInstance(CommandManager.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(instance, file);
            recordMd5(new File(LocalProperties.getConfigPath()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isSave = false;
        }
    }

    private static void recordMd5(File file) {
        md5Record = getMd5(file);
    }

    private static String getMd5(File file) {
        try {
            return Md5Util.MD5(file);
        } catch (Exception e) {
            e.printStackTrace();
            return "defaultMD5";
        }
    }

    /**
     * 重复指令检查
     */
    public static boolean errorCheck() {
        Map<String, List<CommandObjectEntity>> nameMap = new HashMap<>();
        Map<Integer, List<CommandObjectEntity>> idMap = new HashMap<>();
        for (CommandObjectEntity entity : instance.commandList) {
            nameMap.computeIfAbsent(entity.getName(), key -> new ArrayList<>()).add(entity);
            idMap.computeIfAbsent(entity.getIntId(), key -> new ArrayList<>()).add(entity);
        }

        StringBuilder stringBuilder = new StringBuilder();
        nameMap.values().stream()
                .filter(list -> list.size() > 1)
                .forEach(list -> {
                    list.stream().findAny().ifPresent(entity -> stringBuilder.append(entity.getName()).append("："));
                    list.forEach(entity -> stringBuilder.append(entity.getId()).append(", "));
                    stringBuilder.append("\n");
                });

        idMap.values().stream()
                .filter(list -> list.size() > 1)
                .forEach(list -> {
                    list.stream().findAny().ifPresent(entity -> stringBuilder.append(entity.getId()).append("："));
                    list.forEach(entity -> stringBuilder.append(entity.getName()).append(", "));
                    stringBuilder.append("\n");
                });
        if (stringBuilder.length() != 0) {
            stringBuilder.append("\n");
            stringBuilder.append("以上指令重复，请修改");
            Platform.runLater(() -> ConfirmController.show(stringBuilder.toString()));
            return false;
        }
        return true;
    }

    public static String getPackageName() {
        return instance.packageName;
    }

    public static void setPackageName(String packageName) {
        instance.packageName = packageName;
        save();
    }

    public static String getGroupId() {
        return instance.groupId;
    }

    public static void setGroupId(String groupId) {
        instance.groupId = groupId;
        save();
    }

    public static String getArtifactId() {
        return instance.artifactId;
    }

    public static void setArtifactId(String artifactId) {
        instance.artifactId = artifactId;
        save();
    }

    public static String getVersion() {
        return instance.version;
    }

    public static void setVersion(String version) {
        instance.version = version;
        save();
    }

    public static boolean containsId(int id) {
        return instance.commandList.stream().anyMatch(commandObjectEntity -> commandObjectEntity.getIntId() == id);
    }

    public static void addCommand(CommandObjectEntity command) {
        ObservableList<CommandObjectEntity> list = getList(command.getType());
        if (!list.contains(command)) {
            list.add(command);
        }
        save();
    }

    public static void removeCommand(CommandObjectEntity command) {
        getList(command.getType()).remove(command);
        save();
    }

    private static ObservableList<CommandObjectEntity> getList(CommandType type) {
        return type.isCommand() ? instance.commandList : instance.itemList;
    }
}
