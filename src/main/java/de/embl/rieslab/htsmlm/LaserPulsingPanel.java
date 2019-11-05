package de.embl.rieslab.htsmlm;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

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
import de.embl.rieslab.htsmlm.components.LogarithmicJSlider;
import de.embl.rieslab.htsmlm.flags.CameraExpFlag;

public class LaserPulsingPanel extends ConfigurablePanel {
	
	private static final long serialVersionUID = 1379090644780233443L;

	//////// Components
	private JTextField textfieldmax_;
	private JTextField textfieldvalue_;
	private LogarithmicJSlider logslider_;
	private TitledBorder border_;

	//////// Properties
	public final static String CAMERA_EXPOSURE = "Camera exposure";
	public final static String LASER_PULSE = "UV pulse duration (main frame)";	
	
	//////// Parameters
	public final static String PARAM_TITLE = "Name";
	public final static String PARAM_COLOR = "Color";
	public final static String PARAM_DEFAULT_MAX = "Default max pulse";	
	private String title_;	
	private Color color_;
	
	//////// Internal property
	public final static String INTERNAL_MAXPULSE = "Maximum pulse";
	
	//////// Convenience variables
	private int maxpulse_;
	
	public LaserPulsingPanel(String label) {
		super(label);
		
		setupPanel();
	}

