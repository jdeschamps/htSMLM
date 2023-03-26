package de.embl.rieslab.htsmlm;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.micromanager.mmproperties.MMProperty;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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
import de.embl.rieslab.htsmlm.activation.ActivationController;
import de.embl.rieslab.htsmlm.activation.utils.ActivationParameters;
import de.embl.rieslab.htsmlm.activation.utils.ActivationResults;
import de.embl.rieslab.htsmlm.graph.TimeChart;

public class ActivationPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;

	private JTextField textFieldCutOff_;
	private JTextField textFieldAverage_;
	private JTextField textFieldN0_;
	private JTextField textFieldDynFactor_;
	private JTextField textFieldFeedback_;
	private JToggleButton toggleButtonRun_;
	private JCheckBox checkBoxActivate_;
	private JButton buttonGetN_;

	private JToggleButton toggleButtonAutoCutoff_;
	private JButton buttonClear_;
	private JCheckBox checkboxNMS_;

	private TimeChart graph_;
	private JPanel graphPanel_;
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
	private static final String PARAM_DEF_DF = "Def. dynamic factor";
	private static final String PARAM_DEF_FB = "Default feedback";
	private static final String PARAM_ACTIVATION_NAME1 = "Activation 1 name";
	private static final String PARAM_ACTIVATION_NAME2 = "Activation 2 name";

	//////// Misc variables
	public static int INPUT_WHICH_ACTIVATION = 0;
	private boolean activate_, showNMS_, autoCutoff_;
	private boolean useActivation1_;
	private double dynamicFactor_, feedback_, N0_ = 1, cutoff_ = 100;
	private int nPos_, idleTime_, maxPulse1_, maxPulse2_;
	private double averagingWeight_;
	
	private ActivationController activationController_;
	
	public ActivationPanel(String label, SystemController systemController) {
		super(label);
		
		setupPanel();		
		activationController_ = new ActivationController(systemController, this);
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
		this.add(getLeftPanel(),c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 3;
		c.weightx = 0.8;
		c.weighty = 0.8;
		c.fill = GridBagConstraints.BOTH;
		this.add(getGraphPanel(),c);
		
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 0.03;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(getLowerPanel(),c);
		
	}
	
	public JPanel getLeftPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		//////// Components
		JLabel labelDynFactor_ = new JLabel("DynFactor:");
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2,6,2,6);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		pane.add(labelDynFactor_,c);
		
		textFieldDynFactor_ = new JTextField(String.valueOf(dynamicFactor_));
		textFieldDynFactor_.setToolTipText("The higher the Factor coefficient, the higher the auto cutoff value.");

		c.gridy = 1;
		pane.add(textFieldDynFactor_,c);

		JLabel labelFeedback_ = new JLabel("Feedback:");
		c.gridy = 2;
		pane.add(labelFeedback_,c);
		
		textFieldFeedback_ = new JTextField(String.valueOf(feedback_));
		textFieldFeedback_.setToolTipText("The higher the Feedback coefficient, the faster the activation ramps up.");

		c.gridy = 3;
		pane.add(textFieldFeedback_,c);

		JLabel labelAverage = new JLabel("Average:");
		c.gridy = 4;
		pane.add(labelAverage,c);

		averagingWeight_ = 1.;
		textFieldAverage_ = new JTextField(String.valueOf(averagingWeight_));
		textFieldAverage_.setToolTipText("Averaging weights (between 0 and 1) of the auto cutoff.");

		c.gridy = 5;
		pane.add(textFieldAverage_,c);

		buttonGetN_ = new JButton("Get N:");
		buttonGetN_.setToolTipText("Sets N0 to the last measured number of emitters.");

		c.gridy = 6;
		c.insets = new Insets(20,6,2,6);
		pane.add(buttonGetN_,c);
		
		N0_ = 1;
		textFieldN0_ = new JTextField(String.valueOf(N0_));
		textFieldN0_.setToolTipText("Target number of emitters.");

		c.gridy = 7;
		c.insets = new Insets(2,6,2,6);
		pane.add(textFieldN0_,c);

		String[] properties = {"Activation 1", "Activation 2"};
		activationProp_ = new JComboBox<String>(properties);

		c.gridy = 8;
		c.insets = new Insets(15,6,15,6);
		pane.add(activationProp_,c);

		checkBoxActivate_ = new JCheckBox("Activate");
		checkBoxActivate_.setToolTipText("Turn on activation.");

		c.gridy = 9;
		c.insets = new Insets(15,6,2,6);
		pane.add(checkBoxActivate_,c);
		
		toggleButtonRun_ = new JToggleButton("Run");
		toggleButtonRun_.setToolTipText("Start/stop the emitter estimation script.");

		toggleButtonRun_.setPreferredSize(new Dimension(40,40));
		c.gridy = 10;
		c.gridheight = 2;
		c.insets = new Insets(2,6,2,6);
		pane.add(toggleButtonRun_,c);
		
		return pane;	
	}
	
	public JPanel getGraphPanel(){
		graphPanel_ = new JPanel();
		graph_ = newGraph();
		graphPanel_.add(graph_.getChart());
		return graphPanel_;
	}
	
	public JPanel getLowerPanel(){
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());

		// cutoff text field
		textFieldCutOff_ = new JTextField(String.valueOf(cutoff_));
		textFieldCutOff_.setToolTipText("Cutoff of the detected peak pixel value.");

		// auto cutoff button
		toggleButtonAutoCutoff_ = new JToggleButton("Auto");
		toggleButtonAutoCutoff_.setToolTipText("Turn on automated cutoff.");

		// clear graph button
		buttonClear_ = new JButton("Clear");
		buttonClear_.setToolTipText("Clear the graph.");

		// show NMS checkbox
		checkboxNMS_ = new JCheckBox("NMS");
		checkboxNMS_.setToolTipText("Show/hide the last image with the detected emitters.");

		//////////////////////////////// grid bag setup
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		c.ipadx = 50;
		pane.add(textFieldCutOff_,c);
		
		c.gridx = 1;
		c.ipadx = 0;
		pane.add(toggleButtonAutoCutoff_,c);
		
		c.gridx = 2;
		pane.add(buttonClear_,c);
		
		c.gridx = 3;
		pane.add(checkboxNMS_,c);

		return pane;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	//////
	////// Convenience methods
	//////
	/**
	 * Update graph, cutoff and pulse.
	 * 
	 * @param output Results of the activation.
	 */
	public void updateResults(final ActivationResults output) {
		graph_.addPoint(output.getNewN());
		
		if(autoCutoff_){
			textFieldCutOff_.setText(String.valueOf(output.getNewCutOff()));
			cutoff_ = output.getNewCutOff();
		}
		
		if(activate_){
			try {
				if(getMMPropertyType().equals(MMPropertyType.INTEGER)) {
					setUIPropertyValue(getProperty(), String.valueOf((int) output.getNewPulse()));
				} else {
					setUIPropertyValue(getProperty(), String.valueOf(output.getNewPulse()));
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get the current activation parameters.
	 * 
	 * @return Activation parameters
	 */
	public ActivationParameters getActivationParameters() {
		ActivationParameters parameters = new ActivationParameters();
		
		// set parameters
		parameters.setActivate(this.activate_);
		parameters.setAutoCutoff(this.autoCutoff_);
		parameters.setCutoff(this.cutoff_);
		parameters.setAveragingWeight(this.averagingWeight_);
		parameters.setCurrentPulse( getCurrentPulse());
		parameters.setFeedbackParameter(this.feedback_);
		parameters.setMaxPulse((double) getMaxPulse());
		parameters.setN0(this.N0_);
		parameters.setDynamicFactor(this.dynamicFactor_);
		
		return parameters;
	}
	
	/**
	 * Return the max pulse for the currently selected activation.
	 * 
	 * @return Maximum pulse
	 */
	public int getMaxPulse(){
		if(useActivation1_){
			return maxPulse1_;
		} else {
			return maxPulse2_;
		}
	}
		
	private TimeChart newGraph(){
		return new TimeChart("Number of locs","time","N", nPos_, 300, 240, true);
	}	
	
	private void showNMS(boolean b){
		showNMS_ = b;
		activationController_.showNMS(showNMS_);
	}

	/**
	 * Return the state of the NMS checkbox.
	 *
	 * @return True if it is
	 */
	public boolean isNMSSelected() {
		return showNMS_;
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
	
	/**
	 * Return the configured idle time.
	 * 
	 * @return Idle time of the activation thread.
	 */
	public int getIdleTime() {
		return idleTime_;
	}

	/**
	 * Return current activation property value.
	 * 
	 * @return
	 */
	public String getCurrentActivationValue() {
		try {
			return getUIPropertyValue(getProperty());
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		return "0";
	}

	private double getCurrentPulse() {
		try {
			if (EmuUtils.isNumeric(getUIProperty(getProperty()).getPropertyValue())) {
				return Double.parseDouble(getUIProperty(getProperty()).getPropertyValue());
			}
		} catch (NumberFormatException | UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	/**
	 * Return the properties allocated to the activation laser pulses.
	 * @return List of UIProperty names
	 */
	public String[] getAllocatedProperties(){
		ArrayList<String> str = new ArrayList<String>();

		String[] props = {LASER_PULSE1, LASER_PULSE2};
		for(String prop: props) {
			try {
				if(this.getUIProperty(prop).isAssigned()){
					str.add(prop);
				}
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}

		return str.toArray(new String[0]);
	}

	/**
	 * Return the parameter corresponding to the friendly activation
	 * property's name (user defined).
	 *
	 * @param property Activation laser property
	 * @return Parameter name corresponding to the user-defined activation laser's name
	 */
	private String getCorrespondingParameterName(String property){
		if(LASER_PULSE1.equals(property)){
			return PARAM_ACTIVATION_NAME1;
		} else{
			return PARAM_ACTIVATION_NAME2;
		}
	}

	/**
	 * Return the list of user-defined names for the activation properties.
	 *
	 * The names are defined through the panel parameters.
	 *
	 * @return List of friendly names
	 */
	public String[] getFriendlyActivationNames() {
		ArrayList<String> str = new ArrayList<String>();

		// get list of allocated properties (activation pulses)
		String[] props = getAllocatedProperties();
		for(String prop: props) {
			try {
				// retrieve the user-defined name for the activation laser property
				String propName = this.getStringUIParameterValue(getCorrespondingParameterName(prop));
				str.add(propName);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}

		return str.toArray(new String[0]);
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
		dynamicFactor_ = 1.5;
		feedback_ = 0.4;
		idleTime_ = 100;
		nPos_ = 30;

		String descDF = "Default value of the parameter controlling the auto cut-off level when the Activation script "
				+ "is running. A high value leads to a high cut-off level, which in turns decreases the number of "
				+ "molecules detected.";
		
		String descFb = "Default value of the parameter controlling the speed at which the pulse length (or power) of "
				+ "the activation laser is increased when the Activation script is running. A higher value leads to a "
				+ "faster increase.";
		
		String descIdle = "Idle time (ms) between each iteration of the Activation script.";

		String descNPos = "Number of points on the x axis of the Activation script graph.";

		String descActivation = "Name of the activation property (pulse duration) appearing in the drop-down menu of " +
				"the activation panel.";
		
		addUIParameter(new DoubleUIParameter(this, PARAM_DEF_DF, descDF, dynamicFactor_));
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
		if(PARAM_DEF_DF.equals(label)){
			try {
				dynamicFactor_ = getDoubleUIParameterValue(PARAM_DEF_DF);
				textFieldDynFactor_.setText(String.valueOf(dynamicFactor_));
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
					activationController_.updateIdleTime(idleTime_);
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
			// check if the two properties have been allocated
			String[] props = getFriendlyActivationNames();

			// set choices
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>( props );
			activationProp_.setModel( model );

			// disable if not 2 properties
			if(props.length != 2){
				activationProp_.setEnabled(false);
			}

			if(props.length != 0) activationProp_.setSelectedIndex(0);
		}
	}

	@Override
	public void shutDown() {
		activationController_.shutDown();
	}

	@Override
	public String getDescription() {
		return "The activation panel includes a script for automated activation (localization microscopy) using a laser pulsing or power percentage. "
				+ "Start the script using the \"run\" button. Then set a cutoff or use the auto cutoff feature (\"Auto\" button). The results of the "
				+ "spot detection algorithm can be displayed by checking the \"NMS\" checkbox. The \"Get N\" extracts the last measured value and sets "
				+ "it as the target number of molecules. This number can be changed manually in the corresponding text area. The script can then update "
				+ "the activation laser pulse length or power by checking the \"Activate\" checkbox. The script has three parameters: DynamicFactor, Feedback "
				+ "and Average. The DynamicFactor is used in the auto-cutoff feature, the higher the value, the higher the cutoff. The feedback parameter "
				+ "impacts the strength of the feedback to the activation pulse. The stronger the feedback, the faster the pulse will increase. Note that "
				+ "the algorithm is slow to go over 1-2, then can increase rapidly. Finally, the average parameter is the weight of new cutoffs in the rolling "
				+ "cutoff average. Its value is between 0 and 1 and lower value provide more stable cutoffs.";
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
		if(INTERNAL_MAXPULSE2.equals(label)){
			try {
				maxPulse2_ = getIntegerInternalPropertyValue(INTERNAL_MAXPULSE2);
			} catch (IncorrectInternalPropertyTypeException | UnknownInternalPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set the activation property to the required index.
	 * @param activationIndex Index of activation property to select
	 */
	public void setSelectedActivation(int activationIndex) {
		if (activationIndex == 0 || activationIndex == 1) {
			Runnable selectActivation = new Runnable() {
				public void run() {
					activationProp_.setSelectedIndex(activationIndex);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				selectActivation.run();
			} else {
				EventQueue.invokeLater(selectActivation);
			}
		}
	}

	/**
	 * Update the UI to reflect the fact that the activation
	 * script has been started.
	 *
	 * Can be called from the acquisition controller.
	 */
	public void activationHasStarted() {
		// if the activate checkbox is not checked, check it.
		if (!checkBoxActivate_.isSelected()) {
			Runnable checkActivate = new Runnable() {
				public void run() {
					checkBoxActivate_.setSelected(true);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				checkActivate.run();
			} else {
				EventQueue.invokeLater(checkActivate);
			}
			activate_ = true;
		}

		// if the run button is not selected, select it.
		if (!toggleButtonRun_.isSelected()) {
			Runnable checkActivate = new Runnable() {
				public void run() {
					toggleButtonRun_.setSelected(true);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				checkActivate.run();
			} else {
				EventQueue.invokeLater(checkActivate);
			}
		}
	}

	/**
	 * Stop update of the activation laser property.
	 */
	public void stopActivationUpdate() {
		if (activate_) {	
			Runnable checkActivate = new Runnable() {
				public void run() {
					checkBoxActivate_.setSelected(false);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				checkActivate.run();
			} else {
				EventQueue.invokeLater(checkActivate);
			}
			activate_ = false;
		}
	}

	/**
	 * Set the activation laser property to 0.
	 */
	public void zeroProperty() {
		setUIPropertyValue(getProperty(),"0");
	}

	@Override
	protected void addComponentListeners() {
		// store averaging value weight in a variable only if it is between 0 and 1
		SwingUIListeners.addActionListenerToDoubleAction(
				val -> {
						if(val <= 1){
							averagingWeight_ = Math.max(val, 0);
						} else {
							averagingWeight_ = 1;
						}
					},
				textFieldAverage_,
				0, // min
				Double.POSITIVE_INFINITY // max
		);

		// store positive value of the parameters in member variables
		SwingUIListeners.addActionListenerToDoubleAction(val -> dynamicFactor_ = val, textFieldDynFactor_, 0, Double.POSITIVE_INFINITY);
		SwingUIListeners.addActionListenerToDoubleAction(val -> feedback_ = val, textFieldFeedback_, 0, Double.POSITIVE_INFINITY);
		SwingUIListeners.addActionListenerToDoubleAction(val -> N0_ = val, textFieldN0_, 0, Double.POSITIVE_INFINITY);
		SwingUIListeners.addActionListenerToBooleanAction(b -> autoCutoff_ = b, toggleButtonAutoCutoff_);
		SwingUIListeners.addActionListenerToDoubleAction(val -> cutoff_ = val, textFieldCutOff_, 0., Double.POSITIVE_INFINITY);
		SwingUIListeners.addActionListenerToBooleanAction(b -> activate_ = b, checkBoxActivate_);

		// start/stop the activation script
		SwingUIListeners.addActionListenerToBooleanAction(b -> activationController_.runActivation(b), toggleButtonRun_);

		// show/hide NMS result
		SwingUIListeners.addActionListenerToBooleanAction(b -> showNMS(b), checkboxNMS_);

		// clear graph
		buttonClear_.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				graph_.clearChart();
			}
		});

		// select activation
		activationProp_.addActionListener(e -> {
			try {
				// this is also called when calling setSelectedIndex
				String propName = this.getStringUIParameterValue(PARAM_ACTIVATION_NAME1);
				useActivation1_ = activationProp_.getSelectedItem().equals(propName);
			} catch (UnknownUIParameterException ex) {
				ex.printStackTrace();
			}
		});

		// get the latest estimated N and set it as target N0
		buttonGetN_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String val = String.valueOf(graph_.getLastPoint());
				textFieldN0_.setText(val);
				N0_ = graph_.getLastPoint();
			}
		});
	}

	/**
	 * Return the activation controller.
	 * @return Activation controller
	 */
	public ActivationController getActivationController() {
		return this.activationController_;
	}
}