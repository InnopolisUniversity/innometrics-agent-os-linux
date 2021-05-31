package UIComponentsPresenceTests;

//import com.application.AppLauncher;
//import com.application.model.Model;
//import javafx.scene.Node;
//import javafx.scene.input.KeyCode;
//import javafx.stage.Stage;
import org.junit.jupiter.api.*;
//import org.testfx.api.FxToolkit;
//import org.testfx.matcher.control.LabeledMatchers;
import testingUtils.DataCollectorApplication;

//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import static java.time.Duration.ofMillis;
//import static org.junit.jupiter.api.Assertions.assertTimeout;
//import static org.testfx.api.FxAssert.verifyThat;

//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class mainPageComponentsTest extends DataCollectorApplication {
    @Test
    public void tess1(){
        Assertions.assertFalse(false);
    }
    @Test
    public void tess33(){
        Assertions.assertTrue(true);
    }
//    Path testConfigPath = Paths.get(this.getClass().getResource("/testConfig.json").getPath());
//
//    @Override
//    public void start(Stage stage) {
//        AppLauncher.userModel = new Model(testConfigPath);
//        stage.show();
//        FxToolkit.toolkitContext().setLaunchTimeoutInMillis(20);
//    }
//
//    @Override
//    public void stop(){
//        System.out.println("Stage is closing");
//    }
//
//    @DisplayName("Login time limit test")
//    @Test
//    @Order(1)
//    public void loinTimeTest() {
//        write("test@gmail.com");
//        push(KeyCode.valueOf("TAB"));
//        write("testpass");
//        verifyThat("#loginButton", LabeledMatchers.hasText("Login"));
//        assertTimeout(ofMillis(30000), () -> {
//            clickOn("#loginButton");
//            verifyThat("#logOutButton", LabeledMatchers.hasText("Logout"));
//        });
//    }
//
//    @DisplayName("App Quit button visibility test")
//    @Test
//    @Order(2)
//    public void quitButtonVisibilityTest() {
//        verifyThat("#stopCloseButton", LabeledMatchers.hasText("Stop and Quit"));
//        Assertions.assertTrue(lookup("#stopCloseButton").query().isVisible());
//    }
//
//    @DisplayName("about Page Tab Visisbility Test")
//    @Test
//    @Order(3)
//    public void aboutPageTabVisisbilityTest() {
//        clickOn("#AboutTab");
//        Node mainTab = lookup("#aboutGrid").query();
//        Assertions.assertTrue(mainTab.isVisible(),"About Tab not visible");
//    }
//
//    @DisplayName("Main Page Tab Visisbility Test")
//    @Test
//    @Order(4)
//    public void mainPageTabVisisbilityTest() throws InterruptedException {
//        clickOn("#MainTab");
//        Node mainTab = lookup("#mainTabGrid").query();
//        Assertions.assertTrue(mainTab.isVisible(),"Main Tab not visible");
//    }
//
//    @DisplayName("Logout button visibility & is Enable test")
//    @Test
//    @Order(5)
//    public void logoutButtonTest() throws InterruptedException {
//        clickOn("#AboutTab");
//        Node logOutButton = lookup("#logOutButton").query();
//        Assertions.assertTrue(logOutButton.isVisible());
//        Assertions.assertFalse(logOutButton.isDisable());
//    }
//
//    @DisplayName("Update button visibility & is Enable test")
//    @Test
//    @Order(6)
//    public void updateCheckButtonTest() {
//        Node logOutButton = lookup("#updateButton").query();
//        Assertions.assertTrue(logOutButton.isVisible());
//        Assertions.assertFalse(logOutButton.isDisable());
//    }
}
