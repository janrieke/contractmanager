package de.janrieke.contractmanager.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;
/*
 * CREATE TABLE costs (
 *   id IDENTITY,
 *   contract_id int(5),
 *   description varchar(255),
 *   money double,
 *   period int(1),
 *   UNIQUE (id),
 *   PRIMARY KEY (id)
 * );
 */
public interface Costs extends DBObject {
	public Contract getContract() throws RemoteException;
	public void setContract(Contract contract) throws RemoteException;

	public String getDescription() throws RemoteException;
	public void setDescription(String description) throws RemoteException;

	public double getMoney() throws RemoteException;
	public void setMoney(double money) throws RemoteException;

	public Contract.IntervalType getPeriod() throws RemoteException;
	public void setPeriod(Contract.IntervalType period) throws RemoteException;
}