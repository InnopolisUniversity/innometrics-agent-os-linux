//package ModelTest;
//
//import com.application.model.Model;
////import com.sun.javafx.application.PlatformImpl;
//import org.json.simple.JSONObject;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//
//public class ConfigLoadTest {
//
//    @Test
//    public  void t1(){
//        Assertions.assertFalse(false);
//    }
//    @Test
//    public  void t2(){
//        Assertions.assertFalse(false);
//    }
//    @Test
//    public  void t3(){
//        Assertions.assertFalse(false);
//    }
//
//
//
////    @BeforeAll
////    public static void Statup(){
////        //create test config files
////        PlatformImpl.startup(()->{});
////    }
////
////    @AfterAll
////    public static void Teardown() {
////        //Delete created config files
////        System.out.println("Invoked once after all test methods");
////    }
////
////    @Test
////    public void validConfigTest() {
////        Path configPath = Paths.get(this.getClass().getResource("/config.json").getPath());
////        Model testerModel = new Model(configPath);
////        JSONObject settings = testerModel.getUserSettingsJSON();
////        Assertions.assertNotNull(settings);
////    }
////
////    @Test
////    public void invalidConfigTest() {
////        Assertions.assertThrows(NullPointerException.class, () -> {
////            Path configPath = Paths.get(this.getClass().getResource("/configInvalid.json").getPath());
////        });
////
////    }
////
////    @Test
////    public void validEmptyConfigTest() {
////        Path configPath = Paths.get(this.getClass().getResource("/emptyTestConfig.json").getPath());
////        Model testerModel = new Model(configPath);
////        JSONObject settings = testerModel.getUserSettingsJSON();
////        Assertions.assertEquals(0, settings.size());
////    }
////
////    @Test
////    public void validNonEmptyConfigTest() {
////        Path configPath = Paths.get(this.getClass().getResource("/testConfig.json").getPath());
////        Model testerModel = new Model(configPath);
////        JSONObject settings = testerModel.getUserSettingsJSON();
////        Assertions.assertTrue(settings.keySet().size() != 0);
////    }
//
//}
//
