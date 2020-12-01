package de.embl.rieslab.htsmlm;

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

import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.MultiStateUIProperty;
import de.embl.rieslab.emu.utils.ColorRepository;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.uipropertyflags.FilterWheelFlag;

public class DualFWPanel extends AbstractFiltersPanel {

	private static final long serialVersionUID = 1L;
	
	//////// Components
	private JToggleButton[] togglebuttons1_;
	private JToggleButton[] togglebuttons2_;
	private TitledBorder border_;
	
	//////// Properties
	private static final String FW_POSITION1 = "Filter wheel position";
	private static final String FW_POSITION2 = "Filter wheel 2 position";
	
	//////// Parameters
	private static final String TITLE = "Filters";
	private static final String PARAM_NAMES1 = "Filter names";
	private static final String PARAM_COLORS1 = "Filter colors";
	private static final String PARAM_NAMES2 = "Filter names 2";
	private static final String PARAM_COLORS2 = "Filter colors 2";
	private static final String PARAM_TITLE = "Panel title";
	
	//////// Initial parameters
	private static final int NUM_POS = 6;
	private static final String NAME_EMPTY = "None";
	private static final String COLOR_EMPTY = ColorRepository.strgray;
	private String names1_, colors1_, names2_, colors2_, title_; 


	public DualFWPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		this.setLayout(new GridBagLayout());
		border_ = BorderFactory.createTitledBorder(null, title_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0,0,0));
		this.setBorder(border_);
		border_.setTitleFont(border_.getTitleFont().deriveFont(Font.BOLD, 12));

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
			togglebuttons1_[i].setToolTipText("Set the first filter property to the "+i+"th position (as defined in the wizard).");
			
			c.gridx = i;
			this.add(togglebuttons1_[i], c);
			
			group1.add(togglebuttons1_[i]);
			
			togglebuttons1_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(0);
						if(pos>=0 && pos<togglebuttons1_.length){
							setUIPropertyValueByStateIndex(FW_POSITION1,pos);
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
			togglebuttons2_[i].setToolTipText("Set the second filter property to the "+i+"th position (as defined in the wizard).");
			
			c.gridx = i;
			this.add(togglebuttons2_[i], c);
			
			group2.add(togglebuttons2_[i]);
			
			togglebuttons2_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(1);
						if(pos>=0 && pos<togglebuttons2_.length){
							setUIPropertyValueByStateIndex(FW_POSITION2,pos);
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
			try {
				((MultiStateUIProperty) getUIProperty(FW_POSITION1)).setStateNames(astr);
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else {
			String[] astr = names2_.split(",");
			int maxind = togglebuttons2_.length > astr.length ? astr.length : togglebuttons2_.length; 
			for(int i=0;i<maxind;i++){
				togglebuttons2_[i].setText(astr[i]);
			}
			try {
				((MultiStateUIProperty) getUIProperty(FW_POSITION2)).setStateNames(astr);
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
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
	
	@Override
	protected void initializeProperties() {
		String desc = "Filter wheel No1 position. Choose a device property that corresponds to an element with a "
				+ "finite number of states (e.g. a filter wheel). The filter wheel property has "+NUM_POS+" positions. "
				+ "For each position, indicate in the \"Filter wheel position state #\" the corresponding "
				+ "device property value. In order to determine the value, use the Micro-Manager device "
				+ "property browser. All states must be set, but multiple states can have the same value. "
				+ "Each state name and color can be configured in the Parameters tab.";
		
		String desc2 = "Filter wheel No2 position. Choose a device property that corresponds to an element with a "
				+ "finite number of states (e.g. a filter wheel). The filter wheel property has "+NUM_POS+" positions. "
				+ "For each position, indicate in the \"Filter wheel position state #\" the corresponding "
				+ "device property value. In order to determine the value, use the Micro-Manager device "
				+ "property browser. All states must be set, but multiple states can have the same value. "
				+ "Each state name and color can be configured in the Parameters tab.";
		
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION1, desc, new FilterWheelFlag(),NUM_POS));
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION2, desc2, new FilterWheelFlag(),NUM_POS));		
	}


	@Override
	protected void initializeParameters() {
		title_ = TITLE;
		String names = "Name0";
		String colors = "Color";
		names1_ = NAME_EMPTY;
		colors1_ = COLOR_EMPTY;
		names2_ = NAME_EMPTY;
		colors2_ = COLOR_EMPTY;
		for(int j=1;j<NUM_POS;j++){
			names += ","+"Name"+j; 
			colors += ","+"Color"+j; 

			names1_ += ","+NAME_EMPTY;
			colors1_ += ","+COLOR_EMPTY;
			names2_ += ","+NAME_EMPTY;
			colors2_ += ","+COLOR_EMPTY;
		};
		
		String desc_c1 = "Filter name colors displayed on the GUI. The entry should be written "
				+ "as \""+colors+"\". The colors should be separated by commas. The maximum number "
				+ "of filters color is "+NUM_POS+", beyond that the colors will be ignored. The available "
						+ "colors are: "+ColorRepository.getCommaSeparatedColors()+".";

		String desc_n1 = "Filter names displayed on the GUI. The entry should be written "
				+ "as \""+names+"\". The names should be separated by commas. The maximum "
				+ "number of filters name is "+NUM_POS+", beyond that the names will be ignored.";
		
	
		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Title of the dual FW panel",title_));
		addUIParameter(new StringUIParameter(this, PARAM_NAMES1,desc_n1,names1_));
		addUIParameter(new StringUIParameter(this, PARAM_COLORS1,desc_c1,colors1_));
	
		addUIParameter(new StringUIParameter(this, PARAM_NAMES2,desc_n1,names2_));
		addUIParameter(new StringUIParameter(this, PARAM_COLORS2,desc_c1,colors2_));
	
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(FW_POSITION1.equals(name)){
			int pos;
			try {
				pos = ((MultiStateUIProperty) getUIProperty(FW_POSITION1)).getStateIndex(newvalue);
				if(pos<togglebuttons1_.length){
					togglebuttons1_[pos].setSelected(true);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(FW_POSITION2.equals(name)){
			int pos;
			try {
				pos = ((MultiStateUIProperty) getUIProperty(FW_POSITION2)).getStateIndex(newvalue);
				if(pos<togglebuttons2_.length){
					togglebuttons2_[pos].setSelected(true);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_NAMES1.equals(label)){
			try {
				names1_ = getStringUIParameterValue(PARAM_NAMES1);
				setNames(0);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_COLORS1.equals(label)){
			try {
				colors1_ = getStringUIParameterValue(PARAM_COLORS1);
				setColors(0);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_NAMES2.equals(label)){
			try {
				names2_ = getStringUIParameterValue(PARAM_NAMES2);
				setNames(1);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_COLORS2.equals(label)){
			try {
				colors2_ = getStringUIParameterValue(PARAM_COLORS2);
				setColors(1);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_TITLE.equals(label)){
			try {
				title_ = getStringUIParameterValue(PARAM_TITLE);
				border_.setTitle(title_);
				this.repaint();
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutDown() {
		// do nothing
	}

	@Override
	public String getDescription() {
		return "The "+getPanelLabel()+" panel is meant to control a dual filterwheel with at most "+NUM_POS+" filters in each filterwheel. The filter colors and names can be customized from the configuration menu.";
	}

	@Override
	protected void initializeInternalProperties() {
		// Do nothing
	}

	@Override
	public void internalpropertyhasChanged(String label) {
		// Do nothing
	}

	@Override
	protected void addComponentListeners() {
		// Do nothing		
	}

}
