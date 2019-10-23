package de.embl.rieslab.htsmlm;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.exceptions.IncorrectUIPropertyTypeException;
import de.embl.rieslab.emu.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.emu.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.ColorRepository;
import de.embl.rieslab.emu.utils.utils;
import de.embl.rieslab.htsmlm.components.TogglePower;
import de.embl.rieslab.htsmlm.components.ToggleSlider;
import de.embl.rieslab.htsmlm.flags.FocusLockFlag;

public class FocusLockPanel extends ConfigurablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6136665461633874956L;

	//////// Components
	private JTextField textfieldUserPower_;
	private JSlider sliderPower_;
	private JSlider sliderFinea_;
	private JSlider sliderFineb_;
	private JToggleButton togglebuttonLaser_;
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
	
	/////// Convenience variables
	private int max_power;
	
	public FocusLockPanel(String label) {
		super(label);
		
		setupPanel();
	}

	private void setupPanel() {

		///////////////////////////////////////////////////////////////////////////// set-up components
		// Power text field
		textfieldUserPower_ = new JTextField(String.valueOf(max_power));
		textfieldUserPower_.setPreferredSize(new Dimension(35,20));
		textfieldUserPower_.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
				String typed = textfieldUserPower_.getText();
				if (!utils.isNumeric(typed)) {
					return;
				}

				try {
					double val = Double.parseDouble(typed);
					if (Double.compare(val, max_power) <= 0 && Double.compare(val, 0.) >= 0) {
						setUIPropertyValue(getPanelLabel() + " " + LASER_POWER, typed);
						sliderPower_.setValue((int) val);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        });
		
		textfieldUserPower_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String typed = textfieldUserPower_.getText();
				if (!utils.isNumeric(typed)) {
					return;
				}

				try {
					double val = Double.parseDouble(typed);
					if (Double.compare(val, max_power) <= 0 && Double.compare(val, 0.) >= 0) {
						setUIPropertyValue(getPanelLabel() + " " + LASER_POWER, typed);
						sliderPower_.setValue((int) val);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        });
		
		// slider channel 1
		sliderPower_ = new JSlider(JSlider.HORIZONTAL, 0, (int) max_power, 0);
		SwingUIListeners.addActionListenerOnIntegerValue(this, getPanelLabel()+" "+LASER_POWER, sliderPower_, textfieldUserPower_);
		
		// slider fine a
		sliderFinea_ = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		sliderFinea_.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				fineaperc_.setText(String.valueOf(sliderFinea_.getValue())+" %");		
				setUIPropertyValue(getPanelLabel()+" "+LASER_PERCFINEA,String.valueOf(sliderFinea_.getValue()));
			}});

		// slider fine b
		sliderFineb_ = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		sliderFineb_.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {	
				finebperc_.setText(String.valueOf(sliderFineb_.getValue())+" %");						
				setUIPropertyValue(getPanelLabel()+" "+LASER_PERCFINEB,String.valueOf(sliderFineb_.getValue()));
			}});
		
		togglebuttonLaser_ = new TogglePower();
		try {
			SwingUIListeners.addActionListenerToTwoState(this, getPanelLabel()+" "+LASER_OPERATION, togglebuttonLaser_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
		
		// Fine enable
		togglesliderenableFine_ = new ToggleSlider();
		try {
			SwingUIListeners.addActionListenerToTwoState(this, getPanelLabel()+" "+LASER_ENABLEFINE, togglesliderenableFine_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}

		fineaperc_ = new JLabel("100 %");
		finebperc_ = new JLabel("100 %");

		// others
		JLabel fineAperc = new JLabel("a");
		JLabel finebperc = new JLabel("b");
		JLabel power = new JLabel("Power (mW):");
		
		///////////////////////////////////////////////////////////////////////////// Channel 1
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridBagLayout());
		TitledBorder border2 = BorderFactory.createTitledBorder(null, "Power", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
				null, ColorRepository.getColor(ColorRepository.strblack));
		panel2.setBorder(border2);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 1;
		c2.gridy = 0;
		c2.ipadx = 5;
		c2.ipady = 5;
		c2.weightx = 0.2;
		c2.weighty = 0.3;
		c2.fill = GridBagConstraints.BOTH;
		panel2.add(power, c2);

		c2.gridx = 2;
		panel2.add(textfieldUserPower_, c2);
		

		c2.gridx = 3;
		panel2.add(togglebuttonLaser_, c2);

		c2.gridx = 0;
		c2.gridy = 4;
		c2.gridwidth = 4;
		c2.weightx = 0.9;
		c2.weighty = 0.5;
		panel2.add(sliderPower_, c2);


		///////////////////////////////////////////////////////////////////////////// fine
		JPanel panelfine = new JPanel();
		panelfine.setLayout(new GridBagLayout());
		TitledBorder borderfine = BorderFactory.createTitledBorder(null, "Fine", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
				null, ColorRepository.getColor(ColorRepository.strblack));
		panelfine.setBorder(borderfine);
		
		// gridbad layout
		GridBagConstraints cfine = new GridBagConstraints();
		cfine.fill = GridBagConstraints.HORIZONTAL;
		cfine.ipadx = 35;
		cfine.ipady = 2;
		cfine.gridx = 0;
		cfine.gridy = 0;
		panelfine.add(togglesliderenableFine_, cfine);

		cfine.gridx = 1;
		cfine.gridy = 1;
		cfine.ipadx = 5;
		panelfine.add(fineAperc, cfine);
		
		cfine.gridy = 2;
		panelfine.add(finebperc, cfine);
		
		cfine.gridx = 2;
		cfine.gridy = 1;
		cfine.ipadx = 4;
		cfine.gridwidth = 3;
		panelfine.add(sliderFinea_, cfine);
		
		cfine.gridy = 2;
		panelfine.add(sliderFineb_, cfine);
		
		cfine.gridx = 5;
		cfine.gridy = 1;
		cfine.ipadx = 5;
		cfine.gridwidth = 1;
		cfine.insets = new Insets(2,35,2,2);
		panelfine.add(fineaperc_, cfine);
		
		cfine.gridy = 2;
		cfine.insets = new Insets(2,35,2,2);
		panelfine.add(finebperc_, cfine);

		
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
		this.add(panel2,c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(panelfine,c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 0.8;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JPanel(),c);
	}
	
	@Override
	protected void initializeProperties() {
		max_power = 200;
		
		addUIProperty(new UIProperty(this, getPanelLabel()+" "+LASER_POWER,"iBeamSmart Power (mW).", new FocusLockFlag()));
		addUIProperty(new UIProperty(this, getPanelLabel()+" "+LASER_PERCFINEA,"iBeamSmart Power percentage of fine a.", new FocusLockFlag()));
		addUIProperty(new UIProperty(this, getPanelLabel()+" "+LASER_PERCFINEB,"iBeamSmart Power percentage of fine b.", new FocusLockFlag()));
		addUIProperty(new UIProperty(this, getPanelLabel()+" "+LASER_MAXPOWER,"iBeamSmart Maximum power (mW).", new FocusLockFlag()));

		addUIProperty(new TwoStateUIProperty(this,getPanelLabel()+" "+LASER_OPERATION,"iBeamSmart On/Off operation property.", new FocusLockFlag()));
		addUIProperty(new TwoStateUIProperty(this,getPanelLabel()+" "+LASER_ENABLEFINE,"iBeamSmart Enable property of fine.", new FocusLockFlag()));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(name.equals(getPanelLabel()+" "+LASER_POWER)){
			if(utils.isNumeric(newvalue)){
				double val = Double.parseDouble(newvalue);
				if(val>=0 && val<=max_power){
					textfieldUserPower_.setText(String.valueOf(val));
					sliderPower_.setValue((int) val);	
				}
			}
		} else if(name.equals(getPanelLabel()+" "+LASER_PERCFINEA)){
			if(utils.isNumeric(newvalue)){
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
		} else if(name.equals(getPanelLabel()+" "+LASER_PERCFINEB)){
			if(utils.isNumeric(newvalue)){
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
		} else if(name.equals(getPanelLabel()+" "+LASER_OPERATION)){
			try {
				togglebuttonLaser_.setSelected(((TwoStateUIProperty) getUIProperty(getPanelLabel()+" "+LASER_ENABLEFINE)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}

		} else if(name.equals(getPanelLabel()+" "+LASER_ENABLEFINE)){
			try {
				togglesliderenableFine_.setSelected(((TwoStateUIProperty) getUIProperty(getPanelLabel()+" "+LASER_ENABLEFINE)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		} else if(name.equals(getPanelLabel()+" "+LASER_MAXPOWER)){
			if(utils.isNumeric(newvalue)){
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
		// Do nothing
	}

	@Override
	public void parameterhasChanged(String label) {
		// Do nothing		
	}

	@Override
	protected void addComponentListeners() {
		// TODO Auto-generated method stub
		
	}
}
