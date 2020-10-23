package com.application;

import com.application.model.Model;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.Scanner;

public class AppLauncher extends Application {
    public Stage window;
    public static Model userModel = null;
    public static String version_local = "0", version_latest = "0";


    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        File file = new File(userHome, "my.lock");
        try {
            FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            FileLock lock = fc.tryLock();
            if (lock == null) {
                System.out.println("another instance is already running");
                System.exit(0);
            }else {
                launch(args);
            }
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        this.window = primaryStage;
        this.window.initStyle(StageStyle.UTILITY);

        this.window.setMinWidth(360.0D);
        this.window.setMaxWidth(360.0D);
        this.window.setMinHeight(390.0D);
        this.window.setMaxHeight(390.0D);

        Path settingsPath = Paths.get("/opt/datacollectorlinux/lib/app/config.json");
        try {
            version_latest = getLatestVersion().trim();
            version_local = getLocalVersion().trim();
        }catch (Exception ignore){}

        userModel = new Model(settingsPath);
        userModel.setVersions(version_local, version_latest);
        userModel.setUpSystemTray(this.window);

        this.window.setOnCloseRequest((event) -> {
            event.consume();
            window.setIconified(true);
        });
        Boolean updt = userModel.flipToUpdatePage(this.window);
        this.window.getIcons().add(new Image(this.getClass().getResource("/metrics-collector.png").toExternalForm()));

        this.window.setResizable(false);
        if (!updt){

            if (userModel.tokenValid) {
                userModel.flipToMainPage(this.window);
                this.window.setIconified(true);
                this.window.show();
            } else {
                this.window.setTitle("InnoMetrics Login");
                userModel.flipToLoginPage(this.window);
                this.window.show();
                this.window.toFront();
            }
        }else {
            this.window.show();
            if (userModel.systemTray != null){
                userModel.setTrayStatus("Updating");
            }
        }
    }

    public static String getLatestVersion(){
        try{
            String result = new Scanner(new URL("https://innometric.guru:9091/V1/Admin/collector-version?osversion=LINUX").openStream(), "UTF-8").useDelimiter("\\A").next();
            return result;

        } catch (IOException ignore) {
            return "0.0.0";
        }
    }

    public static String getLocalVersion() {
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
        return prop.getProperty("app.version");
    }
}
