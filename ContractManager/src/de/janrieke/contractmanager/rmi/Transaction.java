package de.janrieke.contractmanager.rmi;

import de.willuhn.datasource.rmi.DBObject;

/*
 * CREATE TABLE umsaetze (
 *  umsatz_id int(4),
 *  contract_id int(4),
 *  UNIQUE (umsatz_id),
 *  PRIMARY KEY (umsatz_id)
 * );
 */
public interface Transaction extends DBObject {
	public Contract getContract();
	public void setContract(Contract contract);
}
