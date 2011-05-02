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
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/buttons/Back.java,v $
 * $Revision: 1.1 $
 * $Date: 2009-01-20 10:51:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.gui.button;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.RestoreAction;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.Button;

/**
 * Vorkonfigurierter Cancel-Button, der die aktuelle View neu lädt.
 */
public class RestoreButton extends Button
{
  /**
   * ct.
   * @param isDefault true, wenn es der Default-Button sein soll.
   */
  public RestoreButton(AbstractView view, Object object, boolean isDefault)
  {
    super(Settings.i18n().tr("Restore"), new RestoreAction(view), object, isDefault, "edit-undo.png");
  }
}