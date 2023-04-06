package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.*;

import org.micromanager.Studio;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.SequenceSettings.Builder;
import org.micromanager.acquisition.internal.DefaultAcquisitionManager;
import org.micromanager.data.Datastore;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.activation.ActivationController;

/**
 * Localization acquisition.
 *
 * A localization acquisition is a time series that can run the
 * activation script in parallel, and be stopped when the maximum
 * activation pulse is reached.
 */
public class LocalizationAcquisition implements Acquisition {
	
	private GenericAcquisitionParameters params_;
	
	private final static String PANE_NAME = "Localization panel";
	private final static String LABEL_EXPOSURE = "Exposure (ms):";
	private final static String LABEL_PAUSE = "Pause (s):";
	private final static String LABEL_NUMFRAME = "Number of frames:";
	private final static String LABEL_INTERVAL = "Interval (ms):";
	private final static String LABEL_USEACTIVATION = "Use activation";
	private final static String LABEL_USESTOPONMAXUV = "Stop on max";
	private final static String LABEL_MAXUVTIME = "Stop on max delay (s):";
	private final static String LABEL_ACTIVATION = "Activation:";
	
	public final static String KEY_USEACT = "Use activation?";
	public final static String KEY_STOPONMAX = "Stop on max?";
	public final static String KEY_STOPDELAY = "Stop on max delay";
	public final static String KEY_ACTIVATION = "Activation:";
	
	private final ActivationController activationController_;
	private boolean useActivation_, stopOnMax_, noActivation_;
	private volatile boolean stopAcq_, running_;
	private int stopOnMaxDelay_;
	private boolean interruptionRequested_;
	private String activationName_ = "None";

	private final Studio studio_;

	public LocalizationAcquisition(ActivationController activationController, double exposure, Studio studio) {
		this.studio_ = studio;

		if(activationController == null){
			throw new IllegalArgumentException("Activation controller cannot be null.");
		}
		activationController_ = activationController;

		// if no activation property is allocated
		noActivation_ = activationController_.getActivationPropertiesFriendlyName().length == 0;

		// control booleans
		stopAcq_ = false;
		running_ = false;
		interruptionRequested_ = false;

		// default values
		stopOnMax_ = true;
		useActivation_ = true;
		stopOnMaxDelay_ = 5;

		params_ = new GenericAcquisitionParameters(AcquisitionType.LOCALIZATION, 
				exposure, 0, 3, 30000, new HashMap<String,String>(), new HashMap<String,String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		pane.setName(getPanelName());
		
		final JLabel exposureLabel, waitingLabel, numFrameLabel, intervalLabel, waitOnMaxLabel, activationLabel;
		final JSpinner exposureSpin, waitingSpin, numFrameSpin, intervalSpin, waitOnMaxSpin;
		final JCheckBox activateCheck, stopOnMaxCheck;
		final JComboBox<String> activationCombo;
		
		exposureLabel = new JLabel(LABEL_EXPOSURE);
		waitingLabel = new JLabel(LABEL_PAUSE);
		numFrameLabel = new JLabel(LABEL_NUMFRAME);
		intervalLabel = new JLabel(LABEL_INTERVAL);
		waitOnMaxLabel = new JLabel(LABEL_MAXUVTIME);
		activationLabel = new JLabel(LABEL_ACTIVATION);
		
		exposureSpin = new JSpinner(new SpinnerNumberModel(Math.max(params_.getExposureTime(),1), 1, 10000000, 1));
		exposureSpin.setName(LABEL_EXPOSURE);
		exposureSpin.setToolTipText("Camera exposure (ms).");
		
		waitingSpin = new JSpinner(new SpinnerNumberModel(params_.getWaitingTime(), 0, 10000000, 1));
		waitingSpin.setName(LABEL_PAUSE);
		waitingSpin.setToolTipText("Waiting time (s) to allow device state changes before this acquisition.");
		
		numFrameSpin = new JSpinner(new SpinnerNumberModel(params_.getNumberFrames(), 1, 10000000, 1));
		numFrameSpin.setName(LABEL_NUMFRAME);
		numFrameSpin.setToolTipText("Number of frames.");
		
		intervalSpin = new JSpinner(new SpinnerNumberModel(params_.getIntervalMs(), 0, 10000000, 1));
		intervalSpin.setName(LABEL_INTERVAL);
		intervalSpin.setToolTipText("Interval between frames (ms).");
		
		waitOnMaxSpin = new JSpinner(new SpinnerNumberModel(stopOnMaxDelay_, 0, 10000, 1));
		waitOnMaxSpin.setName(LABEL_MAXUVTIME);
		waitOnMaxSpin.setToolTipText("Time (s) before stopping the acquisition after reaching the maximum activationCombo value.");

		activateCheck = new JCheckBox(LABEL_USEACTIVATION);
		activateCheck.setSelected(useActivation_); // set selected
		activateCheck.setName(LABEL_USEACTIVATION);
		activateCheck.setToolTipText("Use activationCombo during the acquisition.");
		
		stopOnMaxCheck = new JCheckBox(LABEL_USESTOPONMAXUV);
		stopOnMaxCheck.setSelected(stopOnMax_);
		stopOnMaxCheck.setName(LABEL_USESTOPONMAXUV);
		stopOnMaxCheck.setToolTipText("Stop the acquisition after reaching the maximum activationCombo value.");

		final String[] activationsName = activationController_.getActivationPropertiesFriendlyName();
		activationCombo = new JComboBox<>(activationsName);
		activationCombo.setName(LABEL_ACTIVATION);
		if(activationsName.length != 2){ // number of activation is either 0 or 1
			// we do not need the activation selection
			activationCombo.setEnabled(false);

			// if there is no activation, we disable all activation related elements
			if(activationsName.length == 0){
				stopOnMaxCheck.setEnabled(false);
				waitOnMaxSpin.setEnabled(false);
				activateCheck.setEnabled(false);
				useActivation_ = false;
			}
		} else {
			if (Arrays.asList(activationsName).contains(activationName_)) {
				activationCombo.setSelectedItem(activationName_);
			}
		}

		// selecting activation enables and select other elemeents
		activateCheck.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			boolean selected = abstractButton.getModel().isSelected();
			if (!selected) {
				stopOnMaxCheck.setSelected(false);
				stopOnMaxCheck.setEnabled(false);
				waitOnMaxSpin.setValue(0);
				waitOnMaxSpin.setEnabled(false);
				activationCombo.setEnabled(false);
			} else {
				stopOnMaxCheck.setEnabled(true);
				waitOnMaxSpin.setEnabled(true);

				if(activationsName.length == 2) activationCombo.setEnabled(true);
			}
		});

