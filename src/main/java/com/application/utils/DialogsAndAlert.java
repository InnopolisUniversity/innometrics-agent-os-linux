package com.application.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Pair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DialogsAndAlert {
    public static void Infomation(String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(info);

        alert.showAndWait();
    }

    public static void warning(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Data collector Warning");
        alert.setContentText(text);

        alert.showAndWait();
    }

    public static void errorToDevTeam(Exception ex, final String head) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception");
        alert.setHeaderText(head);
        alert.setContentText(ex.getClass().getSimpleName());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        ButtonType buttonReport = new ButtonType("Report");
        ButtonType buttonTypeCancel = new ButtonType("Don't report", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(buttonTypeCancel,buttonReport);

        alert.showAndWait();
    }

    private static void simpleError(Exception ex, final String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(ex.getClass().getSimpleName());

        alert.showAndWait();
    }

    public static void collectSysProperties(){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> event.consume());

        dialog.setTitle("System hardware Properties");
        dialog.setHeaderText("Please enter your system hardware properties \n\nTerminal with command : 'lscpu'");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType);

        TextField textFieldCPU = new TextField();
        textFieldCPU.setPromptText("i.e Intel(R) Core(TM) i3-4160 CPU @ 3.60GHz");
        TextField systemType = new TextField();
        systemType.setPromptText("Laptop or Desktop");
        TextField numThreads = new TextField();
        numThreads.setPromptText("4");
        TextField diskType = new TextField();
        diskType.setPromptText("SSD");

        ObservableList<String> options = FXCollections.observableArrayList(new ArrayList<>(List.of("64-bits", "32-bits", "32-bit, 64-bit")));
        ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();

        ObservableList<String> drivesoptions = FXCollections.observableArrayList(new ArrayList<>(List.of("HDD", "SSD", "SSD & HDD")));
        ComboBox<String> driveComboBox = new ComboBox<>(drivesoptions);
        driveComboBox.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        grid.add(new Label("CPU Model name"), 0, 0);
        grid.add(textFieldCPU, 1, 0);

        grid.add(new Label("System type"), 0, 1);
        grid.add(systemType, 1, 1);

        grid.add(new Label("Thread(s) per core"), 0, 2);
        grid.add(numThreads, 1, 2);

        grid.add(new Label("Disk type"), 0, 3);
        grid.add(driveComboBox, 1, 3);

        grid.add(new Label("CPU op-mode(s): "), 0, 4);
        grid.add(comboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<Pair<String, String>> result = dialog.showAndWait();

    }
}