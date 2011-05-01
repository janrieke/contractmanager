/*
 *   ContractManager for Jameica
 *   Copyright (C) 2010-2011  Jan Rieke
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.janrieke.contractmanager;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.gui.action.ExportCancelationReminders;
import de.janrieke.contractmanager.rmi.ContractDBService;
import de.janrieke.contractmanager.server.ContractDBServiceImpl;
import de.janrieke.contractmanager.server.DBSupportH2Impl;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * You need to have at least one class which inherits from
 * <code>AbstractPlugin</code>. If so, Jameica will detect your plug-in
 * automatically at startup.
 * 
 * @author willuhn, jrieke
 */
public class ContractManagerPlugin extends AbstractPlugin {

	private static ContractManagerPlugin instance;

	/**
	 * constructor.
	 */
	public ContractManagerPlugin() {
		super();
	}

	/**
	 * This method is invoked on every startup. You can make here some stuff to
	 * init your plug-in. If you get some errors here and you don't want to
	 * activate the plug-in, simply throw an ApplicationException.
	 * 
	 * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
	 */
	public void init() throws ApplicationException {
		instance = this;
		
	    call(new ServiceCall()
	    {
	      public void call(ContractDBService service) throws ApplicationException, RemoteException
	      {
	        service.checkConsistency();
	      }
	    });

//		Application.getMessagingFactory().registerMessageConsumer(
//				new MessageConsumer() {
//
//					@Override
//					public void handleMessage(Message message) throws Exception {
//						if (((SystemMessage) message).getStatusCode() == SystemMessage.SYSTEM_SHUTDOWN) {
//							try {
//								if (Settings.getICalAutoExport()) {
//									new ExportCancelationReminders()
//											.handleAction(null);
//								}
//							} catch (RemoteException e) {
//								GUI.getStatusBar()
//										.setErrorText(
//												Settings.i18n()
//														.tr("Error during cancellation reminder export."));
//							} catch (ApplicationException e) {
//								GUI.getStatusBar()
//										.setErrorText(
//												Settings.i18n()
//														.tr("Error during cancellation reminder export."));
//							}
//						}
//					}
//
//					@Override
//					public Class<?>[] getExpectedMessageTypes() {
//						return new Class[] { SystemMessage.class };
//					}
//
//					@Override
//					public boolean autoRegister() {
//						return false;
//					}
//				});
	}

	/**
	 * This method is called only the first time, the plug-in is loaded (before
	 * executing init()). if your installation procedure was not successful,
	 * throw an ApplicationException.
	 * 
	 * @see de.willuhn.jameica.plugin.AbstractPlugin#install()
	 */
	public void install() throws ApplicationException {

		call(new ServiceCall() {

			public void call(ContractDBService service)
					throws ApplicationException, RemoteException {
				service.install();
			}
		});
	}

	/**
	 * This method will be executed on every version change.
	 * 
	 * @see de.willuhn.jameica.plugin.AbstractPlugin#update(double)
	 */
	public void update(double oldVersion) throws ApplicationException {
	}

	/**
	 * Here you can do some cleanup stuff. The method will be called on every
	 * clean shutdown of Jameica.
	 * 
	 * @see de.willuhn.jameica.plugin.AbstractPlugin#shutDown()
	 */
	public void shutDown() {
		try {
			if (Settings.getICalAutoExport()) {
				new ExportCancelationReminders()
				.handleAction(null);
			}
		} catch (RemoteException e) {
			GUI.getStatusBar()
			.setErrorText(
					Settings.i18n()
					.tr("Error during cancellation reminder export."));
		} catch (ApplicationException e) {
			GUI.getStatusBar()
			.setErrorText(
					Settings.i18n()
					.tr("Error during cancellation reminder export."));
		}
	}

	public static ContractManagerPlugin getInstance() {
		return instance;
	}

	/**
	 * Hilfsmethode zum bequemen Ausfuehren von Aufrufen auf dem Service.
	 */
	private interface ServiceCall {
		/**
		 * @param service
		 * @throws ApplicationException
		 * @throws RemoteException
		 */
		public void call(ContractDBService service)
				throws ApplicationException, RemoteException;
	}

	/**
	 * Hilfsmethode zum bequemen Ausfuehren von Methoden auf dem Service.
	 * 
	 * @param call
	 *            der Call.
	 * @throws ApplicationException
	 */
	private void call(ServiceCall call) throws ApplicationException {
		// If we are running in client/server mode and this instance
		// is the client, we do not need to create a database.
		// Instead of this we will get our objects via RMI from
		// the server
		if (Application.inClientMode())
			return; // als Client muessen wir die DB nicht installieren

		ContractDBService service = null;
		try {
			// Da die Service-Factory zu diesem Zeitpunkt noch nicht da ist,
			// erzeugen wir uns eine lokale Instanz des Services.
			//Application.getServiceFactory().lookup(ContractManagerPlugin.class, "contract_db");
			
			service = new ContractDBServiceImpl();
			service.start();
			call.call(service);
		} catch (ApplicationException ae) {
			throw ae;
		} catch (Exception e) {
			Logger.error("Unable to init db service", e);
			I18N i18n = getResources().getI18N();
			String msg = i18n
					.tr("Unable to initialize database for ContractManager.\n\n{0} ",
							e.getMessage());

			// Wenn wir die H2-DB verwenden, koennte es sich um eine korrupte
			// Datenbank handeln
			String driver = ContractDBService.SETTINGS.getString(
					"database.driver", null);
			if (driver != null
					&& driver.equals(DBSupportH2Impl.class.getName())) {
				msg += "\n\nPossible database problem. Try restoring an old version.";
			}

			throw new ApplicationException(msg, e);
		} finally {
			if (service != null) {
				try {
					service.stop(true);
				} catch (Exception e) {
					Logger.error("Error while closing db service", e);
				}
			}
		}
	}

}