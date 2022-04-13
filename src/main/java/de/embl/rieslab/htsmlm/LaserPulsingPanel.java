package de.embl.rieslab.htsmlm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.micromanager.mmproperties.MMProperty;
import de.embl.rieslab.emu.micromanager.mmproperties.MMProperty.MMPropertyType;
import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.internalproperties.IntegerInternalProperty;
import de.embl.rieslab.emu.ui.uiparameters.ColorUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.IntegerUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.emu.utils.exceptions.IncorrectInternalPropertyTypeException;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownInternalPropertyException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.components.LogarithmicJSlider;
import de.embl.rieslab.htsmlm.uipropertyflags.CameraExpFlag;

public class LaserPulsingPanel extends ConfigurablePanel {
	
	private static final long serialVersionUID = 1L;

	//////// Components
	private JTextField textFieldMax1_;
	private JTextField textFieldValue1_;
	private LogarithmicJSlider logSlider1_;
	private JTextField textFieldMax2_;
	private JTextField textFieldValue2_;
	private LogarithmicJSlider logSlider2_;

	private TitledBorder border_;

	private JComboBox<String> pulseCombo_;
	private JPanel card_;

	//////// Properties
	private static final String CAMERA_EXPOSURE = "Camera exposure";
	private static final String LASER_PULSE1 = "Pulse duration 1 (main frame)";
	private static final String LASER_PULSE2 = "Pulse duration 2 (main frame)";

	//////// Parameters
	private static final String PARAM_TITLE = "Name";
	private static final String PARAM_COLOR = "Color";
	private static final String PARAM_DEFAULT_MAX = "Default max pulse";
	private static final String PARAM_ACTIVATION_NAME1 = "Activation 1 name";
	private static final String PARAM_ACTIVATION_NAME2 = "Activation 2 name";
	private String title_;	
	private Color color_;
	
	//////// Internal property
	public final static String INTERNAL_MAXPULSE1 = "Maximum pulse 1";
	public final static String INTERNAL_MAXPULSE2 = "Maximum pulse 2";
	
	//////// Convenience variables
	private int maxpulse1_, maxpulse2_;
	private boolean showPulseDuration1_;

	private static final String[] CARDS_NAME = {"0", "1"};

	public LaserPulsingPanel(String label) {
		super(label);
		
		setupPanel();
	}

