package de.janrieke.contractmanager.gui.view;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Our "About..." dialog.
 */
public class About extends AbstractDialog {

	/**
	 * ct.
	 * 
	 * @param position
	 */
	public About(int position) {
		super(position);
		this.setTitle(Settings.i18n().tr("About..."));
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {

		FormTextPart text = new FormTextPart();
		text.setText("<form>" + "<p><b>Contract Manager</b></p>"
				+ "<br/>Licence: GPL (http://www.gnu.org/copyleft/gpl.html)"
				+ "<br/><p>Copyright by Jan Rieke [it@janrieke.de]</p>"
				+ "<p>http://www.janrieke.de/projects/contractmanager</p>"
				+ "<br/><p>Based upon Jameica Example Plugin, copyright by Olaf Willuhn [info@jameica.org]</p>"
				+ "<p>http://www.jameica.org</p>" + "</form>");

		text.paint(parent);

		LabelGroup group = new LabelGroup(parent, " Information ");

		AbstractPlugin p = Application.getPluginLoader().getPlugin(
				ContractManagerPlugin.class);

		group.addLabelPair(Settings.i18n().tr("Version"), new LabelInput(""
				+ p.getManifest().getVersion()));
		group.addLabelPair(Settings.i18n().tr("Working directory"),
				new LabelInput("" + p.getResources().getWorkPath()));

	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	protected Object getData() throws Exception {
		return null;
	}

}
