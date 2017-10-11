package com.egls.server.command.model.type;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/26
 */

public enum CommandType {
    command,
    item;

    public boolean isCommand() {
        return this == command;
    }

    public boolean isItem() {
        return this == item;
    }
}
