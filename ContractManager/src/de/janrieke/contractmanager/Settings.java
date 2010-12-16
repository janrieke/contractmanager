package de.janrieke.contractmanager;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import de.janrieke.contractmanager.rmi.ContractDBService;
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

}