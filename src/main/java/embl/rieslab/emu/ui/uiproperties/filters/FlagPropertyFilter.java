package main.java.embl.rieslab.emu.ui.uiproperties.filters;

import main.java.embl.rieslab.emu.ui.uiproperties.UIProperty;
import main.java.embl.rieslab.emu.ui.uiproperties.flag.PropertyFlag;

public class FlagPropertyFilter extends PropertyFilter {

	private PropertyFlag flag_;
	
	public FlagPropertyFilter(PropertyFlag flag){
		flag_ = flag;
	}

	public FlagPropertyFilter(PropertyFlag flag, PropertyFilter additionalfilter){
		super(additionalfilter);
		
		flag_ = flag;
	}
	
	@Override
	public boolean filterOut(UIProperty property) {
		if(property.getFlag().equals(flag_)){
			return false;
		}
		return true;
	}

}