		// same for stop on max
		stopOnMaxCheck.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			boolean selected = abstractButton.getModel().isSelected();
			if (!selected) {
				waitOnMaxSpin.setValue(0);
				waitOnMaxSpin.setEnabled(false);
				activationCombo.setEnabled(false);
			} else {
				waitOnMaxSpin.setEnabled(true);
				waitOnMaxSpin.setEnabled(true);

				if(activationsName.length == 2) activationCombo.setEnabled(true);
			}
		});

		// set up elements in the layout
		int nrow = 4;
		int ncol = 4;
		JPanel[][] panelHolder = new JPanel[nrow][ncol];    
		pane.setLayout(new GridLayout(nrow,ncol));
	
		for(int m = 0; m < nrow; m++) {
		   for(int n = 0; n < ncol; n++) {
		      panelHolder[m][n] = new JPanel();
		      pane.add(panelHolder[m][n]);
		   }
		}

		panelHolder[0][0].add(exposureLabel);
		panelHolder[1][0].add(waitingLabel);
		panelHolder[2][0].add(waitOnMaxLabel);

		panelHolder[0][1].add(exposureSpin);
		panelHolder[1][1].add(waitingSpin);
		panelHolder[2][1].add(waitOnMaxSpin);

		panelHolder[0][2].add(numFrameLabel);
		panelHolder[1][2].add(intervalLabel);
		panelHolder[2][2].add(stopOnMaxCheck);
		panelHolder[3][2].add(activationLabel);

		panelHolder[0][3].add(numFrameSpin);
		panelHolder[1][3].add(intervalSpin);
		panelHolder[2][3].add(activateCheck);
		panelHolder[3][3].add(activationCombo);
	
		return pane;
	}

	private void setUseActivation(boolean b){
		if(!noActivation_){
			useActivation_ = b;
		} else {
			useActivation_ = false;
		}
	}

	private void setUseStopOnMaxUV(boolean b){
		stopOnMax_ = b;
	}
	
	private void setUseStopOnMaxUVDelay(int delay){
		stopOnMaxDelay_ = delay;
	}

	private void setActivation(String act){
		studio_.logs().logDebugMessage("[htSMLM loc] set activation to "+act);
		activationName_ = act;
	}

	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		if(pane.getName().equals(getPanelName())){
			Component[] panelComponents = pane.getComponents();

			// retrieve the value of the parameters from the UI elements
			for(int j=0;j<panelComponents.length;j++){
				if(panelComponents[j] instanceof JPanel){
					Component[] comp = ((JPanel) panelComponents[j]).getComponents();
					for(int i=0;i<comp.length;i++){
						if(!(comp[i] instanceof JLabel) && comp[i].getName() != null){
							if(comp[i].getName().equals(LABEL_EXPOSURE) && comp[i] instanceof JSpinner){
								params_.setExposureTime((Double) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_PAUSE) && comp[i] instanceof JSpinner){
								params_.setWaitingTime((Integer) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_NUMFRAME) && comp[i] instanceof JSpinner){
								params_.setNumberFrames((Integer) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_INTERVAL) && comp[i] instanceof JSpinner){
								params_.setIntervalMs((Double) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_USEACTIVATION) && comp[i] instanceof JCheckBox){
								this.setUseActivation(((JCheckBox) comp[i]).isSelected());
							} else if(comp[i].getName().equals(LABEL_USESTOPONMAXUV) && comp[i] instanceof JCheckBox){
								this.setUseStopOnMaxUV(((JCheckBox) comp[i]).isSelected());
							} else if(comp[i].getName().equals(LABEL_MAXUVTIME) && comp[i] instanceof JSpinner){
								this.setUseStopOnMaxUVDelay((Integer) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_ACTIVATION) && comp[i] instanceof JComboBox){
								this.setActivation((String) ((JComboBox<String>) comp[i]).getSelectedItem());
							}
						}
					}
				}
			}	
		}
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		return new NoPropertyFilter();
	}

	@Override
	public String[] getHumanReadableSettings() {
		String[] s = new String[7];
		s[0] = "Exposure = "+params_.getExposureTime()+" ms";
		s[1] = "Interval = "+params_.getIntervalMs()+" ms";
		s[2] = "Number of frames = "+params_.getNumberFrames();
		s[3] = "Use activation = "+ useActivation_;
		s[4] = "Stop on max = "+ stopOnMax_;
		s[5] = "Stop on max delay = "+ stopOnMaxDelay_ +" s";
		s[6] = "Activation = "+activationName_;
		return s;
	}

	@Override
	public String getPanelName() {
		return PANE_NAME;
	}
	
	@Override
	public String[][] getAdditionalParameters() {
		String[][] s = new String[4][2];

		s[0][0] = KEY_USEACT;
		s[0][1] = String.valueOf(useActivation_);
		s[1][0] = KEY_STOPONMAX;
		s[1][1] = String.valueOf(stopOnMax_);
		s[2][0] = KEY_STOPDELAY;
		s[2][1] = String.valueOf(stopOnMaxDelay_);
		s[3][0] = KEY_ACTIVATION;
		s[3][1] = activationName_;
		
		return s;
	}
	
	@Override
	public void setAdditionalParameters(String[][] parameters) {
		if(parameters.length != 4 || parameters[0].length != 2) {
			throw new IllegalArgumentException("The parameters array has the wrong size: expected (4,2), got ("
					+ parameters.length + "," + parameters[0].length + ")");
		}
		
		useActivation_ = Boolean.parseBoolean(parameters[0][1]);
		stopOnMax_ = Boolean.parseBoolean(parameters[1][1]);
		stopOnMaxDelay_ = Integer.parseInt(parameters[2][1]);
		activationName_ = parameters[3][1];
	}

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	private int getActivationIndex(){
		final String[] acts = activationController_.getActivationPropertiesFriendlyName();
		int counter = 0;
		for(String act: acts){
			if(activationName_.equals(act)){
				return counter;
			}
			counter++;
		}
		return counter;
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path, Datastore.SaveMode savemode) throws IOException, InterruptedException {
		
		if(useActivation_){
			activationController_.initializeTask(getActivationIndex());
			activationController_.startTask();
		}
		
		stopAcq_ = false;
		interruptionRequested_ = false;
		running_ = true;

		// sequence settings builder
		Builder seqBuilder = new SequenceSettings.Builder();		
		seqBuilder.save(true);
		seqBuilder.timeFirst(true);
		seqBuilder.root(path);
		seqBuilder.prefix(name);
		seqBuilder.numFrames(params_.getNumberFrames());
		seqBuilder.intervalMs(params_.getIntervalMs()); 
		seqBuilder.shouldDisplayImages(true);
		seqBuilder.useAutofocus(false);
		seqBuilder.useChannels(false);
		seqBuilder.useCustomIntervals(false);
		seqBuilder.useFrames(true);
		seqBuilder.usePositionList(false);
		seqBuilder.useSlices(false);
		seqBuilder.saveMode(savemode);
		
		// runs acquisition
		AcquisitionManager acqManager = studio.acquisitions();
		Datastore store = acqManager.runAcquisitionWithSettings(seqBuilder.build(), false);
		
		// loop to check if it needs to be stopped or not
		while(studio.acquisitions().isAcquisitionRunning()) {
			
			// check if reached stop criterion
			if(useActivation_ && stopOnMax_ && activationController_.isCriterionReached()){
				Thread.sleep(1_000L * stopOnMaxDelay_);
												
				interruptAcquisition(studio);
				interruptionRequested_ = true; // avoid calling it multiple times
			}
					
			// checks if exit
			if(stopAcq_){
				interruptAcquisition(studio);
				interruptionRequested_ = true;
			}
			
			Thread.sleep(1_000);
		}

		studio.displays().closeDisplaysFor(store);
	
		store.close();
		
		if(useActivation_){
			activationController_.pauseTask();
			activationController_.initializeTask(getActivationIndex());
		}
		
		running_ = false;
	}

	private void interruptAcquisition(Studio studio) {
		if(interruptionRequested_ == false) { // avoid calling interrupt multiple times
			try {
				studio.acquisitions().abortAcquisition();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	@Override
	public void stopAcquisition() {
		stopAcq_ = true;
	}

	@Override
	public boolean isRunning() {
		return running_;
	}

	@Override
	public boolean skipPosition() {
		return false;
	}
	
	@Override
	public AcquisitionType getType() {
		return AcquisitionType.LOCALIZATION;
	}

	@Override
	public String getShortName() {
		return "Loc";
	}
}
