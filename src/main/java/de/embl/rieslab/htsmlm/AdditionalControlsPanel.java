package de.embl.rieslab.htsmlm;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.BoolUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIPropertyTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.uipropertyflags.TwoStateFlag;

public class AdditionalControlsPanel extends ConfigurablePanel{
	

	private static final long serialVersionUID = 1L;
	//////// Components
	private JToggleButton[] togglebuttons_;
	private TitledBorder border_;

	//////// Properties
	private static final String DEVICE_1 = "Two-state device 1";
	private static final String DEVICE_2 = "Two-state device 2";
	private static final String DEVICE_3 = "Two-state device 3";
	private static final String DEVICE_4 = "Two-state device 4";
	private static final String DEVICE_5 = "Two-state device 5";
	private static final String DEVICE_6 = "Two-state device 6";
	
	//////// Parameters
	private static final String PARAM_TITLE = "Title";
	private static final String PARAM_NAME1 = "Two-state device 1 name";
	private static final String PARAM_NAME2 = "Two-state device 2 name";
	private static final String PARAM_NAME3 = "Two-state device 3 name";
	private static final String PARAM_NAME4 = "Two-state device 4 name";
	private static final String PARAM_NAME5 = "Two-state device 5 name";
	private static final String PARAM_NAME6 = "Two-state device 6 name";
	private static final String PARAM_ENABLE1 = "Enable two-state device 1";
	private static final String PARAM_ENABLE2 = "Enable two-state device 2";
	private static final String PARAM_ENABLE3 = "Enable two-state device 3";
	private static final String PARAM_ENABLE4 = "Enable two-state device 4";
	private static final String PARAM_ENABLE5 = "Enable two-state device 5";
	private static final String PARAM_ENABLE6 = "Enable two-state device 6";
	private static final int PARAM_NPOS = 6;
	
