package com.application.nativeimpl;

import com.sun.jna.Platform;
import com.application.model.Model;

import java.io.File;

public abstract class ActiveWindowInfo {
	public static ActiveWindowInfo INSTANCE;

	static {
		//TODO: OS X: proc_pidpath() from libproc.h (http://stackoverflow.com/a/8149380)
		//TODO: FreeBSD: sysctl CTL_KERN (http://stackoverflow.com/q/799679)
		if (Platform.isWindows())
			//System.out.println("We have windows OS here!!");
			INSTANCE = null;//new Win32WindowInfo();
		else
			INSTANCE = new X11ProcFsWindowInfo();
	}

	public abstract String getActiveWindowProcess();
	public abstract String getActiveWindowCommand();
	public abstract String getActiveWindowTitle();
	public abstract void startListening(Model model);

	public String getActiveWindowApplication() {
		String command = getActiveWindowCommand();
		if (command != null) {
			if (command.contains("org.eclipse.equinox.launcher"))
				return "eclipse";
		}
		command = getActiveWindowProcess();
		return command.substring(command.lastIndexOf(File.separatorChar) + 1);
	}
}
