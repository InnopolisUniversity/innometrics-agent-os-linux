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

    public static void main(String[] args) {
        launch(args);
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

        if (userModel.tokenValid) {
            userModel.flipToMainPage(this.window);
        } else {
            userModel.flipToLoginPage(this.window);
        }

        this.window.setResizable(false);
        this.window.getIcons().add(new Image(this.getClass().getResource("/metrics-collector.png").toExternalForm()));
        this.window.show();
    }
}
