package com.egls.server.command.shortcut;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author LiuQi - [Created on 2017-11-25]
 */
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.NONE)
public class ShortcutManager {
    private static final String ConfigFile = "shortcut.xml";
    private static ShortcutManager singleton = new ShortcutManager();

    @XmlElement
    private Map<ShortcutType, Shortcut> shortcutMap = new HashMap<>();

    public static boolean load() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ShortcutManager.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            singleton = (ShortcutManager) jaxbUnmarshaller.unmarshal(getConfigFile());
        } catch (JAXBException e) {
            System.err.println("read shortcut config fail");
        } finally {
            for (ShortcutType shortcutType : ShortcutType.values()) {
                if (!singleton.shortcutMap.containsKey(shortcutType)) {
                    singleton.shortcutMap.put(shortcutType, shortcutType.getDefaultShortcut());
                }
            }
        }
        return true;
    }

    public static void save() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ShortcutManager.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(singleton, getConfigFile());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static File getConfigFile() {
        return new File(System.getProperty("user.dir") + File.separator + ConfigFile);
    }

    public static Shortcut getShortcut(ShortcutType type) {
        return singleton.shortcutMap.get(type);
    }

    public static void onGlobalKeyPress(KeyEvent keyEvent) {
        for (ShortcutType shortcutType : ShortcutType.values()) {
            if (onKeyPress(shortcutType, keyEvent)) {
                return;
            }
        }
    }

    public static boolean onKeyPress(ShortcutType type, KeyEvent event) {
        if (isMatch(type, event)) {
            type.doAction();
            return true;
        }
        return false;
    }

    private static boolean isMatch(ShortcutType type, KeyEvent event) {
        Shortcut shortcut = singleton.shortcutMap.get(type);
        return shortcut != null && shortcut.getKeyType().isMatch(event) && Objects.equals(shortcut.getCode(), event.getCode());
    }

    public static void update(ShortcutType type, FunctionKey keyType, KeyCode keyCode) {
        singleton.shortcutMap.put(type, new Shortcut(type, keyType, keyCode));
        save();
    }

}
