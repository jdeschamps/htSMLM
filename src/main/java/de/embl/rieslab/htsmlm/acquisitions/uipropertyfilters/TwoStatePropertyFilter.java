package de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters;

import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class TwoStatePropertyFilter extends PropertyFilter {

	public TwoStatePropertyFilter(){
	}

	public TwoStatePropertyFilter(PropertyFilter propertyFilter){
		super(propertyFilter);
	}
	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property instanceof TwoStateUIProperty){
			return false;
		}
		return true;
	}

}
