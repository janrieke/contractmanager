/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2014  Jan Rieke
 *
 *   ContractManager is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContractManager is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.janrieke.contractmanager.util;

import java.io.File;

import de.willuhn.logging.Logger;

public class FileOpener {

	// hide the constructor.
	FileOpener() {
	}

	protected final static int WAIT_TIME = 500;

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
			//		"cmd /c start \"" + file.getAbsolutePath() + "\"");
					"rundll32 SHELL32.DLL,ShellExec_RunDLL "+ "\"" + file.getAbsolutePath() + "\""); 
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
