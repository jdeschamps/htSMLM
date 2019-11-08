package de.embl.rieslab.htsmlm;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.BoolUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.ColorRepository;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIPropertyTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.components.TogglePower;
import de.embl.rieslab.htsmlm.components.ToggleSlider;
import de.embl.rieslab.htsmlm.flags.FocusLockFlag;

public class IBeamSmartPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;

	//////// Components
	private JTextField textfieldUserPower_;
	private JSlider sliderPower_;
	private JSlider sliderFinea_;
	private JSlider sliderFineb_;
	private JToggleButton togglebuttonLaserOnOff_;
	private ToggleSlider togglebuttonExternalTrigger_;
	private ToggleSlider togglesliderenableFine_;
	private JLabel fineaperc_;
	private JLabel finebperc_;

	//////// Properties
	public final static String LASER_OPERATION = "operation";
	public final static String LASER_ENABLEFINE = "enable fine";	
	public final static String LASER_POWER = "laser power";	
	public final static String LASER_PERCFINEA = "fine a (%)";	
	public final static String LASER_PERCFINEB = "fine b (%)";	
	public final static String LASER_MAXPOWER = "max power";	
	public final static String LASER_EXTERNALTRIGGER = "ext trigger";	
	
	// parameters
	public final static String PARAM_ENABLE_FINE = "fine available";
	public final static String PARAM_ENABLE_EXT_TRIGGER = "external trigger available";
	
	/////// Convenience variables
	private int max_power;
	private JPanel cardTrigger, cardFine;
	private final static String ENABLED = "enabled";
	private final static String DISABLED = "disabled";
	
	public IBeamSmartPanel(String label) {
		super(label);
		
		setupPanel();
	}

	private void setupPanel() {

		///////////////////////////////////////////////////////////////////////////// set-up components
		// Power text field
		textfieldUserPower_ = new JTextField(String.valueOf(max_power));
		textfieldUserPower_.setPreferredSize(new Dimension(35,20));

		
		// slider channel 1
		sliderPower_ = new JSlider(JSlider.HORIZONTAL, 0, (int) max_power, 0);
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPanelLabel()+" "+LASER_POWER, sliderPower_, textfieldUserPower_);
		
		// slider fine a
		sliderFinea_ = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
	
		// Slider fine b
		sliderFineb_ = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);

		
		togglebuttonLaserOnOff_ = new TogglePower();
		
		
		// ext trigger
		togglebuttonExternalTrigger_ = new ToggleSlider();

		// Fine enable
		togglesliderenableFine_ = new ToggleSlider();	
		
		fineaperc_ = new JLabel("100 %");
		finebperc_ = new JLabel("100 %");

		// others
		JLabel fineAperc = new JLabel("a");
		JLabel finebperc = new JLabel("b");
		JLabel power = new JLabel("Power (mW):");
		
		///////////////////////////////////////////////////////////////////////////// Channel 1
		
		JPanel panelOperation = new JPanel();
		panelOperation.setLayout(new GridBagLayout());
		TitledBorder border2 = BorderFactory.createTitledBorder(null, "Power", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
				null, ColorRepository.getColor(ColorRepository.strblack));
		panelOperation.setBorder(border2);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		c2.ipadx = 5;
		c2.ipady = 5;
		c2.weightx = 0.2;
		c2.weighty = 0.3;
		c2.fill = GridBagConstraints.BOTH;
		panelOperation.add(power, c2);

		c2.gridx = 2;
		panelOperation.add(textfieldUserPower_, c2);
		

		c2.gridx = 3;
		panelOperation.add(togglebuttonLaserOnOff_, c2);

		c2.gridx = 0;
		c2.gridy = 4;
		c2.gridwidth = 4;
		c2.weightx = 0.9;
		c2.weighty = 0.5;
		panelOperation.add(sliderPower_, c2);
		
		///////////////////////////////////////////////////////////////////////////// trigger
		cardTrigger = new JPanel(new CardLayout());
		JPanel panelTrigger = new JPanel();
		panelTrigger.setLayout(new GridBagLayout());
		TitledBorder borderTrigger = BorderFactory.createTitledBorder(null, "External trigger", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
				null, ColorRepository.getColor(ColorRepository.strblack));
		panelTrigger.setBorder(borderTrigger);
	
		// gridbad layout
		GridBagConstraints cTrig = new GridBagConstraints();
		cTrig.fill = GridBagConstraints.HORIZONTAL;
		cTrig.gridx = 0;
		cTrig.gridy = 0;
		panelTrigger.add(togglebuttonExternalTrigger_, cTrig);
		cardTrigger.add(panelTrigger, ENABLED);
		cardTrigger.add(new JPanel(), DISABLED);

		///////////////////////////////////////////////////////////////////////////// fine
		cardFine = new JPanel(new CardLayout());
		JPanel panelFine = new JPanel();
		panelFine.setLayout(new GridBagLayout());
		TitledBorder borderfine = BorderFactory.createTitledBorder(null, "Fine", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
				null, ColorRepository.getColor(ColorRepository.strblack));
		panelFine.setBorder(borderfine);
		
		// gridbad layout
		GridBagConstraints cfine = new GridBagConstraints();
		cfine.fill = GridBagConstraints.HORIZONTAL;
		cfine.ipadx = 35;
		cfine.ipady = 2;
		cfine.gridx = 0;
		cfine.gridy = 0;
		panelFine.add(togglesliderenableFine_, cfine);

		cfine.gridx = 1;
		cfine.gridy = 1;
		cfine.ipadx = 5;
		panelFine.add(fineAperc, cfine);
		
		cfine.gridy = 2;
		panelFine.add(finebperc, cfine);
		
		cfine.gridx = 2;
		cfine.gridy = 1;
		cfine.ipadx = 4;
		cfine.gridwidth = 3;
		panelFine.add(sliderFinea_, cfine);
		
		cfine.gridy = 2;
		panelFine.add(sliderFineb_, cfine);
		
		cfine.gridx = 5;
		cfine.gridy = 1;
		cfine.ipadx = 5;
		cfine.gridwidth = 1;
		cfine.insets = new Insets(2,35,2,2);
		panelFine.add(fineaperc_, cfine);
		
		cfine.gridy = 2;
		cfine.insets = new Insets(2,35,2,2);
		panelFine.add(finebperc_, cfine);
		
		cardFine.add(panelFine, ENABLED);
		cardFine.add(new JPanel(), DISABLED);

		
		//////////////////////////////////////////////////////////////////////////// Main panel
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.4;
		c.weighty = 0.2;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		this.add(panelOperation,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(cardFine,c);

		c.gridy = 2;
		this.add(cardTrigger,c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.weighty = 0.8;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JPanel(),c);
	}
	
	@Override
	protected void initializeProperties() {
		max_power = 200;
		
		addUIProperty(new UIProperty(this, getPropertyName(LASER_POWER),"iBeamSmart Power (mW).", new FocusLockFlag()));
		addUIProperty(new UIProperty(this, getPropertyName(LASER_PERCFINEA),"iBeamSmart Power percentage of fine a.", new FocusLockFlag()));
		addUIProperty(new UIProperty(this, getPropertyName(LASER_PERCFINEB),"iBeamSmart Power percentage of fine b.", new FocusLockFlag()));
		addUIProperty(new UIProperty(this, getPropertyName(LASER_MAXPOWER),"iBeamSmart Maximum power (mW).", new FocusLockFlag()));

		addUIProperty(new TwoStateUIProperty(this,getPropertyName(LASER_OPERATION),"iBeamSmart On/Off operation property.", new FocusLockFlag()));
		addUIProperty(new TwoStateUIProperty(this,getPropertyName(LASER_ENABLEFINE),"iBeamSmart Enable property of fine.", new FocusLockFlag()));
		addUIProperty(new TwoStateUIProperty(this,getPropertyName(LASER_EXTERNALTRIGGER),"iBeamSmart digital trigger on/off.", new FocusLockFlag()));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(getPropertyName(LASER_POWER).equals(name)){
			if(EmuUtils.isNumeric(newvalue)){
				double val = Double.parseDouble(newvalue);
				if(val>=0 && val<=max_power){
					textfieldUserPower_.setText(String.valueOf(val));
					sliderPower_.setValue((int) val);	
				}
			}
		} else if(getPropertyName(LASER_PERCFINEA).equals(name)){
			if(EmuUtils.isNumeric(newvalue)){
				double val = Double.parseDouble(newvalue);
				if(val>=0 && val<=100){
					if(val < 100){
						fineaperc_.setText("  "+String.valueOf(val)+" %");				
					} else {
						fineaperc_.setText(String.valueOf(val)+" %");		
					}	
					sliderFinea_.setValue((int) val);	
				}
			}
		} else if(getPropertyName(LASER_PERCFINEB).equals(name)){
			if(EmuUtils.isNumeric(newvalue)){
				double val = Double.parseDouble(newvalue);
				if(val>=0 && val<=100){	
					if(val < 100){
						finebperc_.setText("  "+String.valueOf(val)+" %");				
					} else {
						finebperc_.setText(String.valueOf(val)+" %");		
					}	
					sliderFineb_.setValue((int) val);	
				}
			}
		} else if(getPropertyName(LASER_OPERATION).equals(name)){
			try {
				togglebuttonLaserOnOff_.setSelected(((TwoStateUIProperty) getUIProperty(getPropertyName(LASER_OPERATION))).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}

		} else if(getPropertyName(LASER_EXTERNALTRIGGER).equals(name)){
			try {
				togglebuttonExternalTrigger_.setSelected(((TwoStateUIProperty) getUIProperty(getPropertyName(LASER_EXTERNALTRIGGER))).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}

		} else if(getPropertyName(LASER_ENABLEFINE).equals(name)){
			try {
				togglesliderenableFine_.setSelected(((TwoStateUIProperty) getUIProperty(getPropertyName(LASER_ENABLEFINE))).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(getPropertyName(LASER_MAXPOWER).equals(name)){
			if(EmuUtils.isNumeric(newvalue)){
				double val = Double.parseDouble(newvalue);
				max_power = (int) val;
				if(sliderPower_ != null){
					sliderPower_.setMaximum(max_power);
				}
			}
		}
	}

	@Override
	public void shutDown() {
		// Do nothing
	}

	@Override
	public String getDescription() {
		return "This panel controls the focus-lock laser fron Toptica, iBeam-smart.";
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
	protected void initializeParameters() {		
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE_FINE,"Fine settings available in the iBeamSmart laser.", true));
		addUIParameter(new BoolUIParameter(this, PARAM_ENABLE_EXT_TRIGGER,"External trigger available in the iBeamSmart laser.", true));
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_ENABLE_FINE.equals(label)){
			try {
				if(getBoolUIParameterValue(PARAM_ENABLE_FINE)) {
					((CardLayout) cardFine.getLayout()).show(cardFine, ENABLED);
				} else {
					((CardLayout) cardFine.getLayout()).show(cardFine, DISABLED);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ENABLE_EXT_TRIGGER.equals(label)){
			try {
				if(getBoolUIParameterValue(PARAM_ENABLE_EXT_TRIGGER)) {
					((CardLayout) cardTrigger.getLayout()).show(cardTrigger, ENABLED);
				} else {
					((CardLayout) cardTrigger.getLayout()).show(cardTrigger, DISABLED);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} 	
	}

	private String getPropertyName(String propertyLabel) {
		return getPanelLabel()+" "+propertyLabel;
	}
	
	@Override
	protected void addComponentListeners() {
		// power textfield
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPropertyName(LASER_POWER), textfieldUserPower_,
				sliderPower_);

		// slider power
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPropertyName(LASER_POWER), sliderPower_,
				textfieldUserPower_);

		// slider fine a
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPropertyName(LASER_PERCFINEA), sliderFinea_,
				fineaperc_, "", " %");

		// slider fine b
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPropertyName(LASER_PERCFINEA), sliderFineb_,
				finebperc_, "", " %");
		
		try {
			SwingUIListeners.addActionListenerToTwoState(this, getPropertyName(LASER_OPERATION), togglebuttonLaserOnOff_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
		try {
			SwingUIListeners.addActionListenerToTwoState(this, getPropertyName(LASER_EXTERNALTRIGGER), togglebuttonExternalTrigger_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
		
		try {
			SwingUIListeners.addActionListenerToTwoState(this, getPropertyName(LASER_ENABLEFINE), togglesliderenableFine_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
	}
}
