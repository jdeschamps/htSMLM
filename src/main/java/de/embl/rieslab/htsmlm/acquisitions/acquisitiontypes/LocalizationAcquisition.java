package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.*;

import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.htsmlm.ActivationPanel;
import org.micromanager.Studio;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.SequenceSettings.Builder;
import org.micromanager.acquisition.internal.DefaultAcquisitionManager;
import org.micromanager.data.Datastore;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;

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
	
	@SuppressWarnings("rawtypes")
	private TaskHolder activationTask_;
	private boolean useactivation_, stoponmax_, nullActivation_;
	private volatile boolean stopAcq_, running_;
	private int stoponmaxdelay_, activationIndex_;
	private boolean interruptionRequested_;
	
	@SuppressWarnings("rawtypes")
	public LocalizationAcquisition(TaskHolder activationtask, double exposure) {
		
		if(activationtask == null){
			nullActivation_ = true;
			useactivation_ = false;
		} else {
			nullActivation_ = false;
			useactivation_ = true;
			activationTask_ = activationtask;
		}
		
		stopAcq_ = false;
		running_ = false;
		interruptionRequested_ = false;
		stoponmax_ = true;
		stoponmaxdelay_ = 5;
		activationIndex_ = ((ActivationPanel) activationTask_).isActivation1() ? 0:1;

		params_ = new GenericAcquisitionParameters(AcquisitionType.LOCALIZATION, 
				exposure, 0, 3, 30000, new HashMap<String,String>(), new HashMap<String,String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		
		pane.setName(getPanelName());
		
		final JLabel exposurelab, waitinglab, numframelab, intervallab,waitonmaxlab, labelActivation;
		final JSpinner exposurespin, waitingspin, numframespin, intervalspin, waitonmaxspin;
		final JCheckBox activatecheck, stoponmaxcheck;
		final JComboBox<String> activationCombo;
		
		exposurelab = new JLabel(LABEL_EXPOSURE);
		waitinglab = new JLabel(LABEL_PAUSE);
		numframelab = new JLabel(LABEL_NUMFRAME);
		intervallab = new JLabel(LABEL_INTERVAL);
		waitonmaxlab = new JLabel(LABEL_MAXUVTIME);
		
		exposurespin = new JSpinner(new SpinnerNumberModel(Math.max(params_.getExposureTime(),1), 1, 10000000, 1));
		exposurespin.setName(LABEL_EXPOSURE);
		exposurespin.setToolTipText("Camera exposure (ms).");
		
		waitingspin = new JSpinner(new SpinnerNumberModel(params_.getWaitingTime(), 0, 10000000, 1)); 
		waitingspin.setName(LABEL_PAUSE);
		waitingspin.setToolTipText("Waiting time (s) to allow device state changes before this acquisition.");
		
		numframespin = new JSpinner(new SpinnerNumberModel(params_.getNumberFrames(), 1, 10000000, 1)); 
		numframespin.setName(LABEL_NUMFRAME);
		numframespin.setToolTipText("Number of frames.");
		
		intervalspin = new JSpinner(new SpinnerNumberModel(params_.getIntervalMs(), 0, 10000000, 1));
		intervalspin.setName(LABEL_INTERVAL);
		intervalspin.setToolTipText("Interval between frames (ms).");
		
		waitonmaxspin = new JSpinner(new SpinnerNumberModel(stoponmaxdelay_, 0, 10000, 1));
		waitonmaxspin.setName(LABEL_MAXUVTIME);
		waitonmaxspin.setToolTipText("Time (s) before stopping the acquisition after reaching the maximum activationCombo value.");

		activatecheck = new JCheckBox(LABEL_USEACTIVATION);
		activatecheck.setSelected(useactivation_);
		activatecheck.setEnabled(!nullActivation_);
		activatecheck.setName(LABEL_USEACTIVATION);
		activatecheck.setToolTipText("Use activationCombo during the acquisition.");
		
		stoponmaxcheck = new JCheckBox(LABEL_USESTOPONMAXUV);
		stoponmaxcheck.setSelected(stoponmax_);
		stoponmaxcheck.setEnabled(!nullActivation_);
		stoponmaxcheck.setName(LABEL_USESTOPONMAXUV);
		stoponmaxcheck.setToolTipText("Stop the acquisition after reaching the maximum activationCombo value.");

		String[] acts = {};
		try {
			acts = ((ActivationPanel) activationTask_).getPropertiesName();
		} catch (UnknownUIParameterException e) {
			e.printStackTrace();
		}

		final boolean canSwitchActivation = acts.length > 1;
		labelActivation = new JLabel(LABEL_ACTIVATION);
		activationCombo = new JComboBox<>(acts);
		activatecheck.setName(LABEL_ACTIVATION);
		if(!canSwitchActivation){
			activationCombo.setEnabled(false);
		}

		activatecheck.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			boolean selected = abstractButton.getModel().isSelected();
			if (!selected) {
				stoponmaxcheck.setSelected(false);
				stoponmaxcheck.setEnabled(false);
				waitonmaxspin.setValue(0);
				waitonmaxspin.setEnabled(false);
				activationCombo.setEnabled(false);
			} else {
				stoponmaxcheck.setEnabled(true);
				waitonmaxspin.setEnabled(true);

				if(canSwitchActivation)	activationCombo.setEnabled(true);
			}
		});
		
		stoponmaxcheck.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			boolean selected = abstractButton.getModel().isSelected();
			if (!selected) {
				waitonmaxspin.setValue(0);
				waitonmaxspin.setEnabled(false);
				activationCombo.setEnabled(false);
			} else {
				waitonmaxspin.setEnabled(true);
				waitonmaxspin.setEnabled(true);

				if(canSwitchActivation)	activationCombo.setEnabled(true);
			}
		});

		
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

		panelHolder[0][0].add(exposurelab);
		panelHolder[1][0].add(waitinglab);
		panelHolder[2][0].add(waitonmaxlab);

		panelHolder[0][1].add(exposurespin);
		panelHolder[1][1].add(waitingspin);
		panelHolder[2][1].add(waitonmaxspin);

		panelHolder[0][2].add(numframelab);
		panelHolder[1][2].add(intervallab);
		panelHolder[2][2].add(stoponmaxcheck);
		panelHolder[3][2].add(labelActivation);

		panelHolder[0][3].add(numframespin);
		panelHolder[1][3].add(intervalspin);
		panelHolder[2][3].add(activatecheck);
		panelHolder[3][3].add(activationCombo);
	
		return pane;
	}

	private void setUseActivation(boolean b){
		if(!nullActivation_){
			useactivation_  = b;
		} else {
			useactivation_  = false;
		}
	}

	private void setUseStopOnMaxUV(boolean b){
		stoponmax_ = b;
	}
	
	private void setUseStopOnMaxUVDelay(int delay){
		stoponmaxdelay_ = delay;
	}

	private void setActivation(int index){
		activationIndex_ = index;
	}

	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		if(pane.getName().equals(getPanelName())){
			Component[] pancomp = pane.getComponents();

			for(int j=0;j<pancomp.length;j++){
				if(pancomp[j] instanceof JPanel){
					Component[] comp = ((JPanel) pancomp[j]).getComponents();
					for(int i=0;i<comp.length;i++){
						if(!(comp[i] instanceof JLabel) && comp[i].getName() != null){
							if(comp[i].getName().equals(LABEL_EXPOSURE) && comp[i] instanceof JSpinner){
								params_.setExposureTime((Double) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_PAUSE) && comp[i] instanceof JSpinner){
								params_.setWaitingTime((Integer) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_NUMFRAME) && comp[i] instanceof JSpinner){
								params_.setNumberFrames((Integer) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_INTERVAL) && comp[i] instanceof JSpinner){
								params_.setIntervalMs((Double) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_USEACTIVATION) && comp[i] instanceof JCheckBox){
								this.setUseActivation(((JCheckBox) comp[i]).isSelected());
							}else if(comp[i].getName().equals(LABEL_USESTOPONMAXUV) && comp[i] instanceof JCheckBox){
								this.setUseStopOnMaxUV(((JCheckBox) comp[i]).isSelected());
							}else if(comp[i].getName().equals(LABEL_MAXUVTIME) && comp[i] instanceof JSpinner){
								this.setUseStopOnMaxUVDelay((Integer) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_ACTIVATION) && comp[i] instanceof JComboBox){
								this.setActivation(((JComboBox) comp[i]).getSelectedIndex());
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
		s[3] = "Use activation = "+useactivation_;
		s[4] = "Stop on max UV = "+stoponmax_;
		s[5] = "Stop on max delay = "+stoponmaxdelay_+" s";
		try {
			s[6] = "Activation = "+((ActivationPanel) activationTask_).getPropertiesName()[activationIndex_];
		} catch (UnknownUIParameterException e) {
			e.printStackTrace();
		}
		return s;
	}

	@Override
	public String getPanelName() {
		return PANE_NAME;
	}
	
	@Override
	public String[][] getAdditionalParameters() {
		String[][] s = new String[3][2];

		s[0][0] = KEY_USEACT;
		s[0][1] = String.valueOf(useactivation_);
		s[1][0] = KEY_STOPONMAX;
		s[1][1] = String.valueOf(stoponmax_);
		s[2][0] = KEY_STOPDELAY;
		s[2][1] = String.valueOf(stoponmaxdelay_);
		
		return s;
	}
	
	@Override
	public void setAdditionalParameters(String[][] parameters) {
		if(parameters.length != 3 || parameters[0].length != 2) {
			throw new IllegalArgumentException("The parameters array has the wrong size: expected (3,2), got ("
					+ parameters.length + "," + parameters[0].length + ")");
		}
		
		useactivation_ = Boolean.parseBoolean(parameters[0][1]);
		stoponmax_ = Boolean.parseBoolean(parameters[1][1]);
		stoponmaxdelay_ = Integer.parseInt(parameters[2][1]);
	}

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path, Datastore.SaveMode savemode) throws IOException, InterruptedException {
		
		if(useactivation_){
			Double[] initialization = {new Double(activationIndex_)};
			activationTask_.initializeTask(initialization);
			activationTask_.resumeTask();
		}
		
		stopAcq_ = false;
		interruptionRequested_ = false;
		running_ = true;
		
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
		
		// loop to check if needs to be stopped or not
		while(studio.acquisitions().isAcquisitionRunning()) {
			
			// check if reached stop criterion
			if(useactivation_ && stoponmax_ && activationTask_.isCriterionReached()){
				Thread.sleep(1000*stoponmaxdelay_);
												
				interruptAcquisition(studio);
				interruptionRequested_ = true;
			}
					
			// checks if exit
			if(stopAcq_){
				interruptAcquisition(studio);
				interruptionRequested_ = true;
			}
			
			Thread.sleep(1000);
		}

		studio.displays().closeDisplaysFor(store);
	
		store.close();
		
		if(useactivation_){			
			activationTask_.pauseTask();
			Double[] initialization = {new Double(activationIndex_)};
			activationTask_.initializeTask(initialization);
		}
		
		running_ = false;
	}

	private void interruptAcquisition(Studio studio) {
		if(interruptionRequested_ == false) {
			try {
				// not pretty but I could not find any other way to stop the acquisition without getting a JDialog popping up and requesting user input
				((DefaultAcquisitionManager) studio.acquisitions()).getAcquisitionEngine().stop(true);
				
				//((DefaultAcquisitionManager) studio.acquisitions()).haltAcquisition();
				//((DefaultAcquisitionManager) studio.acquisitions()).getAcquisitionEngine().abortRequested();
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
