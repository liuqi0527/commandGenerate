package com.egls.server.command.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import com.egls.server.command.CommandManager;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.utils.file.FileType;
import com.egls.server.utils.file.FileUtil;
import com.egls.server.utils.file.TxtUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 * @author LiuQi - [Created on 2017-12-27]
 */
public class CodeGenerate extends Thread {

    private String buildCmd;
    private Consumer<String> messageConsumer;
    private Runnable onExist;

    public CodeGenerate(String buildCmd, Consumer<String> messageConsumer, Runnable onExist) {
        this.buildCmd = buildCmd;
        this.messageConsumer = messageConsumer;
        this.onExist = onExist;
    }

    @Override
    public void run() {
        try {
            String projectPath = getPath(System.getProperty("user.dir"), CommandManager.getArtifactId());
            String commandPath = getPath(projectPath, "src", "main", "java", StringUtils.replace(CommandManager.getPackageName(), ".", File.separator));

            printMessage("生成项目目录\n");
            File projectDir = new File(projectPath);
            FileUtil.createDirOnNoExists(projectDir);
            FileUtils.cleanDirectory(projectDir);

            printMessage("生成Maven配置文件\n");
            generalPomFile(projectPath);

            printMessage("生成Java源文件文件\n");
            generalJavaFile(commandPath);

            printMessage("开始编译打包\n");
            Process process;
            if (SystemUtils.IS_OS_WINDOWS) {
                process = Runtime.getRuntime().exec("mvn.cmd clean " + buildCmd + " && exit", null, projectDir);
            } else {
                process = Runtime.getRuntime().exec("mvn clean " + buildCmd, null, projectDir);
            }

            try {
                Thread.sleep(2000L);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {
                if (!printStream(process.getInputStream()) && !printStream(process.getErrorStream())) {
                    try {
                        // Nothing to do, sleep a while...
                        Thread.sleep(100);
                        // Throw IllegalThreadStateException, if the subprocess represented by this Process object has not yet terminated.
                        printMessage("Process finished with exit code " + process.exitValue() + "\n");
                        break;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (IllegalThreadStateException ex) {
                        // Process still alive
                    }
                }
            }

            process.destroy();
            printMessage("执行完成\n");
        } catch (Exception e) {
            e.printStackTrace();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                printMessage(stackTraceElement.toString());
            }
            printMessage("执行失败\n");
        } finally {
            if (onExist != null) {
                onExist.run();
            }
        }
    }

    private boolean printStream(InputStream stream) throws Exception {
        StringBuilder sb = new StringBuilder();
        while (stream.available() > 0) {
            sb.append((char) stream.read());
        }

        if (sb.length() > 0) {
            printMessage(sb.toString());
        }
        return sb.length() > 0;
    }


    private void printMessage(String message) {
        if (StringUtils.isNotBlank(message) && messageConsumer != null) {
            messageConsumer.accept(message);
        }
    }


    private void generalPomFile(String projectPath) throws Exception {
        Charset charset = Charset.forName("utf-8");

        //pom.xml
        BufferedReader pomReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/xml/pom.xml")));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = pomReader.readLine()) != null) {
            line = StringUtils.replaceFirst(line, "\\$\\{groupId\\}", CommandManager.getGroupId());
            line = StringUtils.replaceFirst(line, "\\$\\{artifactId\\}", CommandManager.getArtifactId());
            line = StringUtils.replaceFirst(line, "\\$\\{version\\}", CommandManager.getVersion());
            stringBuilder.append(line);
        }
        FileUtils.writeStringToFile(new File(projectPath + File.separator + "pom.xml"), stringBuilder.toString(), charset);


        //assembly.xml
        BufferedReader assemblyReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/xml/assembly.xml")));
        StringBuilder assemblyFile = new StringBuilder();
        while ((line = assemblyReader.readLine()) != null) {
            assemblyFile.append(line);
        }
        FileUtils.writeStringToFile(new File(projectPath + File.separator + "assembly.xml"), assemblyFile.toString(), charset);
    }

    private void generalJavaFile(String commandPath) throws Exception {
        for (CommandObjectEntity commandObjectEntity : CommandManager.getInstance().commandList) {
            String javaFilePath = commandPath + File.separator + commandObjectEntity.getName() + FileType.java.getExtension();
            TxtUtil.write(commandObjectEntity.toJavaCode(), javaFilePath, false, false);
        }
        for (CommandObjectEntity commandObjectEntity : CommandManager.getInstance().itemList) {
            String javaFilePath = commandPath + File.separator + commandObjectEntity.getName() + FileType.java.getExtension();
            TxtUtil.write(commandObjectEntity.toJavaCode(), javaFilePath, false, false);
        }
    }

    private String getPath(String... pathArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathArray.length; i++) {
            sb.append(pathArray[i]);
            if (i < pathArray.length - 1) {
                sb.append(File.separator);
            }
        }
        return sb.toString();
    }

}
