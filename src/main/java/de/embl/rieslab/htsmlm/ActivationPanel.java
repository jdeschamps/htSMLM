package de.embl.rieslab.htsmlm;

import de.embl.rieslab.emu.micromanager.mmproperties.MMProperty;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import de.embl.rieslab.emu.micromanager.mmproperties.MMProperty.MMPropertyType;
import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.internalproperties.IntegerInternalProperty;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.DoubleUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.IntegerUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.emu.utils.exceptions.IncorrectInternalPropertyTypeException;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownInternalPropertyException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.activation.ActivationTask;
import de.embl.rieslab.htsmlm.graph.TimeChart;
import de.embl.rieslab.htsmlm.tasks.Task;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;
import mmcorej.CMMCore;

public class ActivationPanel extends ConfigurablePanel implements TaskHolder<Double> {

	private static final long serialVersionUID = 1L;

	//////// Task
	public final static String TASK_NAME = "Activation task";

	private JTextField textFieldCutOff_;
	private JTextField textFieldN0_;
	private JTextField textFieldSDCoeff_;
	private JTextField textFieldFeedback_;
	private JToggleButton toggleButtonRun_;
	private JCheckBox checkBoxActivate_;
	private TimeChart graph_;
	private JPanel graphPanel_;
	private ActivationTask task_;
	private JComboBox<String> activationProp_;
	
	//////// Properties
	private static final String LASER_PULSE1 = "Pulse duration 1 (activation)";
	private static final String LASER_PULSE2 = "Pulse duration 2 (activation)";
	
	//////// Internal properties
	private static final String INTERNAL_MAXPULSE1 = LaserPulsingPanel.INTERNAL_MAXPULSE1;
	private static final String INTERNAL_MAXPULSE2 = LaserPulsingPanel.INTERNAL_MAXPULSE2;
	
	//////// Parameters
	private static final String PARAM_IDLE = "Idle time (ms)";
	private static final String PARAM_NPOS = "Number of points";
	private static final String PARAM_DEF_SD = "Default sd coeff";
	private static final String PARAM_DEF_FB = "Default feedback";
	private static final String PARAM_ACTIVATION_NAME1 = "Activation 1 name";
	private static final String PARAM_ACTIVATION_NAME2 = "Activation 2 name";

	//////// Misc variables
	private boolean activate_, showNMS_, autoCutoff_;
	private boolean useActivation1_;
	private double sdCoeff_, feedback_, N0_ = 1, cutoff_ = 100;
	private int nPos_, idleTime_, maxPulse1_, maxPulse2_;
	private double dT_;
	private ImagePlus im_;
	private ImageProcessor ip_;
	private int nmsCounter_ = 0;
	private Double[] params;
	private CMMCore core_;
	
	public ActivationPanel(String label, CMMCore core) {
		super(label);
		
		setupPanel();
		
		task_ = new ActivationTask(this, core, idleTime_);
    	ip_ = new ShortProcessor(200,200);
		im_ = new ImagePlus("NMS result");
		core_ = core;

		params = new Double[ActivationTask.NUM_PARAMETERS];
		
	}
	
	private void setupPanel() {
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.weightx = 0.2;
		c.weighty = 0.9;
		c.fill = GridBagConstraints.VERTICAL;
		this.add(getleftpanel(),c);  
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 3;
		c.weightx = 0.8;
		c.weighty = 0.8;
		c.fill = GridBagConstraints.BOTH;
		this.add(getgraphpanel(),c);
		
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 0.03;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(getlowerpanel(),c);
		
	}
	
