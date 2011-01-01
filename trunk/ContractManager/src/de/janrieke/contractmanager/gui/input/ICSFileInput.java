package de.janrieke.contractmanager.gui.input;

import org.eclipse.swt.widgets.FileDialog;

import de.janrieke.contractmanager.Settings;
import de.willuhn.jameica.gui.input.FileInput;

/**
 * FileInput with customized dialog for ICS file selection.
 */
public class ICSFileInput extends FileInput {

	private boolean save;

	public ICSFileInput(String file, boolean save) {
		super(file, save);
		this.save = save;
	}

	@Override
	protected void customize(FileDialog fd) {
		String[] names = {
				Settings.i18n().tr("iCalendar file" + " (*.ics)"),
				Settings.i18n().tr("All files") + " (*.*)" };
		String[] filters = { "*.ics", "*.*" };
		fd.setFilterExtensions(filters);
		fd.setFilterNames(names);
		if (save)
			fd.setOverwrite(true);
		fd.setFileName(value);
		super.customize(fd);
	}
}