package de.embl.rieslab.htsmlm.updaters;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
		return prop.isAssigned() && EmuUtils.isNumeric(prop.getPropertyValue());
	}

	@Override
	public void updateComponent(String val) {
		if(EmuUtils.isNumeric(val)){
			int value = (int) Double.parseDouble(val);
			this.getComponent().setValue(value);
		}
	}

	// TODO does not belong here
	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}
