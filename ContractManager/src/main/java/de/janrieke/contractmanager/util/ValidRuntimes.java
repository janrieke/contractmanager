package de.janrieke.contractmanager.util;

import de.janrieke.contractmanager.rmi.Contract.IntervalType;

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
}