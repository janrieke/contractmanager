/*
 *   This file is part of This file is part of ContractManager for Jameica..
 *   Copyright (C) 2010-2011  Jan Rieke
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
package de.janrieke.contractmanager;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import de.janrieke.contractmanager.rmi.ContractDBService;
import de.janrieke.contractmanager.server.SettingsUtil;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * This class holds some settings for our plugin.
 * 
 * @author willuhn, jrieke 
 */
public class Settings {

	private static DBService db;
	private static I18N i18n;

	/**
	 * Our DateFormatter.
	 */
	//public final static DateFormat DATEFORMAT = DateFormat.getDateInstance(
	//		DateFormat.DEFAULT, Application.getConfig().getLocale());
	public final static DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");


	/**
	 * Our decimal formatter.
	 */
	public final static DecimalFormat DECIMALFORMAT = (DecimalFormat) DecimalFormat
			.getInstance(Application.getConfig().getLocale());

	/**
	 * Our currency name.
	 */
	public final static String CURRENCY = "EUR";

	static {
		DECIMALFORMAT.setMinimumFractionDigits(2);
		DECIMALFORMAT.setMaximumFractionDigits(2);
	}

	/**
	 * Small helper function to get the database service.
	 * 
	 * @return db service.
	 * @throws RemoteException
	 */
	public static DBService getDBService() throws RemoteException {
	    if (db != null)
	        return db;
	  		try {
	  			db = (ContractDBService) Application.getServiceFactory().lookup(ContractManagerPlugin.class,"contract_db");
	  			return db;
	  		}
	      catch (ConnectException ce)
	      {
	        // Die Exception fliegt nur bei RMI-Kommunikation mit fehlendem RMI-Server
	        I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();
	        String host = Application.getServiceFactory().getLookupHost(ContractManagerPlugin.class,"contract_db");
	        int    port = Application.getServiceFactory().getLookupPort(ContractManagerPlugin.class,"contract_db");
	        String msg = i18n.tr("Hibiscus-Server \"{0}\" nicht erreichbar", (host + ":" + port));
	        try
	        {
	          Application.getCallback().notifyUser(msg);
	          throw new RemoteException(msg);
	        }
	        catch (Exception e)
	        {
	          Logger.error("error while notifying user",e);
	          throw new RemoteException(msg);
	        }
	      }
	      catch (ApplicationException ae)
	      {
	        // Da interessiert uns der Stacktrace nicht
	        throw new RemoteException(ae.getMessage());
	      }
	      catch (RemoteException re)
	      {
	        throw re;
	      }
	  		catch (Exception e)
	  		{
	  			throw new RemoteException("unable to open/create database",e);
	  		}
	}

	/**
	 * Small helper function to get the translator.
	 * 
	 * @return translator.
	 */
	public static I18N i18n() {
		if (i18n != null)
			return i18n;
		i18n = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getI18N();
		return i18n;
	}

	public static int getExtensionWarningTime() throws RemoteException {
		return Integer.parseInt(SettingsUtil.get("extension_warning_time", "7"));
	}

	public static void setExtensionWarningTime(int time) throws RemoteException, ApplicationException {
		SettingsUtil.set("extension_warning_time", ((Integer)time).toString());
	}

	public static int getExtensionNoticeTime() throws RemoteException {
		return Integer.parseInt(SettingsUtil.get("extension_notice_time", "30"));
	}

	public static void setExtensionNoticeTime(int time) throws RemoteException, ApplicationException {
		SettingsUtil.set("extension_notice_time", ((Integer)time).toString());
	}

	public static boolean getICalAutoExport() throws RemoteException {
		return Boolean.parseBoolean(SettingsUtil.get("ical_auto_export", "false"));
	}

	public static void setICalAutoExport(boolean export) throws RemoteException, ApplicationException {
		SettingsUtil.set("ical_auto_export", ((Boolean)export).toString());
	}
	
	public static String getICalFileLocation() throws RemoteException {
		return SettingsUtil.get("ical_file", "");
	}

	public static void setICalFileLocation(String file) throws RemoteException, ApplicationException {
		SettingsUtil.set("ical_file", file);
	}

	public static boolean getNamedICalExport() throws RemoteException {
		return Boolean.parseBoolean(SettingsUtil.get("ical_name_export", "false"));
	}

	public static void setNamedICalExport(boolean name) throws RemoteException, ApplicationException {
		SettingsUtil.set("ical_name_export", Boolean.toString(name));
	}

}