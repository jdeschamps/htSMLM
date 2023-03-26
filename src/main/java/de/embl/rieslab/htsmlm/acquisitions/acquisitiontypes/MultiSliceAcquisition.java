package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mmcorej.Configuration;
import mmcorej.PropertySetting;
import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.Studio;
import org.micromanager.data.*;
import org.micromanager.data.Coords.Builder;
import org.micromanager.display.DisplayWindow;

import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.acquisitions.AcquisitionController;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.SinglePropertyFilter;
import de.embl.rieslab.htsmlm.activation.ActivationController;
import de.embl.rieslab.htsmlm.activation.processor.ActivationContext;
import de.embl.rieslab.htsmlm.activation.processor.ActivationProcessor;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.internal.MMStudio;

/**
 * Multi-slice localization acquisition.
 *
 * This acquisition performs times series at multiple z positions, one
 * after the other, following a user-defined number of z-steps and loops.
 * At each slice, the acquisition can run the activation script in parallel.
 * The activation script can also be run solely on a specific step.
 *
 */
public class MultiSliceAcquisition implements Acquisition {

	private GenericAcquisitionParameters params_;

	private final static String PANE_NAME = "Localization panel";
	private final static String LABEL_EXPOSURE = "Exposure (ms):";
	private final static String LABEL_PAUSE = "Pause (s):";
	private final static String LABEL_NUMFRAME = "Number of frames:";
	private final static String LABEL_INTERVAL = "Interval (ms):";
	private final static String LABEL_USEACTIVATION = "Use activation";
	private final static String LABEL_USESTOPONMAXUV = "Stop on max";
	private final static String LABEL_MAXUVTIME = "Stop on max delay (s):";
	private final static String LABEL_NLOOPS = "Number of loops";
	private final static String LABEL_NSLICES = "Number of slices";
	private final static String LABEL_DELTAZ = "Z difference (um)";
	private final static String LABEL_LOOPS = "N loops / N slices / \u0394Z (um)";
	private final static String LABEL_ZDEVICE = "Moving device:";
	private final static String LABEL_DISABLE_FL = "disable focus-lock";
	private final static String LABEL_FL_AT_Z0 = "only at Z0";
	private final static String LABEL_SLICEST = "Slice St";
	private final static String LABEL_ACTATST = "Only activate at slice:";
	private final static String LABEL_ACTIVATION = "Activation:";

	public final static String KEY_USEACT = "Use activation?";
	public final static String KEY_STOPONMAX = "Stop on max?";
	public final static String KEY_STOPDELAY = "Stop on max delay";
	public final static String KEY_NLOOPS = "N loops";
	public final static String KEY_NSLICES = "N slices";
	public final static String KEY_DELTAZ = "Delta z";
	public final static String KEY_ZDEVICE = "Z stage";
	public final static String KEY_DISABLE_FL = "Disable focus-lock";
	public final static String KEY_FLATZ0 = "Use focus-lock at Z0";
	public final static String KEY_SLICE_SA = "Slice Sa";
	public final static String KEY_ACT_AT_SA = "Activation at Sa";
	public final static String KEY_ACTIVATION = "Activation:";
	
	public final static int NUM_KEYS = 12;

	private final ActivationController activationController_;
	private final AcquisitionController acquisitionController_;
	
	private boolean useActivation_, stopOnMax_, noActivation_;
	private volatile boolean stopAcq_, running_;
	private int stopOnMaxDelay_;

	// UI property
	private TwoStateUIProperty zStabilizationProperty_;
	private String zDevice_;
	private String[] zDevicesName_;
	private double deltaZ;
	private int nSlices, nLoops, sliceSt;
	private boolean focusLockAtZ0_, disableFocusLock_, actAtSa;
	private String activationName_ = "None";

