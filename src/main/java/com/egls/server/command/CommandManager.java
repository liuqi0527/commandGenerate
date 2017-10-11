package com.egls.server.command;

import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.command.model.type.CommandType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/13
 */
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.NONE)
public class CommandManager {

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

    public static CommandManager getInstance() {
        return instance;
    }

    public static boolean load(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        try {
            LocalProperties.setConfigPath(file.getAbsolutePath());
            CommandManager.instance = new CommandManager();

            JAXBContext jaxbContext = JAXBContext.newInstance(CommandManager.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            CommandManager.instance = (CommandManager) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            System.err.println("read config fail");
        }
        return true;
    }

    public static void save() {
        try {
            File file = new File(LocalProperties.getConfigPath());
            JAXBContext jaxbContext = JAXBContext.newInstance(CommandManager.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(instance, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
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
