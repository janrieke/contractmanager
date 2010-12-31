/**********************************************************************
 * Copied from Jameica.
 * 
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/FileInput.java,v $
 * $Revision: 1.16 $
 * $Date: 2008-07-17 08:47:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.janrieke.contractmanager.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.ButtonInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 * Ist zustaendig fuer Text-Eingabefelder, hinter denen sich jedoch noch ein
 * zusaetzlicher Button fuer eine Dateisuche befindet.
 */
public class FileInput extends ButtonInput
{

	private Text text;
	
  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param file der initial einzufuegende Wert fuer das Eingabefeld.
   */
  public FileInput(final String file)
  {
    this(file,false);
  }
  

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param file der initial einzufuegende Wert fuer das Eingabefeld.
   * @param save legt fest, ob es ein Speichern-Dialog sein soll.
   */
  public FileInput(String file, boolean save)
  {
    this(file, save, null, null);
  }

  /**
   * Erzeugt ein neues Eingabefeld und schreibt den uebergebenen Wert rein.
   * @param file der initial einzufuegende Wert fuer das Eingabefeld.
   * @param save legt fest, ob es ein Speichern-Dialog sein soll.
   * @param extensions legt die zulaessigen Datei-Endungen fest, die vom Dialog angezeigt werden sollen.
   */
  public FileInput(String file, final boolean save, final String[] filters, final String[] extensions)
  {
    this.value = file;
    addButtonListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Logger.debug("starting file dialog");
        FileDialog dialog = new FileDialog(GUI.getShell(), save ? SWT.SAVE : SWT.OPEN);
        if (extensions != null && extensions.length > 0)
          dialog.setFilterExtensions(extensions);
        if (filters != null && filters.length > 0)
            dialog.setFilterNames(filters);
        if (save)
        	dialog.setOverwrite(true);
        dialog.setText(Application.getI18n().tr("Bitte wählen Sie die Datei aus"));
        dialog.setFileName(value);
        setValue(dialog.open());
        text.forceFocus(); // das muessen wir machen, damit die Listener ausgeloest werden
      }
    });
  }

  /**
   * Liefert ein Objekt des Typs java.lang.String.
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    if (text == null || text.isDisposed())
      return this.value;
    return text.getText();
  }

  /**
   * Erwartet ein Objekt des Typs java.lang.String.
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null)
      return;

    if (!(value instanceof String))
			return;

    this.value = (String) value;
    if (this.text != null && !this.text.isDisposed())
    {
      this.text.setText((String) value);
      this.text.redraw();
    }
  }

  /**
	 * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
	 */
  public Control getClientControl(Composite parent) {
    text = GUI.getStyleFactory().createText(parent);
    text.setText(this.value == null ? "" : this.value);
  	return text;
  }

}