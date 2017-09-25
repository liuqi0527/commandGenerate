package com.egls.server.command.controller;

import com.egls.server.command.CommandManager;
import com.egls.server.command.MainApplication;
import com.egls.server.command.model.CommandObjectEntity;
import com.egls.server.utils.file.FileType;
import com.egls.server.utils.file.FileUtil;
import com.egls.server.utils.file.TxtUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/9/22
 */

public class GenerateController {
    public static final String INSTALL = "install";
    public static final String DEPLOY = "deploy";

    @FXML
    private TextArea consoleField;

    @FXML
    private Button closeBtn;

    private Stage stage;

    private String buildCmd;

    public static void showAndGenerate(String buildCmd) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AddCommandController.class.getResource("/GenerateConsole.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Console");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initOwner(MainApplication.getPrimaryStage());
            stage.setScene(new Scene(loader.load()));

            GenerateController generateController = loader.getController();
            generateController.stage = stage;
            generateController.buildCmd = buildCmd;
            generateController.generate();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        consoleField.setText("");
        closeBtn.setVisible(false);
    }

    @FXML
    private void close() {
        stage.close();
    }

    private void generate() {
        new Thread(() -> {
            try {

                String projectPath = getPath(System.getProperty("user.dir"), CommandManager.getArtifactId());
                String commandPath = getPath(projectPath, "src", "main", "java", StringUtils.replace(CommandManager.getPackageName(), ".", File.separator));

                printMessage("生成项目目录");
                File projectDir = new File(projectPath);
                FileUtil.createDirOnNoExists(projectDir);
                FileUtils.cleanDirectory(projectDir);

                printMessage("生成Maven配置文件");
                generalPomFile(projectPath);

                printMessage("生成Java源文件文件");
                generalJavaFile(commandPath);

                printMessage("开始编译打包");
                Process process;
                if (SystemUtils.IS_OS_WINDOWS) {
                    process = Runtime.getRuntime().exec("cmd /k mvn clean " + buildCmd, null, projectDir);
                } else {
                    process = Runtime.getRuntime().exec("mvn clean " + buildCmd, null, projectDir);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while (StringUtils.isNotBlank((line = reader.readLine()))) {
                    printMessage(line);
                }
                printMessage("执行完成");
            } catch (Exception e) {
                e.printStackTrace();
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    printMessage(stackTraceElement.toString());
                }
                printMessage("执行失败");
            } finally {
                closeBtn.setVisible(true);
            }
        }).start();
    }

    private void generalPomFile(String projectPath) throws Exception {
        Charset charset = Charset.forName("utf-8");

        String pomFile = FileUtils.readFileToString(new File(getClass().getResource("/xml/pom.xml").getFile()), charset);

        pomFile = StringUtils.replaceFirst(pomFile, "\\$\\{groupId\\}", CommandManager.getGroupId());
        pomFile = StringUtils.replaceFirst(pomFile, "\\$\\{artifactId\\}", CommandManager.getArtifactId());
        pomFile = StringUtils.replaceFirst(pomFile, "\\$\\{version\\}", CommandManager.getVersion());

        FileUtils.writeStringToFile(new File(projectPath + File.separator + "pom.xml"), pomFile, charset);

        String assemblyFile = FileUtils.readFileToString(new File(getClass().getResource("/xml/assembly.xml").getFile()), charset);
        FileUtils.writeStringToFile(new File(projectPath + File.separator + "assembly.xml"), assemblyFile, charset);
    }

    private void generalJavaFile(String commandPath) throws Exception {
        for (CommandObjectEntity commandObjectEntity : CommandManager.getInstance().commandList) {
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

    private void printMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            System.out.println(message);

            Platform.runLater(() -> {
                consoleField.appendText(message);
                consoleField.appendText("\n");
                consoleField.setScrollLeft(0D);
                consoleField.setScrollTop(Double.MAX_VALUE);
            });
        }
    }
}
