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

package de.janrieke.contractmanager.ext.hibiscus;

import de.janrieke.contractmanager.Settings;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;

/**
 * Automatically import incoming Hibiscus transactions.
 */
public class NewTransactionsListener implements MessageConsumer {
	/**
	 * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
	 */
	public boolean autoRegister() {
		return (Application.getPluginLoader().isInstalled(HBCI.class.getName()));
	}

	/**
	 * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
	 */
	@SuppressWarnings("rawtypes")
	public Class[] getExpectedMessageTypes() {
		return new Class[] { ImportMessage.class };
	}

	/**
	 * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
	 */
	public void handleMessage(Message message) throws Exception {
		if (!Settings.getHibiscusAutoImportNewTransactions())
			return;
		
		// Ignore non-import messages
		if (message == null || !(message instanceof ImportMessage))
			return;

		GenericObject o = ((ImportMessage) message).getObject();

		if (o == null || !(o instanceof Umsatz) || o.getID() == null)
			return; // no ID means: not stored in DB
		
		if (((Umsatz) o).hasFlag(Umsatz.FLAG_NOTBOOKED))
			return; // ignore transactions that have not been booked, yet

		Umsatz[] u = {(Umsatz) o};
		UmsatzImportWorker worker = new UmsatzImportWorker(u, true);
		//Application.getController().start(worker);
		worker.run(null);
	}
}