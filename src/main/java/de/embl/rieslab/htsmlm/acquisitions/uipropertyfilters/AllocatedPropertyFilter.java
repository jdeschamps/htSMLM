package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class AllocatedPropertyFilter extends PropertyFilter {
	
	public AllocatedPropertyFilter(PropertyFilter additionalFilter){
		super(additionalFilter);
	}

	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property.isAssigned()){
			return false;
		}
		return true;
	}

}
