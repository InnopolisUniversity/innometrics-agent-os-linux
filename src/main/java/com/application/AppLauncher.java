package com.application;

import com.application.model.Model;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        this.window.setTitle("InnoMetrics Login");
        this.window.setMinWidth(360.0D);
        this.window.setMinHeight(350.0D);
        Path settingsPath = Paths.get("/opt/datacollectorlinux/lib/app/config.json");
        //Path settingsPath = Paths.get(AppLauncher.class.getResource("/config.json").getPath());
        userModel = new Model(settingsPath);
        userModel.setVersions(version_local, version_latest);
        userModel.setUpSystemTray(this.window);

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