	public MultiSliceAcquisition(AcquisitionController acquisitionController,
								 double exposure, 
								 String[] zdevices, 
								 String defaultzdevice,
			TwoStateUIProperty zStabilizationProperty) {

		if (acquisitionController.getActivationController() == null) {
			throw new IllegalArgumentException("Activation controller cannot be null.");
		}
		activationController_ = acquisitionController.getActivationController();
		acquisitionController_ = acquisitionController;

		// if no activation property is allocated
		noActivation_ = activationController_.getActivationPropertiesFriendlyName().length == 0;

		// state booleans
		stopAcq_ = false;
		running_ = false;

		// default values
		actAtSa = false;
		useActivation_ = true;
		stopOnMax_ = true;
		stopOnMaxDelay_ = 5;
		deltaZ = 0.3;
		nSlices = 4;
		nLoops = 5;
		sliceSt = 0;

		// z stabilization related
		if (zStabilizationProperty != null && zStabilizationProperty.isAssigned()) {
			zStabilizationProperty_ = zStabilizationProperty;
			disableFocusLock_ = true;
		} else {
			zStabilizationProperty_ = null;
			disableFocusLock_ = false;
		}
		focusLockAtZ0_ = false;

		// z devices
		zDevice_ = defaultzdevice;
		zDevicesName_ = zdevices;

		params_ = new GenericAcquisitionParameters(AcquisitionType.MULTISLICELOC, exposure, 0, 3, 50000,
				new HashMap<String, String>(), new HashMap<String, String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();

		pane.setName(getPanelName());

		final JLabel exposureLabel, waitingLabel, numFrameLabel, intervalLabel, waitOnMaxLabel, activationLabel;
		final JLabel zDeviceLabel, loopLabel;
		final JSpinner numberSlice, deltaZ, numberLoops, sliceSaSpin;
		final JSpinner exposureSpin, waitingSpin, numFrameSpin, intervalSpin, waitOnMaxSpin;
		final JCheckBox activateCheck, stopOnMaxCheck, disableFocusLock, flOnlyAtZ0, activateAtSa;
		final JComboBox<String> activationCombo;

		exposureLabel = new JLabel(LABEL_EXPOSURE);
		waitingLabel = new JLabel(LABEL_PAUSE);
		numFrameLabel = new JLabel(LABEL_NUMFRAME);
		intervalLabel = new JLabel(LABEL_INTERVAL);
		waitOnMaxLabel = new JLabel(LABEL_MAXUVTIME);

		exposureSpin = new JSpinner(new SpinnerNumberModel(Math.max(params_.getExposureTime(), 1), 1, 10000000, 1));
		exposureSpin.setName(LABEL_EXPOSURE);
		exposureSpin.setToolTipText("Camera exposure (ms).");

		waitingSpin = new JSpinner(new SpinnerNumberModel(params_.getWaitingTime(), 0, 10000000, 1));
		waitingSpin.setName(LABEL_PAUSE);
		waitingSpin.setToolTipText("Waiting time (s) to allow device state changes before this acquisition.");

		numFrameSpin = new JSpinner(new SpinnerNumberModel(params_.getNumberFrames(), 1, 10000000, 1));
		numFrameSpin.setName(LABEL_NUMFRAME);
		numFrameSpin.setToolTipText("Number of frames per slice.");

		intervalSpin = new JSpinner(new SpinnerNumberModel(params_.getIntervalMs(), 0, 10000000, 1));
		intervalSpin.setName(LABEL_INTERVAL);
		intervalSpin.setToolTipText("Interval between frames (ms).");

		waitOnMaxSpin = new JSpinner(new SpinnerNumberModel(stopOnMaxDelay_, 0, 10000, 1));
		waitOnMaxSpin.setName(LABEL_MAXUVTIME);
		waitOnMaxSpin.setToolTipText(
				"Time (s) before stopping the acquisition after reaching the maximum activation value.");

		//// activation
		activateCheck = new JCheckBox(LABEL_USEACTIVATION);
		activateCheck.setSelected(useActivation_);
		activateCheck.setName(LABEL_USEACTIVATION);
		activateCheck.setToolTipText("Use activation during the acquisition.");

		stopOnMaxCheck = new JCheckBox(LABEL_USESTOPONMAXUV);
		stopOnMaxCheck.setSelected(stopOnMax_);
		stopOnMaxCheck.setName(LABEL_USESTOPONMAXUV);
		stopOnMaxCheck.setToolTipText("Stop the acquisition after reaching the maximum activation value.");
		stopOnMaxCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (!selected) {
					waitOnMaxSpin.setValue(0);
					waitOnMaxSpin.setEnabled(false);
				} else {
					waitOnMaxSpin.setEnabled(true);
				}
			}
		});

		// activate at slice Sa
		activateAtSa = new JCheckBox(LABEL_ACTATST);
		activateAtSa.setName(LABEL_ACTATST);
		activateAtSa.setSelected(actAtSa);
		sliceSaSpin = new JSpinner(new SpinnerNumberModel(sliceSt, 0, nSlices - 1, 1));
		sliceSaSpin.setName(LABEL_SLICEST);

		// choice of activation
		final String[] activationPropertiesName = activationController_.getActivationPropertiesFriendlyName();
		activationLabel = new JLabel(LABEL_ACTIVATION);
		activationCombo = new JComboBox<>(activationPropertiesName);
		activationCombo.setName(LABEL_ACTIVATION);
		if(activationPropertiesName.length != 2){
			// either 0 or 1
			activationCombo.setEnabled(false); // no need for combobox

			// if none, then disable all related parameters
			if(activationPropertiesName.length == 0){
				stopOnMaxCheck.setEnabled(false);
				waitOnMaxSpin.setEnabled(false);
				activateCheck.setEnabled(false);
				activateAtSa.setEnabled(false);
				sliceSaSpin.setEnabled(false);

				useActivation_ = false;
			}
		} else {
			if (Arrays.asList(activationPropertiesName).contains(activationName_)) {
				activationCombo.setSelectedItem(activationName_);
			}
		}

		// activate
		activateCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (!selected) {
					stopOnMaxCheck.setEnabled(false);
					stopOnMaxCheck.setSelected(false);
					waitOnMaxSpin.setValue(0);
					waitOnMaxSpin.setEnabled(false);
					activateAtSa.setEnabled(false);
					activateAtSa.setSelected(false);
					sliceSaSpin.setEnabled(false);
				} else {
					stopOnMaxCheck.setEnabled(true);
					activateAtSa.setEnabled(true);
					sliceSaSpin.setEnabled(true);
				}
			}
		});

		//// z part
		zDeviceLabel = new JLabel(LABEL_ZDEVICE);
		loopLabel = new JLabel(LABEL_LOOPS);

		numberLoops = new JSpinner(new SpinnerNumberModel(nLoops, 1, 100, 1));
		numberLoops.setName(LABEL_NLOOPS);
		numberLoops.setToolTipText("Number of loops through the slices.");

		numberSlice = new JSpinner(new SpinnerNumberModel(nSlices, 2, 100, 1));
		numberSlice.setName(LABEL_NSLICES);
		numberSlice.setToolTipText("Number of slices in the stack.");
		numberSlice.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int nSlices = (int) spinner.getValue();

				// update the slice Sa spinner
				int sliceSa = Math.min((int) sliceSaSpin.getValue(), nSlices - 1);
				sliceSaSpin.setModel(new SpinnerNumberModel(sliceSa, 0, nSlices - 1, 1));
			}
		});

		deltaZ = new JSpinner(new SpinnerNumberModel(this.deltaZ, -1000, 1000, 0.5));
		deltaZ.setName(LABEL_DELTAZ);
		deltaZ.setToolTipText("Distance (um) between each slice.");

		JComboBox<String> zDevices = new JComboBox<String>(zDevicesName_);
		zDevices.setSelectedItem(zDevice_);
		zDevices.setName(LABEL_ZDEVICE);
		zDevices.setToolTipText("Device performing the multi-slice acquisition.");

		disableFocusLock = new JCheckBox(LABEL_DISABLE_FL);
		disableFocusLock.setSelected(disableFocusLock_);
		disableFocusLock.setName(LABEL_DISABLE_FL);
		disableFocusLock.setEnabled(zStabilizationProperty_ != null);
		disableFocusLock.setToolTipText("Check to disable focus stabilization (if applicable).");

		flOnlyAtZ0 = new JCheckBox(LABEL_FL_AT_Z0);
		flOnlyAtZ0.setName(LABEL_FL_AT_Z0);
		flOnlyAtZ0.setSelected(focusLockAtZ0_);
		flOnlyAtZ0.setEnabled(!disableFocusLock_ && zStabilizationProperty_ != null);
		disableFocusLock
				.setToolTipText("Check to use the focus stabilization only during the first slice of every loop.");

		disableFocusLock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (selected) {
					flOnlyAtZ0.setEnabled(false);
					flOnlyAtZ0.setSelected(false);
				} else {
					flOnlyAtZ0.setEnabled(true);
				}
			}
		});

		// arrange elements in the layout
		int nrow = 6;
		int ncol = 4;
		JPanel[][] panelHolder = new JPanel[nrow][ncol];
		pane.setLayout(new GridLayout(nrow, ncol));

		for (int m = 0; m < nrow; m++) {
			for (int n = 0; n < ncol; n++) {
				panelHolder[m][n] = new JPanel();
				pane.add(panelHolder[m][n]);
			}
		}

		panelHolder[0][0].add(exposureLabel);
		panelHolder[0][1].add(exposureSpin);
		panelHolder[0][2].add(numFrameLabel);
		panelHolder[0][3].add(numFrameSpin);

		panelHolder[1][0].add(waitingLabel);
		panelHolder[1][1].add(waitingSpin);
		panelHolder[1][2].add(intervalLabel);
		panelHolder[1][3].add(intervalSpin);

		panelHolder[2][0].add(waitOnMaxLabel);
		panelHolder[2][1].add(waitOnMaxSpin);
		panelHolder[2][2].add(stopOnMaxCheck);
		panelHolder[2][3].add(activateCheck);

		panelHolder[3][0].add(activationLabel);
		panelHolder[3][1].add(activationCombo);
		panelHolder[3][2].add(activateAtSa);
		panelHolder[3][3].add(sliceSaSpin);

		panelHolder[4][0].add(zDeviceLabel);
		panelHolder[4][1].add(zDevices);
		panelHolder[4][2].add(disableFocusLock);
		panelHolder[4][3].add(flOnlyAtZ0);

		panelHolder[5][0].add(loopLabel);
		panelHolder[5][1].add(numberLoops);
		panelHolder[5][2].add(numberSlice);
		panelHolder[5][3].add(deltaZ);

		return pane;
	}

	/**
	 * Whether to use the activation.
	 * @param useActivation True if activation should be used.
	 */
	public void setUseActivation(boolean useActivation) {
		if (!noActivation_) {
			useActivation_ = useActivation;
		} else {
			useActivation_ = false;
		}
	}

	/**
	 * Whether to use the stop on max activation.
	 * @param stopOnMaxUV
	 */
	public void setUseStopOnMaxUV(boolean stopOnMaxUV) {
		stopOnMax_ = stopOnMaxUV;
	}

	public void setUseStopOnMaxUVDelay(int delay) {
		stopOnMaxDelay_ = delay;
	}

	private void setActivation(String act){
		activationName_ = act;
	}
	
	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		if (pane.getName().equals(getPanelName())) {
			Component[] panelComponents = pane.getComponents();

			for (int j = 0; j < panelComponents.length; j++) {
				if (panelComponents[j] instanceof JPanel) {
					Component[] comp = ((JPanel) panelComponents[j]).getComponents();
					for (int i = 0; i < comp.length; i++) {
						if (!(comp[i] instanceof JLabel) && comp[i].getName() != null) {
							if (comp[i].getName().equals(LABEL_EXPOSURE) && comp[i] instanceof JSpinner) {
								params_.setExposureTime((Double) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_PAUSE) && comp[i] instanceof JSpinner) {
								params_.setWaitingTime((Integer) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_NUMFRAME) && comp[i] instanceof JSpinner) {
								params_.setNumberFrames((Integer) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_INTERVAL) && comp[i] instanceof JSpinner) {
								params_.setIntervalMs((Double) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_USEACTIVATION) && comp[i] instanceof JCheckBox) {
								this.setUseActivation(((JCheckBox) comp[i]).isSelected());
							} else if (comp[i].getName().equals(LABEL_USESTOPONMAXUV) && comp[i] instanceof JCheckBox) {
								this.setUseStopOnMaxUV(((JCheckBox) comp[i]).isSelected());
							} else if (comp[i].getName().equals(LABEL_MAXUVTIME) && comp[i] instanceof JSpinner) {
								this.setUseStopOnMaxUVDelay((Integer) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_ZDEVICE) && comp[i] instanceof JComboBox) {
								zDevice_ = ((String) ((JComboBox<String>) comp[i]).getSelectedItem());
							} else if (comp[i].getName().equals(LABEL_FL_AT_Z0) && comp[i] instanceof JCheckBox) {
								focusLockAtZ0_ = ((JCheckBox) comp[i]).isSelected();
							} else if (comp[i].getName().equals(LABEL_DISABLE_FL) && comp[i] instanceof JCheckBox) {
								disableFocusLock_ = ((JCheckBox) comp[i]).isSelected();
							} else if (comp[i].getName().equals(LABEL_NSLICES) && comp[i] instanceof JSpinner) {
								nSlices = ((Integer) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_NLOOPS) && comp[i] instanceof JSpinner) {
								nLoops = ((Integer) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_DELTAZ) && comp[i] instanceof JSpinner) {
								deltaZ = ((Double) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_SLICEST) && comp[i] instanceof JSpinner) {
								sliceSt = ((Integer) ((JSpinner) comp[i]).getValue());
							} else if (comp[i].getName().equals(LABEL_ACTATST) && comp[i] instanceof JCheckBox) {
								actAtSa = ((JCheckBox) comp[i]).isSelected();
							} else if(comp[i].getName().equals(LABEL_ACTIVATION) && comp[i] instanceof JComboBox){
								this.setActivation((String) ((JComboBox<String>) comp[i]).getSelectedItem());
							}
						}
					}
				}
			}
		}

		if (sliceSt >= nSlices) {
			sliceSt = nSlices - 1;
		}
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		if (zStabilizationProperty_ == null) {
			return new NoPropertyFilter();
		}
		return new SinglePropertyFilter(zStabilizationProperty_.getPropertyLabel());
	}

	@Override
	public String[] getHumanReadableSettings() {
		String[] s = new String[15];
		s[0] = "Exposure = " + params_.getExposureTime() + " ms";
		s[1] = "Interval = " + params_.getIntervalMs() + " ms";
		s[2] = "Number of frames = " + params_.getNumberFrames();
		s[3] = "Use activation = " + useActivation_;
		s[4] = "Stop on max UV = " + stopOnMax_;
		s[5] = "Stop on max delay = " + stopOnMaxDelay_ + " s";
		s[6] = "Focus stage = " + zDevice_;
		s[7] = "Use FL at St = " + focusLockAtZ0_;
		s[8] = "Disable focus-lock = " + disableFocusLock_;
		s[9] = "Number of loops = " + nLoops;
		s[10] = "Number of slices = " + nSlices;
		s[11] = "Z difference = " + deltaZ + " um";
		s[12] = "Slice St = " + sliceSt;
		s[13] = "Activate at St = " + actAtSa;
		s[14] = "Activation = "+activationName_;
		return s;
	}

	@Override
	public String getPanelName() {
		return PANE_NAME;
	}

	@Override
	public String[][] getAdditionalParameters() {
		String[][] parameters = new String[NUM_KEYS][2];

		parameters[0][0] = KEY_USEACT;
		parameters[0][1] = String.valueOf(useActivation_);
		parameters[1][0] = KEY_STOPONMAX;
		parameters[1][1] = String.valueOf(stopOnMax_);
		parameters[2][0] = KEY_STOPDELAY;
		parameters[2][1] = String.valueOf(stopOnMaxDelay_);
		parameters[3][0] = KEY_ZDEVICE;
		parameters[3][1] = zDevice_;
		parameters[4][0] = KEY_FLATZ0;
		parameters[4][1] = String.valueOf(focusLockAtZ0_);
		parameters[5][0] = KEY_DISABLE_FL;
		parameters[5][1] = String.valueOf(disableFocusLock_);
		parameters[6][0] = KEY_NLOOPS;
		parameters[6][1] = String.valueOf(nLoops);
		parameters[7][0] = KEY_NSLICES;
		parameters[7][1] = String.valueOf(nSlices);
		parameters[8][0] = KEY_DELTAZ;
		parameters[8][1] = String.valueOf(deltaZ);
		parameters[9][0] = KEY_SLICE_SA;
		parameters[9][1] = String.valueOf(sliceSt);
		parameters[10][0] = KEY_ACT_AT_SA;
		parameters[10][1] = String.valueOf(actAtSa);
		parameters[11][0] = KEY_ACTIVATION;
		parameters[11][1] = activationName_;

		return parameters;
	}

	@Override
	public void setAdditionalParameters(String[][] parameters) {
		if (parameters.length != NUM_KEYS || parameters[0].length != 2) {
			throw new IllegalArgumentException("The parameters array has the wrong size: expected (11,2), got ("
					+ parameters.length + "," + parameters[0].length + ")");
		}

		useActivation_ = Boolean.parseBoolean(parameters[0][1]);
		stopOnMax_ = Boolean.parseBoolean(parameters[1][1]);
		stopOnMaxDelay_ = Integer.parseInt(parameters[2][1]);
		zDevice_ = parameters[3][1];
		focusLockAtZ0_ = Boolean.parseBoolean(parameters[4][1]);
		disableFocusLock_ = Boolean.parseBoolean(parameters[5][1]);
		nLoops = Integer.parseInt(parameters[6][1]);
		nSlices = Integer.parseInt(parameters[7][1]);
		deltaZ = Double.parseDouble(parameters[8][1]);
		sliceSt = Integer.parseInt(parameters[9][1]);
		actAtSa = Boolean.parseBoolean(parameters[10][1]);
		activationName_ = parameters[11][1];
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
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	private void stabilizeFocus(boolean b) {
		if (zStabilizationProperty_ != null) {
			if (b) {
				zStabilizationProperty_.setPropertyValue(TwoStateUIProperty.getOnStateLabel());
			} else {
				zStabilizationProperty_.setPropertyValue(TwoStateUIProperty.getOffStateLabel());
			}
		}
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path, Datastore.SaveMode savemode)
			throws InterruptedException, IOException {

		CMMCore core = studio.core();

		stopAcq_ = false;
		running_ = true;

		if (disableFocusLock_ || (!disableFocusLock_ && focusLockAtZ0_)) {
			stabilizeFocus(false);
		} else {
			stabilizeFocus(true);
		}

		if (useActivation_) {
			activationController_.initializeTask(getActivationIndex());
			activationController_.startTask();
		}

		double z0 = 0;
		try {
			z0 = core.getPosition(zDevice_);
		} catch (Exception e1) {
			running_ = false;
			e1.printStackTrace();
		}

		if (running_) {
			for (int i = 0; i < nLoops; i++) {
				for (int j = 0; j < nSlices; j++) {
					if (useActivation_ && ((actAtSa && j == sliceSt) || !actAtSa)) {
						activationController_.startTask();
					}

					// set z
					double z = z0 + j * deltaZ;

					try {
						// moves the stage
						core.setPosition(zDevice_, z);

						Thread.sleep(1000);

						if (j == 0 && !disableFocusLock_ && focusLockAtZ0_) {
							stabilizeFocus(true);

							// wait to give time for the stabilization to settle
							Thread.sleep(3000);
						}

						// sets-up name
						// seqBuilder.prefix("L"+i+"S"+j+"_"+name);

						if (stopAcq_) {
							studio.logs().logDebugMessage("[htSMLM] Multislice interruption before slice " + j + ".");
							stopAcquisition();
						}

						// runs acquisition
						// AcquisitionManager acqManager = studio.acquisitions();
						// acqManager.setAcquisitionSettings(seqBuilder.build());
						// writer.println("Nframes: "+acqManager.getAcquisitionSettings().numFrames());

						// Datastore store = acqManager.runAcquisitionNonblocking();

						// creates directory
						String acqName = "L" + i + "S" + j + "_" + name;
						String final_path = path + File.separator + acqName;
						String newAcqName = acqName;
						while (new File(path + File.separator + newAcqName).exists()) {
							newAcqName = incrementName(newAcqName);
							final_path = path + File.separator + newAcqName;
						}

						// creates runnable acquisition and starts a thread associated with the runnable
						RunnableAcq acqThread = new RunnableAcq(studio, savemode, final_path, path, newAcqName);
						new Thread(acqThread).start();

						// wait 1sec to make sure we don't miss the start of the experiment
						Thread.sleep(1000);

						// loops to check if needs to be stopped or not
						while (acqThread.isRunning()) {

							// checks if reached stop criterion
							if (useActivation_ && stopOnMax_ && (!actAtSa || j == sliceSt)
									&& activationController_.isCriterionReached()) {
								Thread.sleep(1_000L * stopOnMaxDelay_);

								stopAcquisition();
							}

							// checks if exit requested
							if (stopAcq_) {
								studio.logs().logDebugMessage("[htSMLM] Multislice interruption during slice " + j + ".");
								break;
							}

							Thread.sleep(1_000);
						}

						// disable focus-lock and update z0
						if (j == 0 && !disableFocusLock_ && focusLockAtZ0_) {
							// updates z0 for the next iterations
							z0 = core.getPosition(zDevice_);

							stabilizeFocus(false);
						}

						// pause activation
						if (useActivation_ && ((actAtSa && j == sliceSt) || !actAtSa)) {
							activationController_.pauseTask();
						}

					} catch (Exception e) {
						running_ = false;
						studio.logs().logDebugMessage("[htSMLM] Failed to move stage or query position after slice " + j + ".");
						e.printStackTrace();
					}

					if (stopAcq_) {
						studio.logs().logDebugMessage("[htSMLM] Multislice interruption after slice " + j + ".");
						break;
					}
				}

				if (stopAcq_) {
					studio.logs().logDebugMessage("[htSMLM] Multislice interruption after loop " + i + ".");
					break;
				}
			}
		}

		// go back to position z0
		try {
			core.setPosition(zDevice_, z0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// if focus locked disabled, set to ON state
		if (zStabilizationProperty_ != null && zStabilizationProperty_.isAssigned()) {
			if (zStabilizationProperty_.isOffState(zStabilizationProperty_.getPropertyValue())) {
				stabilizeFocus(true);
			}
		}
		
		if (useActivation_) {
			activationController_.pauseTask();
			activationController_.initializeTask(getActivationIndex());
		}

		running_ = false;
	}

	/*
	 * private void interruptAcquisition(Studio studio) {
	 * //if(interruptionRequested_ == false) { try { // not pretty but I could not
	 * find any other way to stop the acquisition without getting a JDialog popping
	 * up and requesting user input ((DefaultAcquisitionManager)
	 * studio.acquisitions()).getAcquisitionEngine().stop(true); stopAcquisition();
	 * //((DefaultAcquisitionManager)
	 * studio.acquisitions()).getAcquisitionEngine().abortRequested(); } catch
	 * (Exception e) { e.printStackTrace(); } //} }
	 */

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
		return AcquisitionType.MULTISLICELOC;
	}

	@Override
	public String getShortName() {
		return "MultiSliceLoc";
	}

	// does not belong here, should go into a utility class
	private static String incrementName(String name) {
		String newName;
		int ind = name.length();
		while (EmuUtils.isInteger(name.substring(ind - 1, ind))) {
			ind--;
		}

		if (ind == name.length()) {
			if (name.charAt(ind - 1) == '_') {
				newName = name + "1";
			} else {
				newName = name + "_1";
			}
		} else {
			if (name.charAt(ind - 1) == '_') {
				int i = Integer.parseInt(name.substring(ind)) + 1;
				newName = name.substring(0, ind) + i;
			} else {
				newName = name + "_1";
			}
		}
		return newName;
	}

	private class RunnableAcq implements Runnable {

		private final Studio studio;
		private final Datastore.SaveMode saveMode;
		private final String path, directory, name;
		private boolean isRunning_;

		public RunnableAcq(Studio studio, Datastore.SaveMode saveMode, String path, String directory, String name) {
			this.studio = studio;
			this.saveMode = saveMode;
			this.path = path;
			this.directory = directory;
			this.name = name;

			isRunning_ = false;
		}

		public boolean isRunning() {
			return isRunning_;
		}

		@Override
		public void run() {
			isRunning_ = true;

			CMMCore core = studio.core();

			try {
				// creates store
				Datastore store;
				
				// get instance of image processor
				final ActivationProcessor imageProcessor = ActivationProcessor.getInstance();

				// TODO in case users want to generate a metadata.txt, this will not be taken into account here
				if (Datastore.SaveMode.MULTIPAGE_TIFF == saveMode) {
					store = studio.data().createMultipageTIFFDatastore(path, false, false);
				} else {
					store = studio.data().createSinglePlaneTIFFSeriesDatastore(path);
				}

				// set summary metadata
				SummaryMetadata summaryMetaData = generateSummaryMetadata(studio, directory, name, params_.getNumberFrames());
				store.setSummaryMetadata(summaryMetaData);
				final ActivationContext processorContext = new ActivationContext(summaryMetaData);

				// display and coordinate builder
				DisplayWindow display = studio.displays().createDisplay(store);
				Builder cb = studio.data().coordsBuilder().z(0).c(0).p(0).t(0);

				core.startSequenceAcquisition(params_.getNumberFrames(), params_.getIntervalMs(), true);
				Metadata metadata = studio.data().metadataBuilder().build();

				int curFrame = 0;
				try {
					while (!stopAcq_
							&& (core.getRemainingImageCount() > 0 || core.isSequenceRunning(core.getCameraDevice()))) {
						if (core.getRemainingImageCount() > 0) {
							TaggedImage tagged = core.popNextTaggedImage();

							// convert to an Image at the desired time point
							Image image = studio.data().convertTaggedImage(tagged, cb.t(curFrame).build(), generateMetadata(studio, metadata));
							
							// pass image to the processor for the activation
							imageProcessor.processImage(image, processorContext);

							// store image
							store.putImage(image);
							
							// increment frame
							curFrame++;
						} else {
							core.sleep(5);
						}
					}

					if (stopAcq_) {
						core.stopSequenceAcquisition();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				// close store
				display.close();
				store.close();
			} catch (Exception e) {
				e.printStackTrace();
				studio.logs().logDebugMessage("[htSMLM] Acquisition failed.");
			}

			isRunning_ = false;
		}
	}

	private SummaryMetadata generateSummaryMetadata(Studio studio, String path, String name, int n){//Map<String, Object> properties){

		SummaryMetadata defaultSM = studio.acquisitions().generateSummaryMetadata();
		SummaryMetadata.Builder smBuilder = defaultSM.copyBuilder();

		// stacks dimensions
		Coords coords = defaultSM.getIntendedDimensions();
		smBuilder.intendedDimensions(coords.copyBuilder().t(n).c(1).p(1).z(1).build());

		// others
		smBuilder.prefix(name);
		smBuilder.directory(path);

		return smBuilder.build();
	}

	/*
	 * adapted from DefaultAcquisitionManager
	 */
	private Metadata generateMetadata(Studio studio, Metadata metadata) {
		String camera = studio.core().getCameraDevice();

		MMStudio mmstudio = (MMStudio) studio;
		Metadata.Builder result = metadata.copyBuilderWithNewUUID()
				.camera(camera)
				.receivedTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(new Date()))
				.pixelSizeUm(mmstudio.cache().getPixelSizeUm())
				.pixelSizeAffine(mmstudio.cache().getPixelSizeAffine())
				.xPositionUm(mmstudio.cache().getStageX())
				.yPositionUm(mmstudio.cache().getStageY())
				.zPositionUm(mmstudio.cache().getStageZ())
				.bitDepth(mmstudio.cache().getImageBitDepth())
				.positionName("Position "+acquisitionController_.getCurrentPositionIndex());

		try {
			String binning = studio.core().getPropertyFromCache(camera, "Binning");
			if (binning.contains("x")) {
				// HACK: assume the binning parameter is e.g. "1x1" or "2x2" and
				// just take the first number.
				try {
					result.binning(Integer.parseInt(binning.split("x", 2)[0]));
				}
				catch (NumberFormatException e) {
					studio.logs().logError("Unable to determine binning from " + binning);
				}
			}
			else {
				try {
					result.binning(Integer.parseInt(binning));
				}
				catch (NumberFormatException e) {
					studio.logs().logError("Unable to determine binning from " + binning);
				}
			}
		}
		catch (Exception ignored) {
			// Again, this can fail if there is no camera.
		}

		try
		{
			final double expo = mmstudio.core().getExposure();
			result.exposureMs( expo );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		try
		{
			final Rectangle r = mmstudio.core().getROI();
			result.roi( r );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		PropertyMap.Builder scopeBuilder = PropertyMaps.builder();
		Configuration config = studio.core().getSystemStateCache();
		for (long i = 0; i < config.size(); ++i) {
			PropertySetting setting = null;
			try
			{
				setting = config.getSetting(i);

				// NOTE: this key format chosen to match that used by the current
				// acquisition engine.
				scopeBuilder.putString(setting.getDeviceLabel() + "-" + setting.getPropertyName(),
						setting.getPropertyValue());
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		result.scopeData(scopeBuilder.build());

		return result.build();
	}
}
