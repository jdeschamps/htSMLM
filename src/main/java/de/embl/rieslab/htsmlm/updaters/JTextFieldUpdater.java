package de.embl.rieslab.htsmlm.updaters;

import javax.swing.JTextField;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

/**
 * A class updating a text field with the value of a device property.
 */
public class JTextFieldUpdater extends ComponentUpdater<JTextField> {

	public JTextFieldUpdater(JTextField component, UIProperty prop, int idleTime) {
		super(component, prop, idleTime);
	}

	@Override
	public boolean sanityCheck(UIProperty prop) {
		return true;
	}

	@Override
	public void updateComponent(String val) {
		this.getComponent().setText(val);
	}

}
