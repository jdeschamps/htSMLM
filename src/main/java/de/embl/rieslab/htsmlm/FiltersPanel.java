package main.java.de.embl.rieslab.htsmlm;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import main.java.de.embl.rieslab.emu.ui.ConfigurablePanel;
import main.java.de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import main.java.de.embl.rieslab.emu.ui.uiproperties.MultiStateUIProperty;
import main.java.de.embl.rieslab.emu.utils.ColorRepository;
import main.java.de.embl.rieslab.htsmlm.flags.FilterWheelFlag;

public class FiltersPanel extends ConfigurablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8562433353787092702L;

	//////// Components
	private JToggleButton[] togglebuttons1_;
	private JToggleButton[] togglebuttons2_;
	
	//////// Properties
	public static String FW_POSITION1 = "FW1 pos";
	public static String FW_POSITION2 = "FW2 pos";
	
	//////// Parameters
	public final static String PARAM_NAMES1 = "Filters name 1";
	public final static String PARAM_COLORS1 = "Filters color 1";
	public final static String PARAM_NAMES2 = "Filters name 2";
	public final static String PARAM_COLORS2 = "Filters color 2";
	
	//////// Initial parameters
	public final static int NUM_POS = 6;
	public final static String NAME_EMPTY = "None";
	public final static String COLOR_EMPTY = ColorRepository.strgray;
	String names1_, colors1_, names2_, colors2_; 


	public FiltersPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder(null, getLabel(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new Color(0,0,0)));
		((TitledBorder) this.getBorder()).setTitleFont(((TitledBorder) this.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));

		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.9;
		c.weightx = 0.7;
		c.gridy = 0;
		c.insets = new Insets(2,0,2,0);

		ButtonGroup group1 = new ButtonGroup();
		togglebuttons1_ = new JToggleButton[NUM_POS];
		for(int i=0;i<togglebuttons1_.length;i++){
			togglebuttons1_[i] = new JToggleButton();
			
			c.gridx = i;
			this.add(togglebuttons1_[i], c);
			
			group1.add(togglebuttons1_[i]);
			
			togglebuttons1_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(0);
						if(pos>=0 && pos<togglebuttons1_.length){
							setUIPropertyValue(FW_POSITION1,getValueFromPosition(0, pos));
						}				
					} 
				}
	        });
		}  
		

		c.gridy = 1;
		ButtonGroup group2 = new ButtonGroup();
		togglebuttons2_ = new JToggleButton[NUM_POS];
		for(int i=0;i<togglebuttons2_.length;i++){
			togglebuttons2_[i] = new JToggleButton();
			
			c.gridx = i;
			this.add(togglebuttons2_[i], c);
			
			group2.add(togglebuttons2_[i]);
			
			togglebuttons2_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(1);
						if(pos>=0 && pos<togglebuttons2_.length){
							setUIPropertyValue(FW_POSITION2,getValueFromPosition(1, pos));
						}				
					} 
				}
	        });
		}  

		setNames(0);
		setColors(0);
		setNames(1);
		setColors(1);
	}

	protected int getSelectedButtonNumber(int fw_num) {
		int val=-1;
		if(fw_num == 0) {
			for(int i=0;i<togglebuttons1_.length;i++){
				if(togglebuttons1_[i].isSelected()){
					return i;
				}
			}
		} else {
			for(int i=0;i<togglebuttons2_.length;i++){
				if(togglebuttons2_[i].isSelected()){
					return i;
				}
			}
		}
		return val;
	}
	
	private void setNames(int fw){
		if(fw == 0) {
			String[] astr = names1_.split(",");
			int maxind = togglebuttons1_.length > astr.length ? astr.length : togglebuttons1_.length; 
			for(int i=0;i<maxind;i++){
				togglebuttons1_[i].setText(astr[i]);
			}
			((MultiStateUIProperty) getUIProperty(FW_POSITION1)).setStatesName(astr);
		} else {
			String[] astr = names2_.split(",");
			int maxind = togglebuttons2_.length > astr.length ? astr.length : togglebuttons2_.length; 
			for(int i=0;i<maxind;i++){
				togglebuttons2_[i].setText(astr[i]);
			}
			((MultiStateUIProperty) getUIProperty(FW_POSITION2)).setStatesName(astr);
		}
	}
	
	private void setColors(int fw){
		if(fw == 0) {
			String[] astr = colors1_.split(",");
			int maxind = togglebuttons1_.length > astr.length ? astr.length : togglebuttons1_.length;
			for(int i=0;i<maxind;i++){
				togglebuttons1_[i].setForeground(ColorRepository.getColor(astr[i]));
			}
		} else {
			String[] astr = colors2_.split(",");
			int maxind = togglebuttons2_.length > astr.length ? astr.length : togglebuttons2_.length;
			for(int i=0;i<maxind;i++){
				togglebuttons2_[i].setForeground(ColorRepository.getColor(astr[i]));
			}
		}
	}
	
	
	protected String getValueFromPosition(int fw, int pos){
		if(fw == 0) {
			return ((MultiStateUIProperty) getUIProperty(FW_POSITION1)).getStateValue(pos);
		} else {
			return ((MultiStateUIProperty) getUIProperty(FW_POSITION2)).getStateValue(pos);
		}
	}
	
	@Override
	protected void initializeProperties() {
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION1, "Filter wheel 1 position property.", new FilterWheelFlag(),NUM_POS));
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION2, "Filter wheel 2 position property.", new FilterWheelFlag(),NUM_POS));		
	}

	@Override
	protected void initializeParameters() {
		names1_ = NAME_EMPTY;
		colors1_ = COLOR_EMPTY;
		for(int i=0;i<NUM_POS-1;i++){
			names1_ += ","+NAME_EMPTY; 
			colors1_ += ","+COLOR_EMPTY; 
		}
		names2_ = names1_;
		colors2_ = colors1_;
		
		addUIParameter(new StringUIParameter(this, PARAM_NAMES1,"Filter names displayed by the UI. The entry should be written as \"name1,name2,name3,None,None,None\". The names should be separated by a comma. "
				+ "The maximum number of filters name is "+NUM_POS+", beyond that the names will be ignored. If the comma are not present, then the entry will be set as the name of the first filter.",names1_));
		addUIParameter(new StringUIParameter(this, PARAM_COLORS1,"Filter colors displayed by the UI. The entry should be written as \"color1,color2,color3,grey,grey,grey\". The names should be separated by a comma. "
				+ "The maximum number of filters color is "+NUM_POS+", beyond that the colors will be ignored. If the comma are not present, then no color will be allocated. The available colors are:\n"+ColorRepository.getColorsInOneColumn(),colors1_));
	
		addUIParameter(new StringUIParameter(this, PARAM_NAMES2,"Filter names displayed by the UI. The entry should be written as \"name1,name2,name3,None,None,None\". The names should be separated by a comma. "
				+ "The maximum number of filters name is "+NUM_POS+", beyond that the names will be ignored. If the comma are not present, then the entry will be set as the name of the first filter.",names2_));
		addUIParameter(new StringUIParameter(this, PARAM_COLORS2,"Filter colors displayed by the UI. The entry should be written as \"color1,color2,color3,grey,grey,grey\". The names should be separated by a comma. "
				+ "The maximum number of filters color is "+NUM_POS+", beyond that the colors will be ignored. If the comma are not present, then no color will be allocated. The available colors are:\n"+ColorRepository.getColorsInOneColumn(),colors2_));
	
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(name.equals(FW_POSITION1)){
			int pos = ((MultiStateUIProperty) getUIProperty(FW_POSITION1)).getStatePositionNumber(newvalue);
			if(pos<togglebuttons1_.length){
				togglebuttons1_[pos].setSelected(true);
			}
		} else if(name.equals(FW_POSITION2)){
			int pos = ((MultiStateUIProperty) getUIProperty(FW_POSITION2)).getStatePositionNumber(newvalue);
			if(pos<togglebuttons2_.length){
				togglebuttons2_[pos].setSelected(true);
			}
		}
	}

	@Override
	public void parameterhasChanged(String label) {
		if(label.equals(PARAM_NAMES1)){
			names1_ = getStringUIParameterValue(PARAM_NAMES1);
			setNames(0);
		} else if(label.equals(PARAM_COLORS1)){
			colors1_ = getStringUIParameterValue(PARAM_COLORS1);
			setColors(0);
		} else if(label.equals(PARAM_NAMES2)){
			names2_ = getStringUIParameterValue(PARAM_NAMES2);
			setNames(1);
		} else if(label.equals(PARAM_COLORS2)){
			colors2_ = getStringUIParameterValue(PARAM_COLORS2);
			setColors(1);
		}
	}

	@Override
	public void shutDown() {
		// do nothing
	}

	@Override
	public String getDescription() {
		return "The "+getLabel()+" panel should be liked to te filterwheel and allows the contol of up to "+NUM_POS+" filters. The colors and names can bu customized from the configuration menu.";
	}

	@Override
	protected void initializeInternalProperties() {
		// Do nothing
	}

	@Override
	public void internalpropertyhasChanged(String label) {
		// Do nothing
	}

}
