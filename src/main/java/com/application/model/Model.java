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
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static javafx.scene.text.TextAlignment.CENTER;

public class Model {

	private final SettingsPersister settings;
	public boolean tokenValid, internetConnection = true;;
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
	public SystemTray systemTray;

	private final int browserEventsPort = 1487;

	//constructor
	public Model(Path settingsFile) {
		settings = new SettingsPersister(settingsFile);
		tokenValid = tokenIsValid(settings.get("tokenDate"));
		this.windowName.setPrefWidth(200);
		this.windowName.setWrapText(true);
		this.windowName.setAlignment(Pos.CENTER);
		this.updateNotification.setVisible(false);
		this.updateNotification.setFont(Font.font("Verdana", FontWeight.THIN, 12));
		this.updateNotification.setTextFill(Color.GREEN);
		this.updateNotification.setTextAlignment(CENTER);
		this.loginUsername = "";
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

	HttpServer server = null;
	ThreadPoolExecutor threadPoolExecutor = null;

	protected void setupBrowserEventsServer() {
	    threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		try {
			server = HttpServer.create(new InetSocketAddress("127.0.0.1", this.browserEventsPort), 0);
			server.createContext("/", new BrowserEventsHandler(this));
			server.setExecutor(threadPoolExecutor);
			server.start();
		} catch (IOException e) {
			System.err.println("Unable to setup browser tabs listener");
			e.printStackTrace();
		}
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

		setupBrowserEventsServer();
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
			server.stop(1);
			threadPoolExecutor.shutdown();
			addActivitiesToDb();
			cleanDb();
			vacuum();
			this.conn.close();
			systemTray.shutdown();
		} catch (Exception ex) {
			DialogsAndAlert.errorToDevTeam(ex,"Shutdown Ex");
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
		//File f = new File(this.getClass().getResource("/userdb.db").getPath());
		//String dbpath = f.getPath();
		Path dbpath = Paths.get("/opt/datacollectorlinux/lib/app/userdb.db");

		if(Files.exists(dbpath)){
			this.conn = ConnectToDB(dbpath.toString());
			cleanDb();
			vacuum();
		}else{
			this.conn = ConnectToDB(dbpath.toString());
			createTable(conn);
		}
		try {
			insetStmt = this.conn.prepareStatement("INSERT INTO activitiesTable(activityType, browser_title, browser_url, end_time, executable_name, idle_activity, ip_address, mac_address, osversion," +
					" pid, start_time, userID, posted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ProcsinsetStmt = this.conn.prepareStatement("INSERT INTO processesReports(collectedTime, ip_address, mac_address, alternativeLabelCpu, capturedDateCpu, measurementTypeIdCpu, valueCpu, alternativeLabelMem, capturedDateMem, measurementTypeIdMem, valueMem, osversion," +
					" pid, processName, userID, posted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		}
		catch (SQLException ex){
			DialogsAndAlert.errorToDevTeam(ex,"Local database tables error");
		}
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

			String processesTable = "CREATE TABLE IF NOT EXISTS processesReports (\n"
					+ " ProcID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
					+ " collectedTime text,\n"
					+ " ip_address text,\n"
					+ " mac_address text,\n"
					+ " alternativeLabelCpu text,\n"
					+ " capturedDateCpu text,\n"
					+ " measurementTypeIdCpu text,\n"
					+ " valueCpu text,\n"
					+ " alternativeLabelMem text,\n"
					+ " capturedDateMem text,\n"
					+ " measurementTypeIdMem text,\n"
					+ " valueMem text,\n"
					+ " osversion text,\n"
					+ " pid text,\n"
					+ " processName text,\n"
					+ " userID text,\n"
					+ " posted INTEGER\n"
					+ ");";

			Statement createProcessTableStmt = conn.createStatement();
			createProcessTableStmt.execute(processesTable);
		} catch (SQLException ex) {
			DialogsAndAlert.errorToDevTeam(ex,"Local data storage create error");
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
//						Thread.sleep(300000); //5 min
						Thread.sleep(5000); //5 s (for debugging)
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

			while ( rs.next() ) {
				JSONObject temp = new JSONObject();
				int activityID = rs.getInt("activityID");
				temp.put("activityID", activityID);
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
				temp.put("idle_activity",idle_activity.equals("true"));
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
			}catch (Exception ignored){
			}

		}else{
			if(activitiesPostResponse != 0){
//				DialogsAndAlert.Infomation("Data post issue with code ("+activitiesPostResponse+")");
				// causes 			"Not on FX application thread"
				System.err.println("Data post issue with code ("+activitiesPostResponse+")");

			}
		}

		//Post processes
		List <Integer> ProcUpdateIDs = new ArrayList<>();
		JSONArray resultPr = new JSONArray();
		Statement procSelectstmt = null;
		try{
			procSelectstmt = this.conn.createStatement();
			ResultSet rs = procSelectstmt.executeQuery( "SELECT * FROM processesReports WHERE posted = 0;" );

			while ( rs.next() ) {
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
				String measurementTypeIdMem = rs.getString("measurementTypeIdCpu");
				memObj.put("measurementTypeId",measurementTypeIdMem);
				String valueMem = rs.getString("valueCpu");
				memObj.put("value",valueMem);
				measurements.add(memObj);

				temp.put("measurementsReportList",measurements);

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
			}
			rs.close();
			procSelectstmt.close();

		} catch (SQLException ignored) {
		}

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

					ProcsinsetStmt.setString(4, (String) processJson.get("alternativeLabelCpu"));
					ProcsinsetStmt.setString(5, (String) processJson.get("capturedDateCpu"));
					ProcsinsetStmt.setString(6, (String) processJson.get("measurementTypeIdCpu"));
					ProcsinsetStmt.setString(7, (String) processJson.get("valueCpu"));

					ProcsinsetStmt.setString(8, (String) processJson.get("alternativeLabelMem"));
					ProcsinsetStmt.setString(9, (String) processJson.get("capturedDateMem"));
					ProcsinsetStmt.setString(10, (String) processJson.get("measurementTypeIdMem"));
					ProcsinsetStmt.setString(11, (String) processJson.get("valueMem"));

					ProcsinsetStmt.setString(12, (String) processJson.get("osversion"));
					ProcsinsetStmt.setString(13, (String) processJson.get("pid"));
					ProcsinsetStmt.setString(14, (String) processJson.get("processName"));
					ProcsinsetStmt.setString(15, (String) processJson.get("userID"));
					ProcsinsetStmt.setInt(16, 0);
					ProcsinsetStmt.execute();

				} catch (SQLException | JSONException ignored) {
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
//                        Thread.sleep(5000); //5 s (for debugging)
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
			String[] args = new String[] {"/bin/bash", "-c", "ps axco pid,command,%mem,%cpu --no-header"};
			Process proc = new ProcessBuilder(args).start();

			Clock clock = Clock.systemDefaultZone();
			ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
			String captureTime = t.toLocalDateTime().toString();

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;

			while((line = reader.readLine())!= null ){
				String[] processLine = line.split("\\s+");
				String pid = processLine[1];
				String pName = processLine[2];
				SystemProcess tempProc = new SystemProcess();

				Map <String, JSONObject> measurements = new HashMap<>();

				JSONObject cpu = new JSONObject();
				cpu.put("alternativeLabel", "CPU%");
				cpu.put("capturedDate", captureTime);
				cpu.put("measurementTypeId", "5");
				cpu.put("value", processLine[4]);
				measurements.put("Cpu",cpu);

				JSONObject mem = new JSONObject();
				mem.put("alternativeLabel", "MEM%");
				mem.put("capturedDate", captureTime);
				mem.put("measurementTypeId", "3");
				mem.put("value", processLine[3]);
				measurements.put("Mem",mem);

				tempProc.setProcessValues(this, measurements, captureTime, pid, pName);
				this.processesQueue.add(tempProc);
			}
			proc.waitFor();

		}catch (InterruptedException | IOException ignored){
		}
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
					DialogsAndAlert.errorToDevTeam(ex,"DB clean failure");
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
