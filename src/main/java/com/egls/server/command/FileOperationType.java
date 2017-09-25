package com.egls.server.command;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/19
 */

public enum FileOperationType {

    create("Create Config File"),
    save("Save Config File"),
    open("Open Config File");

    private String title;

    FileOperationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
