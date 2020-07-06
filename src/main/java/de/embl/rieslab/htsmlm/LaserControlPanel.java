package de.embl.rieslab.htsmlm;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.BoolUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.ColorUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.RescaledUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIPropertyTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.components.TogglePower;
import de.embl.rieslab.htsmlm.uipropertyflags.LaserFlag;

public class LaserControlPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;
	
	//////// Components
	private JTextField textfieldUser_;
	private JToggleButton togglebutton100_;
	private JToggleButton togglebuttonUser_;
	private JToggleButton togglebutton20_;
	private JToggleButton togglebutton1_;
	private JToggleButton togglebuttonOnOff_;	
	private TitledBorder border_;
	private JSlider slider_;
	private JPanel cardpanel_;

	//////// Properties
	private static final String LASER_PERCENTAGE = "power percentage";
	private static final String LASER_OPERATION = "enable";	
	
	//////// Parameters
	private static final String PARAM_TITLE = "Name";
	private static final String PARAM_COLOR = "Color";
	private static final String PARAM_ONOFF = "Use on/off";	
	private static final String PARAM_SLIDER = "Use slider";	

	private static final String CARD_SLIDER = "slider";
	private static final String CARD_BUTTONS = "buttons";
	
	private String title_;	
	private Color color_;
	private boolean useOnOff_;
	private boolean useSlider_;
	
	/////// Convenience variables
	
	public LaserControlPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		cardpanel_ = new JPanel(new CardLayout());
		
		JPanel sliderPane = new JPanel(new BorderLayout());

		JPanel buttonPane = new JPanel(new GridBagLayout());
		
		this.setLayout(new GridBagLayout());
		
		border_ = BorderFactory.createTitledBorder(null, title_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, color_);
		this.setBorder(border_);
		border_.setTitleFont(border_.getTitleFont().deriveFont(Font.BOLD, 12));

		///////////////////////////////////////////////////////////////////////// slider
		slider_ = new JSlider();
		slider_.setMajorTickSpacing(20);
		slider_.setPaintTicks(true);
		slider_.setPaintLabels(true);
		slider_.setOrientation(SwingConstants.VERTICAL);
		slider_.setPreferredSize(new Dimension(20, 80));
		
		///////////////////////////////////////////////////////////////////////// User input text field
		textfieldUser_ = new JTextField("50");
		textfieldUser_.setToolTipText("Sets the power percentage and the value of the user-defined button.");

        
		///////////////////////////////////////////////////////////////////////// Percentage buttons
		togglebutton100_ = new JToggleButton("100%");
		togglebutton100_.setToolTipText("Sets the power percentage to 100%.");

		togglebuttonUser_ = new JToggleButton("50%");
		togglebuttonUser_.setToolTipText("Sets the power percentage to value defined in text field above.");
		
		togglebutton20_ = new JToggleButton("20%");
		togglebutton20_.setToolTipText("Sets the power percentage to 20%.");

		togglebutton1_ = new JToggleButton("1%");
		togglebutton1_.setToolTipText("Sets the power percentage to 1%.");

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
        togglebuttonOnOff_.setToolTipText("Turn on/off the laser.");
        try {
			SwingUIListeners.addActionListenerToTwoState(this, getPropertylabel(LASER_OPERATION), togglebuttonOnOff_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
        
		/*
		 * Buttons panel
		 */
		////// grid bag layout
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.weighty = 0.7;
		c.weightx = 0.7;
		c.insets = new Insets(2,15,2,15);
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		//buttonPane.add(textfieldUser_, c);
		c.ipady = 8;
		//c.gridy = 1;
		buttonPane.add(togglebuttonUser_, c);
		c.gridy = 1;
		buttonPane.add(togglebutton100_, c);
		c.gridy = 2;
		buttonPane.add(togglebutton20_, c);
		c.gridy = 3;
		buttonPane.add(togglebutton1_, c);
		//c.ipady = 0;
		//c.gridy = 5;
		//c.weighty = 1;
		//this.add(togglebuttonOnOff_, c);
		
		/*
		 * Slider panel
		 */
		sliderPane.add(slider_);
		
		/*
		 * assemble all
		 */
		cardpanel_.add(buttonPane, CARD_BUTTONS);
		cardpanel_.add(sliderPane, CARD_SLIDER);
		c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.3;
		c.insets = new Insets(2,15,2,15);
		c.fill = GridBagConstraints.BOTH;
		this.add(textfieldUser_,c);
		
		c.gridy = 1;
		c.weighty = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		this.add(cardpanel_,c);
		
		c.gridy = 2;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.BOTH;
		this.add(togglebuttonOnOff_,c);
	}
	
	private String getUserInput(){
		String s = textfieldUser_.getText();
		if(EmuUtils.isInteger(s)){
			return s;
		}
		return null;
	}

	@Override
	protected void initializeProperties() {
		String text = "Power percentage of the laser. If the laser only has a power set point (mW) property instead of a percentage property, "
				+ "then use a slope value equal to (maximum power / 100) to turn the property it into a power percentage.";
		
		addUIProperty(new RescaledUIProperty(this, getPropertylabel(LASER_PERCENTAGE),text, new LaserFlag()));
		addUIProperty(new TwoStateUIProperty(this,getPropertylabel(LASER_OPERATION),"Laser On/Off property (e.g. 1/0 or On/Off).", new LaserFlag()));
	}

	@Override
	protected void initializeParameters() {
		title_ = "Laser";	
		color_ = Color.black;
		useOnOff_ = true;
		useSlider_ = true;
		
		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Panel title.",title_));
		addUIParameter(new ColorUIParameter(this, PARAM_COLOR,"Panel title color.",color_));
		addUIParameter(new BoolUIParameter(this, PARAM_ONOFF,"Enable/disable the On/Off button.",useOnOff_));
		addUIParameter(new BoolUIParameter(this, PARAM_SLIDER,"Use a slider to control the laser power, if disabled"
				+ "the slider is replaced by a user-defined and three pre-defined buttons.",useSlider_));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(getPropertylabel(LASER_PERCENTAGE).equals(name)){
			if(EmuUtils.isNumeric(newvalue)){
				int val = (int) Double.parseDouble(newvalue);
				
				if(!useSlider_) {
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
				} else {
					slider_.setValue(val);
					textfieldUser_.setText(String.valueOf(val));
				}
			}
		} else if(getPropertylabel(LASER_OPERATION).equals(name)){
			try {
				togglebuttonOnOff_.setSelected(((TwoStateUIProperty) getUIProperty(getPropertylabel(LASER_OPERATION))).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_TITLE.equals(label)){
			try {
				title_ = getStringUIParameterValue(PARAM_TITLE);
				border_.setTitle(title_);
				this.repaint();
				getUIProperty(getPropertylabel(LASER_PERCENTAGE)).setFriendlyName(title_+" "+LASER_PERCENTAGE);
				getUIProperty(getPropertylabel(LASER_OPERATION)).setFriendlyName(title_+" "+LASER_OPERATION);
			} catch (UnknownUIParameterException | UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(PARAM_COLOR.equals(label)){
			try {
				color_ = getColorUIParameterValue(PARAM_COLOR);
				border_.setTitleColor(color_);
				this.repaint();
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ONOFF.equals(label)){
			try {
				useOnOff_ = getBoolUIParameterValue(PARAM_ONOFF);
				togglebuttonOnOff_.setEnabled(useOnOff_);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_SLIDER.equals(label)){
			try {
				useSlider_ = getBoolUIParameterValue(PARAM_SLIDER);
				if(useSlider_) {
				    CardLayout cl = (CardLayout)(cardpanel_.getLayout());
				    cl.show(cardpanel_, CARD_SLIDER);
				} else {
				    CardLayout cl = (CardLayout)(cardpanel_.getLayout());
				    cl.show(cardpanel_, CARD_BUTTONS);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getPropertylabel(String label) {
		return getPanelLabel()+" "+label;
	}

	@Override
	public void shutDown() {
		// nothing to do
	}

	@Override
	public String getDescription() {
		return "The " + getPanelLabel()
				+ " laser panel controls a single laser and allows for on/off and power percentage changes. Several parameters "
				+ "are available to customize the panel, such as title and title color. "
				+ "In addition, if the laser should not be turned on/off, then a parameter allows disabling the on/off button. "
				+ "In case the device has an absolute power device property instead of a laser "
				+ "power percentage, the slope of the corresponding UI property can be used to rescale linearly the value to a percentage."
				+ "Finally, the laser power can be controlled either by a slider or buttons (see parameter).";
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
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPropertylabel(LASER_PERCENTAGE), slider_, textfieldUser_);
		
		/////////////////////////////////////////////////////////////// textfield
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
						if(togglebuttonUser_.isSelected() && !useSlider_) {
							setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE), String.valueOf(val));
						}
						if(useSlider_) {
							slider_.setValue(val);
							setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE), String.valueOf(val));
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
						if (togglebuttonUser_.isSelected() && !useSlider_) {
							setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE), String.valueOf(val));
						}

						if(useSlider_) {
							slider_.setValue(val);
							setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE), String.valueOf(val));
						}
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
        	}
        });

		/////////////////////////////////////////////////////////////// buttons
		togglebutton100_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE),String.valueOf(100));
				}
			}
        });		
		togglebuttonUser_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String typed = getUserInput();
					if (typed == null) {
						return;
					}
					setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE), String.valueOf(typed));
				}
			}
        });
		togglebutton20_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE),String.valueOf(20));
				}
			}
        });
		togglebutton1_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					setUIPropertyValue(getPropertylabel(LASER_PERCENTAGE),String.valueOf(1));
				}
			}
        });
	}
}