	private void setupPanel() {

		this.setLayout(new GridBagLayout());
		
		border_ = BorderFactory.createTitledBorder(null, title_, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, color_);
		this.setBorder(border_);
		border_.setTitleFont(border_.getTitleFont().deriveFont(Font.BOLD, 12));
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(1,15,1,15);
		
		///////////////////////////////////////////////////////////////////////// User max text field
		textfieldmax_ = new JTextField("10000");
		textfieldmax_.setPreferredSize(new Dimension(30,15));
		textfieldmax_.setBackground(new Color(220,220,220));

		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		c.ipady = 7;
		this.add(textfieldmax_, c);
		
		
		///////////////////////////////////////////////////////////////////////// User value text field
		textfieldvalue_ = new JTextField();
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 1;
		this.add(textfieldvalue_, c);
		
		textfieldvalue_.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
	       	    String typed = textfieldvalue_.getText();
        	    if(!EmuUtils.isInteger(typed)) {
        	        return;
        	    }  
        	    int val = Integer.parseInt(typed);
        	    if(val<=logslider_.getMaxWithin()){
        	    	logslider_.setValueWithin(val);
        	    	setUIPropertyValue(LASER_PULSE,typed);
        	    } else {
        	    	logslider_.setValueWithin(logslider_.getMaxWithin());
        	    	setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getMaxWithin()));
        	    }
        	}
         });
		textfieldvalue_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	       	    String typed = textfieldvalue_.getText();
        	    if(!EmuUtils.isInteger(typed)) {
        	        return;
        	    }  
        	    int val = Integer.parseInt(typed);
        	    if(val<=logslider_.getMaxWithin()){
        	    	logslider_.setValueWithin(val);
        	    	setUIPropertyValue(LASER_PULSE,typed);
        	    } else {
        	    	logslider_.setValueWithin(logslider_.getMaxWithin());
        	    	setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getMaxWithin()));
        	    }
        	}
        });
		
		///////////////////////////////////////////////////////////////////////// Log JSlider
		logslider_ = new LogarithmicJSlider(JSlider.VERTICAL,1, 10000, 10);
		
		logslider_.setPaintTicks(true);
		logslider_.setPaintTrack(true);
		logslider_.setPaintLabels(true);
		logslider_.setMajorTickSpacing(10);
		logslider_.setMinorTickSpacing(10);  
		logslider_.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {				
				  int val = logslider_.getValue();
				  logslider_.setValueWithin(val);
				  try{
					  textfieldvalue_.setText(String.valueOf(logslider_.getValue()));
					  setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getValue()));
				  } catch(Exception ex){
					  ex.printStackTrace();
				  }  
			}});
		

		c.gridy = 2;
		c.ipady = 0;
		this.add(logslider_, c);
	}
	

	@Override
	protected void initializeProperties() {
		addUIProperty(new UIProperty(this, CAMERA_EXPOSURE,"Camera exposure in ms.", new CameraExpFlag()));
		addUIProperty(new UIProperty(this, LASER_PULSE,"Pulse duration of the activation laser."));
	}

	@Override
	protected void initializeParameters() {
		title_ = "UV";	
		color_ = Color.black;
		maxpulse_  = 10000;
		
		addUIParameter(new StringUIParameter(this, PARAM_TITLE,"Panel title.",title_));
		addUIParameter(new ColorUIParameter(this, PARAM_COLOR,"Default value for large z stage step.",color_));
		addUIParameter(new IntegerUIParameter(this, PARAM_DEFAULT_MAX,"Default maximum value for the activation laser pulse length.",maxpulse_));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(name.equals(LASER_PULSE)){
			if(EmuUtils.isInteger(newvalue)){
				int val = Integer.parseInt(newvalue);
				
				if(val>logslider_.getMaxWithin()){
					logslider_.setValueWithin(logslider_.getMaxWithin());
					textfieldvalue_.setText(String.valueOf(logslider_.getMaxWithin()));
					setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getMaxWithin()));
				} else {
					logslider_.setValueWithin(val);
					textfieldvalue_.setText(newvalue);
				}
			} else if(EmuUtils.isFloat(newvalue)){
				int val = Math.round(Float.parseFloat(newvalue));
				
				if(val>logslider_.getMaxWithin()){
					logslider_.setValueWithin(logslider_.getMaxWithin());
					textfieldvalue_.setText(String.valueOf(logslider_.getMaxWithin()));
					setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getMaxWithin()));
				} else {
					logslider_.setValueWithin(val);
					textfieldvalue_.setText(newvalue);
				}
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
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
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
		} else if(label.equals(PARAM_DEFAULT_MAX)){
			try {
				maxpulse_ = getIntegerUIParameterValue(PARAM_DEFAULT_MAX);
				logslider_.setMaxWithin(maxpulse_);
				if (logslider_.getValue() > logslider_.getMaxWithin()) {
					logslider_.setValueWithin(logslider_.getMaxWithin());
					textfieldvalue_.setText(String.valueOf(logslider_.getValue()));
					setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getValue()));
				}
				textfieldmax_.setText(String.valueOf(maxpulse_));
				changeMaxPulseProperty(maxpulse_);
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
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
		return "The "+getPanelLabel()+" panel is meant to control the pulse length of the activation laser. "
				+ "The user can set a maximum to the slider by entering a value in the second text area. The pulse length can be set by entering a value in the first text area or by moving the slider.";
	}

	@Override
	protected void initializeInternalProperties() {		
		addInternalProperty(new IntegerInternalProperty(this, INTERNAL_MAXPULSE, maxpulse_));
	}

	@Override
	public void internalpropertyhasChanged(String label) {
		if(label.equals(INTERNAL_MAXPULSE)){
			try {
				maxpulse_ = getIntegerInternalPropertyValue(label);
				logslider_.setMaxWithin(maxpulse_);
				if (logslider_.getValue() > logslider_.getMaxWithin()) {
					logslider_.setValueWithin(logslider_.getMaxWithin());
					textfieldvalue_.setText(String.valueOf(logslider_.getValue()));
					setUIPropertyValue(LASER_PULSE,String.valueOf(logslider_.getValue()));
				}
				textfieldmax_.setText(String.valueOf(maxpulse_));
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	private void changeMaxPulseProperty(int val){
		try {
			setInternalPropertyValue(INTERNAL_MAXPULSE,val);
		} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void addComponentListeners() {
		// Do nothing 
	}
}
