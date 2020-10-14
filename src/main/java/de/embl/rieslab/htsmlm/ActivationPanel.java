package de.embl.rieslab.htsmlm;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

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
	
	//////// Components
	private JLabel labelsdcoeff_;
	private JLabel labelfeeback_;
	private JLabel labeldT_;
	private JTextField textfieldcutoff_;
	private JTextField textfieldN0_;
	private JTextField textfieldsdcoeff_;
	private JTextField textfieldfeedback_;
	private JTextField textfielddT_;
	private JToggleButton togglebuttonrun_;
	private JToggleButton togglebuttonautocutoff_;
	private JButton buttongetN_;
	private JButton buttonclear_;
	private JCheckBox checkboxnms_;
	private JCheckBox checkboxactivate_;
	private TimeChart graph_;
	private JPanel graphpane_;
	private ActivationTask task_;
	
	//////// Properties
	private static final String LASER_PULSE = "UV pulse duration (activation)";
	
	//////// Internal properties
	private static final String INTERNAL_MAXPULSE = LaserPulsingPanel.INTERNAL_MAXPULSE;
	
	//////// Parameters
	private static final String PARAM_IDLE = "Idle time (ms)";
	private static final String PARAM_NPOS = "Number of points";
	private static final String PARAM_DEF_SD = "Default sd coeff";
	private static final String PARAM_DEF_FB = "Default feedback";

	//////// Misc variables
	private boolean activate_, shownms_, autocutoff_;
	private double sdcoeff_, feedback_, N0_ = 1, cutoff_ = 100;
	private int npos_, idletime_, maxpulse_;
	private double dT_;
	private ImagePlus im_;
	private ImageProcessor ip_;
	private int counternms_ = 0;
	private Double[] params;
	
	public ActivationPanel(String label, CMMCore core) {
		super(label);
		
		setupPanel();
		
		task_ = new ActivationTask(this, core, idletime_);
    	ip_ = new ShortProcessor(200,200);
		im_ = new ImagePlus("NMS result");
		
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
	
	public JPanel getleftpanel(){
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		
		labelsdcoeff_ = new JLabel("Sd coeff:");
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2,6,2,6);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		pane.add(labelsdcoeff_,c);
		
		textfieldsdcoeff_ = new JTextField(String.valueOf(sdcoeff_));
		textfieldsdcoeff_.setToolTipText("The higher the Sd coefficient, the higher the auto cutoff value.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> sdcoeff_ = val, textfieldsdcoeff_, 0, Double.POSITIVE_INFINITY);

		c.gridy = 1;
		pane.add(textfieldsdcoeff_,c);	
		
		labelfeeback_ = new JLabel("Feedback:");
		c.gridy = 2;
		pane.add(labelfeeback_,c);
		
		textfieldfeedback_ = new JTextField(String.valueOf(feedback_));
		textfieldfeedback_.setToolTipText("The higher the Feedback coefficient, the faster the activation ramps up.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> feedback_ = val, textfieldfeedback_, 0, Double.POSITIVE_INFINITY);

		c.gridy = 3;
		pane.add(textfieldfeedback_,c);	
		
		labeldT_ = new JLabel("Average:");
		c.gridy = 4;
		pane.add(labeldT_,c);

		dT_ = 1.;
		textfielddT_ = new JTextField(String.valueOf(dT_));
		textfielddT_.setToolTipText("Averaging time (in number of frames) of the auto cutoff.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> dT_ = val, textfielddT_, 1, Double.POSITIVE_INFINITY);

		c.gridy = 5;
		pane.add(textfielddT_,c);	
		
		buttongetN_ = new JButton("Get N:");
		buttongetN_.setToolTipText("Sets N0 to the last measured number of emitters.");

		buttongetN_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	String val = String.valueOf(graph_.getLastPoint());
            	textfieldN0_.setText(val);
            	N0_ = graph_.getLastPoint();
            }
        });
		c.gridy = 6;
		c.insets = new Insets(20,6,2,6);
		pane.add(buttongetN_,c);	
		
		N0_ = 1;
		textfieldN0_ = new JTextField(String.valueOf(N0_));
		textfieldN0_.setToolTipText("Target number of emitters.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> N0_ = val, textfieldN0_, 0, Double.POSITIVE_INFINITY);

		c.gridy = 7;
		c.insets = new Insets(2,6,2,6);
		pane.add(textfieldN0_,c);
		
		checkboxactivate_ = new JCheckBox("Activate");
		checkboxactivate_.setToolTipText("Turn on activation.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> activate_ = b, checkboxactivate_);

		c.gridy = 8;
		c.insets = new Insets(40,6,2,6);
		pane.add(checkboxactivate_,c);	
		
		togglebuttonrun_ = new JToggleButton("Run");
		togglebuttonrun_.setToolTipText("Start/stop the emitter estimation script.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> runActivation(b), togglebuttonrun_);

		togglebuttonrun_.setPreferredSize(new Dimension(40,40));
		c.gridy = 9;
		c.gridheight = 2;
		c.insets = new Insets(2,6,2,6);
		pane.add(togglebuttonrun_,c);	
		
		return pane;	
	}
	
	public JPanel getgraphpanel(){
		graphpane_  = new JPanel();
		graph_ = newGraph();
		graphpane_.add(graph_.getChart());
		return graphpane_;
	}
	
	public JPanel getlowerpanel(){
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
				
		textfieldcutoff_ = new JTextField(String.valueOf(cutoff_));
		togglebuttonrun_.setToolTipText("Cutoff of the detected peak pixel value.");
		SwingUIListeners.addActionListenerToDoubleAction(val -> cutoff_ = val, textfieldcutoff_, 0., Double.POSITIVE_INFINITY);
		
		togglebuttonautocutoff_ = new JToggleButton("Auto");
		togglebuttonautocutoff_.setToolTipText("Turn on automated cutoff.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> autocutoff_ = b, togglebuttonautocutoff_);
		
		buttonclear_ = new JButton("Clear");
		buttonclear_.setToolTipText("Clear the graph.");
		buttonclear_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	graph_.clearChart();
            }  
        });	
		
		checkboxnms_ = new JCheckBox("NMS");
		checkboxnms_.setToolTipText("Show/hide the last image with the detected emitters.");
		SwingUIListeners.addActionListenerToBooleanAction(b -> showNMS(b), checkboxnms_);
		
		//////////////////////////////// grid bag setup
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		c.ipadx = 50;
		pane.add(textfieldcutoff_,c);	
		
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
			shownms_ = true;
			im_.setProcessor(ip_);
			im_.show();
		} else {
			shownms_ = false;
			im_.close();
		}
	}
	
	private TimeChart newGraph(){
		return new TimeChart("Number of locs","time","N",npos_,300,240, true);	
	}
	
	public boolean isActivationAtMax(){
		try {
			String val = getUIPropertyValue(LASER_PULSE);
			if(EmuUtils.isNumeric(val)){
				if(Double.parseDouble(val)>=maxpulse_){
					return true;
				}
			}
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		return false;
	}

	/////////////////////////////////////////////////////////////////////////////
	//////
	////// PropertyPanel methods
	//////
	
	@Override
	protected void initializeProperties() {
		addUIProperty(new UIProperty(this, LASER_PULSE,"Pulse length property of the activation laser"));		
	}

	@Override
	protected void initializeParameters() {
		sdcoeff_ = 1.5;
		feedback_ = 0.4;
		idletime_ = 100;
		npos_ = 30; 
		
		addUIParameter(new DoubleUIParameter(this, PARAM_DEF_SD,"Default value of the cutoff coefficient.",sdcoeff_));
		addUIParameter(new DoubleUIParameter(this, PARAM_DEF_FB,"Default value of the activation feedback coefficient.",feedback_));
		addUIParameter(new IntegerUIParameter(this, PARAM_IDLE,"Idle time (ms) between each iteration.",idletime_)); // thread idle time
		addUIParameter(new IntegerUIParameter(this, PARAM_NPOS,"Number of points on the x axis.",npos_)); // number of point in the graph
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		// do nothing
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_DEF_SD.equals(label)){
			try {
				sdcoeff_ = getDoubleUIParameterValue(PARAM_DEF_SD);
				textfieldsdcoeff_.setText(String.valueOf(sdcoeff_));
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_DEF_FB.equals(label)){
			try {
				feedback_ = getDoubleUIParameterValue(PARAM_DEF_FB);
				textfieldfeedback_.setText(String.valueOf(feedback_));
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_IDLE.equals(label)){
			try {
				int val = getIntegerUIParameterValue(PARAM_IDLE);
				if(val != idletime_){
					idletime_ = val;
					task_.setIdleTime(idletime_);
					}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_NPOS.equals(label)){
			try {
				int val = getIntegerUIParameterValue(PARAM_NPOS);
				if(val != npos_){
					npos_ = val;
					graphpane_.remove(graph_.getChart());
					graph_ = newGraph();
					graphpane_.add(graph_.getChart());
					graphpane_.updateUI();
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
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
		maxpulse_ = 10000;
		
		addInternalProperty(new IntegerInternalProperty(this, INTERNAL_MAXPULSE, maxpulse_));
	}

	@Override
	public void internalpropertyhasChanged(String label) {
		if(INTERNAL_MAXPULSE.equals(label)){
			try {
				maxpulse_ = getIntegerInternalPropertyValue(INTERNAL_MAXPULSE);
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
		
		if(autocutoff_){
			textfieldcutoff_.setText(String.valueOf(output[ActivationTask.OUTPUT_NEWCUTOFF]));
			cutoff_ = output[ActivationTask.OUTPUT_NEWCUTOFF];
		}
		
		if(shownms_ && counternms_ % 10 == 0){
			ImageProcessor imp = task_.getNMSResult();
			if(imp != null && imp.getPixels() != null){
				ip_ = task_.getNMSResult();
				im_.setProcessor(ip_);
				im_.updateAndRepaintWindow();
			}
		} else if(counternms_ == Integer.MAX_VALUE){
			counternms_ = 0;
		}
		
		if(activate_){
			try {
				if(getUIProperty(LASER_PULSE).getMMPropertyType().equals(MMPropertyType.INTEGER)) {
					setUIPropertyValue(LASER_PULSE,String.valueOf((int) output[ActivationTask.OUTPUT_NEWPULSE].doubleValue()));
				} else {
					setUIPropertyValue(LASER_PULSE,String.valueOf(output[ActivationTask.OUTPUT_NEWPULSE]));	
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
		
		counternms_ ++;
	}

	@Override
	public Double[] retrieveAllParameters() {
		params[ActivationTask.PARAM_ACTIVATE] = activate_ ? 1. : 0.; 
		params[ActivationTask.PARAM_AUTOCUTOFF] = autocutoff_ ? 1. : 0.; 
		params[ActivationTask.PARAM_CUTOFF] = cutoff_; 
		params[ActivationTask.PARAM_dT] = dT_; 
		params[ActivationTask.PARAM_FEEDBACK] = feedback_; 
		params[ActivationTask.PARAM_MAXPULSE] = (double) maxpulse_;
		params[ActivationTask.PARAM_N0] = N0_; 
		
		try {
			if(EmuUtils.isNumeric(getUIProperty(LASER_PULSE).getPropertyValue())){
				params[ActivationTask.PARAM_PULSE] = Double.parseDouble(getUIProperty(LASER_PULSE).getPropertyValue()); 
			} else {
				params[ActivationTask.PARAM_PULSE] =  0.;
			}
		} catch (NumberFormatException | UnknownUIPropertyException e) {
			e.printStackTrace();
			params[ActivationTask.PARAM_PULSE] = 0.;
		}
		
		params[ActivationTask.PARAM_SDCOEFF] = sdcoeff_; 
		
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
						 checkboxactivate_.setSelected(true);
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
						 togglebuttonrun_.setSelected(true);
						 checkboxactivate_.setSelected(true);
					 }
				 };
				 if (SwingUtilities.isEventDispatchThread()) {
					 checkactivate.run();
				 } else {
					  EventQueue.invokeLater(checkactivate);
				 }
				 activate_ = true;
			} else {
				if(!togglebuttonrun_.isSelected()) {
					 Runnable checkactivate = new Runnable() {
						 public void run() {
							 togglebuttonrun_.setSelected(true);
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
		if(activate_){ 
			 Runnable checkactivate = new Runnable() {
				 public void run() {
					 checkboxactivate_.setSelected(false);
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
		setUIPropertyValue(LASER_PULSE,"0");
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

}