package com.application.nativeimpl;

import com.application.data.Activity;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import com.application.model.Model;
import javafx.application.Platform;
import com.application.jnacontrib.x11.api.X;
import com.application.jnacontrib.x11.api.X.X11Exception;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class X11ProcFsWindowInfo extends ActiveWindowInfo {
	private interface Handle extends Library {
		Handle module = (Handle) Native.load("c", Handle.class);

		int readlink(String path, byte[] buffer, int size);
	}

	private static String readlink(String path) throws FileNotFoundException {
		byte[] buffer = new byte[300];
		int size = Handle.module.readlink(path, buffer, 300);
		if (size > 0)
			return new String(buffer, 0, size);
		else
			throw new FileNotFoundException(path);
	}

	private X.Display display;

	public String getActiveWindowProcess() {
		try {
			return readlink("/proc/" + display.getActiveWindow().getPID() + "/exe");
		} catch (FileNotFoundException | X11Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getActiveWindowCommand() {
		try {
			return new String(Files.readAllBytes(Paths.get("/proc/" + display.getActiveWindow().getPID() + "/cmdline"))).replaceAll("\0", " ");
		} catch (IOException | X11Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void getActivity(String pid, String title, String process, Model m){
		try{
			String[] args = new String[] {"/bin/bash", "-c", "ps -o user= -o %mem= -o %cpu= -o stat= -o command= -p "+pid };
			Process proc = new ProcessBuilder(args).start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line = reader.readLine();
			if (line != null){
				String[] a = line.split("\\s+");
				Clock clock = Clock.systemDefaultZone();
				ZonedDateTime t = clock.instant().atZone(ZoneId.systemDefault());
				String start_time = t.toLocalDateTime().toString();

				HashMap hashMap = new HashMap();
				hashMap.put("start_time",start_time);
				hashMap.put("activityType",a[0]);
				hashMap.put("idle_activity",a[3].trim().split("")[0]);
				hashMap.put("executable_name",process);
				hashMap.put("browser_title",title);
				hashMap.put("pid",pid);

				Activity currentActivity = new Activity();
				currentActivity.setActivityValues(m,hashMap);
				m.setAddActivity(currentActivity);
			}
			proc.waitFor();

		}catch (IOException | InterruptedException ignore) {
		}
	}

	public String getActiveWindowTitle() {
		try {
			return display.getActiveWindow().getTitle();
		} catch (X11Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void startListening(Model m){
		Runnable task = new Runnable() {
			@Override
			public void run() {
				runTask(m);
			}
		};
		// Run the task in a background thread
		Thread ActivitiesListenThread = new Thread(task);
		// Terminate the running thread if the application exits
		ActivitiesListenThread.setDaemon(true);
		// Start the thread
		ActivitiesListenThread.start();
		m.threadsContainer.put("ActivitiesListen",ActivitiesListenThread);
	}

	public void runTask(Model m){
		display = new X.Display();
		final AtomicBoolean stop = new AtomicBoolean(false);

		X11.XEvent event = new X11.XEvent();
		display.getRootWindow().selectInput(X11.PropertyChangeMask);
		//TODO: http://www.linuxquestions.org/questions/showthread.php?p=2431345#post2431345
		int currentProcess = 0;
		while (!stop.get()) {
			try{
				display.getRootWindow().nextEvent(event);
				event.setType(X11.XPropertyEvent.class);
				event.read();
				switch (event.type) {
					case X11.PropertyNotify:
						if (X11.INSTANCE.XGetAtomName(display.getX11Display(), event.xproperty.atom).equals("_NET_ACTIVE_WINDOW") && display.getActiveWindow().getID() != 0) {
							int nowProcess = display.getActiveWindow().getPID();
							if (nowProcess != currentProcess) {
								currentProcess = nowProcess;
								final String title = getActiveWindowTitle();
								final String process = getActiveWindowApplication();
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										getActivity(String.valueOf(nowProcess), title, process, m);
										//System.out.println("TITLE: " + title);
										m.setWindowName(process);
									}
								});
								m.setActivityEndTime();
							}
						}
						break;
				}
			} catch(X11Exception | NullPointerException ignore){
			}
		}
	}
}
