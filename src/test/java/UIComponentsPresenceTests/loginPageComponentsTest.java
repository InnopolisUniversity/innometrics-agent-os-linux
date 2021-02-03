//package UIComponentsPresenceTests;
//
//import com.application.AppLauncher;
//import com.application.model.Model;
//import javafx.scene.Node;
//import javafx.stage.Stage;
//import org.junit.jupiter.api.*;
//import testingUtils.DataCollectorApplication;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
////@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class loginPageComponentsTest extends DataCollectorApplication {
////    Path kk = Paths.get(this.getClass().getResource("/emptyTestConfig.json").getPath());
////
////    @Override
////    public void start(Stage stage) {
////        AppLauncher.userModel = new Model(kk);
////        stage.show();
////    }
////
////    @Override
////    public void stop(){
////        AppLauncher.userModel.shutdown();
////    }
////
////    @Test
////    @Order(1)
////    public void loginStageTitleVisibilityTest() {
////        Node mainTab = lookup("#loginPage").query();
////        Assertions.assertTrue(mainTab.isVisible(),"Login page not visible");
////    }
////
////    @Test
////    @Order(2)
////    public void userLoginInputVisibilityTest() {
////        Node field = lookup("#userNameInput").query();
////        Assertions.assertTrue(field.isVisible(),"Username not visible");
////        Assertions.assertFalse(field.isDisabled(), "Username input field is Disabled");
////    }
////
////    @Test
////    @Order(3)
////    public void passwordFieldVisibilityTest() {
////        Node passwordField = lookup("#passwordField").query();
////        Assertions.assertTrue(passwordField.isVisible(),"password input field not visible");
////        Assertions.assertFalse(passwordField.isDisabled(), "password input field is Disabled");
////    }
////
////    @Test
////    @Order(4)
////    public void loginButtonVisibilityTest() {
////        Node logInButton = lookup("#loginButton").query();
////        Assertions.assertTrue(logInButton.isVisible(),"Login button not visible");
////        Assertions.assertFalse(logInButton.isDisable(),"Login button is disabled");
////    }
//}
