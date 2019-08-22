package de.embl.rieslab.htsmlm;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.exceptions.IncorrectUIPropertyTypeException;
import de.embl.rieslab.emu.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.emu.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.uiparameters.BoolUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.ColorUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.IntegerUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.utils;
import de.embl.rieslab.htsmlm.components.TogglePower;
import de.embl.rieslab.htsmlm.flags.LaserFlag;

public class LaserControlPanel extends ConfigurablePanel {

	private static final long serialVersionUID = -6553153910855055671L;
	
	//////// Components
	private JTextField textfieldUser_;
	private JToggleButton togglebutton100_;
	private JToggleButton togglebuttonUser_;
	private JToggleButton togglebutton20_;
	private JToggleButton togglebutton1_;
	private JToggleButton togglebuttonOnOff_;	
	private TitledBorder border_;

	//////// Properties
	public final static String LASER_PERCENTAGE = "power percentage";
	public final static String LASER_OPERATION = "enable";	
	
	//////// Parameters
	public final static String PARAM_TITLE = "Name";
	public final static String PARAM_COLOR = "Color";	
	public final static String PARAM_SCALING = "Scaling max";	
	public final static String PARAM_ONOFF = "Use on/off";	
	private String title_;	
	private Color color_;
	private int scaling_;
	private boolean useOnOff_;
	
	/////// Convenience variables
	
	public LaserControlPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		
		this.setLayout(new GridBagLayout());
		
		border_ = BorderFactory.createTitledBorder(null, title_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, color_);
		this.setBorder(border_);
		border_.setTitleFont(border_.getTitleFont().deriveFont(Font.BOLD, 12));
				
		///////////////////////////////////////////////////////////////////////// User input text field
		textfieldUser_ = new JTextField("50");

		textfieldUser_.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
				String typed = getUserInput();
				if (typed == null) {
					return;
				}
				try {
					int val = Integer.parseInt(typed);
					if (val <= 100 && val >= 0) {
						togglebuttonUser_.setText(typed + "%");
						if (togglebuttonUser_.isSelected()) {
							int value = (int) (val * scaling_ / 100);
							setUIPropertyValue(getPanelLabel() + " " + LASER_PERCENTAGE, String.valueOf(value));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
         });
		textfieldUser_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String typed = getUserInput();
				if (typed == null) {
					return;
				}
				try {
					int val = Integer.parseInt(typed);
					if (val <= 100 && val >= 0) {
						togglebuttonUser_.setText(typed + "%");
						if (togglebuttonUser_.isSelected()) {
							int value = (int) (val * scaling_ / 100);
							setUIPropertyValue(getPanelLabel() + " " + LASER_PERCENTAGE, String.valueOf(value));
						}
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
        	}
        });
        
