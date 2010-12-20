/**********************************************************************
 * Copied from Jameica
 * 
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/views/License.java,v $
 * $Revision: 1.5 $
 * $Date: 2009-01-20 10:51:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.janrieke.contractmanager.gui.view;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.NavigateBack;
import de.janrieke.contractmanager.gui.control.LicenseControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View fuer die Lizenz-Informationen
 */
public class LicenseInfoView extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		I18N i18n = Application.getI18n();
		GUI.getView().setTitle(i18n.tr("License Information"));
    
		LicenseControl control = new LicenseControl(this);

		Part libs = control.getLibList();
		libs.paint(getParent());

		ButtonArea buttons = new ButtonArea(getParent(), 1);
		buttons.addButton(Settings.i18n().tr("<< Back"), new NavigateBack());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}