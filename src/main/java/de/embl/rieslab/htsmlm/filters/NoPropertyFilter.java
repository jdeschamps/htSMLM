package de.embl.rieslab.htsmlm.filters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class NoPropertyFilter extends PropertyFilter {

	public NoPropertyFilter(){
	}

	public NoPropertyFilter(PropertyFilter additionalfilter){
		super(additionalfilter);
	}
	
	@Override
	protected boolean filterOut(UIProperty property) {
		return false;
	}

}
