package de.embl.rieslab.htsmlm.filters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class NonConfigGroupPropertyFilter extends PropertyFilter {

	public NonConfigGroupPropertyFilter(PropertyFilter additionalfilter){
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