package com.egls.server.command.shortcut;

import javafx.scene.input.KeyCode;

/**
 * @author LiuQi - [Created on 2017-11-25]
 */
public class Shortcut {

    private ShortcutType type;
    private FunctionKey keyType;
    private KeyCode code;

    private Shortcut() {
    }

    public Shortcut(ShortcutType type, FunctionKey keyType, KeyCode code) {
        this.type = type;
        this.keyType = keyType;
        this.code = code;
    }

    public ShortcutType getType() {
        return type;
    }

    public void setType(ShortcutType type) {
        this.type = type;
    }

    public FunctionKey getKeyType() {
        return keyType;
    }

    public void setKeyType(FunctionKey keyType) {
        this.keyType = keyType;
    }

    public KeyCode getCode() {
        return code;
    }

    public void setCode(KeyCode code) {
        this.code = code;
    }
}