		///////////////////////////////////////////////////////////////////////// Percentage buttons
		togglebutton100_ = new JToggleButton("100%");
		togglebutton100_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					setUIPropertyValue(getPanelLabel()+" "+LASER_PERCENTAGE,String.valueOf(scaling_));
				}
			}
        });		

		togglebuttonUser_ = new JToggleButton("50%");
		togglebuttonUser_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String typed = getUserInput();
					if (typed == null) {
						return;
					}
					int val = (int) (Double.valueOf(typed) * scaling_ / 100);
					setUIPropertyValue(getPanelLabel() + " " + LASER_PERCENTAGE, String.valueOf(val));
				}
			}
        });
		
		togglebutton20_ = new JToggleButton("20%");
		togglebutton20_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					int val = (int) (scaling_*0.2);
					setUIPropertyValue(getPanelLabel()+" "+LASER_PERCENTAGE,String.valueOf(val));
				}
			}
        });

		togglebutton1_ = new JToggleButton("1%");
		togglebutton1_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					int val = (int) (scaling_*0.01);
					setUIPropertyValue(getPanelLabel()+" "+LASER_PERCENTAGE,String.valueOf(val));
				}
			}
        });

        ButtonGroup group=new ButtonGroup();
        group.add(togglebutton100_);
        group.add(togglebuttonUser_);
        group.add(togglebutton20_);
        group.add(togglebutton1_);
        
        /*Font buttonfont = togglebutton100_.getFont();
        Font newfont = buttonfont.deriveFont((float) 10);
        togglebutton100_.setFont(newfont);
        togglebuttonUser_.setFont(newfont);
        togglebutton20_.setFont(newfont);
        togglebutton1_.setFont(newfont);*/

        togglebutton100_.setMargin(new Insets(2,8,2,8));
        togglebuttonUser_.setMargin(new Insets(2,8,2,8));
        togglebutton20_.setMargin(new Insets(2,8,2,8));
        togglebutton1_.setMargin(new Insets(2,8,2,8));
        
		///////////////////////////////////////////////////////////////////////// On/Off button
       
        togglebuttonOnOff_ = new TogglePower();
        try {
			SwingUIListeners.addActionListenerToTwoState(this, getPanelLabel()+" "+LASER_OPERATION, togglebuttonOnOff_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
        
		////// grid bag layout
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weighty = 0.7;
		c.weightx = 0.7;
		c.insets = new Insets(2,15,2,15);
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		this.add(textfieldUser_, c);
		c.ipady = 8;
		c.gridy = 1;
		this.add(togglebuttonUser_, c);
		c.gridy = 2;
		this.add(togglebutton100_, c);
		c.gridy = 3;
		this.add(togglebutton20_, c);
		c.gridy = 4;
		this.add(togglebutton1_, c);
		c.ipady = 0;
		c.gridy = 5;
		c.weighty = 1;
		this.add(togglebuttonOnOff_, c);
		
	}
	
	private String getUserInput(){
		String s = textfieldUser_.getText();
		if(utils.isInteger(s)){
			return s;
		}
		return null;
	}

	@Override
	protected void initializeProperties() {
		String text = "Power percentage of the laser. If the laser only has a power set point (mW) property, select this property and use the scaling parameter in the parameters tab.";
		
		addUIProperty(new UIProperty(this, getPanelLabel()+" "+LASER_PERCENTAGE,text, new LaserFlag()));
		addUIProperty(new TwoStateUIProperty(this,getPanelLabel()+" "+LASER_OPERATION,"Laser On/Off property. Enter the values for the on and off states (e.g. 1/0 or On/Off).", new LaserFlag()));
	}

	@Override
	protected void initializeParameters() {
		title_ = "Laser";	
		color_ = Color.black;
		scaling_ = 100;
		useOnOff_ = true;
		
		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Panel title.",title_));
		addUIParameter(new ColorUIParameter(this, PARAM_COLOR,"Laser color.",color_));
		addUIParameter(new IntegerUIParameter(this, PARAM_SCALING,"Maximum value of the laser percentage after scaling.",scaling_));
		addUIParameter(new BoolUIParameter(this, PARAM_ONOFF,"Use On/Off button.",useOnOff_));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(name.equals(getPanelLabel()+" "+LASER_PERCENTAGE)){
			if(utils.isNumeric(newvalue)){
				int val = (int) Double.parseDouble(newvalue);
				
				// scale if necessary
				if(scaling_ != 100){
					val = (int) (val*scaling_/100);
				}
				
				if(val == 100){
					togglebutton100_.setSelected(true);
				} else if(val == 20){
					togglebutton20_.setSelected(true);
				} else if(val == 1){
					togglebutton1_.setSelected(true);
				} else {
					if(val>=0 && val<100){
						togglebuttonUser_.setSelected(true);
						togglebuttonUser_.setText(String.valueOf(val)+"%");
						textfieldUser_.setText(String.valueOf(val));
					} 
				}
			}
		} else if(name.equals(getPanelLabel()+" "+LASER_OPERATION)){
			try {
				if(newvalue.equals(((TwoStateUIProperty) getUIProperty(getPanelLabel()+" "+LASER_OPERATION)).getOnStateValue())){
					togglebuttonOnOff_.setSelected(true);
				} else {  
					togglebuttonOnOff_.setSelected(false);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void parameterhasChanged(String label) {
		if(label.equals(PARAM_TITLE)){
			try {
				title_ = getStringUIParameterValue(PARAM_TITLE);
				border_.setTitle(title_);
				this.repaint();
				getUIProperty(getPanelLabel()+" "+LASER_PERCENTAGE).setFriendlyName(title_+" "+LASER_PERCENTAGE);
				getUIProperty(getPanelLabel()+" "+LASER_OPERATION).setFriendlyName(title_+" "+LASER_OPERATION);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(label.equals(PARAM_COLOR)){
			try {
				color_ = getColorUIParameterValue(PARAM_COLOR);
				border_.setTitleColor(color_);
				this.repaint();
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(label.equals(PARAM_SCALING)){
			try {
				scaling_ = getIntegerUIParameterValue(PARAM_SCALING);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(label.equals(PARAM_ONOFF)){
			try {
				useOnOff_ = getBoolUIParameterValue(PARAM_ONOFF);
				togglebuttonOnOff_.setEnabled(useOnOff_);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutDown() {
		// nothing to do
	}

	@Override
	public String getDescription() {
		return "The "+getPanelLabel()+" panel controls a single laser and allows for rapid on/off and power percentage changes.";
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
