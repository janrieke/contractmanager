/**********************************************************************
 * Copied from Jameica
 * 
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/action/License.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-01-19 00:15:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn
 */
public class ShowLicenseInfoView implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.startView(de.janrieke.contractmanager.gui.view.LicenseInfo.class,null);
  }

}