	public JPanel getleftpanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		//////// Components
		JLabel labelsdcoeff_ = new JLabel("Sd coeff:");
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2,6,2,6);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		pane.add(labelsdcoeff_,c);
		
		textFieldSDCoeff_ = new JTextField(String.valueOf(sdCoeff_));
		textFieldSDCoeff_.setToolTipText("The higher the Sd coefficient, the higher the auto cutoff value.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> sdCoeff_ = val, textFieldSDCoeff_, 0, Double.POSITIVE_INFINITY);

		c.gridy = 1;
		pane.add(textFieldSDCoeff_,c);

		JLabel labelfeeback_ = new JLabel("Feedback:");
		c.gridy = 2;
		pane.add(labelfeeback_,c);
		
		textFieldFeedback_ = new JTextField(String.valueOf(feedback_));
		textFieldFeedback_.setToolTipText("The higher the Feedback coefficient, the faster the activation ramps up.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> feedback_ = val, textFieldFeedback_, 0, Double.POSITIVE_INFINITY);

		c.gridy = 3;
		pane.add(textFieldFeedback_,c);

		JLabel labeldT_ = new JLabel("Average:");
		c.gridy = 4;
		pane.add(labeldT_,c);

		dT_ = 1.;
		JTextField textfielddT_ = new JTextField(String.valueOf(dT_));
		textfielddT_.setToolTipText("Averaging time (in number of frames) of the auto cutoff.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> dT_ = val, textfielddT_, 1, Double.POSITIVE_INFINITY);

		c.gridy = 5;
		pane.add(textfielddT_,c);

		JButton buttongetN_ = new JButton("Get N:");
		buttongetN_.setToolTipText("Sets N0 to the last measured number of emitters.");

		buttongetN_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	String val = String.valueOf(graph_.getLastPoint());
            	textFieldN0_.setText(val);
            	N0_ = graph_.getLastPoint();
            }
        });
		c.gridy = 6;
		c.insets = new Insets(20,6,2,6);
		pane.add(buttongetN_,c);	
		
		N0_ = 1;
		textFieldN0_ = new JTextField(String.valueOf(N0_));
		textFieldN0_.setToolTipText("Target number of emitters.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> N0_ = val, textFieldN0_, 0, Double.POSITIVE_INFINITY);

		c.gridy = 7;
		c.insets = new Insets(2,6,2,6);
		pane.add(textFieldN0_,c);

		String[] properties;
		try {
			properties = getPropertiesName();
		} catch (UnknownUIParameterException e) {
			e.printStackTrace();
			properties = new String[]{"Activation 1, Activation 2"};
		}
		activationProp_ = new JComboBox(properties);
		activationProp_.addActionListener(e -> useActivation1_ = activationProp_.getSelectedIndex() == 0);
		c.gridy = 8;
		pane.add(activationProp_,c);

		checkBoxActivate_ = new JCheckBox("Activate");
		checkBoxActivate_.setToolTipText("Turn on activation.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> activate_ = b, checkBoxActivate_);

		c.gridy = 9;
		c.insets = new Insets(40,6,2,6);
		pane.add(checkBoxActivate_,c);
		
		toggleButtonRun_ = new JToggleButton("Run");
		toggleButtonRun_.setToolTipText("Start/stop the emitter estimation script.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> runActivation(b), toggleButtonRun_);

		toggleButtonRun_.setPreferredSize(new Dimension(40,40));
		c.gridy = 10;
		c.gridheight = 2;
		c.insets = new Insets(2,6,2,6);
		pane.add(toggleButtonRun_,c);
		
		return pane;	
	}
	
	public JPanel getgraphpanel(){
		graphPanel_ = new JPanel();
		graph_ = newGraph();
		graphPanel_.add(graph_.getChart());
		return graphPanel_;
	}
	
	public JPanel getlowerpanel(){
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
				
		textFieldCutOff_ = new JTextField(String.valueOf(cutoff_));
		toggleButtonRun_.setToolTipText("Cutoff of the detected peak pixel value.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> cutoff_ = val, textFieldCutOff_, 0., Double.POSITIVE_INFINITY);

		JToggleButton togglebuttonautocutoff_ = new JToggleButton("Auto");
		togglebuttonautocutoff_.setToolTipText("Turn on automated cutoff.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> autoCutoff_ = b, togglebuttonautocutoff_);

		JButton buttonclear_ = new JButton("Clear");
		buttonclear_.setToolTipText("Clear the graph.");
		buttonclear_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	graph_.clearChart();
            }  
        });

		JCheckBox checkboxnms_ = new JCheckBox("NMS");
		checkboxnms_.setToolTipText("Show/hide the last image with the detected emitters.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> showNMS(b), checkboxnms_);
		
		//////////////////////////////// grid bag setup
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		c.ipadx = 50;
		pane.add(textFieldCutOff_,c);
		
		c.gridx = 1;
		c.ipadx = 0;
		pane.add(togglebuttonautocutoff_,c);	
		
		c.gridx = 2;
		pane.add(buttonclear_,c);	
		
		c.gridx = 3;
		pane.add(checkboxnms_,c);	

		return pane;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	//////
	////// Convenience methods
	//////
	protected void runActivation(boolean b){
		if(b && !task_.isRunning()){
			task_.startTask();
		} else if(!b && task_.isRunning()){
			task_.stopTask();
		}
	}
	
	protected void showNMS(boolean b){
		if(b){
			showNMS_ = true;
			im_.setProcessor(ip_);
			core_.logMessage("[panel] Max value of image is "+ip_.getStatistics().max);
			core_.logMessage("[panel] Width value of image is "+ip_.getWidth());
			im_.show();
		} else {
			showNMS_ = false;
			im_.close();
		}
	}
	
	private TimeChart newGraph(){
		return new TimeChart("Number of locs","time","N", nPos_,300,240, true);
	}
	
	public boolean isActivationAtMax(){
		try {
			String val = getUIPropertyValue(getProperty());
			if(EmuUtils.isNumeric(val)){
				if(Double.parseDouble(val) >= getMaxPulse()){
					return true;
				}
			}
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		return false;
	}

	private MMProperty.MMPropertyType getMMPropertyType() throws UnknownUIPropertyException {
		if(useActivation1_){
			return this.getUIProperty(LASER_PULSE1).getMMPropertyType();
		} else {
			return this.getUIProperty(LASER_PULSE2).getMMPropertyType();
		}
	}

	private String getProperty(){
		if(useActivation1_){
			return LASER_PULSE1;
		} else {
			return LASER_PULSE2;
		}
	}

	private int getMaxPulse(){
		if(useActivation1_){
			return maxPulse1_;
		} else {
			return maxPulse2_;
		}
	}

	private String[] getPropertiesName() throws UnknownUIParameterException {
		String[] str = {this.getStringUIParameterValue(PARAM_ACTIVATION_NAME1),
				this.getStringUIParameterValue(PARAM_ACTIVATION_NAME2)};

		return str;
	}

	/////////////////////////////////////////////////////////////////////////////
	//////
	////// PropertyPanel methods
	//////
	
	@Override
	protected void initializeProperties() {
		
		String descPulse = "Pulse length, power or power percentage property of an activation laser. This property"
				+ " is required for the Activation script. Note that it should be mapped to the same device property"
				+ " as \"Pulse duration X (main frame)\" for consistence.";

		addUIProperty(new UIProperty(this, LASER_PULSE1, descPulse));
		addUIProperty(new UIProperty(this, LASER_PULSE2, descPulse));
	}

	@Override
	protected void initializeParameters() {
		sdCoeff_ = 1.5;
		feedback_ = 0.4;
		idleTime_ = 100;
		nPos_ = 30;

		String descSd = "Default value of the parameter controlling the auto cut-off level when the Activation script "
				+ "is running. A high value leads to a high cut-off level, which in turns decreases the number of "
				+ "molecules detected.";
		
		String descFb = "Default value of the parameter controlling the speed at which the pulse length (or power) of "
				+ "the activation laser is increased when the Activation script is running. A higher value leads to a "
				+ "faster increase.";
		
		String descIdle = "Idle time (ms) between each iteration of the Activation script.";

		String descNPos = "Number of points on the x axis of the Activation script graph.";

		String descActivation = "Name of the activation property (pulse duration) appearing in the drop-down menu of " +
				"the activation panel.";
		
		addUIParameter(new DoubleUIParameter(this, PARAM_DEF_SD, descSd, sdCoeff_));
		addUIParameter(new DoubleUIParameter(this, PARAM_DEF_FB, descFb, feedback_));
		addUIParameter(new IntegerUIParameter(this, PARAM_IDLE, descIdle, idleTime_)); // thread idle time
		addUIParameter(new IntegerUIParameter(this, PARAM_NPOS, descNPos, nPos_)); // number of point in the graph

		addUIParameter(new StringUIParameter(this, PARAM_ACTIVATION_NAME1, descActivation,"Activation 1"));
		addUIParameter(new StringUIParameter(this, PARAM_ACTIVATION_NAME2, descActivation,"Activation 2"));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		// do nothing
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_DEF_SD.equals(label)){
			try {
				sdCoeff_ = getDoubleUIParameterValue(PARAM_DEF_SD);
				textFieldSDCoeff_.setText(String.valueOf(sdCoeff_));
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_DEF_FB.equals(label)){
			try {
				feedback_ = getDoubleUIParameterValue(PARAM_DEF_FB);
				textFieldFeedback_.setText(String.valueOf(feedback_));
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_IDLE.equals(label)){
			try {
				int val = getIntegerUIParameterValue(PARAM_IDLE);
				if(val != idleTime_){
					idleTime_ = val;
					task_.setIdleTime(idleTime_);
					}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_NPOS.equals(label)){
			try {
				int val = getIntegerUIParameterValue(PARAM_NPOS);
				if(val != nPos_){
					nPos_ = val;
					graphPanel_.remove(graph_.getChart());
					graph_ = newGraph();
					graphPanel_.add(graph_.getChart());
					graphPanel_.updateUI();
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_ACTIVATION_NAME1.equals(label) || PARAM_ACTIVATION_NAME2.equals(label)) {
			try {
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel( getPropertiesName() );
				activationProp_.setModel( model );
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutDown() {
		task_.stopTask();
		showNMS(false);
	}

	@Override
	public String getDescription() {
		return "The activation panel includes a script for automated activation (localization microscopy) using a laser pulsing or power percentage. "
				+ "Start the script using the \"run\" button. Then set a cutoff or use the auto cutoff feature (\"Auto\" button). The results of the "
				+ "spot detection algorithm can be displayed by checking the \"NMS\" checkbox. The \"Get N\" extracts the last measured value and sets "
				+ "it as the target number of molecules. This number can be changed manually in the corresponding text area. The script can then update "
				+ "the activation laser pulse length or power by checking the \"Activate\" checkbox. The script has three parameters: Sd coeff, Feedback "
				+ "and Average. The Sd coeff is used in the auto-cutoff feature, the higher the value, the higher the cutoff. The feedback parameter "
				+ "impacts the strength of the feedback to the activation pulse. The stronger the feedback, the faster the pulse will increase. Note that "
				+ "the algorithm struggles to go over 1-2, then can increase rapidly. Finally, the average parameter is the number of cycle on which to "
				+ "average the cutoff, in order to provide more stable sport number estimations.";
	}

	@Override
	protected void initializeInternalProperties() {
		maxPulse1_ = 10000;
		maxPulse2_ = 10000;

		addInternalProperty(new IntegerInternalProperty(this, INTERNAL_MAXPULSE1, maxPulse1_));
		addInternalProperty(new IntegerInternalProperty(this, INTERNAL_MAXPULSE2, maxPulse2_));
	}

	@Override
	public void internalpropertyhasChanged(String label) {
		if(INTERNAL_MAXPULSE1.equals(label)){
			try {
				maxPulse1_ = getIntegerInternalPropertyValue(INTERNAL_MAXPULSE1);
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		}
		if(INTERNAL_MAXPULSE1.equals(label)){
			try {
				maxPulse2_ = getIntegerInternalPropertyValue(INTERNAL_MAXPULSE1);
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	//////
	////// TaskHolder methods
	//////
	
	@Override
	public void update(final Double[] output) {
		graph_.addPoint(output[ActivationTask.OUTPUT_N]);
		
		if(autoCutoff_){
			textFieldCutOff_.setText(String.valueOf(output[ActivationTask.OUTPUT_NEWCUTOFF]));
			cutoff_ = output[ActivationTask.OUTPUT_NEWCUTOFF];
		}
		
		if(showNMS_ && nmsCounter_ % 10 == 0){
			ImageProcessor imp = task_.getNMSResult();
			if(imp != null && imp.getPixels() != null){
				ip_ = imp;
				im_.setProcessor(ip_);
				im_.updateAndRepaintWindow();
			}
		} else if(nmsCounter_ == Integer.MAX_VALUE){
			nmsCounter_ = 0;
		}
		
		if(activate_){
			try {
				if(getMMPropertyType().equals(MMPropertyType.INTEGER)) {
					setUIPropertyValue(getProperty(),String.valueOf((int) output[ActivationTask.OUTPUT_NEWPULSE].doubleValue()));
				} else {
					setUIPropertyValue(getProperty(),String.valueOf(output[ActivationTask.OUTPUT_NEWPULSE]));
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
		
		nmsCounter_++;
	}

	@Override
	public Double[] retrieveAllParameters() {
		params[ActivationTask.PARAM_ACTIVATE] = activate_ ? 1. : 0.; 
		params[ActivationTask.PARAM_AUTOCUTOFF] = autoCutoff_ ? 1. : 0.;
		params[ActivationTask.PARAM_CUTOFF] = cutoff_; 
		params[ActivationTask.PARAM_dT] = dT_; 
		params[ActivationTask.PARAM_FEEDBACK] = feedback_; 
		params[ActivationTask.PARAM_MAXPULSE] = (double) getMaxPulse();
		params[ActivationTask.PARAM_N0] = N0_; 
		
		try {
			if (EmuUtils.isNumeric(getUIProperty(getProperty()).getPropertyValue())) {
				params[ActivationTask.PARAM_PULSE] = Double.parseDouble(getUIProperty(getProperty()).getPropertyValue());
			} else {
				params[ActivationTask.PARAM_PULSE] = 0.;
			}
		} catch (NumberFormatException | UnknownUIPropertyException e) {
			e.printStackTrace();
			params[ActivationTask.PARAM_PULSE] = 0.;
		}
		
		params[ActivationTask.PARAM_SDCOEFF] = sdCoeff_;
		
		return params;
	}

	/**
	 * Called from automated acquisition?
	 * 
	 */
	@Override
	public boolean startTask() {
		if(task_.isRunning()){ // if task is running 
			if(!activate_){ // but not changing the pulse
				 Runnable checkactivate = new Runnable() {
					 public void run() {
						 checkBoxActivate_.setSelected(true);
					 }
				 };
				 if (SwingUtilities.isEventDispatchThread()) {
					 checkactivate.run();
				 } else {
					  EventQueue.invokeLater(checkactivate);
				 }
				 activate_ = true;
			}
		} else { // task not running
			runActivation(true); // then run
			if(!activate_){ // task not changing the pulse
				 Runnable checkactivate = new Runnable() {
					 public void run() {
						 toggleButtonRun_.setSelected(true);
						 checkBoxActivate_.setSelected(true);
					 }
				 };
				 if (SwingUtilities.isEventDispatchThread()) {
					 checkactivate.run();
				 } else {
					  EventQueue.invokeLater(checkactivate);
				 }
				 activate_ = true;
			} else {
				if(!toggleButtonRun_.isSelected()) {
					 Runnable checkactivate = new Runnable() {
						 public void run() {
							 toggleButtonRun_.setSelected(true);
						 }
					 };
					 if (SwingUtilities.isEventDispatchThread()) {
						 checkactivate.run();
					 } else {
						  EventQueue.invokeLater(checkactivate);
					 }
				}
			}
		}
		return true;
	}

	@Override
	public void stopTask() {
		// do nothing
	}

	@Override
	public boolean isPausable() {
		return true;
	}

	@Override
	public void pauseTask() {
		if (activate_) {	
			Runnable checkactivate = new Runnable() {
				public void run() {
					checkBoxActivate_.setSelected(false);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				checkactivate.run();
			} else {
				EventQueue.invokeLater(checkactivate);
			}
			activate_ = false;
		}
	}

	@Override
	public void resumeTask() {
		startTask();	
	}

	@Override
	public boolean isTaskRunning() {
		return task_.isRunning();
	}

	@Override
	public String getTaskName() {
		return TASK_NAME;
	}

	@Override
	public boolean isCriterionReached() {
		return isActivationAtMax();
	}

	@Override
	public void initializeTask() {
		setUIPropertyValue(getProperty(),"0");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Task getTask() {
		return task_;
	}

	@Override
	public void taskDone() {
		// Do nothing
	}

	@Override
	protected void addComponentListeners() {
		// Do nothing
	}

	// in order to maintain UV activation during Multislice localization
	// we need to be able to circumvent Micro-Manager cache 
}