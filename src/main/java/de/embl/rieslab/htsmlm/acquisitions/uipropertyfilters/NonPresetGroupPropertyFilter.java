package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class NonPresetGroupPropertyFilter extends PropertyFilter {

	public NonPresetGroupPropertyFilter(PropertyFilter additionalfilter){
		super(additionalfilter);
	}
	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property.isConfigGroupMMProperty()){
			return true;
		}
		return false;
	}

}