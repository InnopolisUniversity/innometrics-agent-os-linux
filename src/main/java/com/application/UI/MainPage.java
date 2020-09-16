package com.application.UI;

import com.application.AppLauncher;
import com.application.model.Model;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import static javafx.scene.text.TextAlignment.CENTER;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;

public class MainPage {
    public MainPage(){}

    public static String getLocalIP(){
        //Get IP-address
        String HostIp = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            HostIp = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return HostIp;
    }
    public static String getLocalMac() throws SocketException {
        //Get MAC-address
        String macAdrs = "00-00-00-00-00";
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        NetworkInterface inter;
        while (networks.hasMoreElements()) {
            inter = networks.nextElement();
            byte[] mac = inter.getHardwareAddress();
            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                macAdrs = sb.toString();
            }
        }

        return macAdrs;
    }

    public static String getLocalOSVersion(){
        try {
            String content = Files.readString(Paths.get("/etc/issue.net"));
            return content;
        } catch (IOException e) {
            //e.printStackTrace();
            return "Linux";
        }
    }
    public static String geCurrCollectorVersion(){

        Properties prop = new Properties();
        String fileName = "/opt/datacollectorlinux/lib/app/DataCollectorLinux.cfg";
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
        }
        try {
            prop.load(is);
        } catch (IOException ex) {
        }
        return prop.getProperty("app.version");
    }


    private GridPane getMainTab(Model m) throws SocketException {
        //Main Tab contents
        GridPane mainGrid = new GridPane();
        mainGrid.setAlignment(Pos.TOP_CENTER);
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(30, 15, 5, 15));

        Text currentSess = new Text("Current session");
        currentSess.setFont(Font.font(currentSess.getFont().toString(), FontWeight.BOLD, 15));
        HBox mainTitleBox = new HBox();
        mainTitleBox.setAlignment(Pos.CENTER);
        mainTitleBox.getChildren().add(currentSess);
        mainGrid.add(mainTitleBox, 0, 0, 2, 1);

        final Label UserName = new Label("User Name:");
        final Label HostIpAdrs = new Label("IP-address:");
        final Label HostMacAdrs = new Label("Mac-address:");
        final Label HostOS = new Label("Operating System:");

        Label HostOsVal = new Label(this.getLocalOSVersion());
        Label HostIpAdrsVal = new Label(this.getLocalIP());
        Label UserNameVal = new Label(m.getUsername());
        Label HostMacAdrsVal = new Label(this.getLocalMac());

        VBox vBox1 = new VBox(10);
        vBox1.setAlignment(Pos.CENTER_LEFT);
        vBox1.getChildren().addAll(HostOsVal,UserNameVal,HostIpAdrsVal,HostMacAdrsVal);
        mainGrid.add(vBox1,1,1);

        VBox vBox2 = new VBox(10);
        vBox2.setMinWidth(130);
        vBox2.setAlignment(Pos.CENTER_LEFT);
        vBox2.getChildren().addAll(HostOS,UserName,HostIpAdrs,HostMacAdrs);
        mainGrid.add(vBox2,0,1);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(10,0,5,0));
        mainGrid.add(separator,0,2,2,1);

        //Focus Application
        Text activeapp = new Text("Current Focused Window");
        activeapp.setFont(Font.font(activeapp.getFont().toString(), FontWeight.BOLD, 15));
        HBox activeappTitleBox = new HBox();
        activeappTitleBox.setAlignment(Pos.CENTER);
        activeappTitleBox.getChildren().add(activeapp);
        mainGrid.add(activeappTitleBox, 0, 3, 2, 1);

        //focused window process name
        TextFlow flow = new TextFlow();
        flow.setTextAlignment(CENTER);
        Label windowName = m.getWindowName();
        flow.getChildren().add(windowName);

        VBox focusedVBox = new VBox(10);
        focusedVBox.setAlignment(Pos.CENTER);
        focusedVBox.getChildren().add(flow);
        mainGrid.add(focusedVBox,0,4,2,1);

        Button stopCloseButton = new Button();
        stopCloseButton.setStyle("-fx-background-color: #399cbd; -fx-text-fill: white");
        stopCloseButton.setText("Stop and Quit");
        stopCloseButton.setFont(Font.font("Verdana",FontWeight.BOLD,15));
        stopCloseButton.setPadding(new Insets(10));
        stopCloseButton.setId("stopCloseButton");

        stopCloseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Sign out Confirmation");
                alert.setHeaderText("Are you sure you want to quit the Data collector?");
                alert.setContentText("Do you really want to quit \"Innometrics Data Collector\" ?");

                ButtonType buttonYes = new ButtonType("Yes");
                ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonYes,buttonCancel);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonYes){
                    m.shutdown();
                }
            }
        });

        HBox stopCloseHbox = new HBox(10);
        stopCloseHbox.setAlignment(Pos.CENTER);
        stopCloseHbox.setPadding(new Insets(10));
        stopCloseHbox.getChildren().add(stopCloseButton);

        mainGrid.add(stopCloseHbox,0,5,2,1);
        mainGrid.setId("mainTabGrid");

        return mainGrid;
    }

    private GridPane getAboutTab(Model m){
        //About Tab
        GridPane aboutGrid = new GridPane();
        aboutGrid.setAlignment(Pos.CENTER);
        aboutGrid.setHgap(10);
        aboutGrid.setVgap(10);
        aboutGrid.setPadding(new Insets(10, 5, 10, 5));

        //Add InnoMetrics Icon
        Image image = new Image(this.getClass().getResource("/metrics-collector.png").toExternalForm());
        HBox hbimg = new HBox(10);
        hbimg.setAlignment(Pos.BOTTOM_CENTER);
        hbimg.getChildren().add(new ImageView(image));
        aboutGrid.add(hbimg, 0, 0);

        VBox aboutVbox = new VBox(10);
        aboutVbox.setAlignment(Pos.CENTER);
        final Label collectorVersion = new Label("Version "+geCurrCollectorVersion());
        collectorVersion.setTextAlignment(CENTER);
        collectorVersion.setFont(Font.font(collectorVersion.getFont().toString(), FontWeight.LIGHT, 15));
        aboutVbox.getChildren().add(collectorVersion);

        //User account
        final Label usern = new Label("Logged in as");
        usern.setFont(Font.font( usern.getFont().toString(),FontWeight.BOLD,15 ));
        usern.setTextAlignment(CENTER);
        final Label LoginUsername = new Label(m.getLoginUsername());
        LoginUsername.setFont(Font.font( LoginUsername.getFont().toString(),FontWeight.LIGHT,15 ));
        LoginUsername.setTextAlignment(CENTER);
        LoginUsername.setWrapText(true);
        aboutVbox.setFillWidth(true);
        aboutVbox.getChildren().add(usern);
        aboutVbox.getChildren().add(LoginUsername);
        aboutGrid.add(aboutVbox,0,1);

        // add logout and update check
        HBox hboxLogInUpdate = new HBox(15);
        hboxLogInUpdate.setAlignment(Pos.BOTTOM_CENTER);
        Button logOutBtn = new Button("Logout");
        logOutBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        logOutBtn.setId("logOutButton");

        Button updateBtn = new Button("Check for updates");
        updateBtn.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        updateBtn.setId("updateButton");

        hboxLogInUpdate.setPadding(new Insets(20,0,5,0));
        hboxLogInUpdate.getChildren().addAll(logOutBtn,updateBtn);
        aboutGrid.add(hboxLogInUpdate,0,2);

        logOutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Sign out Confirmation");
                alert.setHeaderText("This action will log you out and reset your settings");
                alert.setContentText("Are you ok with this?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    try {
                        m.endWatching(true);
                        m.shutdown();
                        //m.flipToLoginPage((Stage) logOutBtn.getScene().getWindow());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });


        updateBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                updateBtn.setDisable(true);
                boolean updatesAvailable = m.checkUpdates();
                if (updatesAvailable){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Update downloads");
                    alert.setHeaderText("Update is available!");
                    alert.setContentText("This action will download & install updates. \nDo you want to install the updates?");

                    ButtonType buttonInstall = new ButtonType("Install");
                    ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonInstall,buttonCancel);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == buttonInstall){
                        m.shutdown();
                    }else { updateBtn.setDisable(false); }
                }
            }
        });
        aboutGrid.setId("aboutGrid");
        return aboutGrid;
    }

    public Scene constructMainPage(Model m) throws SocketException {

        TabPane tabPane = new TabPane();

        Tab tab1 = new Tab("Main");
        tab1.setId("MainTab");
        GridPane mainGrid = this.getMainTab(m);
        tab1.setContent(mainGrid);

        Tab tab3 = new Tab("About");
        tab3.setId("AboutTab");
        GridPane aboutGrid = this.getAboutTab(m);
        tab3.setContent(aboutGrid);

        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab3);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.tabMinWidthProperty().set(155);//set the tabPane's tabs min and max widths to be the same.
        tabPane.tabMaxWidthProperty().set(155);

        //set the tabPane's minWidth and maybe max width to the tabs combined width + a padding value
        tabPane.setMinWidth((100 * tabPane.getTabs().size()) + 55);
        tabPane.setPrefWidth((100 * tabPane.getTabs().size()) + 55 );

        VBox vBox = new VBox(tabPane);
        vBox.setAlignment(Pos.TOP_CENTER);

        return new Scene(vBox,360, 350);
    }
}
