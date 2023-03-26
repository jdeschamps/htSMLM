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


/**
 * A ConfigurablePanel with two toggle button groups, suitable
 * for filter wheels or any device with discrete/discretized number
 * of positions/states.
 * 
 * @author Joran Deschamps
 *
 */
public class AdditionalFiltersPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;
	
	//////// Components
	private JToggleButton[] toggleButtons1_;
	private JToggleButton[] toggleButtons2_;
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
		// instantiate the two panels
		JPanel pane1 = new JPanel();
		pane1.setLayout(new GridBagLayout());
		
		JPanel pane2 = new JPanel();
		pane2.setLayout(new GridBagLayout());
		
		pane1.setLayout(new GridBagLayout());
		pane2.setLayout(new GridBagLayout());

		// set borders
		border1_ = BorderFactory.createTitledBorder(null, title1_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black);
		pane1.setBorder(border1_);
		border1_.setTitleFont(border1_.getTitleFont().deriveFont(Font.BOLD, 12));

		border2_ = BorderFactory.createTitledBorder(null, title2_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black);
		pane2.setBorder(border2_);
		border2_.setTitleFont(border2_.getTitleFont().deriveFont(Font.BOLD, 12));
		
		// create button groups
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.9;
		c.weightx = 0.7;
		c.gridy = 0;
		c.insets = new Insets(2,0,2,0);

		ButtonGroup group1=new ButtonGroup();
		ButtonGroup group2=new ButtonGroup();

		// add buttons to the first panel
		toggleButtons1_ = new JToggleButton[NUM_POS];
		for(int i = 0; i< toggleButtons1_.length; i++){
			// instantiate button
			toggleButtons1_[i] = new JToggleButton();
			toggleButtons1_[i].setToolTipText("Set the first filter property to the "+i+"th position (as defined in the wizard).");
			
			// add to panel and group
			c.gridx = i;
			pane1.add(toggleButtons1_[i], c);
			group1.add(toggleButtons1_[i]);
			
			// set item listener
			toggleButtons1_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(toggleButtons1_);
						if(pos>=0 && pos< toggleButtons1_.length){
							// set the UI property to the corresponding position.
							setUIPropertyValueByStateIndex(SLIDER1_POSITION,pos);
						}				
					} 
				}
	        });
		}  

		// add buttons to the second panel
		c.gridy = 1;
		toggleButtons2_ = new JToggleButton[NUM_POS];
		for(int i = 0; i< toggleButtons2_.length; i++){
			toggleButtons2_[i] = new JToggleButton();
			toggleButtons2_[i].setToolTipText("Set the second filter property to the "+i+"th position (as defined in the wizard).");
			
			c.gridx = i;
			pane2.add(toggleButtons2_[i], c);
			
			group2.add(toggleButtons2_[i]);
			
			toggleButtons2_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber(toggleButtons2_);
						if(pos>=0 && pos< toggleButtons2_.length){
							// set the UI property to the corresponding position.
							setUIPropertyValueByStateIndex(SLIDER2_POSITION,pos);
						}				
					} 
				}
	        });
		}  

		// set names and colors
		setNames(0);
		setColors(0);
		setNames(1);
		setColors(1);

		// set up the two panels
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

	/**
	 * Return the position of the first selected button in the array.
	 * 
	 * @param togglebuttons Array of buttons
	 * @return Index of the first selected button
	 */
	protected int getSelectedButtonNumber(JToggleButton[] togglebuttons) {
		int val=-1;
		
		for(int i=0;i<togglebuttons.length;i++){
			if(togglebuttons[i].isSelected()){
				return i;
			}
		}
		return val;
	}
	
	/**
	 * Set the names and states of the buttons of group 0 or 1.
	 * 
	 * @param panelIndex Button group index
	 */
	private void setNames(int panelIndex){
		if(panelIndex == 0){
			String[] astr = names1_.split(",");
			int maxind = toggleButtons1_.length > astr.length ? astr.length : toggleButtons1_.length;
			for(int i=0;i<maxind;i++){
				toggleButtons1_[i].setText(astr[i]);
			}
			try {
				((MultiStateUIProperty) getUIProperty(SLIDER1_POSITION)).setStateNames(astr);
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(panelIndex==1){	
			String[] astr = names2_.split(",");
			int maxind = toggleButtons2_.length > astr.length ? astr.length : toggleButtons2_.length;
			for(int i=0;i<maxind;i++){
				toggleButtons2_[i].setText(astr[i]);
			}
			try {
				((MultiStateUIProperty) getUIProperty(SLIDER2_POSITION)).setStateNames(astr);
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Set the color of the buttons of group 0 or 1.
	 * 
	 * @param panelIndex Button group index
	 */
	private void setColors(int j){
		if(j == 0){
			String[] astr = colors1_.split(",");
			int maxind = toggleButtons1_.length > astr.length ? astr.length : toggleButtons1_.length;
			for(int i=0;i<maxind;i++){
				toggleButtons1_[i].setForeground(ColorRepository.getColor(astr[i]));
			}
		} else if(j==1){	
			String[] astr = colors2_.split(",");
			int maxind = toggleButtons2_.length > astr.length ? astr.length : toggleButtons2_.length;
			for(int i=0;i<maxind;i++){
				toggleButtons2_[i].setForeground(ColorRepository.getColor(astr[i]));
			}
		}
	}
	
	private String getPosDescription(int i) {
		return "Slider No"+i+" position. Choose a device property that corresponds to an element with a finite"
				+ " number of states (e.g. a filter wheel). Each slider property has 4 positions. For each "
				+ "position, indicate in the \"Slider No"+i+" position state x\" (where x is between 0 and "+(NUM_POS-1)+") the "
						+ "corresponding device property value. In order to determine the value, use the "
						+ "Micro-Manager device property browser. All states must be set, but multiple states "
						+ "can have the same value. Each state name and color can be configured in the "
						+ "Parameters tab.";
	}
	
	@Override
	protected void initializeProperties() {
		addUIProperty(new MultiStateUIProperty(this, SLIDER1_POSITION, getPosDescription(1), new FilterWheelFlag(),NUM_POS));		
		addUIProperty(new MultiStateUIProperty(this, SLIDER2_POSITION, getPosDescription(2), new FilterWheelFlag(),NUM_POS));		
	}

	
	private String getNameDescription(int i) {
		String names = "Name0";
		for(int j=1;j<NUM_POS;j++){
			names += ","+"Name"+j; 
		}
	
		return "Filter names displayed on the GUI for the additional filter wheel No"+i+". The entry should be written "
				+ "as \""+names+"\". The names should be separated by commas. The maximum "
				+ "number of filters name is "+NUM_POS+", beyond that the names will be ignored. If the commas are not "
				+ "present, then the entry will be set as the name of the first filter.";
	}
	
	private String getColorDescription(int i) {
		String colors = "Color";
		for(int j=1;j<NUM_POS;j++){
			colors += ","+"Color"+j; 
		}
		
		return "Colors of the filter names displayed on the GUI for the additional filter wheel No"+i+". The entry "
				+ "should be written as \""+colors+"\". The colors should be separated by commas. The maximum number "
						+ "of filters color is "+NUM_POS+", beyond that the colors will be ignored. The available "
								+ "colors are: "+ColorRepository.getCommaSeparatedColors()+".";
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

		addUIParameter(new StringUIParameter(this, PARAM1_TITLE, "Title of the set of additional filters No1.",title1_));
		addUIParameter(new StringUIParameter(this, PARAM2_TITLE, "Title of the set of additional filters No2.",title2_));
		
		addUIParameter(new StringUIParameter(this, PARAM1_NAMES,getNameDescription(1),names1_));
		addUIParameter(new StringUIParameter(this, PARAM1_COLORS,getColorDescription(1),colors1_));
		
		addUIParameter(new StringUIParameter(this, PARAM2_NAMES,getNameDescription(2),names2_));
		addUIParameter(new StringUIParameter(this, PARAM2_COLORS,getColorDescription(2),colors2_));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(SLIDER1_POSITION.equals(name)){
			int pos;
			try {
				// retrieve current property value
				pos = ((MultiStateUIProperty) getUIProperty(SLIDER1_POSITION)).getStateIndex(newvalue);
				
				// select the corresponding button
				if(pos < toggleButtons1_.length){
					toggleButtons1_[pos].setSelected(true);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(SLIDER2_POSITION.equals(name)){
			int pos;
			try {
				// retrieve current property value
				pos = ((MultiStateUIProperty) getUIProperty(SLIDER2_POSITION)).getStateIndex(newvalue);	
				
				// select the corresponding button		
				if(pos< toggleButtons2_.length){
					toggleButtons2_[pos].setSelected(true);
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
		return "The "+getPanelLabel()+" is meant to control a dual filterwheel/slider"
				+ " with at most "+NUM_POS+" filter in each filter wheel. The filter "
						+ "colors and names can be customized from the configuration menu.";
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
