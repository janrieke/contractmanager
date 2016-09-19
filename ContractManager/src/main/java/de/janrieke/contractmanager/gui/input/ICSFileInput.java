/*
 *   This file is part of ContractManager for Jameica.
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