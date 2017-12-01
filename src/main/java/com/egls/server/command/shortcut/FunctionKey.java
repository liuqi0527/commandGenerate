package com.egls.server.command.shortcut;

import java.util.function.Predicate;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author LiuQi - [Created on 2017-11-25]
 */
public enum FunctionKey {

    Control(KeyEvent::isControlDown, KeyCode.CONTROL),
    Alt(KeyEvent::isAltDown, KeyCode.ALT, KeyCode.ALT_GRAPH),
    Shift(KeyEvent::isShiftDown, KeyCode.SHIFT),;

    public static FunctionKey search(KeyCode code) {
        for (FunctionKey functionKey : FunctionKey.values()) {
            if (functionKey.isCode(code)) {
                return functionKey;
            }
        }
        return null;
    }

    private Predicate<KeyEvent> predicate;
    private KeyCode[] codes;

    FunctionKey(Predicate<KeyEvent> predicate, KeyCode... code) {
        this.predicate = predicate;
        this.codes = code;
    }

    public boolean isMatch(KeyEvent event) {
        return predicate.test(event);
    }

    public boolean isCode(KeyCode keyCode) {
        for (KeyCode code : codes) {
            if (code == keyCode) {
                return true;
            }
        }
        return false;
    }
}
