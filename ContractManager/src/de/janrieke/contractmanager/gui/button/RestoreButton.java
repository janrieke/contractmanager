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