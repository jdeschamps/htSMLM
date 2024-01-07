package de.embl.rieslab.htsmlm.updaters;

import javax.swing.JProgressBar;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;

/**
 * Updates a progress bar with the value of a device property.
 */
public class ProgressBarUpdater extends ComponentUpdater<JProgressBar>{

	
	public ProgressBarUpdater(JProgressBar component, UIProperty prop,
							  int idleTime) {
		super(component, prop, idleTime);
	}

	@Override
	public boolean sanityCheck(UIProperty prop) {
		// if the property is not assigned, return true
		if(prop.isAssigned()) {
			return EmuUtils.isNumeric(prop.getPropertyValue());
		}
		return true;
	}

	@Override
	public void updateComponent(String val) {
		if(EmuUtils.isNumeric(val)){
			int value = (int) Double.parseDouble(val);
			this.getComponent().setValue(value);
		}
	}
}