	public AdditionalControlsPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		this.setLayout(new GridBagLayout());
		border_ = BorderFactory.createTitledBorder(null, getPanelLabel(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0,0,0));
		this.setBorder(border_);
		border_.setTitleFont(((TitledBorder) this.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
	
		togglebuttons_ = new JToggleButton[PARAM_NPOS];
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.insets = new Insets(2,2,2,2);
		c.ipady = 10;
		c.weightx = 0.2;
		
		String[] devices = {DEVICE_1, DEVICE_2, DEVICE_3, DEVICE_4, DEVICE_5, DEVICE_6};
		for(int i=0;i<togglebuttons_.length;i++){
			togglebuttons_[i] = new JToggleButton();
			togglebuttons_[i].setToolTipText("Turn the device on/off.");
			
			if(i == togglebuttons_.length-1){
				c.insets = new Insets(2,2,30,2);	
			}
			
			c.gridy = i;
			this.add(togglebuttons_[i], c);
			
			try {
				SwingUIListeners.addActionListenerToTwoState(this, devices[i], togglebuttons_[i]);
			} catch (IncorrectUIPropertyTypeException e) {
				e.printStackTrace();
			}
		}  
	}
		
	private String getPropertyDescription(int i) {
		return "Map to this GUI property (button No"+i+" in controls panel) a device property with two positions (e.g. On/Off or In/Out). "
				+ "Consult the Micro-Manager device property browser to determine them  (e.g. \"1\" and "
				+ "\"0\" or \"On\" and Off\"). The two-state device appears in the interface as a single "
				+ "toggle button. The name of the button can be set in the Parameters tab.";
	}
	
	@Override
	protected void initializeProperties() {
		addUIProperty(new TwoStateUIProperty(this, DEVICE_1,getPropertyDescription(1), new TwoStateFlag()));
		addUIProperty(new TwoStateUIProperty(this, DEVICE_2,getPropertyDescription(2), new TwoStateFlag()));
		addUIProperty(new TwoStateUIProperty(this, DEVICE_3,getPropertyDescription(3), new TwoStateFlag()));
		addUIProperty(new TwoStateUIProperty(this, DEVICE_4,getPropertyDescription(4), new TwoStateFlag()));
		addUIProperty(new TwoStateUIProperty(this, DEVICE_5,getPropertyDescription(5), new TwoStateFlag()));
		addUIProperty(new TwoStateUIProperty(this, DEVICE_6,getPropertyDescription(6), new TwoStateFlag()));
	}

	private String getEnableDescription(int i) {
		return "Select to enable the toggle button No"+i+" in the controls panel of the GUI. When unselected, the button is greyed out.";
	}	
	
	private String getNameDescription(int i) {
		return "Text appearing on the button No"+i+" of the controls panel.";
	}
	
	@Override
	protected void initializeParameters() {
		String descTitle = "Title appearing at the top of the controls panel.";
		addUIParameter(new StringUIParameter(this, PARAM_TITLE, descTitle,"Controls"));
		
		addUIParameter(new StringUIParameter(this, PARAM_NAME1,getNameDescription(1),"BFP"));
		addUIParameter(new StringUIParameter(this, PARAM_NAME2,getNameDescription(2),"3DA"));
		addUIParameter(new StringUIParameter(this, PARAM_NAME3,getNameDescription(3),"None"));
		addUIParameter(new StringUIParameter(this, PARAM_NAME4,getNameDescription(4),"None"));
		addUIParameter(new StringUIParameter(this, PARAM_NAME5,getNameDescription(5),"None"));
		addUIParameter(new StringUIParameter(this, PARAM_NAME6,getNameDescription(6),"None"));
		
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE1,getEnableDescription(1),true));
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE2,getEnableDescription(2),true));
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE3,getEnableDescription(3),false));
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE4,getEnableDescription(4),false));
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE5,getEnableDescription(5),false));
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE6,getEnableDescription(6),false));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(DEVICE_1.equals(name)){
			try {
				togglebuttons_[0].setSelected(((TwoStateUIProperty) getUIProperty(DEVICE_1)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(DEVICE_2.equals(name)){
			try {
				togglebuttons_[1].setSelected(((TwoStateUIProperty) getUIProperty(DEVICE_2)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(DEVICE_3.equals(name)){
			try {
				togglebuttons_[2].setSelected(((TwoStateUIProperty) getUIProperty(DEVICE_3)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(DEVICE_4.equals(name)){
			try {
				togglebuttons_[3].setSelected(((TwoStateUIProperty) getUIProperty(DEVICE_4)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(DEVICE_5.equals(name)){
			try {
				togglebuttons_[4].setSelected(((TwoStateUIProperty) getUIProperty(DEVICE_5)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(DEVICE_6.equals(name)){
			try {
				togglebuttons_[5].setSelected(((TwoStateUIProperty) getUIProperty(DEVICE_6)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_NAME1.equals(label)){
			try {
				String s = getStringUIParameterValue(PARAM_NAME1);
				togglebuttons_[0].setText(s);
				getUIProperty(DEVICE_1).setFriendlyName(s);
			} catch (UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_NAME2.equals(label)){
			try {
				String s = getStringUIParameterValue(PARAM_NAME2);
				togglebuttons_[1].setText(s);
				getUIProperty(DEVICE_2).setFriendlyName(s);
			} catch (UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_NAME3.equals(label)){
			try {
				String s = getStringUIParameterValue(PARAM_NAME3);
				togglebuttons_[2].setText(s);
				getUIProperty(DEVICE_3).setFriendlyName(s);
			} catch ( UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_NAME4.equals(label)){
			try {
				String s = getStringUIParameterValue(PARAM_NAME4);
				togglebuttons_[3].setText(s);
				getUIProperty(DEVICE_4).setFriendlyName(s);
			} catch (UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_NAME5.equals(label)){
			try {
				String s = getStringUIParameterValue(PARAM_NAME5);
				togglebuttons_[4].setText(s);
				getUIProperty(DEVICE_5).setFriendlyName(s);
			} catch (UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_NAME6.equals(label)){
			try {
				String s = getStringUIParameterValue(PARAM_NAME6);
				togglebuttons_[5].setText(s);
				getUIProperty(DEVICE_6).setFriendlyName(s);
			} catch (UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE1.equals(label)){
			try {
				boolean b = getBoolUIParameterValue(PARAM_ENABLE1);
				togglebuttons_[0].setEnabled(b);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE2.equals(label)){
			try {
				boolean b = getBoolUIParameterValue(PARAM_ENABLE2);
				togglebuttons_[1].setEnabled(b);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE3.equals(label)){
			try {
				boolean b = getBoolUIParameterValue(PARAM_ENABLE3);
				togglebuttons_[2].setEnabled(b);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE4.equals(label)){
			try {
				boolean b = getBoolUIParameterValue(PARAM_ENABLE4);
				togglebuttons_[3].setEnabled(b);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE5.equals(label)){
			try {
				boolean b = getBoolUIParameterValue(PARAM_ENABLE5);
				togglebuttons_[4].setEnabled(b);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE6.equals(label)){
			try {
				boolean b = getBoolUIParameterValue(PARAM_ENABLE6);
				togglebuttons_[5].setEnabled(b);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_TITLE.equals(label)){
			try {
				border_.setTitle(getStringUIParameterValue(PARAM_TITLE));
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
		return "The "+getPanelLabel()+" panel makes use of toggle buttons to control devices with only two states (e.g.: flip mirrors, servos with in/out positions..etc..).";
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