package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class NoPropertyFilter extends PropertyFilter {

	public NoPropertyFilter(){
	}

	public NoPropertyFilter(PropertyFilter additionalFilter){
		super(additionalFilter);
	}
	
	@Override
	protected boolean filterOut(UIProperty property) {
		return false;
	}

}
