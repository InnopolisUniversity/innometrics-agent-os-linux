package com.application.UI;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class UpdatePage {
    public UpdatePage(){}

    public Scene getUpdateScene(){
        ProgressIndicator pi = new ProgressIndicator();

        Text text = new Text("Updating...");
        Platform.setImplicitExit(false);
        VBox vBox = new VBox(30);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(pi,text);
        return new Scene(vBox, 300, 200);
    }

}
