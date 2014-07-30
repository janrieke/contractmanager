package de.janrieke.contractmanager.util;

import java.io.File;

import de.willuhn.logging.Logger;

public class FileOpener {

	// hide the constructor.
	FileOpener() {
	}

	protected static int WAIT_TIME = 500;

	// Created the appropriate instance
	public static FileOpener getFileOpener() {

		String os = System.getProperty("os.name").toLowerCase();

		FileOpener desktop = new FileOpener();
		if (os.indexOf("windows") != -1 || os.indexOf("nt") != -1) {
			desktop = new WindowsFileOpener();
		} else if (os.equals("windows 95") || os.equals("windows 98")) {
			desktop = new Windows9xFileOpener();
		} else if (os.indexOf("mac") != -1) {
			desktop = new OSXFileOpener();
		} else if (os.indexOf("linux") != -1) {
			desktop = new XDGFileOpener();
		} else {
			throw new UnsupportedOperationException(String.format(
					"The platform %s is not supported ", os));
		}
		return desktop;
	}

	public boolean open(File file) {
		throw new UnsupportedOperationException();
	}
}

// one class per OS
class XDGFileOpener extends FileOpener {
	public boolean open(File file) {
		boolean result = false;
		try {
			Process proc = Runtime.getRuntime().exec(new String[] 
					{"xdg-open", file.getAbsolutePath()}, null, file.getParentFile());
			try {
			    Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}
			try {
				result = proc.exitValue() == 0;
			} catch (IllegalThreadStateException e) {
				// still running, should be OK then
				result = true;
			}
		} catch (Exception e) {
			Logger.warn("Error while executing command line: " + e.getMessage());
			return false;
		}
		return result;
	}
}

class OSXFileOpener extends FileOpener {
	public boolean open(File file) {
		boolean result = false;
		try {
			Process proc = Runtime.getRuntime().exec(new String[] 
					{"open", file.getAbsolutePath()}, null, file.getParentFile());
			try {
			    Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}
			try {
				result = proc.exitValue() == 0;
			} catch (IllegalThreadStateException e) {
				// still running, should be OK then
				result = true;
			}
		} catch (Exception e) {
			Logger.warn("Error while executing command line: " + e.getMessage());
			return false;
		}
		return result;
	}
}

class WindowsFileOpener extends FileOpener {
	public boolean open(File file) {
		boolean result = false;
		try {
			Process proc = Runtime.getRuntime().exec(
					"cmd /c start \"" + file.getAbsolutePath() + "\"");
			try {
			    Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}
			try {
				result = proc.exitValue() == 0;
			} catch (IllegalThreadStateException e) {
				// still running, should be OK then
				result = true;
			}
		} catch (Exception e) {
			Logger.warn("Error while executing command line: " + e.getMessage());
			return false;
		}
		return result;
	}
}

class Windows9xFileOpener extends FileOpener {
	public boolean open(File file) {
		boolean result = false;
		try {
			Process proc = Runtime.getRuntime().exec(
					"command.com /C start \"" + file.getAbsolutePath() + "\"");
			try {
			    Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}
			try {
				result = proc.exitValue() == 0;
			} catch (IllegalThreadStateException e) {
				// still running, should be OK then
				result = true;
			}
		} catch (Exception e) {
			Logger.warn("Error while executing command line: " + e.getMessage());
			return false;
		}
		return result;
	}
}
