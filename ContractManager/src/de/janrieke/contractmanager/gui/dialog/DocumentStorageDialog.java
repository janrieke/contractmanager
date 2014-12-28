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

/*
 * Partially copied from Hibiscus/Syntax, (c) by willuhn.webdesign
 */

package de.janrieke.contractmanager.gui.dialog;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.ContractDBService;
import de.janrieke.contractmanager.rmi.Storage;
import de.janrieke.contractmanager.util.FileOpener;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog for storing files for a contract.
 */
public class DocumentStorageDialog extends AbstractDialog<Contract> {
	private Contract contract = null;

	private TablePart table = null;
	private Button buttonAddFile = null;
	private Button buttonAddLocalFileLink = null;
	private Button buttonRemove = null;
	private Button buttonOpen = null;

	// TODO: calculate this from the maximum text width
	private int BUTTON_HORIZONTAL_SIZE = 160;
	private final static int BUTTON_VERTICAL_SIZE = SWT.DEFAULT;

	/**
	 * ct.
	 * 
	 * @param position
	 * @param preselected
	 *            the preselected contract.
	 * @throws RemoteException
	 */
	public DocumentStorageDialog(Contract c) throws RemoteException {
		super(AbstractDialog.POSITION_CENTER, true);
		this.contract = c;

		this.setTitle(Settings.i18n().tr("Document Storage"));
		this.setSize(570, 300);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		Container group = new SimpleContainer(parent, true);

		Container left = new SimpleContainer(group.getComposite(), true, 1);

		left.addText(Settings.i18n().tr("List of files:"), true);
		left.addPart(this.getTable());
		
		//compute max width of the buttons
		GC gc = new GC(parent.getShell());
		int maxwidth = 0;
		maxwidth = Math.max(maxwidth, gc.stringExtent(Settings.i18n().tr("Add File...")).x);
		maxwidth = Math.max(maxwidth, gc.stringExtent(Settings.i18n().tr("Add Local File Link...")).x);
		maxwidth = Math.max(maxwidth, gc.stringExtent(Settings.i18n().tr("Remove")).x);
		maxwidth = Math.max(maxwidth, gc.stringExtent(Settings.i18n().tr("Open")).x);
		BUTTON_HORIZONTAL_SIZE = maxwidth + 12;
		
		Container right = new SimpleContainer(group.getComposite(), false, 1);
		right.getComposite().setLayoutData(
				new GridData(BUTTON_HORIZONTAL_SIZE + 10, SWT.DEFAULT));
		right.addPart(getAddFileButton());
		right.addPart(getAddLocalLinkFileButton());
		right.addPart(getRemoveButton());
		right.addPart(getOpenButton());

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(Settings.i18n().tr("Close"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				close();
			}
		}, null, false, "process-stop.png");

