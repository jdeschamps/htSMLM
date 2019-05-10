package main.java.de.embl.rieslab.htsmlm.filters;

import main.java.de.embl.rieslab.emu.ui.uiproperties.UIProperty;

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
