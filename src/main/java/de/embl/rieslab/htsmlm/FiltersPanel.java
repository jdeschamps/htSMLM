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

/**
 * A ConfigurablePanel with a button group, suitable
 * for filter wheels or any device with discrete/discretized number
 * of positions/states.
 * 
 * @author Joran Deschamps
 *
 */
public class FiltersPanel extends AbstractFiltersPanel {


	private static final long serialVersionUID = 1L;

	//////// Components
	private JToggleButton[] togglebuttons_;
	private TitledBorder border_;
	
	//////// Properties
	private static final String FW_POSITION = "Filter wheel position";
	
	//////// Parameters
	private static final String PARAM_TITLE = "Panel title";
	private static final String PARAM_NAMES = "Filter names";
	private static final String PARAM_COLORS = "Filter colors";
	
	//////// Initial parameters
	private static final int NUM_POS = 6;
	private static final String TITLE = "Filters";
	private static final String NAME_EMPTY = "None";
	private static final String COLOR_EMPTY = ColorRepository.strgray;
	private String names_, colors_, title_; 


	public FiltersPanel(String label) {
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

		ButtonGroup group=new ButtonGroup();

		togglebuttons_ = new JToggleButton[NUM_POS];
		for(int i=0;i<togglebuttons_.length;i++){
			togglebuttons_[i] = new JToggleButton();
			togglebuttons_[i].setToolTipText("Set the filter property to the "+i+"th position (as defined in the wizard).");
			
			c.gridx = i;
			this.add(togglebuttons_[i], c);
			
			group.add(togglebuttons_[i]);
			
			togglebuttons_[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange()==ItemEvent.SELECTED){
						int pos = getSelectedButtonNumber();
						if(pos>=0 && pos<togglebuttons_.length){
							setUIPropertyValueByStateIndex(FW_POSITION,pos);
						}				
					} 
				}
	        });
		}  
		setNames();
		setColors();
	}

	protected int getSelectedButtonNumber() {
		int val=-1;
		
		for(int i=0;i<togglebuttons_.length;i++){
			if(togglebuttons_[i].isSelected()){
				return i;
			}
		}
		return val;
	}
	
	private void setNames(){
		String[] astr = names_.split(",");
		int maxind = togglebuttons_.length > astr.length ? astr.length : togglebuttons_.length; 
		for(int i=0;i<maxind;i++){
			togglebuttons_[i].setText(astr[i]);
		}
		try {
			((MultiStateUIProperty) getUIProperty(FW_POSITION)).setStateNames(astr);
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
	}
	
	private void setColors(){
		String[] astr = colors_.split(",");
		int maxind = togglebuttons_.length > astr.length ? astr.length : togglebuttons_.length;
		for(int i=0;i<maxind;i++){
			togglebuttons_[i].setForeground(ColorRepository.getColor(astr[i]));
		}
	}
	
	@Override
	protected void initializeProperties() {
		String desc = "Filter wheel position. Choose a device property that corresponds to an element with a "
				+ "finite number of states (e.g. a filter wheel). The filter wheel property has "+NUM_POS+" positions. "
				+ "For each position, indicate in the \"Filter wheel position state #\" the corresponding "
				+ "device property value. In order to determine the value, use the Micro-Manager device "
				+ "property browser. All states must be set, but multiple states can have the same value. "
				+ "Each state name and color can be configured in the Parameters tab.";
		
		addUIProperty(new MultiStateUIProperty(this, FW_POSITION, desc, new FilterWheelFlag(),NUM_POS));		
	}

	
	private String getNameDescription() {
		String names = "Name0";
		for(int j=1;j<NUM_POS;j++){
			names += ","+"Name"+j; 
		}
	
		return "Filter names displayed on the GUI. The entry should be written "
				+ "as \""+names+"\". The names should be separated by commas. The maximum "
				+ "number of filters name is "+NUM_POS+", beyond that the names will be ignored.";
	}
	
	private String getColorDescription() {
		String colors = "Color0";
		for(int j=1;j<NUM_POS;j++){
			colors += ","+"Color"+j; 
		}
		
		return "Filter name colors displayed on the GUI. The entry should be written "
				+ "as \""+colors+"\". The colors should be separated by commas. The maximum number "
						+ "of filters color is "+NUM_POS+", beyond that the colors will be ignored. The available "
								+ "colors are: "+ColorRepository.getCommaSeparatedColors()+".";
	}
	
	@Override
	protected void initializeParameters() {
		title_ = TITLE;
		names_ = NAME_EMPTY;
		colors_ = COLOR_EMPTY;
		for(int i=0;i<NUM_POS-1;i++){
			names_ += ","+NAME_EMPTY; 
			colors_ += ","+COLOR_EMPTY; 
		}
		
		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Title of the FW panel",title_));
		addUIParameter(new StringUIParameter(this, PARAM_NAMES,getNameDescription(),names_));
		addUIParameter(new StringUIParameter(this, PARAM_COLORS,getColorDescription(),colors_));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(FW_POSITION.equals(name)){
			try {
				int pos = ((MultiStateUIProperty) getUIProperty(FW_POSITION)).getStateIndex(newvalue);
				if(pos<togglebuttons_.length){
					togglebuttons_[pos].setSelected(true);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_NAMES.equals(label)){
			try {
				names_ = getStringUIParameterValue(PARAM_NAMES);
				setNames();
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_COLORS.equals(label)){
			try {
				colors_ = getStringUIParameterValue(PARAM_COLORS);
				setColors();
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
		return "The filters panel is meant to control a filterwheel with at "
				+ "most "+NUM_POS+" filters. The filter colors and names can "
						+ "be customized from the configuration menu.";
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
