/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2015  Jan Rieke
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
/**********************************************************************
 * Copy from Hibiscus
 * Copyright (c) by willuhn software & services
 **********************************************************************/

package de.janrieke.contractmanager.gui.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Einspielen eines XML-Backups.
 */
public class BackupRestore implements Action {
	private static I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class)
			.getResources().getI18N();

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {
		// Wir checken vorher, ob die Datenbank leer ist. Ansonsten koennen wir
		// das
		// eh nicht sinnvoll importieren.
		try {
			if (Settings.getDBService().createList(Contract.class).size() > 0) {
				String text = i18n.tr("Die ContractManager-Installation enthält bereits Daten.\n"
						+ "Ein Import kann zu unvorhersehbaren Ergebnissen oder Fehlern führen.\n"
						+ "Wollen Sie wirklich fortfahren?");
				if (!Application.getCallback().askUser(text))
					return;
			}
		} catch (ApplicationException ae) {
			throw ae;
		} catch (OperationCanceledException oce) {
			return;
		} catch (Exception e) {
			Logger.error("unable to notify user", e);
			throw new ApplicationException(i18n.tr("Datenbank-Import fehlgeschlagen"));
		}

		FileDialog fd = new FileDialog(GUI.getShell(), SWT.OPEN);
		fd.setFileName("hibiscus-backup-" + BackupCreate.DATEFORMAT.format(new Date()) + ".xml");
		fd.setFilterExtensions(new String[] { "*.xml" });
		fd.setText("Bitte wählen Sie die Backup-Datei aus");
		String f = fd.open();
		if (f == null || f.length() == 0)
			return;

		final File file = new File(f);
		if (!file.exists())
			return;

		Application.getController().start(new BackgroundTask() {
			private boolean cancel = false;

			/**
			 * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
			 */
			public void run(ProgressMonitor monitor) throws ApplicationException {
				monitor.setStatusText(i18n.tr("Importiere Backup"));
				Logger.info("importing backup " + file.getAbsolutePath());
				final ClassLoader loader = Application.getPluginLoader()
						.getManifest(ContractManagerPlugin.class).getClassLoader();

				Reader reader = null;
				try {
					InputStream is = new BufferedInputStream(new FileInputStream(file));
					reader = new XmlReader(is, new ObjectFactory() {
						@Override
						public GenericObject create(String type, String id, @SuppressWarnings("rawtypes") Map values)
								throws Exception {
							AbstractDBObject object = (AbstractDBObject) Settings.getDBService()
									.createObject(loader.loadClass(type), null);
							object.setID(id);
							Iterator<?> i = values.keySet().iterator();
							while (i.hasNext()) {
								String name = (String) i.next();
								object.setAttribute(name, values.get(name));
							}
							return object;
						}

					});

					long count = 1;
					GenericObject o = null;
					while ((o = reader.read()) != null) {
						try {
							((AbstractDBObject) o).insert();
						} catch (Exception e) {
							Logger.error(
									"unable to import " + o.getClass().getName() + ":" + o.getID()
											+ ", skipping", e);
							monitor.log("  "
									+ i18n.tr("{0} fehlerhaft ({1}), überspringe", new String[] {
											BeanUtil.toString(o), e.getMessage() }));
						}
						if (count++ % 100 == 0)
							monitor.addPercentComplete(1);
					}

					monitor.setStatus(ProgressMonitor.STATUS_DONE);
					monitor.setStatusText("Backup importiert");
					monitor.setPercentComplete(100);
				} catch (Exception e) {
					Logger.error("error while importing data", e);
					throw new ApplicationException(e.getMessage());
				} finally {
					if (reader != null) {
						try {
							reader.close();
							Logger.info("backup imported");
						} catch (Exception e) {/* useless */
						}
					}
				}
			}

			/**
			 * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
			 */
			public boolean isInterrupted() {
				return this.cancel;
			}

			/**
			 * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
			 */
			public void interrupt() {
				this.cancel = true;
			}

		});
	}
}