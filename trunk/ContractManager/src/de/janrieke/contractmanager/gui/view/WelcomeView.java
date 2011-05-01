package de.janrieke.contractmanager.gui.view;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Welcome screen of this example plugin.
 * 
 * @author willuhn
 */
public class WelcomeView extends AbstractView {

	/**
	 * this method will be invoked when starting the view.
	 * 
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		AbstractPlugin p = Application.getPluginLoader().getPlugin(
				ContractManagerPlugin.class);
		GUI.getView().setTitle(Settings.i18n().tr("ContractManager")+" (v" + p.getManifest().getVersion() + ")");

		LabelGroup group = new LabelGroup(this.getParent(), Settings.i18n().tr(
				"Welcome"));

		group.addText(Settings.i18n().tr("Welcome to ContractManager"), true);

		group.addText(Settings.i18n().tr("Be careful: This is software in beta testing. It may still contain bugs and errors."), true, Color.ERROR);

		group = new LabelGroup(this.getParent(), Settings.i18n().tr(
				"Contract Cancellation Reminder"));
		group.addPart(new CancellationReminderBox());
	}

	/**
	 * this method will be executed when exiting the view. You don't need to
	 * dispose your widgets, the GUI controller will do this in a recursive way
	 * for you.
	 * 
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
		// We've nothing to do here ;)
	}

}