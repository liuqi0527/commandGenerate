package com.egls.server.command;

import com.egls.server.command.controller.ConfirmController;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Created on 2017-09-05 14:24
 *
 * @author mayer
 * @version 1.0
 */
public class Constant {

    public static final char[] FIELD_NAME_FIRST_LETTERS = new char[]{
            '$', '_',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    };

    public static final char[] FIELD_NAME_LETTERS = ArrayUtils.addAll(FIELD_NAME_FIRST_LETTERS, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');


    public static boolean isCommandValid(String command) {
        if (StringUtils.isBlank(command)) {
            ConfirmController.show("指令号不能为空");
            return false;
        }

        if (!NumberUtils.isCreatable(command)) {
            ConfirmController.show("指令号只能是数字");
            return false;
        }

        int tempCommand = Integer.decode(command);
        if (tempCommand < Short.MIN_VALUE || Short.MAX_VALUE < tempCommand) {
            ConfirmController.show("指令号范围越界");
            return false;
        }

        if (CommandManager.getInstance().commandList.stream().anyMatch(message -> StringUtils.equals(command, message.getId()))) {
            ConfirmController.show("指令号重复");
            return false;
        }
        return true;
    }

    public static boolean isNameValid(String name) {
        if (StringUtils.isBlank(name)) {
            ConfirmController.show("名称不能为空");
            return false;
        }

        char[] fieldNameChars = name.toCharArray();
        //检查首字母的合法性
        if (!ArrayUtils.contains(Constant.FIELD_NAME_FIRST_LETTERS, fieldNameChars[0])) {
            ConfirmController.show("名称首字母不合法");
            return false;
        }

        //检测变量名字的合法性
        for (int j = 1; j < fieldNameChars.length; j++) {
            if (!ArrayUtils.contains(Constant.FIELD_NAME_LETTERS, fieldNameChars[j])) {
                ConfirmController.show("名称不合法");
                return false;
            }
        }
        return true;
    }

    public static boolean isClassNameValid(String name) {
        if (CommandManager.getInstance().commandList.stream().anyMatch(message -> StringUtils.equals(message.getName(), name))) {
            ConfirmController.show("ClassName重复");
            return false;
        }

        if (!isNameValid(name)) {
            return false;
        }
        return true;
    }
}
