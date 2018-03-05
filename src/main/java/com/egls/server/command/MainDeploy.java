package com.egls.server.command;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import com.egls.server.command.controller.CodeGenerate;
import com.egls.server.command.controller.GenerateController;

import org.apache.commons.lang3.StringUtils;

/**
 * @author LiuQi - [Created on 2017-12-27]
 */
public class MainDeploy {

    private static AtomicBoolean generateOver = new AtomicBoolean(false);

    public static void main(String[] args) {
        if (args.length <= 0 || StringUtils.isBlank(args[0])) {
            System.out.println("未指定文件路径");
        } else {
            File file = new File(args[0]);
            if (file.exists() && CommandManager.loadFile(file)) {
                CodeGenerate codeGenerate = new CodeGenerate(GenerateController.DEPLOY, System.out::println, () -> generateOver.set(true));
                codeGenerate.start();

                while (!generateOver.get()) {
                    try {
                        Thread.sleep(500L);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("加载配置文件错误");
            }
        }
    }
}
