package de.embl.rieslab.htsmlm.updaters;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JProgressBar;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;

public class JProgressBarUpdater extends ComponentUpdater<JProgressBar>{

	
	public JProgressBarUpdater(JProgressBar component, UIProperty prop,
			int idletime) {
		super(component, prop, idletime);
	}

	@Override
	public boolean sanityCheck(UIProperty prop) {
		// performs sanity check
		if(prop.isAssigned() && EmuUtils.isNumeric(prop.getPropertyValue())) {
			return true;
		}

		return false;
	}

	@Override
	public void updateComponent(String val) {
		if(EmuUtils.isNumeric(val)){
			int value = (int) Double.parseDouble(val);
			component_.setValue(value);
		}
	}

	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}
