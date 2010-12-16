package de.janrieke.contractmanager.server;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.willuhn.datasource.db.EmbeddedDBServiceImpl;
import de.willuhn.jameica.system.Application;

/**
 * this is our database service which can work over RMI.
 */
public class ContractDBServiceImpl_old extends EmbeddedDBServiceImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1853505853670676164L;

	/**
	 * ct.
	 * 
	 * @throws RemoteException
	 */
	public ContractDBServiceImpl_old() throws RemoteException {
		super(Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getWorkPath()
				+ "/db/db.conf", "contractmanager", "xxx");

		// We have to define jameicas classfinder.
		// otherwise, the db service will not be able to find
		// implementors by their interfaces.
		this.setClassFinder(Application.getClassLoader().getClassFinder());
	}

}