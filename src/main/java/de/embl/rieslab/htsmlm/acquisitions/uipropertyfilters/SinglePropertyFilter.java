package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class SinglePropertyFilter extends PropertyFilter{

	private String excludedprop_;
	
	public SinglePropertyFilter(String excludedproperty){
		excludedprop_ = excludedproperty;
	}
	
	public SinglePropertyFilter(String excludedproperty, PropertyFilter additionalfilter){
		super(additionalfilter);
		
		excludedprop_ = excludedproperty;
	}

	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property.getPropertyLabel().equals(excludedprop_)){
			return true;
		}
		return false;
	}

}
