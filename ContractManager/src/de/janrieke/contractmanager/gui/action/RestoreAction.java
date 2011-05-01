package de.janrieke.contractmanager.gui.action;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class RestoreAction implements Action {

	private AbstractView view;

	public RestoreAction(AbstractView view) {
		this.view = view;
	}

	@Override
	public void handleAction(Object context) throws ApplicationException {
		GUI.startView(view, context);
	}

}
