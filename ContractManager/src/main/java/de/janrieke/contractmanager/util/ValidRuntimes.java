package de.janrieke.contractmanager.util;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.IntervalType;

/**
 * Instances of this class shall only be returned if all fields are
 * set and valid.
 */
public class ValidRuntimes {
	public IntervalType firstMinRuntimeType;
	public Integer firstMinRuntimeCount;
	public IntervalType followingMinRuntimeType;
	public Integer followingMinRuntimeCount;
	public Boolean fixedTerms = false;

	/**
	 * Convenience method to find out whether all necessary info is available
	 * to calculate terms and cancellation information.
	 * @return a ValidRuntimes object if all info is available, or null otherwise
	 */
	public static ValidRuntimes getValidRuntimes(Contract contract) throws RemoteException {
		ValidRuntimes result = new ValidRuntimes();

		result.firstMinRuntimeType = contract.getFirstMinRuntimeType();
		result.firstMinRuntimeCount = contract.getFirstMinRuntimeCount();
		result.followingMinRuntimeType =contract. getFollowingMinRuntimeType();
		result.followingMinRuntimeCount = contract.getFollowingMinRuntimeCount();
		result.fixedTerms = contract.getFixedTerms();

		// if one of the runtime definition is invalid, use the other one
		if (result.firstMinRuntimeType == null || result.firstMinRuntimeCount == null
				|| result.firstMinRuntimeCount <= 0) {
			result.firstMinRuntimeCount = result.followingMinRuntimeCount;
			result.firstMinRuntimeType = result.followingMinRuntimeType;
		}
		if (result.followingMinRuntimeType == null || result.followingMinRuntimeCount == null
				|| result.followingMinRuntimeCount <= 0) {
			result.followingMinRuntimeCount = result.firstMinRuntimeCount;
			result.followingMinRuntimeType = result.firstMinRuntimeType;
		}
		// do nothing if both are invalid
		if (result.followingMinRuntimeType == null || result.followingMinRuntimeCount == null
				|| result.followingMinRuntimeCount <= 0) {
			return null;
		}

		return result;
	}}