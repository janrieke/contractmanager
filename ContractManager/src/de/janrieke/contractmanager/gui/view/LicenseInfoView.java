/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2011  Jan Rieke
 *
 *   ContractManager is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContractManager is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import de.janrieke.contractmanager.gui.control.LicenseControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
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
		GUI.getView().setTitle(i18n.tr("Licence Information"));
    
		LicenseControl control = new LicenseControl(this);

		Part libs = control.getLibList();
		libs.paint(getParent());

		//ButtonArea buttons = new ButtonArea(getParent(), 1);
		//buttons.addButton(new Back(true));
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}