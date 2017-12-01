package com.egls.server.command.shortcut;

import com.egls.server.command.MainApplication;

import javafx.scene.input.KeyCode;

/**
 * @author LiuQi - [Created on 2017-11-25]
 */
public enum ShortcutType {

    Command(FunctionKey.Control, KeyCode.W, () -> MainApplication.getPrimaryController().showCreateCommandUI()),
    Field(FunctionKey.Control, KeyCode.E, () -> MainApplication.getPrimaryController().showCreateFieldUI()),
    Search(FunctionKey.Control, KeyCode.F, () -> MainApplication.getPrimaryController().selectSearchText()),
    Install(FunctionKey.Shift, KeyCode.C, () -> MainApplication.getPrimaryController().installJar()),
    Deploy(FunctionKey.Shift, KeyCode.V, () -> MainApplication.getPrimaryController().deployJar()),;

    private Shortcut defaultShortcut;
    private Runnable action;

    ShortcutType(FunctionKey functionKey, KeyCode code, Runnable action) {
        this.defaultShortcut = new Shortcut(this, functionKey, code);
        this.action = action;
    }

    public Shortcut getDefaultShortcut() {
        return defaultShortcut;
    }

    public void doAction() {
        action.run();
    }
}
