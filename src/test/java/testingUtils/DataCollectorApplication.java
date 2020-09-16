package testingUtils;

import com.application.AppLauncher;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.framework.junit.ApplicationTest;

public abstract class DataCollectorApplication extends ApplicationTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(AppLauncher.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

}
