package com.application.UI;

import com.application.model.Model;
import com.application.utils.DialogsAndAlert;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginPage {
    public LoginPage(){}

    private static final String uri = "https://innometric.guru:9091/login"; //production server
    //private static final String uri = "http://10.90.138.244:9091/login"; //dev server
    public static String token = "";

    private static String login(String username, String password) throws JSONException {
        final String projectID = "1234";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("email", username);
        jsonBody.put("password", password);
        jsonBody.put("projectID", projectID);

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        try {
            HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                JSONObject responseBody = new JSONObject(response.body().toString());
                token = responseBody.get("token").toString();
            }
        } catch (Exception ex) {
            DialogsAndAlert.errorToDevTeam(ex,"Auth request");
        }
        return token;
    }

    public Scene constructLoginPage(Model m, Stage window){
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(5, 10, 5, 10));
        loginGrid.setId("loginPage");

        //Set login scene title
        final Label scenetitle = new Label("Login to Data Collector");
        scenetitle.setMaxWidth(Double.MAX_VALUE);
        scenetitle.setAlignment(Pos.CENTER);
        scenetitle.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        scenetitle.setPadding(new Insets(25, 25, 25, 25));
        loginGrid.add(scenetitle, 0, 0, 2, 1);

        //Adding Nodes to loin GridPane layout
        Label userName = new Label("Login");
        final TextField txtUserName = new TextField("g.dlamini@innopolis.university");
        //final TextField txtUserName = new TextField("test@gmail.com");
        txtUserName.setId("userNameInput");

        //password field
        // text field to show password as unmasked
        final TextField passTextField = new TextField();
        passTextField.setManaged(false);
        passTextField.setVisible(false);

        // Actual password field
        final PasswordField passwordField = new PasswordField();
        passwordField.setText("InnoMetrics$2020");
        //passwordField.setText("testpass");
        passwordField.setId("passwordField");

        CheckBox checkBox = new CheckBox("Show/Hide password");
        passTextField.managedProperty().bind(checkBox.selectedProperty());
        passTextField.visibleProperty().bind(checkBox.selectedProperty());

        passwordField.managedProperty().bind(checkBox.selectedProperty().not());
        passwordField.visibleProperty().bind(checkBox.selectedProperty().not());

        // Bind the textField and passwordField text values bidirectionally.
        passTextField.textProperty().bindBidirectional(passwordField.textProperty());

        VBox passwordFieldvbox = new VBox(10);
        passwordFieldvbox.getChildren().addAll(passwordField, passTextField);
        Label lblPassword = new Label("Password");

        //Login Button
        Button btnLogin = new Button("Login");
        btnLogin.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_CENTER);
        hbBtn.setPadding(new Insets(10, 5, 5, 5));
        hbBtn.getChildren().add(btnLogin);

        //Optional auth status
        final Label lblMessage = new Label();
        lblMessage.setMaxWidth(Double.MAX_VALUE);
        lblMessage.setAlignment(Pos.CENTER);
        lblMessage.setTextFill(Color.FIREBRICK);

        // Adding nodes to Login grid
        loginGrid.add(userName, 0, 1);
        loginGrid.add(txtUserName, 1, 1);
        loginGrid.add(lblPassword, 0, 2);
        loginGrid.add(passwordFieldvbox, 1, 2);
        loginGrid.add(checkBox, 1, 3);
        loginGrid.add(hbBtn, 1, 4);
        loginGrid.add(lblMessage, 1, 5);

        btnLogin.setId("loginButton");
        btnLogin.setOnAction(new EventHandler<ActionEvent>() {
            private void loggedIn() throws JSONException {
                m.saveUsername(txtUserName);
                btnLogin.setDisable(true);
            }
            @Override
            public void handle(ActionEvent e) {
                btnLogin.setDisable(true);
                String username = txtUserName.getText();
                String password = passwordField.getText();
                try {
                    String loginRes = login(username, password);

                    if(!loginRes.equals("")) {
                        m.updateLoinSettings(loginRes,username,passwordField);
                        lblMessage.setTextFill(Color.GREEN);
                        lblMessage.setText("Login Success");

                        //DialogsAndAlert.collectSysProperties();
                        loggedIn();
                        window.setOnCloseRequest((event) -> {
                            event.consume();
                            window.setIconified(true); });

                        m.flipToMainPage(window);

                    } else {
                        lblMessage.setText("Login failed. Try again");
                        btnLogin.setDisable(false);
                    }
                } catch (JSONException | SocketException ex) {
                    ex.printStackTrace();
                    lblMessage.setText("Login failed. Try again");
                }
            }
        });
        return new Scene(loginGrid, 360, 350);
    }

}
