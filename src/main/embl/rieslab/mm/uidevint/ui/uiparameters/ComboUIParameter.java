package main.embl.rieslab.mm.uidevint.ui.uiparameters;

import main.embl.rieslab.mm.uidevint.ui.PropertyPanel;

public class ComboUIParameter extends UIParameter<String> {

	private String[] combovalues_;
	
	public ComboUIParameter(PropertyPanel owner, String name, String description, String[] combovalues, int ind) {
		super(owner, name, description);
		
		combovalues_ = combovalues;
		if(ind<combovalues_.length){
			setValue(combovalues_[ind]);
		}else {
			setValue(combovalues_[0]);
		}
	}

	@Override
	public void setType() {
		type_ = UIParameterType.COMBO;
	}

	@Override
	public boolean isSuitable(String val) {
		for(int i=0;i<combovalues_.length;i++){
			if(combovalues_[i].equals(val)){
				return true;
			}
		}
		return false;
	}

	@Override
	public String convertValue(String val) {
		return val;
	}

	@Override
	public String getStringValue() {
		return getValue();
	}

	public String[] getComboValues(){
		return combovalues_;
	}
	
}