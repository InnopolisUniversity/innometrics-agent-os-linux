package com.application;

import com.application.model.Model;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

public class AppLauncher extends Application {
    public Stage window;
    public static Model userModel = null;

    public static void main(String[] args) {

        int version_local = 0, version_latest = 0;
        try {
            version_latest = getLatestVersion();
            System.out.println("version latest: "+version_latest);
            version_local = getLocalVersion();
            System.out.println("version local: " + version_local);

        }catch (Exception ignore){}

        if (version_latest > version_local){
            try {
                String[] cmdScript = new String[]{"/bin/bash", "/opt/datacollectorlinux/lib/app/update.sh"};
                Process procScript = Runtime.getRuntime().exec(cmdScript);
                procScript.waitFor();
            } catch (IOException | InterruptedException ignore) {
                System.out.println("Update Failed");
            }
        }else {
            launch(args);
        }

    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        this.window = primaryStage;
        this.window.setMinWidth(360.0D);
        this.window.setMinHeight(350.0D);
        Path settingsPath = Paths.get("/opt/datacollectorlinux/lib/app/config.json");
        userModel = new Model(settingsPath);

        if (userModel.tokenValid) {
            userModel.flipToMainPage(this.window);
        } else {
            this.window.setTitle("InnoMetrics Login");
            userModel.flipToLoginPage(this.window);
        }

        this.window.setResizable(false);
        this.window.getIcons().add(new Image(this.getClass().getResource("/metrics-collector.png").toExternalForm()));
        this.window.show();
    }
    public static int getLatestVersion(){
        try{
            String result = new Scanner(new URL("https://innometric.guru:9091/V1/Admin/collector-version?osversion=LINUX").openStream(), "UTF-8").useDelimiter("\\A").next();
            return Integer.parseInt(result.trim().replaceAll("\\.",""));

        } catch (IOException ignore) {
            return 0;
        }
    }
    public static int getLocalVersion() {
        Properties prop = new Properties();
        String fileName = "/opt/datacollectorlinux/lib/app/DataCollectorLinux.cfg";
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ignored) {
        }
        try {
            prop.load(is);
        } catch (IOException ignored) {
        }
        String result = prop.getProperty("app.version").replaceAll("\\.","");
        return Integer.parseInt(result);
    }
}
