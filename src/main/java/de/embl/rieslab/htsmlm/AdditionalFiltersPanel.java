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
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.MultiStateUIProperty;
import de.embl.rieslab.emu.utils.ColorRepository;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.uipropertyflags.FilterWheelFlag;

public class AdditionalFiltersPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;
	
	//////// Components
	private JToggleButton[] togglebuttons1_;
	private JToggleButton[] togglebuttons2_;
	private TitledBorder border1_;
	private TitledBorder border2_;
	
	//////// Properties
	private static final String SLIDER1_POSITION = "Slider 1 position";
	private static final String SLIDER2_POSITION = "Slider 2 position";
	
	//////// Parameters
	private static final String PARAM1_NAMES = "Slider 1 names";
	private static final String PARAM1_COLORS = "Slider 1 colors";
	private static final String PARAM2_NAMES = "Slider 2 names";
	private static final String PARAM2_COLORS = "Slider 2 colors";
	private static final String PARAM1_TITLE = "Slider 1 title";
	private static final String PARAM2_TITLE = "Slider 2 title";
	
	//////// Initial parameters
	private static final int NUM_POS = 4;
	private static final String NAME_EMPTY = "None";
	private static final String COLOR_EMPTY = ColorRepository.strgray;
	private String names1_, colors1_, names2_, colors2_, title1_, title2_; 


	public AdditionalFiltersPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		JPanel pane1 = new JPanel();
		pane1.setLayout(new GridBagLayout());
		
		JPanel pane2 = new JPanel();
		pane2.setLayout(new GridBagLayout());
		
		pane1.setLayout(new GridBagLayout());
		pane2.setLayout(new GridBagLayout());

		border1_ = BorderFactory.createTitledBorder(null, title1_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black);
		pane1.setBorder(border1_);
		border1_.setTitleFont(border1_.getTitleFont().deriveFont(Font.BOLD, 12));

		border2_ = BorderFactory.createTitledBorder(null, title2_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black);
		pane2.setBorder(border2_);
		border2_.setTitleFont(border2_.getTitleFont().deriveFont(Font.BOLD, 12));
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.9;
		c.weightx = 0.7;
		c.gridy = 0;
		c.insets = new Insets(2,0,2,0);

		ButtonGroup group1=new ButtonGroup();
		ButtonGroup group2=new ButtonGroup();

		togglebuttons1_ = new JToggleButton[NUM_POS];
		for(int i=0;i<togglebuttons1_.length;i++){
			togglebuttons1_[i] = new JToggleButton();
			togglebuttons1_[i].setToolTipText("Set the first filter property to the "+i+"th position (as defined in the wizard).");
			
			c.gridx = i;
			pane1.add(togglebuttons1_[i], c);
			
			group1.add(togglebuttons1_[i]);
			
			togglebuttons1_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(togglebuttons1_);
						if(pos>=0 && pos<togglebuttons1_.length){
							setUIPropertyValueByStateIndex(SLIDER1_POSITION,pos);
						}				
					} 
				}
	        });
		}  

		c.gridy = 1;
		togglebuttons2_ = new JToggleButton[NUM_POS];
		for(int i=0;i<togglebuttons2_.length;i++){
			togglebuttons2_[i] = new JToggleButton();
			togglebuttons2_[i].setToolTipText("Set the second filter property to the "+i+"th position (as defined in the wizard).");
			
			c.gridx = i;
			pane2.add(togglebuttons2_[i], c);
			
			group2.add(togglebuttons2_[i]);
			
			togglebuttons2_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(togglebuttons2_);
						if(pos>=0 && pos<togglebuttons2_.length){
							setUIPropertyValueByStateIndex(SLIDER2_POSITION,pos);
						}				
					} 
				}
	        });
		}  

		setNames(0);
		setColors(0);
		setNames(1);
		setColors(1);

		////////////////////////////////////////////////////////////////////////////Main panel
		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.4;
		c.weighty = 0.2;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		this.add(pane1,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(pane2,c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 0.8;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JPanel(),c);
	}

	protected int getSelectedButtonNumber(JToggleButton[] togglebuttons) {
		int val=-1;
		
		for(int i=0;i<togglebuttons.length;i++){
			if(togglebuttons[i].isSelected()){
				return i;
			}
		}
		return val;
	}
	
	private void setNames(int j){
		if(j == 0){
			String[] astr = names1_.split(",");
			int maxind = togglebuttons1_.length > astr.length ? astr.length : togglebuttons1_.length;
			for(int i=0;i<maxind;i++){
				togglebuttons1_[i].setText(astr[i]);
			}
			try {
				((MultiStateUIProperty) getUIProperty(SLIDER1_POSITION)).setStateNames(astr);
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(j==1){	
			String[] astr = names2_.split(",");
			int maxind = togglebuttons2_.length > astr.length ? astr.length : togglebuttons2_.length;
			for(int i=0;i<maxind;i++){
				togglebuttons2_[i].setText(astr[i]);
			}
			try {
				((MultiStateUIProperty) getUIProperty(SLIDER2_POSITION)).setStateNames(astr);
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setColors(int j){
		if(j == 0){
			String[] astr = colors1_.split(",");
			int maxind = togglebuttons1_.length > astr.length ? astr.length : togglebuttons1_.length;
			for(int i=0;i<maxind;i++){
				togglebuttons1_[i].setForeground(ColorRepository.getColor(astr[i]));
			}
		} else if(j==1){	
			String[] astr = colors2_.split(",");
			int maxind = togglebuttons2_.length > astr.length ? astr.length : togglebuttons2_.length;
			for(int i=0;i<maxind;i++){
				togglebuttons2_[i].setForeground(ColorRepository.getColor(astr[i]));
			}
		}
	}
	
	@Override
	protected void initializeProperties() {
		addUIProperty(new MultiStateUIProperty(this, SLIDER1_POSITION, "Slider 1 position.", new FilterWheelFlag(),NUM_POS));		
		addUIProperty(new MultiStateUIProperty(this, SLIDER2_POSITION, "Slider 2 position.", new FilterWheelFlag(),NUM_POS));		
	}

	@Override
	protected void initializeParameters() {
		names1_ = NAME_EMPTY;
		colors1_ = COLOR_EMPTY;
		names2_ = NAME_EMPTY;
		colors2_ = COLOR_EMPTY;
		for(int i=0;i<NUM_POS-1;i++){
			names1_ += ","+NAME_EMPTY; 
			colors1_ += ","+COLOR_EMPTY; 
			names2_ += ","+NAME_EMPTY; 
			colors2_ += ","+COLOR_EMPTY; 
		}
		title1_ = "Slider 1";
		title2_ = "Slider 2";

		addUIParameter(new StringUIParameter(this, PARAM1_TITLE, "Title of the first set of additional filters.",title1_));
		addUIParameter(new StringUIParameter(this, PARAM2_TITLE, "Title of the second set of additional filters.",title2_));
		addUIParameter(new StringUIParameter(this, PARAM1_NAMES,"Filter names displayed by the UI. The entry should be written as \"name1,name2,name3,None,None,None\". The names should be separated by a comma. "
				+ "The maximum number of filters name is "+NUM_POS+", beyond that the names will be ignored. If the comma are not present, then the entry will be set as the name of the first filter.",names1_));
		addUIParameter(new StringUIParameter(this, PARAM1_COLORS,"Filter colors displayed by the UI. The entry should be written as \"color1,color2,color3,grey,grey,grey\". The names should be separated by a comma. "
				+ "The maximum number of filters color is "+NUM_POS+", beyond that the colors will be ignored. If the comma are not present, then no color will be allocated. The available colors are: "+ColorRepository.getCommaSeparatedColors(),colors1_));
		addUIParameter(new StringUIParameter(this, PARAM2_NAMES,"Filter names displayed by the UI. The entry should be written as \"name1,name2,name3,None,None,None\". The names should be separated by a comma. "
				+ "The maximum number of filters name is "+NUM_POS+", beyond that the names will be ignored. If the comma are not present, then the entry will be set as the name of the first filter.",names2_));
		addUIParameter(new StringUIParameter(this, PARAM2_COLORS,"Filter colors displayed by the UI. The entry should be written as \"color1,color2,color3,grey,grey,grey\". The names should be separated by a comma. "
				+ "The maximum number of filters color is "+NUM_POS+", beyond that the colors will be ignored. If the comma are not present, then no color will be allocated. The available colors are: "+ColorRepository.getCommaSeparatedColors(),colors2_));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(SLIDER1_POSITION.equals(name)){
			int pos;
			try {
				pos = ((MultiStateUIProperty) getUIProperty(SLIDER1_POSITION)).getStateIndex(newvalue);
				if(pos<togglebuttons1_.length){
					togglebuttons1_[pos].setSelected(true);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(SLIDER2_POSITION.equals(name)){
			int pos;
			try {
				pos = ((MultiStateUIProperty) getUIProperty(SLIDER2_POSITION)).getStateIndex(newvalue);			
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
		if(PARAM1_NAMES.equals(label)){
			try {
				names1_ = getStringUIParameterValue(PARAM1_NAMES);
				setNames(0);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM1_COLORS.equals(label)){
			try {
				colors1_ = getStringUIParameterValue(PARAM1_COLORS);
				setColors(0);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM2_NAMES.equals(label)){
			try {
				names2_ = getStringUIParameterValue(PARAM2_NAMES);
				setNames(1);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM2_COLORS.equals(label)){
			try {
				colors2_ = getStringUIParameterValue(PARAM2_COLORS);
				setColors(1);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM1_TITLE.equals(label)){
			try {
				title1_ = getStringUIParameterValue(PARAM1_TITLE);
				border1_.setTitle(title1_);
				this.repaint();
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM2_TITLE.equals(label)){
			try {
				title2_ = getStringUIParameterValue(PARAM2_TITLE);
				border2_.setTitle(title2_);
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
		return "The "+getPanelLabel()+" is meant to control a dual filterwheel/slider with at most "+NUM_POS+" filter in each filter wheel. The filter colors and names can be customized from the configuration menu.";
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
