package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class SinglePropertyFilter extends PropertyFilter{

	private String excludedProp_;
	
	public SinglePropertyFilter(String excludedProperty){
		excludedProp_ = excludedProperty;
	}
	
	public SinglePropertyFilter(String excludedProperty, PropertyFilter additionalFilter){
		super(additionalFilter);
		
		excludedProp_ = excludedProperty;
	}

	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property.getPropertyLabel().equals(excludedProp_)){
			return true;
		}
		return false;
	}

}
