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
	public final static String FW_POSITION1 = "Filter wheel position";
	public final static String FW_POSITION2 = "Filter wheel 2 position";
	
	//////// Parameters
	public final static String TITLE = "Filters";
	public final static String PARAM_NAMES1 = "Filter names";
	public final static String PARAM_COLORS1 = "Filter colors";
	public final static String PARAM_NAMES2 = "Filter names 2";
	public final static String PARAM_COLORS2 = "Filter colors 2";
	public final static String PARAM_TITLE = "Panel title";
	
	//////// Initial parameters
	public final static int NUM_POS = 6;
	public final static String NAME_EMPTY = "None";
	public final static String COLOR_EMPTY = ColorRepository.strgray;
	String names1_, colors1_, names2_, colors2_, title_; 


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
			
			c.gridx = i;
			this.add(togglebuttons1_[i], c);
			
			group1.add(togglebuttons1_[i]);
			
			togglebuttons1_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(0);
						if(pos>=0 && pos<togglebuttons1_.length){
							setUIPropertyValue(FW_POSITION1,String.valueOf(pos));
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
							setUIPropertyValue(FW_POSITION2,String.valueOf(pos));
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
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION1, "Filter wheel 1 position property.", new FilterWheelFlag(),NUM_POS));
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION2, "Filter wheel 2 position property.", new FilterWheelFlag(),NUM_POS));		
	}

	@Override
	protected void initializeParameters() {
		title_ = TITLE;
		names1_ = NAME_EMPTY;
		colors1_ = COLOR_EMPTY;
		for(int i=0;i<NUM_POS-1;i++){
			names1_ += ","+NAME_EMPTY; 
			colors1_ += ","+COLOR_EMPTY; 
		}
		names2_ = names1_;
		colors2_ = colors1_;

		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Title of the dual FW panel",title_));
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
		return "The "+getPanelLabel()+" panel should be liked to te filterwheel and allows the contol of up to "+NUM_POS+" filters. The colors and names can bu customized from the configuration menu.";
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
