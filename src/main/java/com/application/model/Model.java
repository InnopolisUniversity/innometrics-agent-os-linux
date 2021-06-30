package com.application.model;

import com.application.UI.LoginPage;
import com.application.UI.MainPage;
import com.application.UI.UpdatePage;
import com.application.collectorApi.DataCollectorAPI;
import com.application.data.Activity;
import com.application.data.SystemProcess;
import com.application.nativeimpl.ActiveWindowInfo;
import com.application.utils.DialogsAndAlert;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.PowerSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static javafx.scene.text.TextAlignment.CENTER;

public class Model {

	private final SettingsPersister settings;
	public boolean tokenValid, internetConnection = true;
	public volatile Label windowName = new Label("Application");
	public volatile Label updateNotification = new Label("");
	private String loginUsername, username, version_local, version_latest;
	private PasswordField loginPassword;
	private String token;
	public static String currentIP, currentMAC, currentOS;
	private Activity currentActivity = null;
	private Connection conn = null;
	private PreparedStatement insetStmt, ProcsinsetStmt = null;
	Queue <Activity> activitiesQueue = new LinkedList<>();
	Queue <SystemProcess> processesQueue = new LinkedList<>();
	public static final List<String> IdleStates = Arrays.asList("D","S","T","X","t","Z");
	public HashMap<String, Thread> threadsContainer = new HashMap<>();
	private final DataCollectorAPI API;
	public Text timerText = new Text("00:00:00");
	Timeline timeline;
	int mins = 0, secs = 0, hrs = 0;
	public float PrevBatteryEnergy = 0.0f;
	public SystemTray systemTray;
	public final List<String> measurementsCollected = Arrays.asList("Cpu","Mem","vRAM","BatteryCharge","BatteryStatus","BatteryDesignCapacity","BatteryCurrentCapacity","BatteryCurrentCharge","BatteryConsumption");


