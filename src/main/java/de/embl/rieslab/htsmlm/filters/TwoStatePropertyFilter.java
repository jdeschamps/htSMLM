package main.java.de.embl.rieslab.htsmlm.filters;

import main.java.de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import main.java.de.embl.rieslab.emu.ui.uiproperties.UIProperty;

public class TwoStatePropertyFilter extends PropertyFilter {

	public TwoStatePropertyFilter(){
	}

	public TwoStatePropertyFilter(PropertyFilter additionalfilter){
		super(additionalfilter);
	}
	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property instanceof TwoStateUIProperty){
			return false;
		}
		return true;
	}

}
