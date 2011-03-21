package de.janrieke.contractmanager.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SizeableTablePart extends TablePart {

	private int heightHint;

	@Override
	public synchronized void paint(Composite parent) throws RemoteException {
	    SimpleContainer container = new SimpleContainer(parent, true); //give the table a minimum height
	    GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
	    gd.heightHint = heightHint;
	    container.getComposite().setLayoutData(gd);
	    super.paint(container.getComposite());
	}

	public SizeableTablePart(GenericIterator list, Action action) {
		super(list, action);
	}

	public void setHeightHint(int heightHint) {
		this.heightHint = heightHint; 
	}

}
