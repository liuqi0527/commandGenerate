package com.egls.server.command;

import com.egls.server.utils.date.DateTimeFormatterType;
import com.egls.server.utils.date.DateTimeUtil;
import com.egls.server.utils.file.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/15
 */

public class LocalProperties {

    private static final String KEY_LAST_CONFIG_PATH = "last_config_dir";
    private static final String DEFAULT_CONFIG_PATH = System.getProperty("user.dir");

    private static Properties properties = new Properties();

    public static void init() {
        try {
            properties.load(new FileInputStream(getFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getConfigPath() {
        return properties.getProperty(KEY_LAST_CONFIG_PATH, DEFAULT_CONFIG_PATH);
    }

    public static void setConfigPath(String path) {
        properties.setProperty(KEY_LAST_CONFIG_PATH, path);

        try {
            properties.store(new FileOutputStream(getFile()), "save at: " + DateTimeUtil.toString(LocalDateTime.now(), DateTimeFormatterType.date_time));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File getFile() {
        File file = new File(System.getProperty("user.dir") + File.separator + "local.properties");
        try {
            FileUtil.createFileOnNoExists(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