	private void setupPanel() {

		this.setLayout(new GridBagLayout());

		border_ = BorderFactory.createTitledBorder(null, title_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, color_);
		this.setBorder(border_);
		border_.setTitleFont(border_.getTitleFont().deriveFont(Font.BOLD, 12));

		String[] properties;
		properties = getPropertiesName();
		pulseCombo_ = new JComboBox(properties);
		pulseCombo_.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				showPulseDuration1_ = pulseCombo_.getSelectedIndex() == 0;

				CardLayout cl = (CardLayout)(card_.getLayout());
				if(showPulseDuration1_){
					cl.show(card_, CARDS_NAME[0]);
				} else {
					cl.show(card_, CARDS_NAME[1]);
				}
			}
		});
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 0;
		c1.gridheight = 1;
		c1.gridwidth = 1;
		c1.insets = new Insets(1, 15, 1, 15);
		this.add(pulseCombo_, c1);


		card_ = new JPanel(new CardLayout());
		JPanel[] cardPanels = {new JPanel(), new JPanel()};

		textFieldMax1_ = new JTextField("10000");
		textFieldValue1_ = new JTextField();
		logSlider1_ = new LogarithmicJSlider(JSlider.VERTICAL, 1, 10000, 10);
		textFieldMax2_ = new JTextField("10000");
		textFieldValue2_ = new JTextField();
		logSlider2_ = new LogarithmicJSlider(JSlider.VERTICAL, 1, 10000, 10);

		int counter = 0;
		for (JPanel pane :cardPanels){
			pane.setLayout(new GridBagLayout());

			///////////////////////////////////////////////////////////////////////// User max text field
			JTextField textFieldMax = getTextFieldMax(counter);
			textFieldMax.setToolTipText("Maximum value allowed for the activation pulse/power.");
			textFieldMax.setPreferredSize(new Dimension(30, 15));
			textFieldMax.setBackground(new Color(220, 220, 220));


			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.insets = new Insets(1, 15, 1, 15);


			c.fill = GridBagConstraints.BOTH;
			c.ipady = 7;
			c.gridy = 0;
			pane.add(textFieldMax, c);


			///////////////////////////////////////////////////////////////////////// User value text field
			JTextField textFieldValue = getTextFieldValue(counter);
			textFieldValue.setToolTipText("Current value of the activation pulse/power.");
			c.fill = GridBagConstraints.BOTH;
			c.gridy = 1;
			pane.add(textFieldValue, c);


			///////////////////////////////////////////////////////////////////////// Log JSlider
			LogarithmicJSlider logSlider = getLogSlider(counter);
			logSlider.setToolTipText("Current value of the activation pulse/power.");

			logSlider.setPaintTicks(true);
			logSlider.setPaintTrack(true);
			logSlider.setPaintLabels(true);
			logSlider.setMajorTickSpacing(10);
			logSlider.setMinorTickSpacing(10);


			c.gridy = 2;
			c.ipady = 0;
			pane.add(logSlider, c);

			card_.add(pane, CARDS_NAME[counter++]);
		}
		/////////////////////////////////////////////////////////////////////////

		c1.insets = new Insets(0, 0, 0, 0);
		c1.fill = GridBagConstraints.BOTH;
		this.add(card_, c1);
	}

	private LogarithmicJSlider getLogSlider(int index){
		if(index == 0){
			return logSlider1_;
		} else {
			return logSlider2_;
		}
	}

	private JTextField getTextFieldMax(int index){
		if(index == 0){
			return textFieldMax1_;
		} else {
			return textFieldMax2_;
		}
	}

	private JTextField getTextFieldValue(int index){
		if(index == 0){
			return textFieldValue1_;
		} else {
			return textFieldValue2_;
		}
	}

	private String getProperty(int i){
		if(i==0){
			return LASER_PULSE1;
		} else {
			return LASER_PULSE2;
		}
	}


	@Override
	protected void initializeProperties() {
		String descPulse = "Pulse length, power or power percentage property of the activation laser. This "
				+ "property is required for the Activation script. Note that it should be mapped to the same "
				+ "device property as \"Pulse duration X (activation)\" for consistence.";
		
		addUIProperty(new UIProperty(this, CAMERA_EXPOSURE,"Camera exposure in ms.", new CameraExpFlag()));
		addUIProperty(new UIProperty(this, LASER_PULSE1,descPulse));
		addUIProperty(new UIProperty(this, LASER_PULSE2,descPulse));
	}

	@Override
	protected void initializeParameters() {
		title_ = "UV";	
		color_ = Color.black;
		maxpulse1_  = 10000;
		maxpulse2_  = 10000;

		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Laser name displayed on top of the laser "
				+ "control panel in the GUI.",title_));
		addUIParameter(new ColorUIParameter(this, PARAM_COLOR,"Color of the laser name as shown in the GUI.",color_));
				
		String desc = "Default maximum value for the activation laser pulse length (or power). This default "
				+ "value appears in the grey box at the top-left corner of the GUI. The value must be an integer.";
		addUIParameter(new IntegerUIParameter(this, PARAM_DEFAULT_MAX, desc, maxpulse1_));

		String descActivation = "Name of the activation property (pulse duration) appearing in the drop-down menu of " +
				"the activation panel.";
		addUIParameter(new StringUIParameter(this, PARAM_ACTIVATION_NAME1, descActivation,"Activation 1"));
		addUIParameter(new StringUIParameter(this, PARAM_ACTIVATION_NAME2, descActivation,"Activation 2"));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(LASER_PULSE1.equals(name)){
			if(EmuUtils.isInteger(newvalue)){
				int val = Integer.parseInt(newvalue);

				if(val > logSlider1_.getMaxWithin()){
					logSlider1_.setValueWithin(logSlider1_.getMaxWithin());
					textFieldValue1_.setText(String.valueOf(logSlider1_.getMaxWithin()));
					setUIPropertyValue(LASER_PULSE1,String.valueOf(logSlider1_.getMaxWithin()));
				} else {
					logSlider1_.setValueWithin(val);
					textFieldValue1_.setText(newvalue);
				}
			} else if(EmuUtils.isFloat(newvalue)){
				int val = Math.round(Float.parseFloat(newvalue));
				double dval = Math.round(100.*Float.parseFloat(newvalue))/100;

				if(val > logSlider1_.getMaxWithin()){
					logSlider1_.setValueWithin(logSlider1_.getMaxWithin());
					textFieldValue1_.setText(String.valueOf(logSlider1_.getMaxWithin()));
					setUIPropertyValue(LASER_PULSE1,String.valueOf(logSlider1_.getMaxWithin()));
				} else {
					logSlider1_.setValueWithin(val);
					textFieldValue1_.setText(String.valueOf(dval));
				}
			}
		} else if(LASER_PULSE2.equals(name)){
			if(EmuUtils.isInteger(newvalue)){
				int val = Integer.parseInt(newvalue);

				if(val > logSlider2_.getMaxWithin()){
					logSlider2_.setValueWithin(logSlider2_.getMaxWithin());
					textFieldValue2_.setText(String.valueOf(logSlider2_.getMaxWithin()));
					setUIPropertyValue(LASER_PULSE2,String.valueOf(logSlider2_.getMaxWithin()));
				} else {
					logSlider2_.setValueWithin(val);
					textFieldValue2_.setText(newvalue);
				}
			} else if(EmuUtils.isFloat(newvalue)){
				int val = Math.round(Float.parseFloat(newvalue));
				double dval = Math.round(100.*Float.parseFloat(newvalue))/100;

				if(val > logSlider2_.getMaxWithin()){
					logSlider2_.setValueWithin(logSlider2_.getMaxWithin());
					textFieldValue2_.setText(String.valueOf(logSlider2_.getMaxWithin()));
					setUIPropertyValue(LASER_PULSE2,String.valueOf(logSlider2_.getMaxWithin()));
				} else {
					logSlider2_.setValueWithin(val);
					textFieldValue2_.setText(String.valueOf(dval));
				}
			}
		}
	}

	private String getCorrespondingName(String property){
		if(LASER_PULSE1.equals(property)){
			return PARAM_ACTIVATION_NAME1;
		} else{
			return PARAM_ACTIVATION_NAME2;
		}
	}

	public String[] getAllocatedProperties(){
		ArrayList<String> str = new ArrayList();

		String[] props = {LASER_PULSE1, LASER_PULSE2};
		for(String prop: props) {
			try {
				String prop1 = this.getUIProperty(prop).isAssigned() ? prop : "";
				if (prop1.length() > 0) {
					str.add(prop);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}

		return str.toArray(new String[0]);
	}

	public String[] getPropertiesName() {
		ArrayList<String> str = new ArrayList();
		String[] props = getAllocatedProperties();

		for(String prop: props) {
			try {
				String propName = this.getStringUIParameterValue(getCorrespondingName(prop));
				str.add(propName);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}

		return str.toArray(new String[0]);
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_TITLE.equals(label)){
			try { 
				title_ = getStringUIParameterValue(PARAM_TITLE);
				border_.setTitle(title_);
				this.repaint();
			} catch (UnknownUIParameterException e) {
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
		} else if(PARAM_DEFAULT_MAX.equals(label)){
			try {
				maxpulse1_ = getIntegerUIParameterValue(PARAM_DEFAULT_MAX);
				logSlider1_.setMaxWithin(maxpulse1_);
				if (logSlider1_.getValue() > logSlider1_.getMaxWithin()) {
					logSlider1_.setValueWithin(logSlider1_.getMaxWithin());
					textFieldValue1_.setText(String.valueOf(logSlider1_.getValue()));
					setUIPropertyValue(LASER_PULSE1,String.valueOf(logSlider1_.getValue()));
				}
				textFieldMax1_.setText(String.valueOf(maxpulse1_));
				changeMaxPulseProperty(maxpulse1_, 0);

				logSlider2_.setMaxWithin(maxpulse2_);
				if (logSlider2_.getValue() > logSlider2_.getMaxWithin()) {
					logSlider2_.setValueWithin(logSlider2_.getMaxWithin());
					textFieldValue2_.setText(String.valueOf(logSlider2_.getValue()));
					setUIPropertyValue(LASER_PULSE2,String.valueOf(logSlider2_.getValue()));
				}
				textFieldMax2_.setText(String.valueOf(maxpulse2_));
				changeMaxPulseProperty(maxpulse2_, 1);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}  else if(PARAM_ACTIVATION_NAME1.equals(label) || PARAM_ACTIVATION_NAME2.equals(label)) {
			// check if the two properties have been allocated
			String[] props = getPropertiesName();

			// set choices
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel( props );
			pulseCombo_.setModel( model );

			// disable if not 2 properties
			if(props.length != 2){
				pulseCombo_.setEnabled(false);
			}

			if(props.length != 0) pulseCombo_.setSelectedIndex(0);
		}
	}

	@Override
	public void shutDown() {
		// do nothing
	}

	@Override
	public String getDescription() {
		return "The pulsing panel is meant to control the pulse length of the activation laser (localization microscopy). "
				+ "The user can set a maximum to the slider by entering a value in the gray text area. The pulse length can "
				+ "be set by entering a value in the white text area or by moving the slider.";
	}

	@Override
	protected void initializeInternalProperties() {
		addInternalProperty(new IntegerInternalProperty(this, INTERNAL_MAXPULSE1, maxpulse1_));
		addInternalProperty(new IntegerInternalProperty(this, INTERNAL_MAXPULSE2, maxpulse2_));
	}

	@Override
	public void internalpropertyhasChanged(String label) {
		if(INTERNAL_MAXPULSE1.equals(label)){
			try {
				maxpulse1_ = getIntegerInternalPropertyValue(INTERNAL_MAXPULSE1);
				logSlider1_.setMaxWithin(maxpulse1_);
				if (logSlider1_.getValue() > logSlider1_.getMaxWithin()) {
					logSlider1_.setValueWithin(logSlider1_.getMaxWithin());
					textFieldValue1_.setText(String.valueOf(logSlider1_.getValue()));
					setUIPropertyValue(LASER_PULSE1,String.valueOf(logSlider1_.getValue()));
				}
				textFieldMax1_.setText(String.valueOf(maxpulse1_));
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		} else if(INTERNAL_MAXPULSE2.equals(label)){
			try {
				maxpulse2_ = getIntegerInternalPropertyValue(INTERNAL_MAXPULSE2);
				logSlider2_.setMaxWithin(maxpulse2_);
				if (logSlider2_.getValue() > logSlider2_.getMaxWithin()) {
					logSlider2_.setValueWithin(logSlider2_.getMaxWithin());
					textFieldValue2_.setText(String.valueOf(logSlider2_.getValue()));
					setUIPropertyValue(LASER_PULSE2,String.valueOf(logSlider2_.getValue()));
				}
				textFieldMax2_.setText(String.valueOf(maxpulse2_));
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	private void changeMaxPulseProperty(int val, int channel){
		if(channel == 0){
			try {
				setInternalPropertyValue(INTERNAL_MAXPULSE1,val);
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		} else {
			try {
				setInternalPropertyValue(INTERNAL_MAXPULSE2,val);
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void addComponentListeners() {

		for(int i=0; i<2; i++){
			final int index = i;
			JTextField textFieldValue = getTextFieldValue(index);
			JTextField textFieldMax = getTextFieldMax(index);
			LogarithmicJSlider slider = getLogSlider(index);

			try {
				final UIProperty prop = this.getUIProperty(getProperty(index));

				textFieldValue.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(FocusEvent arg0) {
					}

					@Override
					public void focusLost(FocusEvent arg0) {
						String typed = textFieldValue.getText();
						if (!EmuUtils.isNumeric(typed)) {
							return;
						}

						if(prop.getMMPropertyType().equals(MMPropertyType.FLOAT)) {
							int val = Math.round(Float.parseFloat(typed));
							double dval = Math.round(100.*Float.parseFloat(typed))/100;
							if (val <= slider.getMaxWithin()) {
								slider.setValueWithin(val);
								setUIPropertyValue(getProperty(index), String.valueOf(dval));
							} else {
								slider.setValueWithin(slider.getMaxWithin());
								setUIPropertyValue(getProperty(index), String.valueOf(slider.getMaxWithin()));
							}
						} else {
							int val = Math.round(Float.parseFloat(typed));
							if (val <= slider.getMaxWithin()) {
								slider.setValueWithin(val);
								setUIPropertyValue(getProperty(index), String.valueOf(val));
							} else {
								slider.setValueWithin(slider.getMaxWithin());
								setUIPropertyValue(getProperty(index), String.valueOf(slider.getMaxWithin()));
							}
						}
					}
				});
				textFieldValue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String typed = textFieldValue.getText();
						if (!EmuUtils.isNumeric(typed)) {
							return;
						}
						if(prop.getMMPropertyType().equals(MMPropertyType.FLOAT)) {
							int val = Math.round(Float.parseFloat(typed));
							double dval = Math.round(100.*Float.parseFloat(typed))/100;
							if (val <= slider.getMaxWithin()) {
								slider.setValueWithin(val);
								setUIPropertyValue(getProperty(index), String.valueOf(dval));
							} else {
								slider.setValueWithin(slider.getMaxWithin());
								setUIPropertyValue(getProperty(index), String.valueOf(slider.getMaxWithin()));
							}
						} else {
							int val = Math.round(Float.parseFloat(typed));
							if (val <= slider.getMaxWithin()) {
								slider.setValueWithin(val);
								setUIPropertyValue(getProperty(index), String.valueOf(val));
							} else {
								slider.setValueWithin(slider.getMaxWithin());
								setUIPropertyValue(getProperty(index), String.valueOf(slider.getMaxWithin()));
							}
						}
					}
				});

				textFieldMax.addFocusListener(new FocusListener() {
					@Override
					public void focusGained(FocusEvent arg0) {
					}

					@Override
					public void focusLost(FocusEvent arg0) {
						String typed = textFieldMax.getText();
						if (!EmuUtils.isInteger(typed)) {
							return;
						}
						int val = Integer.parseInt(typed);
						if (val > 0) {
							if (slider.getValue() > val) {
								slider.setValueWithin(val);
								textFieldValue.setText(typed);
								setUIPropertyValue(getProperty(index), typed);
							}
							slider.setMaxWithin(val);

							// set maximum value
							changeMaxPulseProperty(val, index);
						}
					}
				});
				textFieldMax.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String typed = textFieldMax.getText();
						if (!EmuUtils.isInteger(typed)) {
							return;
						}
						int val = Integer.parseInt(typed);
						if (val > 0) {
							if (slider.getValue() > val) {
								slider.setValueWithin(val);
								textFieldValue.setText(typed);
								setUIPropertyValue(getProperty(index), typed);
							}
							slider.setMaxWithin(val);

							// set maximum value
							changeMaxPulseProperty(val, index);
						}
					}
				});
				slider.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						int val = slider.getValue();
						slider.setValueWithin(val);
						try {
							textFieldValue.setText(String.valueOf(slider.getValue()));
							setUIPropertyValue(getProperty(index), String.valueOf(slider.getValue()));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});


			} catch (UnknownUIPropertyException e1) {
				e1.printStackTrace();
			}
		}
	}
}