	//constructor
	public Model(Path settingsFile) {
		settings = new SettingsPersister(settingsFile);
		tokenValid = tokenIsValid(settings.get("tokenDate"));
		this.windowName.setPrefWidth(200);
		this.windowName.setWrapText(true);
		this.windowName.setAlignment(Pos.CENTER);
		this.updateNotification.setVisible(false);
		this.updateNotification.setFont(Font.font("Verdana", FontWeight.THIN, 11));
		this.updateNotification.setTextFill(Color.GREEN);
		this.updateNotification.setTextAlignment(CENTER);
		this.loginUsername = "";
		this.PrevBatteryEnergy = this.getBatteryEnergy();
		this.API = new DataCollectorAPI(settings.get("token"));
		initDatabase();

		timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				changeTimer(timerText);
			}
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.setAutoReverse(false);
	}

	public void setUpSystemTray(Stage window) {
		systemTray = SystemTray.get();
		if (systemTray != null) {
			systemTray.setImage(this.getClass().getResource("/metrics-collector.png"));
			systemTray.AUTO_SIZE = true;

			systemTray.getMenu().add(new dorkbox.systemTray.MenuItem("Open Data Collector", e -> {
				Platform.setImplicitExit(false);
				Platform.runLater(() -> {
					window.show();
					window.setIconified(false);
					window.toFront();
				});
			}));
			systemTray.getMenu().add(new dorkbox.systemTray.MenuItem("Minimize to tray", e -> Platform.runLater(() -> window.setIconified(true))));

			systemTray.getMenu().add(new MenuItem("Quit Data Collector", e -> {
				Platform.runLater(() -> {
					systemTray.shutdown();
					shutdown();
				});
			}));
			systemTray.setStatus("Running");
		}
	}

	public void setWindowName(final String newName){
		windowName.setText(newName);
	}
	public Label getWindowName() {
		return windowName;
	}

	public void setUpdateNotification(final String newName){
		updateNotification.setText(newName);
	}
	public Label getUpdateNotification(){
		return updateNotification;
	}
	public void startTimer(){ timeline.play(); }
	public void resetTimeline(){
		mins = 0;
		secs = 0;
		hrs = 0;
		timerText.setText("00:00:00");
	}
	public void changeTimer(Text text){
		secs++;
		if(secs == 60) {
			mins++;
			secs = 0;
		}
		if(mins == 60){
			hrs++;
			mins = 0;
		}
		text.setText((((hrs/10) == 0) ? "0" : "")+hrs + ":" + (((mins/10) == 0) ? "0" : "")+mins + ":" + (((secs/10) == 0) ? "0" : "")+secs);
	}


	/**
	 * Start all the background threads (active window listener and data saving to local db and data post to remote server)
	 */
	public void beginWatching() {
		ActiveWindowInfo.INSTANCE.startListening(this); //Active window capture thread
		startPostActivitiesToDB(); //Post to local SQLlite local db thread

		startWatchingProcesses(); //capture ps thread
		StartpostProcessesToDb(); //Post ps results to SQLlite DB (local) thread

		StartPostingData(); //Post activities and processes to remote DB thread

		checkUpdates();
	}
	public void endWatching(boolean cleanup) throws IOException {
		if (cleanup) {
			settings.cleanup(); //Reset the settings (delete settings)
		}
	}
	public void shutdown(){
		try{
			for(Thread value : threadsContainer.values()){
				value.interrupt();
			}
			addActivitiesToDb();
			cleanDb();
			vacuum();
			this.conn.close();
			systemTray.shutdown();
		} catch (Exception ex) {
			JSONObject sessionDetails = getCurrentSessionDetails();
			DialogsAndAlert.errorToDevTeam(ex,"Shutdown Ex",sessionDetails);
		}finally {
			Platform.exit();
		}
	}

	public void flipToMainPage(Stage window) throws SocketException {

		MainPage mainPage = new MainPage();
		Model.currentIP = MainPage.getLocalIP();
		Model.currentMAC = MainPage.getLocalMac();
		Model.currentOS = MainPage.getLocalOSVersion();

		this.loginUsername = settings.get("username");
		this.username = settings.get("loginUsername");

		window.setTitle("InnoMetrics Data Collector");
		window.setScene(mainPage.constructMainPage(this));
		window.setOnCloseRequest((event) -> {
			event.consume();
			window.setIconified(true);
		});
		beginWatching();
	}
	public Boolean flipToUpdatePage(Stage window){

		int version_local_num = Integer.parseInt(this.version_local.replaceAll("\\.",""));
		int version_latest_num = Integer.parseInt(this.version_latest.replaceAll("\\.",""));

		if (version_latest_num > version_local_num) {
			File f = new File("/tmp/DataCollectorLinux_tmp_dir/datacollectorlinux_"+version_latest+"-1_amd64.deb");
			if (f.exists() && !f.isDirectory()) {
				window.setTitle("InnoMetrics Updating");
				window.setMinWidth(260.0D);
				window.setMaxWidth(260.0D);
				window.setMinHeight(200.0D);
				window.setMaxHeight(200.0D);
				window.initStyle(StageStyle.UNDECORATED);

				UpdatePage updateScene = new UpdatePage();
				window.setScene(updateScene.getUpdateScene());

				Runnable task = () -> {
					try {
						String[] cmdScript = new String[]{"/bin/bash", "/opt/datacollectorlinux/lib/app/update.sh", "install", version_latest};
						Process procScript = Runtime.getRuntime().exec(cmdScript);
						procScript.waitFor();
					} catch (IOException | InterruptedException ignore) {
						System.out.println("Update Failed");
					}
				};
				Thread t1 = new Thread(task);
				t1.setDaemon(true);
				t1.start();
				return true;
			}
		}
		return false;
	}
	public void setLoginPageComponents(String loginUsername, PasswordField loginPassword) {

		this.loginUsername = loginUsername;
		this.username = loginUsername.split("@")[0];
		this.loginPassword = loginPassword;
	}
	public void flipToLoginPage(Stage window) throws IOException {
		endWatching(false);
		LoginPage startPage = new LoginPage();
		window.setScene(startPage.constructLoginPage(this,window));
	}
	public String getLoggedInSessionToken() {
		return this.token;
	}

	public String getUsername() {
		return this.username;
	}
	public void saveUsername(final TextField UsernameField) {
		String username = UsernameField.getText().trim();
		if (!username.isEmpty())
			//System.out.println("Saving user name!! ");
		settings.putSetting("username",username);
		settings.putSetting("loginUsername",username.split("@")[0]);
	}

	public String getLoginUsername() {
		return this.loginUsername;
	}
	public void setToken(String token){this.token = token;}

	public void updateLoinSettings(String loginRes, String loginUsername, PasswordField loginPassword){
		this.setLoginPageComponents(loginUsername,loginPassword);
		this.setToken(loginRes);
		API.setToken(this.token);
		settings.updateSettings(this);
	}
	public JSONObject getUserSettingsJSON(){
		return settings.getAllSettingsJson();
	}

	public void setAddActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}
	public void setActivityEndTime() {
		if (this.currentActivity != null){
			Clock clock = Clock.systemDefaultZone();
			ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
			this.currentActivity.setEndTime(t.toLocalDateTime().toString());
			this.activitiesQueue.add(this.currentActivity);
		}
	}

	/**
	 * Initialize the local database buy connecting to local or creating a new instance if not already existing
	 */
	public void initDatabase() {

		Path dbpath = Paths.get("/opt/datacollectorlinux/lib/app/userdb.db");

		if(Files.exists(dbpath)){
			this.conn = ConnectToDB(dbpath.toString());
			cleanDb();
			vacuum();
		}else{
			this.conn = ConnectToDB(dbpath.toString());
			createTable(conn);
		}
		String ProcsInsetStmtFirstPart = "collectedTime, ip_address, mac_address, ";
		String ProcsInsetStmtLastPart = ", osversion, pid, processName, userID, posted";

		List<String> res = new ArrayList<>();
		for (String s : this.measurementsCollected) {
			res.add("alternativeLabel" + s);
			res.add("capturedDate" + s);
			res.add("measurementTypeId" + s);
			res.add("value" + s);
		}
		String resultSqlStatement = "INSERT INTO processesReports(" + ProcsInsetStmtFirstPart + String.join(", ", res) + ProcsInsetStmtLastPart ;
		resultSqlStatement += ") values (" + "?, ".repeat(res.size() + 7) + "?)";

		try {
			insetStmt = this.conn.prepareStatement("INSERT INTO activitiesTable(activityType, browser_title, browser_url, end_time, executable_name, idle_activity, ip_address, mac_address, osversion," +
					" pid, start_time, userID, posted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ProcsinsetStmt = this.conn.prepareStatement(resultSqlStatement);

		}
		catch (SQLException ex){
			try {
				Files.delete(dbpath);
			} catch (IOException ignored) { }
			initDatabase();
			System.out.println("Local database tables error");
			/*JSONObject session_details = getCurrentSessionDetails();
			DialogsAndAlert.errorToDevTeam(ex,"Local database tables error",session_details);*/
		}
	}
	private JSONObject getCurrentSessionDetails(){
		JSONObject temp = new JSONObject();
		Clock clock = Clock.systemDefaultZone();
		ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
		String current_time = t.toLocalDateTime().toString();

		temp.put("creationdate",current_time);
		temp.put("dataCollectorVersion", version_local);
		temp.put("os", currentOS);
		temp.put("username", username);

		return temp;
	}
	private void createTable(Connection conn){
		try{
			String sql = "CREATE TABLE IF NOT EXISTS activitiesTable (\n"
					+ " activityID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
					+ " activityType text,\n"
					+ " browser_title text,\n"
					+ " browser_url text,\n"
					+ " end_time text,\n"
					+ " executable_name text,\n"
					+ " idle_activity text,\n"
					+ " ip_address text,\n"
					+ " mac_address text,\n"
					+ " osversion text,\n"
					+ " pid text,\n"
					+ " start_time text,\n"
					+ " userID text,\n"
					+ " posted INTEGER\n"
					+ ");";

			Statement createStmt = conn.createStatement();
			createStmt.execute(sql);

			List<String> temp = new ArrayList<>();
			for (String s : this.measurementsCollected) {
				temp.add(" alternativeLabel" + s + " text,\n");
				temp.add(" capturedDate" + s + " text,\n");
				temp.add(" measurementTypeId" + s + " text,\n");
				temp.add(" value" + s + " text,\n");
			}
			String sqlQueryPart = String.join("", temp);
			String processesTable = "CREATE TABLE IF NOT EXISTS processesReports (\n"
					+ " ProcID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
					+ " collectedTime text,\n"
					+ " ip_address text,\n"
					+ " mac_address text,\n"
					+  sqlQueryPart
					+ " osversion text,\n"
					+ " pid text,\n"
					+ " processName text,\n"
					+ " userID text,\n"
					+ " posted INTEGER\n"
					+ ");";

			Statement createProcessTableStmt = conn.createStatement();
			createProcessTableStmt.execute(processesTable);
		} catch (SQLException ex) {
			JSONObject session_details = getCurrentSessionDetails();
			DialogsAndAlert.errorToDevTeam(ex,"Local data storage create error",session_details);
		}

	}
	private Connection ConnectToDB(String dbPath) {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			//conn = DriverManager.getConnection("jdbc:sqlite::memory:");
			conn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
		} catch (ClassNotFoundException | SQLException ignored) {
		}
		return conn;
	}

	/**
	 * This method adds a activity to local database
	 */
	public void addActivitiesToDb() {
		while(!activitiesQueue.isEmpty()) {
			Activity tempActivity = activitiesQueue.remove();
			if (this.conn != null && this.insetStmt != null) {
				try {
					JSONObject activityJson = tempActivity.toJson();
					insetStmt.setString(1, (String) activityJson.get("activityType"));
					insetStmt.setString(2, (String) activityJson.get("browser_title"));
					insetStmt.setString(3, (String) activityJson.get("browser_url"));
					insetStmt.setString(4, (String) activityJson.get("end_time"));

					insetStmt.setString(5, (String) activityJson.get("executable_name"));
					insetStmt.setString(6, (String) activityJson.get("idle_activity"));
					insetStmt.setString(7, (String) activityJson.get("ip_address"));
					insetStmt.setString(8, (String) activityJson.get("mac_address"));

					insetStmt.setString(9, (String) activityJson.get("osversion"));
					insetStmt.setString(10, (String) activityJson.get("pid"));
					insetStmt.setString(11, (String) activityJson.get("start_time"));
					insetStmt.setString(12, (String) activityJson.get("userID"));
					insetStmt.setInt(13, 0);
					insetStmt.execute();

				} catch (SQLException | JSONException ignored) {
				}
			}
		}
	}
	private void startPostActivitiesToDB(){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					try {
						Thread.sleep(120000); //2 min
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					addActivitiesToDb();
				}
			}
		};
		Thread backgroundThread = new Thread(task);
		backgroundThread.setDaemon(true);
		backgroundThread.start();
		threadsContainer.put("PostActivitiesToDB",backgroundThread);
	}

	/**
	 * This method periodically (thread running in background) a posts activities from local database to remote database
	 */
	public void StartPostingData(){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					try {
						sendData();
						Thread.sleep(300000); //5 min
					} catch (InterruptedException e) {
						Platform.runLater(() -> {
							DialogsAndAlert.Infomation("Posting data fail");
						});
					}
				}
			}
		};
		Thread backgroundThread = new Thread(task);
		backgroundThread.setDaemon(true);
		backgroundThread.start();
	}

	/**
	 * read data from local database and send it to remote database (activities)
	 */
	public void sendData(){
		try {
			URL url = new URL("http://www.google.com");
			URLConnection connection = url.openConnection();
			connection.connect();
		}catch ( Exception ex){
			internetConnection = false;
			Platform.runLater(() -> {
				DialogsAndAlert.Infomation("No internet connection");
			});
			return;
		}
		JSONArray result = new JSONArray();
		//read from bd and set field sent to true
		Statement stmt = null;
		List <Integer>toUpdateIDs = new ArrayList<>();
		try {
			stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM activitiesTable WHERE posted = 0;" );
			int counter = 0;
			while (counter < 500 && rs.next() ) {
				JSONObject temp = new JSONObject();
				int activityID = rs.getInt("activityID");
				temp.put("activityID",String.valueOf(activityID));
				String activityType = rs.getString("activityType");
				temp.put("activityType",activityType);
				String browser_title=  rs.getString("browser_title");
				temp.put("browser_title",browser_title);
				String browser_url = rs.getString("browser_url");
				temp.put("browser_url",browser_url);
				String end_time = rs.getString("end_time");
				temp.put("end_time",end_time);

				String executable_name = rs.getString("executable_name");
				temp.put("executable_name",executable_name);
				String idle_activity = rs.getString("idle_activity");
				temp.put("idle_activity",idle_activity);
				String ip_address = rs.getString("ip_address");
				temp.put("ip_address",ip_address);
				String mac_address = rs.getString("mac_address");
				temp.put("mac_address",mac_address);

				String osversion = rs.getString("osversion");
				temp.put("osversion",osversion);
				String pid =rs.getString("pid");
				temp.put("pid",pid);
				String start_time = rs.getString("start_time");
				temp.put("start_time",start_time);
				String userID = rs.getString("userID");
				temp.put("userID",userID);

				result.add(temp);
				toUpdateIDs.add(activityID);
				counter++;
			}
			rs.close();
			stmt.close();
		} catch ( Exception ignored ) {
		}

		int activitiesPostResponse = API.post(result,"activities");
		if (activitiesPostResponse == 200) {
			try{
				String updateids = "("+ toUpdateIDs.stream().map(Object::toString)
						.collect(Collectors.joining(", ")) + ")";
				Statement updateStmt = this.conn.createStatement();
				String updateQuery = "UPDATE activitiesTable " +
						"SET posted = 1 WHERE activityID in "+updateids;
				updateStmt.executeUpdate(updateQuery);
				this.vacuum();
			}catch (Exception ignored){
			}

		}else{
			if(activitiesPostResponse != 0){
				DialogsAndAlert.Infomation("Data post issue with code ("+activitiesPostResponse+")");
			}
		}

		//Post processes
		List <Integer> ProcUpdateIDs = new ArrayList<>();
		JSONArray resultPr = new JSONArray();
		Statement procSelectstmt = null;
		try{
			procSelectstmt = this.conn.createStatement();
			ResultSet rs = procSelectstmt.executeQuery( "SELECT * FROM processesReports WHERE posted = 0;" );
			int counter = 0;
			while ( counter < 500 && rs.next()) {
				JSONObject temp = new JSONObject();

				String collectedTime = rs.getString("collectedTime");
				temp.put("collectedTime",collectedTime);
				String ip_address = rs.getString("ip_address");
				temp.put("ip_address",ip_address);
				String mac_address = rs.getString("mac_address");
				temp.put("mac_address",mac_address);

				JSONArray measurements = new JSONArray();

				JSONObject cpuObj = new JSONObject();
				String alternativeLabelCpu = rs.getString("alternativeLabelCpu");
				cpuObj.put("alternativeLabel",alternativeLabelCpu);
				String capturedDateCpu = rs.getString("capturedDateCpu");
				cpuObj.put("capturedDate",capturedDateCpu);
				String measurementTypeIdCpu = rs.getString("measurementTypeIdCpu");
				cpuObj.put("measurementTypeId",measurementTypeIdCpu);
				String valueCpu = rs.getString("valueCpu");
				cpuObj.put("value",valueCpu);
				measurements.add(cpuObj);

				JSONObject memObj = new JSONObject();
				String alternativeLabelMem = rs.getString("alternativeLabelMem");
				memObj.put("alternativeLabel",alternativeLabelMem);
				String capturedDateMem = rs.getString("capturedDateMem");
				memObj.put("capturedDate",capturedDateMem);
				String measurementTypeIdMem = rs.getString("measurementTypeIdMem");
				memObj.put("measurementTypeId",measurementTypeIdMem);
				String valueMem = rs.getString("valueMem");
				memObj.put("value",valueMem);
				measurements.add(memObj);

				JSONObject vRamObj = new JSONObject();
				String alternativeLabelvRam = rs.getString("alternativeLabelvRAM");
				vRamObj.put("alternativeLabel",alternativeLabelvRam);
				String capturedDatevRam = rs.getString("capturedDatevRAM");
				vRamObj.put("capturedDate",capturedDatevRam);
				String measurementTypeIdvRam = rs.getString("measurementTypeIdvRAM");
				vRamObj.put("measurementTypeId",measurementTypeIdvRam);
				String valuevRam = rs.getString("valuevRAM");
				vRamObj.put("value",valuevRam);
				measurements.add(vRamObj);

				JSONObject BattChageObj = new JSONObject(); //Battery Charge
				String BatteryCharge = rs.getString("alternativeLabelBatteryCharge");
				BattChageObj.put("alternativeLabel",BatteryCharge);
				String BatteryChargecapturedDate = rs.getString("capturedDateBatteryCharge");
				BattChageObj.put("capturedDate",BatteryChargecapturedDate);
				String IdBatteryCharge = rs.getString("measurementTypeIdBatteryCharge");
				BattChageObj.put("measurementTypeId",IdBatteryCharge);
				String valueBatteryCharge = rs.getString("valueBatteryCharge");
				BattChageObj.put("value",valueBatteryCharge);
				measurements.add(BattChageObj);

				JSONObject BatteryStatusObj = new JSONObject(); //Battery Status charging or not
				String alternativeLabelBatteryStatus = rs.getString("alternativeLabelBatteryStatus");
				BatteryStatusObj.put("alternativeLabel",alternativeLabelBatteryStatus);
				String capturedDateBatteryStatus = rs.getString("capturedDateBatteryStatus");
				BatteryStatusObj.put("capturedDate",capturedDateBatteryStatus);
				String IdBatteryStatus = rs.getString("measurementTypeIdBatteryStatus");
				BatteryStatusObj.put("measurementTypeId",IdBatteryStatus);
				String valueBatteryStatus = rs.getString("valueBatteryStatus");
				BatteryStatusObj.put("value",valueBatteryStatus);
				measurements.add(BatteryStatusObj);

				JSONObject BatteryDesignCapacityObj = new JSONObject(); //Battery Design Capacity
				String BatteryDesignCapacity = rs.getString("alternativeLabelBatteryDesignCapacity");
				BatteryDesignCapacityObj.put("alternativeLabel",BatteryDesignCapacity);
				String capturedDateBatteryDesignCapacity = rs.getString("capturedDateBatteryDesignCapacity");
				BatteryDesignCapacityObj.put("capturedDate",capturedDateBatteryDesignCapacity);
				String IdBatteryDesignCapacity = rs.getString("measurementTypeIdBatteryDesignCapacity");
				BatteryDesignCapacityObj.put("measurementTypeId",IdBatteryDesignCapacity);
				String valueBatteryDesignCapacity = rs.getString("valueBatteryDesignCapacity");
				BatteryDesignCapacityObj.put("value",valueBatteryDesignCapacity);
				measurements.add(BatteryDesignCapacityObj);

				JSONObject BatteryCurrentCapacityObj = new JSONObject(); //Battery Current Capacity
				String BatteryCurrentCapacity = rs.getString("alternativeLabelBatteryCurrentCapacity");
				BatteryCurrentCapacityObj.put("alternativeLabel",BatteryCurrentCapacity);
				String capturedDateBatteryCurrentCapacity = rs.getString("capturedDateBatteryCurrentCapacity");
				BatteryCurrentCapacityObj.put("capturedDate",capturedDateBatteryCurrentCapacity);
				String IdBatteryCurrentCapacity = rs.getString("measurementTypeIdBatteryCurrentCapacity");
				BatteryCurrentCapacityObj.put("measurementTypeId",IdBatteryCurrentCapacity);
				String valueBatteryCurrentCapacity = rs.getString("valueBatteryCurrentCapacity");
				BatteryCurrentCapacityObj.put("value",valueBatteryCurrentCapacity);
				measurements.add(BatteryCurrentCapacityObj);

				JSONObject BatteryCurrentChargeObj = new JSONObject(); //Battery Current Charge
				String BatteryCurrentCharge = rs.getString("alternativeLabelBatteryCurrentCharge");
				BatteryCurrentChargeObj.put("alternativeLabel",BatteryCurrentCharge);
				String capturedDateBatteryCurrentCharge = rs.getString("capturedDateBatteryCurrentCharge");
				BatteryCurrentChargeObj.put("capturedDate",capturedDateBatteryCurrentCharge);
				String IdBatteryCurrentCharge = rs.getString("measurementTypeIdBatteryCurrentCharge");
				BatteryCurrentChargeObj.put("measurementTypeId",IdBatteryCurrentCharge);
				String valueBatteryCurrentCharge = rs.getString("valueBatteryCurrentCharge");
				BatteryCurrentChargeObj.put("value",valueBatteryCurrentCharge);
				measurements.add(BatteryCurrentChargeObj);

				JSONObject BatteryConsumptionObj = new JSONObject(); //Battery Consumption
				String BatteryConsumption = rs.getString("alternativeLabelBatteryConsumption");
				BatteryConsumptionObj.put("alternativeLabel",BatteryConsumption);
				String capturedDateBatteryConsumption = rs.getString("capturedDateBatteryConsumption");
				BatteryConsumptionObj.put("capturedDate",capturedDateBatteryConsumption);
				String IdBatteryConsumption = rs.getString("measurementTypeIdBatteryConsumption");
				BatteryConsumptionObj.put("measurementTypeId",IdBatteryConsumption);
				String valueBatteryConsumption = rs.getString("valueBatteryConsumption");
				BatteryConsumptionObj.put("value",valueBatteryConsumption);
				measurements.add(BatteryConsumptionObj);

				temp.put("measurementReportList",measurements);

				String osversion = rs.getString("osversion");
				temp.put("osversion",osversion);
				String pid =rs.getString("pid");
				temp.put("pid",pid);
				String processName = rs.getString("processName");
				temp.put("processName",processName);
				String userID = rs.getString("userID");
				temp.put("userID",userID);

				int ProcID = rs.getInt("ProcID");
				ProcUpdateIDs.add(ProcID);
				resultPr.add(temp);
				counter++;
			}
			rs.close();
			procSelectstmt.close();

		} catch (SQLException ignored) {}

		int processesPostResponse = API.post(resultPr,"processesReport");
		if (processesPostResponse == 200) {
			try{
				String updateids = "("+ ProcUpdateIDs.stream().map(Object::toString)
						.collect(Collectors.joining(", ")) + ");";

				Statement updateStmt = this.conn.createStatement();
				String updateQuery = "UPDATE processesReports SET posted = 1 WHERE ProcID IN "+updateids;
				updateStmt.execute(updateQuery);
			}catch (Exception ignored){
			}

		} else{
			if(processesPostResponse != 0){
				DialogsAndAlert.Infomation("Data post issue with code ("+processesPostResponse+")");
			}
		}
	}

	/**
	 * This method adds a process to local database
	 */
	public void addProcessesToDb(){
		while(!this.processesQueue.isEmpty()) {
			SystemProcess tempProcess = this.processesQueue.remove();
			if (this.conn != null && this.ProcsinsetStmt != null) {
				try {
					JSONObject processJson = tempProcess.toJson();
					ProcsinsetStmt.setString(1, (String) processJson.get("collectedTime"));
					ProcsinsetStmt.setString(2, (String) processJson.get("ip_address"));
					ProcsinsetStmt.setString(3, (String) processJson.get("mac_address"));

					ProcsinsetStmt.setString(4, (String) processJson.getOrDefault("alternativeLabelCpu","Cpu"));
					ProcsinsetStmt.setString(5, (String) processJson.getOrDefault("capturedDateCpu", "2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(6, (String) processJson.getOrDefault("measurementTypeIdCpu",5));
					ProcsinsetStmt.setString(7, (String) processJson.getOrDefault("valueCpu",-1));

					ProcsinsetStmt.setString(8, (String) processJson.getOrDefault("alternativeLabelMem","Mem"));
					ProcsinsetStmt.setString(9, (String) processJson.getOrDefault("capturedDateMem","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(10, (String) processJson.getOrDefault("measurementTypeIdMem",3));
					ProcsinsetStmt.setString(11, (String) processJson.getOrDefault("valueMem",0.0));

					ProcsinsetStmt.setString(12, (String) processJson.getOrDefault("alternativeLabelvRAM","vRAM"));
					ProcsinsetStmt.setString(13, (String) processJson.getOrDefault("capturedDatevRAM","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(14, (String) processJson.getOrDefault("measurementTypeIdvRAM",4));
					ProcsinsetStmt.setString(15, (String) processJson.getOrDefault("valuevRAM",0.0));

					ProcsinsetStmt.setString(16, (String) processJson.getOrDefault("alternativeLabelBatteryCharge","BatteryCharge"));
					ProcsinsetStmt.setString(17, (String) processJson.getOrDefault("capturedDateBatteryCharge","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(18, (String) processJson.getOrDefault("measurementTypeIdBatteryCharge",1));
					ProcsinsetStmt.setString(19, (String) processJson.getOrDefault("valueBatteryCharge",0.0));

					ProcsinsetStmt.setString(20, (String) processJson.getOrDefault("alternativeLabelBatteryStatus","BatteryStatus"));
					ProcsinsetStmt.setString(21, (String) processJson.getOrDefault("capturedDateBatteryStatus","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(22, (String) processJson.getOrDefault("measurementTypeIdBatteryStatus",2));
					ProcsinsetStmt.setString(23, (String) processJson.getOrDefault("valueBatteryStatus",-1));

					ProcsinsetStmt.setString(24, (String) processJson.getOrDefault("alternativeLabelBatteryDesignCapacity","BatteryDesignCapacity"));
					ProcsinsetStmt.setString(25, (String) processJson.getOrDefault("capturedDateBatteryDesignCapacity","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(26, (String) processJson.getOrDefault("measurementTypeIdBatteryDesignCapacity",13));
					ProcsinsetStmt.setString(27, (String) processJson.getOrDefault("valueBatteryDesignCapacity",0.0));

					ProcsinsetStmt.setString(28, (String) processJson.getOrDefault("alternativeLabelBatteryCurrentCapacity","BatteryCurrentCapacity"));
					ProcsinsetStmt.setString(29, (String) processJson.getOrDefault("capturedDateBatteryCurrentCapacity","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(30, (String) processJson.getOrDefault("measurementTypeIdBatteryCurrentCapacity",12));
					ProcsinsetStmt.setString(31, (String) processJson.getOrDefault("valueBatteryCurrentCapacity",0.0));

					ProcsinsetStmt.setString(32, (String) processJson.getOrDefault("alternativeLabelBatteryCurrentCharge","BatteryCurrentCharge"));
					ProcsinsetStmt.setString(33, (String) processJson.getOrDefault("capturedDateBatteryCurrentCharge","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(34, (String) processJson.getOrDefault("measurementTypeIdBatteryCurrentCharge",11));
					ProcsinsetStmt.setString(35, (String) processJson.getOrDefault("valueBatteryCurrentCharge",0.0));

					ProcsinsetStmt.setString(36, (String) processJson.getOrDefault("alternativeLabelBatteryConsumption","BatteryConsumption"));
					ProcsinsetStmt.setString(37, (String) processJson.getOrDefault("capturedDateBatteryConsumption","2000-01-26T16:22:34.970980"));
					ProcsinsetStmt.setString(38, (String) processJson.getOrDefault("measurementTypeIdBatteryConsumption",6));
					ProcsinsetStmt.setString(39, (String) processJson.getOrDefault("valueBatteryConsumption",0.0));

					ProcsinsetStmt.setString(40, (String) processJson.get("osversion"));
					ProcsinsetStmt.setString(41, (String) processJson.get("pid"));
					ProcsinsetStmt.setString(42, (String) processJson.get("processName"));
					ProcsinsetStmt.setString(43, (String) processJson.get("userID"));
					ProcsinsetStmt.setInt(44, 0);
					ProcsinsetStmt.execute();

				} catch (SQLException | JSONException ignored) {
					System.out.println("Failed to post to local DB");
				}
			}
		}
	}

	/**
	 * This method periodically (thread running in background) a posts processes collected data to remote database
	 */
	public void StartpostProcessesToDb(){
		Task <Void> task = new Task() {
			@Override
			public Void call() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					try {
						Thread.sleep(180000); //3 min
						addProcessesToDb();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				return null;
			}
		};

		Thread processtoDb = new Thread(task);
		processtoDb.setDaemon(true);
		processtoDb.start();
		threadsContainer.put("processtoDb",processtoDb);
	}

	public void WatchProcesses(){

		try{
			String[] args = new String[] {"/bin/bash", "-c", "ps axco pid,command,%mem,%cpu,vsz --no-header"};
			Process proc = new ProcessBuilder(args).start();

			Clock clock = Clock.systemDefaultZone();
			ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
			String captureTime = t.toLocalDateTime().toString();

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;

			SystemInfo si = new SystemInfo();
			float designCapacity = getBatteryDesignCapacity();
			float currentBatteryEnergy = this.getBatteryEnergy();
			float temp = this.PrevBatteryEnergy - currentBatteryEnergy;
			if (temp < 0.0f){temp = 0.0f;}

			while((line = reader.readLine())!= null ){
				String[] processLine = line.strip().split("\\s+");
				String pid = processLine[0];
				String pName = processLine[1];
				SystemProcess tempProc = new SystemProcess();

				Map <String, JSONObject> measurements = new HashMap<>();

				JSONObject cpu = new JSONObject();
				cpu.put("alternativeLabel", "CPU%");
				cpu.put("capturedDate", captureTime);
				cpu.put("measurementTypeId", "5");
				cpu.put("value", processLine[3]);
				measurements.put("Cpu",cpu);

				JSONObject mem = new JSONObject();
				mem.put("alternativeLabel", "MEM%");
				mem.put("capturedDate", captureTime);
				mem.put("measurementTypeId", "3");
				mem.put("value", processLine[2]);
				measurements.put("Mem",mem);

				JSONObject vRAM = new JSONObject();
				vRAM.put("alternativeLabel", "vRAM");
				vRAM.put("capturedDate", captureTime);
				vRAM.put("measurementTypeId", "4");
				vRAM.put("value", processLine[4]);
				measurements.put("vRAM",vRAM);

				if (si.getHardware().getPowerSources().size() >= 1){
					List<PowerSource> PowerSources = si.getHardware().getPowerSources();
					for (PowerSource s1 : PowerSources) {
						JSONObject BAT = new JSONObject();
						BAT.put("alternativeLabel", "BatteryCharge");
						BAT.put("capturedDate", captureTime);
						BAT.put("measurementTypeId", "1");
						BAT.put("value", String.valueOf(s1.getRemainingCapacityPercent()));
						measurements.put("BatteryCharge", BAT);

						String batStat = s1.isCharging() ? "Charging" : "Discharging";
						JSONObject BATStat = new JSONObject();
						BATStat.put("alternativeLabel", "BatteryStatus");
						BATStat.put("capturedDate", captureTime);
						BATStat.put("measurementTypeId", "2");
						BATStat.put("value", batStat);
						measurements.put("BatteryStatus", BATStat);

						JSONObject designCap = new JSONObject(); // The design (original) capacity of the battery
						designCap.put("alternativeLabel", "BatteryDesignCapacity");
						designCap.put("capturedDate", captureTime);
						designCap.put("measurementTypeId", "13");
						designCap.put("value", String.valueOf(designCapacity));
						measurements.put("BatteryDesignCapacity", designCap);

						JSONObject currCap = new JSONObject(); // current fullCapacityBattery
						currCap.put("alternativeLabel", "BatteryCurrentCapacity");
						currCap.put("capturedDate", captureTime);
						currCap.put("measurementTypeId", "12");
						currCap.put("value", String.valueOf(s1.getCurrentCapacity()));
						measurements.put("BatteryCurrentCapacity", currCap);


						JSONObject currCharge = new JSONObject(); // The current (remaining) capacity of the battery.
						currCharge.put("alternativeLabel", "BatteryCurrentCharge");
						currCharge.put("capturedDate", captureTime);
						currCharge.put("measurementTypeId", "11");
						currCharge.put("value", String.valueOf(currentBatteryEnergy));
						measurements.put("BatteryCurrentCharge", currCharge);

						JSONObject BaterryConsumtion = new JSONObject();
						BaterryConsumtion.put("alternativeLabel", "BatteryConsumption");
						BaterryConsumtion.put("capturedDate", captureTime);
						BaterryConsumtion.put("measurementTypeId", "6");
						BaterryConsumtion.put("value", String.valueOf(temp));
						measurements.put("BatteryConsumption", BaterryConsumtion);

					}
				}

				tempProc.setProcessValues(this, measurements, captureTime, pid, pName);
				this.processesQueue.add(tempProc);
			}
			this.PrevBatteryEnergy = currentBatteryEnergy;
			proc.waitFor();

		}catch (InterruptedException | IOException ignored){
			System.out.println("catch InterruptedException : WatchProcesses()");
		}
	}

	private float getBatteryEnergy(){

		Path filePath = Paths.get("/sys/class/power_supply/BAT0/charge_now");
		try
		{
			String content = Files.readString(filePath);
			return  Float.parseFloat(content)/100000 ;
		}
		catch (IOException e) {return 0.0F;}
	}
	private float getBatteryDesignCapacity(){

		Path filePath = Paths.get("/sys/class/power_supply/BAT0/charge_full_design");
		try
		{
			String content = Files.readString(filePath);
			return  Float.parseFloat(content) ;
		}
		catch (IOException e) {return 0.0F;}
	}

	public void startWatchingProcesses(){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean stop = new AtomicBoolean(false);
				while(!stop.get()){
					try {
						WatchProcesses();
						Thread.sleep(120000); //2 min
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		Thread watchingProcessesThread = new Thread(task);
		watchingProcessesThread.setDaemon(true);
		watchingProcessesThread.start();
		threadsContainer.put("watchingProcessesThread",watchingProcessesThread);
	}

	private void cleanDb(){
		if (this.conn != null){
			try {
				//clear the activities table
				String deleteQuery = "DELETE FROM activitiesTable WHERE posted = 1;";
				PreparedStatement deleteStmt = this.conn.prepareStatement(deleteQuery);
				deleteStmt.executeUpdate();

				//clear the processes table
				String deleteQueryProcs = "DELETE FROM processesReports WHERE posted = 1;";
				PreparedStatement deleteStmtprocs = this.conn.prepareStatement(deleteQueryProcs);
				deleteStmtprocs.executeUpdate();

			} catch (SQLException ex) {
				Platform.runLater(() -> {
					JSONObject sessionDetails = getCurrentSessionDetails();
					DialogsAndAlert.errorToDevTeam(ex,"DB clean failure",sessionDetails);
				});
			}
		}
		String deleteQuery = "DELETE FROM activitiesTable WHERE posted = ?;";

		try (PreparedStatement pstmt = this.conn.prepareStatement(deleteQuery)) {

			pstmt.setInt(1, 1);
			pstmt.executeUpdate();

		} catch (SQLException ignore) {
		}
	}

	private boolean tokenIsValid(final String tokenDate){
		if (tokenDate.equals("Null")) {return false;}
		LocalDate today = LocalDate.now();
		today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date d1 = null;
		Date d2 = null;

		try {
			d2 = format.parse(today.toString());
			d1 = format.parse(tokenDate);

			long diff = d2.getTime() - d1.getTime();
			long diffDays = diff / (24 * 60 * 60 * 1000);

			return diffDays < 30;

		} catch (Exception e) {
			//todo : Log Exception
			return false;
		}
	}

	public void checkUpdates() {
		int version_local_num = Integer.parseInt(version_local.replaceAll("\\.",""));
		int version_latest_num = Integer.parseInt(version_latest.replaceAll("\\.",""));
		if (version_latest_num  <= version_local_num){
			return;
		}
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					String[] cmdScript = new String[]{"/bin/bash", "/opt/datacollectorlinux/lib/app/update.sh", "download", version_latest};
					Process procScript = Runtime.getRuntime().exec(cmdScript);
					procScript.waitFor();

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							updateNotification.setText("Latest update available \nRestart Data Collector to Update");
							updateNotification.setVisible(true);
						}
					});
				} catch (IOException | InterruptedException ignore) {
					System.out.println("Update download Failed");
				}
			}
		};
		Thread t1 = new Thread(task);
		t1.setDaemon(true);
		t1.start();
	}

	public boolean dBIntialized() {
		return this.conn != null;
	}
	public void vacuum(){
		if (dBIntialized()){
			return;
		}
		Statement stmt = null;
		try {
			stmt = this.conn.createStatement();
			stmt.executeUpdate("VACUUM");
		} catch (SQLException ignore) {}
	}

	public void setVersions(String version_local, String version_latest) {
		this.version_local = version_local;
		this.version_latest = version_latest;
	}

	public void setTrayStatus(String stat) {
		this.systemTray.setStatus(stat);
	}
}