		group.addButtonArea(buttons);
	}

	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getAddFileButton() {
		if (buttonAddFile != null)
			return buttonAddFile;

		buttonAddFile = new Button(Settings.i18n().tr("Add File..."),
				new AddFile()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE,
						BUTTON_VERTICAL_SIZE));
			}
		};
		return buttonAddFile;
	}

	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getAddLocalLinkFileButton() {
		if (buttonAddLocalFileLink != null)
			return buttonAddLocalFileLink;

		buttonAddLocalFileLink = new Button(Settings.i18n().tr(
				"Add Local File Link..."), new AddLocalFileLink()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE,
						BUTTON_VERTICAL_SIZE));
			}
		};
		return buttonAddLocalFileLink;
	}

	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getRemoveButton() {
		if (buttonRemove != null)
			return buttonRemove;

		buttonRemove = new Button(Settings.i18n().tr("Remove"), new Remove()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE,
						BUTTON_VERTICAL_SIZE));
			}
		};
		buttonRemove.setEnabled(false); // initial deaktiviert
		return buttonRemove;
	}

	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getOpenButton() {
		if (buttonOpen != null)
			return buttonOpen;

		buttonOpen = new Button(Settings.i18n().tr("Open"), new OpenFile()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE,
						BUTTON_VERTICAL_SIZE));
			}
		};
		buttonOpen.setEnabled(false); // initial deaktiviert
		return buttonOpen;
	}

	/**
	 * Returns the file table
	 * 
	 * @return the file table
	 */
	private TablePart getTable() {
		if (this.table != null)
			return this.table;

		try {
			this.table = new TablePart(contract.getAttachedFiles(),
					new OpenFile());
			this.table.setSummary(false);
			this.table.addColumn(Settings.i18n().tr("Description"),
					"description", null, true);
			this.table.addColumn(Settings.i18n().tr("Local file URI"), "path");
			table.setFormatter(new TableFormatter() {
				@Override
				public void format(TableItem item) {
					Object o = item.getData();
					if (o instanceof Storage) {
						try {
							if (((Storage) o).getFile() != null) {
								item.setText(1,
										Settings.i18n().tr("File in database"));
								item.setForeground(1,
										Settings.getNotActiveForegroundColor());
							} else
								item.setText(1, ((Storage) o).getPath());
						} catch (RemoteException e) {
							item.setText(1, "Error while reading database");
							item.setForeground(1, Settings.getErrorColor());
						}
					}
				}
			});
			table.addSelectionListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (event.type == SWT.Selection
							&& event.data instanceof Storage) {
						getRemoveButton().setEnabled(true);
						getOpenButton().setEnabled(true);
					}
				}
			});
			table.addChangeListener(new TableChangeListener() {

				@Override
				public void itemChanged(Object object, String attribute,
						String newValue) throws ApplicationException {
					if (object instanceof Storage
							&& "description".equals(attribute)) {
						try {
							((Storage) object).setDescription(newValue);
							((Storage) object).store();
						} catch (RemoteException e) {
							Logger.error("error while setting field", e);
						}
					}
				}
			});
		} catch (RemoteException e) {
			Logger.error("error while getting file list", e);
		}
		return this.table;
	}

	/**
	 * Action for opening the selected entry
	 */
	private class OpenFile implements Action {
		public void handleAction(Object context) throws ApplicationException {
			Storage storage = (Storage) getTable().getSelection();
			if (storage != null) {
				InputStream inputStream;
				try {
					inputStream = storage.getFile();
					if (inputStream != null) {
						// file has been stored in DB as blob
						String suffix = storage.getPath(); // path contains the
															// file suffix when
						File tmp = File.createTempFile(
								"jameica-contractmanager-", suffix);

						// Delete temp file when program exits
						tmp.deleteOnExit();

						// save the DB contents to file
						FileOutputStream output = new FileOutputStream(tmp);
						try {
							byte[] buffer = new byte[1024];
							int len;
							while ((len = inputStream.read(buffer)) != -1) {
								output.write(buffer, 0, len);
							}
						} catch (IOException ex) {
							Logger.error(
									"Error while saving temporary file contents",
									ex);
						} finally {
							output.close();
						}

						// open the temp file
						if (!openFile(tmp)) {
							// if opening has failed, allow the user to access
							// the file manually
							MessageBox messageBox = new MessageBox(
									GUI.getShell(), SWT.ICON_INFORMATION
											| SWT.OK);
							messageBox.setText(Settings.i18n().tr(
									"Opening Failed"));
							messageBox.setMessage(Settings.i18n().tr("Opening the stored file failed. You can access it manually at the following location:")
											+ "\n"
											+ tmp.getAbsolutePath()
											+ "\n"
											+ Settings.i18n().tr("The file will be deleted when exiting Jameica."));
							messageBox.open();
						}
						;
					} else {
						// just open the plain file link from "path" field
						String path = storage.getPath();
						if (path != null && !"".equals(path)) {
							File localfile = new File(path);
							openFile(localfile);
						}
					}
				} catch (IOException e) {
					Logger.error("Error while saving to temporary file", e);
				}
			}
		}

		private boolean openFile(File file) {
			boolean success = false;
			if (file.exists() && file.isFile() && file.canRead()) {
				// 1. Use the AWT Desktop
				if (Desktop.isDesktopSupported()
						&& Desktop.getDesktop()
								.isSupported(Desktop.Action.OPEN)) {
					try {
						Desktop.getDesktop().open(file);
						success = true;
					} catch (IOException e) {
						Logger.warn("IOException while opening file using AWT: "
								+ e.getMessage());
					}
				}
				if (success)
					return true;
				Logger.warn("Error while opening file using AWT");

				// 2. Try using the SWT mechanism: find the associated program
				// by file extension
				int lastDot = file.getAbsolutePath().lastIndexOf(".");
				if (lastDot != -1) {
					String suffix = file.getAbsolutePath().substring(lastDot);
					final Program p = Program.findProgram(suffix);
					if (p != null) {
						success = p.execute(file.getAbsolutePath());
					} else
						Program.launch(file.getAbsolutePath());
				}
				if (success)
					return true;
				Logger.warn("Error while opening file using SWT");
				
				// 3. Use OS command line
				success = FileOpener.getFileOpener().open(file);
				if (success)
					return true;
				Logger.warn("Error while opening file using command line");

				// Finally: report fatal error
				Logger.error("Error while opening file, all methods failed. Check whether file is accessible and is associated with a program.");
			} else {
				Logger.error("Error while opening file: file does not exist or is not readable");
				return false;
			}
			return success;
		}
	}

	/**
	 * Action for adding a new file as blob
	 */
	private class AddFile implements Action {
		public void handleAction(Object context) throws ApplicationException {
			FileDialog fd = new FileDialog(GUI.getShell(), SWT.OPEN);
			fd.setText(Settings.i18n().tr("Select File to Add"));

			final String filename = fd.open();
			if (filename != null && !"".equals(filename)) {
				try {
					File localfile = new File(filename);
					if (localfile.exists() && localfile.isFile()
							&& localfile.canRead()) {
						DBService service = Settings.getDBService();
						if (service instanceof ContractDBService) {
							Connection conn = ((ContractDBService) service)
									.getConnection();
							boolean autocommit = conn.getAutoCommit();
							conn.setAutoCommit(false);

							String sql = "INSERT INTO storage (contract_id, description, path, file) VALUES (?, ?, ?, ?)";
							PreparedStatement stmt = conn.prepareStatement(sql);
							stmt.setString(1, contract.getID());
							stmt.setString(2,
									Settings.i18n()
											.tr("Enter description here"));

							int lastDot = localfile.getName().lastIndexOf(".");
							if (lastDot != -1)
								stmt.setString(3, localfile.getName().substring(lastDot));
							else
								stmt.setString(3, "");

							FileInputStream fis = new FileInputStream(localfile);

							stmt.setBinaryStream(4, fis,
									(int) localfile.length());
							stmt.execute();
							ResultSet res = stmt.getGeneratedKeys();
							int newid = 0;
							if (res != null && res.next())
								newid = res.getInt(1);

							fis.close();
							stmt.close();
							conn.commit();
							conn.setAutoCommit(autocommit);

							// add the new entry to the table
							if (newid != 0) {
								DBIterator storageIterator = service
										.createList(Storage.class);
								storageIterator.addFilter("id = " + newid);
								GenericObject newEntry = storageIterator.next();
								if (newEntry != null) {
									table.addItem(newEntry);
								}
							}
						}
					}
				} catch (RemoteException e) {
					throw new ApplicationException(Settings.i18n().tr(
							"Error while creating new storage entry"), e);
				} catch (FileNotFoundException e) {
					Logger.error("error while loading file", e);
				} catch (SQLException e) {
					Logger.error("error while executing SQL query", e);
				} catch (IOException e) {
					Logger.error("error while loading file", e);
				}
			}
		}
	}

	/**
	 * Action for adding a new link
	 */
	private class AddLocalFileLink implements Action {
		public void handleAction(Object context) throws ApplicationException {
			FileDialog fd = new FileDialog(GUI.getShell(), SWT.OPEN);
			fd.setText(Settings.i18n().tr("Select File to Add as Link"));

			final String filename = fd.open();
			try {
				Storage s = (Storage) Settings.getDBService().createObject(
						Storage.class, null);
				s.setContract(contract);
				s.setDescription(Settings.i18n().tr("Enter description here"));
				s.setPath(filename);
				s.store();
				table.addItem(s);
			} catch (RemoteException e) {
				throw new ApplicationException(Settings.i18n().tr(
						"Error while creating new storage entry"), e);
			}
		}
	}

	/**
	 * Action for removing an entry
	 */
	private class Remove implements Action {
		public void handleAction(Object context) throws ApplicationException {
			try {
				if (table.getSelection() != null
						&& table.getSelection() instanceof Storage) {
					Storage st = (Storage) table.getSelection();
					MessageBox messageBox = new MessageBox(GUI.getShell(),
							SWT.ICON_QUESTION | SWT.YES | SWT.NO);

					// YesNoDialog d = new
					// YesNoDialog(YesNoDialog.POSITION_CENTER);
					messageBox.setText(Settings.i18n().tr("Remove file?"));
					// messageBox.setPanelText(Settings.i18n().tr("Do you really want to remove the file?"));

					if (st.getFile() != null)
						messageBox
								.setMessage(Settings
										.i18n()
										.tr("Do you really want to remove the file?")
										+ "\n"
										+ Settings
												.i18n()
												.tr("This will delete the file from the database permanently."));
					else
						messageBox
								.setMessage(Settings
										.i18n()
										.tr("Do you really want to remove the file?")
										+ "\n"
										+ Settings
												.i18n()
												.tr("This will remove the file link, but will not delete the actual file."));

					boolean choice = messageBox.open() == SWT.YES;

					if (choice) {
						Storage sel = (Storage) getTable().getSelection();
						table.removeItem(sel);
						sel.delete();
					}

					if (table.getSelection() == null) {
						getRemoveButton().setEnabled(false);
						getOpenButton().setEnabled(false);
					} else
						table.select(table.getSelection()); // refresh selection
															// marker
				}
			} catch (OperationCanceledException e) {
				// user pressed ESC, do nothing
			} catch (RemoteException e) {
				throw new ApplicationException(Settings.i18n().tr(
						"Error while deleting storage entry"), e);
			} catch (Exception e) {
				throw new ApplicationException(Settings.i18n().tr(
						"Error while deleting storage entry"), e);
			}
		}
	}

	@Override
	protected Contract getData() throws Exception {
		return null;
	}

	@Override
	protected void onEscape() {
		// Avoid exception when pressing ESC
		close();
	}
}
