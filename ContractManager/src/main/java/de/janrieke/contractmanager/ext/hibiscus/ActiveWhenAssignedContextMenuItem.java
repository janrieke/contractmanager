package de.janrieke.contractmanager.ext.hibiscus;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Helper class to deactivate the menu item if the transaction is already assigned.
 */
class ActiveWhenAssignedContextMenuItem extends CheckedContextMenuItem
{
	private boolean activeWhenAssigned;
	private boolean allowMultiple;

	/**
	 * ct.
	 *
	 * @param text
	 * @param a
	 * @param activeWhenAssigned
	 *            if <code>true</code>, the item will only be active when the
	 *            transaction is assigned to a constract; if <code>false</code>,
	 *            the item will only be active if the transaction is not
	 *            assigned.
	 */
	public ActiveWhenAssignedContextMenuItem(String text, Action a, boolean activeWhenAssigned,
			boolean allowMultiple)
	{
		super(text, a);
		this.activeWhenAssigned = activeWhenAssigned;
		this.allowMultiple = allowMultiple;
	}

	/**
	 * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
	 */
	@Override
	public boolean isEnabledFor(Object o)
	{
		if (o == null) {
			return false;
		}

		if (o instanceof Umsatz[]) {
			if (allowMultiple) {
				// Wenn wir eine ganze Liste von Buchungen haben, pruefen
				// wir nicht jede einzeln, ob sie schon in ContractManager vorhanden
				// ist. Die werden dann beim Import (weiter unten) einfach ausgesiebt.
				return super.isEnabledFor(o);
			} else {
				return false;
			}
		}

		if (!(o instanceof Umsatz)) {
			return false;
		}

		try {
			return (activeWhenAssigned ^ !TransactionUtils.isAssigned((Umsatz) o)) && super.isEnabledFor(o);
		} catch (Exception e) {
			Logger.error("unable to detect if buchung is already assigned",e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(UmsatzListMenuHibiscusExtension.i18n.tr("Error while checking for assigned contracts in ContractManager"), StatusBarMessage.TYPE_ERROR));
		}
		return false;
	}
}