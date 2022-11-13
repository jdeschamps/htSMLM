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
	private final static String LABEL_NUMB = "N loops / N slices / \u0394Z (um)";
	private final static String LABEL_ZDEVICE = "Moving device:";
	private final static String LABEL_DISABLEFL = "disable focus-lock";
	private final static String LABEL_FLATZ0 = "only at Z0";
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
	public final static String KEY_DISABLEFL = "Disable focus-lock";
	public final static String KEY_FLATZ0 = "Use focus-lock at Z0";
	public final static String KEY_SLICEST = "Slice St";
	public final static String KEY_ACTATST = "Activation at St";
	public final static String KEY_ACTIVATION = "Activation:";
	
	public final static int NUM_KEYS = 12;

	private final ActivationController activationController_;
	private final AcquisitionController acquisitionController_;
	
	private boolean useactivation_, stoponmax_, nullActivation_;
	private volatile boolean stopAcq_, running_;
	private int stoponmaxdelay_;

	// UI property
	private TwoStateUIProperty zstabProperty_;
	private String zdevice_;
	private String[] zdevices_;
	private double deltaZ;
	private int nSlices, nLoops, sliceSt;
	private boolean focusLockAtZ0_, disableFocusLock_, actAtSt;
	private String activationName_ = "None";

	public MultiSliceAcquisition(AcquisitionController acquisitionController,
								 double exposure, 
								 String[] zdevices, 
								 String defaultzdevice,
			TwoStateUIProperty zStabilizationProperty) {

		if (acquisitionController.getActivationController() == null) {
			nullActivation_ = true;
			useactivation_ = false;
			activationController_ = null;
		} else {
			nullActivation_ = false;
			useactivation_ = true;
			activationController_ = acquisitionController.getActivationController();
		}
		acquisitionController_ = acquisitionController;

		stopAcq_ = false;
		running_ = false;
		actAtSt = false;
		stoponmax_ = true;
		stoponmaxdelay_ = 5;

		if (zStabilizationProperty != null && zStabilizationProperty.isAssigned()) {
			zstabProperty_ = zStabilizationProperty;
			disableFocusLock_ = true;
		} else {
			zstabProperty_ = null;
			disableFocusLock_ = false;
		}
		focusLockAtZ0_ = false;

		// default values
		deltaZ = 0.3;
		nSlices = 4;
		nLoops = 5;
		sliceSt = 0;

		zdevice_ = defaultzdevice;
		zdevices_ = zdevices;

		params_ = new GenericAcquisitionParameters(AcquisitionType.MULTISLICELOC, exposure, 0, 3, 50000,
				new HashMap<String, String>(), new HashMap<String, String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();

		pane.setName(getPanelName());

		final JLabel exposurelab, waitinglab, numframelab, intervallab, waitonmaxlab, labelActivation;
		final JLabel zdevicelabel, numbLabel;
		final JSpinner numberslice, deltaz, numberloops, slicest;
		final JSpinner exposurespin, waitingspin, numframespin, intervalspin, waitonmaxspin;
		final JCheckBox activatecheck, stoponmaxcheck, disablefocuslock, flonlyatz0, activateSt;
		final JComboBox<String> activationCombo;

		exposurelab = new JLabel(LABEL_EXPOSURE);
		waitinglab = new JLabel(LABEL_PAUSE);
		numframelab = new JLabel(LABEL_NUMFRAME);
		intervallab = new JLabel(LABEL_INTERVAL);
		waitonmaxlab = new JLabel(LABEL_MAXUVTIME);

		exposurespin = new JSpinner(new SpinnerNumberModel(Math.max(params_.getExposureTime(), 1), 1, 10000000, 1));
		exposurespin.setName(LABEL_EXPOSURE);
		exposurespin.setToolTipText("Camera exposure (ms).");

		waitingspin = new JSpinner(new SpinnerNumberModel(params_.getWaitingTime(), 0, 10000000, 1));
		waitingspin.setName(LABEL_PAUSE);
		waitingspin.setToolTipText("Waiting time (s) to allow device state changes before this acquisition.");

		numframespin = new JSpinner(new SpinnerNumberModel(params_.getNumberFrames(), 1, 10000000, 1));
		numframespin.setName(LABEL_NUMFRAME);
		numframespin.setToolTipText("Number of frames per slice.");

		intervalspin = new JSpinner(new SpinnerNumberModel(params_.getIntervalMs(), 0, 10000000, 1));
		intervalspin.setName(LABEL_INTERVAL);
		intervalspin.setToolTipText("Interval between frames (ms).");

		waitonmaxspin = new JSpinner(new SpinnerNumberModel(stoponmaxdelay_, 0, 10000, 1));
		waitonmaxspin.setName(LABEL_MAXUVTIME);
		waitonmaxspin.setToolTipText(
				"Time (s) before stopping the acquisition after reaching the maximum activation value.");

		//// activation
		activatecheck = new JCheckBox(LABEL_USEACTIVATION);
		activatecheck.setSelected(useactivation_);
		activatecheck.setEnabled(!nullActivation_);
		activatecheck.setName(LABEL_USEACTIVATION);
		activatecheck.setToolTipText("Use activation during the acquisition.");

		stoponmaxcheck = new JCheckBox(LABEL_USESTOPONMAXUV);
		stoponmaxcheck.setSelected(stoponmax_);
		stoponmaxcheck.setEnabled(!nullActivation_);
		stoponmaxcheck.setName(LABEL_USESTOPONMAXUV);
		stoponmaxcheck.setToolTipText("Stop the acquisition after reaching the maximum activation value.");
		stoponmaxcheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (!selected) {
					waitonmaxspin.setValue(0);
					waitonmaxspin.setEnabled(false);
				} else {
					waitonmaxspin.setEnabled(true);
				}
			}
		});
		
		// choice of activation
		final String[] acts = activationController_.getActivationPropertiesName();
		labelActivation = new JLabel(LABEL_ACTIVATION);
		activationCombo = new JComboBox<>(acts);
		activationCombo.setName(LABEL_ACTIVATION);
		if(acts.length != 2){
			activationCombo.setEnabled(false);

			if(acts.length == 0){
				stoponmaxcheck.setEnabled(false);
				waitonmaxspin.setEnabled(false);
				activatecheck.setEnabled(false);
				useactivation_ = false;
			}
		} else {
			if (Arrays.asList(acts).contains(activationName_)) {
				activationCombo.setSelectedItem(activationName_);
			}
		}

		activateSt = new JCheckBox(LABEL_ACTATST);
		activateSt.setEnabled(!nullActivation_);
		activateSt.setName(LABEL_ACTATST);
		activateSt.setSelected(actAtSt);
		slicest = new JSpinner(new SpinnerNumberModel(sliceSt, 0, nSlices - 1, 1));
		slicest.setEnabled(!nullActivation_);
		slicest.setName(LABEL_SLICEST);

		activatecheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (!selected) {
					stoponmaxcheck.setEnabled(false);
					stoponmaxcheck.setSelected(false);
					waitonmaxspin.setValue(0);
					waitonmaxspin.setEnabled(false);
					activateSt.setEnabled(false);
					activateSt.setSelected(false);
					slicest.setEnabled(false);
				} else {
					stoponmaxcheck.setEnabled(true);
					activateSt.setEnabled(true);
					slicest.setEnabled(true);
				}
			}
		});

		//// z part
		zdevicelabel = new JLabel(LABEL_ZDEVICE);
		numbLabel = new JLabel(LABEL_NUMB);

		numberloops = new JSpinner(new SpinnerNumberModel(nLoops, 1, 100, 1));
		numberloops.setName(LABEL_NLOOPS);
		numberloops.setToolTipText("Number of loops through the slices.");

		numberslice = new JSpinner(new SpinnerNumberModel(nSlices, 2, 100, 1));
		numberslice.setName(LABEL_NSLICES);
		numberslice.setToolTipText("Number of slices in the stack.");
		numberslice.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int val_nslice = (int) spinner.getValue();

				int val_slice = (int) slicest.getValue();
				if (val_slice <= val_nslice - 1) {
					slicest.setModel(new SpinnerNumberModel(val_slice, 0, val_nslice - 1, 1));
				} else {
					slicest.setModel(new SpinnerNumberModel(val_nslice - 1, 0, val_nslice - 1, 1));
				}
			}
		});

		deltaz = new JSpinner(new SpinnerNumberModel(deltaZ, -1000, 1000, 0.5));
		deltaz.setName(LABEL_DELTAZ);
		deltaz.setToolTipText("Distance (um) between each slice.");

		JComboBox<String> zdevices = new JComboBox<String>(zdevices_);
		zdevices.setSelectedItem(zdevice_);
		zdevices.setName(LABEL_ZDEVICE);
		zdevices.setToolTipText("Device performing the multi-slice acquisition.");

		disablefocuslock = new JCheckBox(LABEL_DISABLEFL);
		disablefocuslock.setSelected(disableFocusLock_);
		disablefocuslock.setName(LABEL_DISABLEFL);
		disablefocuslock.setEnabled(zstabProperty_ != null);
		disablefocuslock.setToolTipText("Check to disable focus stabilization (if applicable).");

		flonlyatz0 = new JCheckBox(LABEL_FLATZ0);
		flonlyatz0.setName(LABEL_FLATZ0);
		flonlyatz0.setSelected(focusLockAtZ0_);
		flonlyatz0.setEnabled(!disableFocusLock_ && zstabProperty_ != null);
		disablefocuslock
				.setToolTipText("Check to use the focus stabilization only during the first slice of every loop.");

		disablefocuslock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (selected) {
					flonlyatz0.setEnabled(false);
					flonlyatz0.setSelected(false);
				} else {
					flonlyatz0.setEnabled(true);
				}
			}
		});

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

		panelHolder[0][0].add(exposurelab);
		panelHolder[0][1].add(exposurespin);
		panelHolder[0][2].add(numframelab);
		panelHolder[0][3].add(numframespin);

		panelHolder[1][0].add(waitinglab);
		panelHolder[1][1].add(waitingspin);
		panelHolder[1][2].add(intervallab);
		panelHolder[1][3].add(intervalspin);

		panelHolder[2][0].add(waitonmaxlab);
		panelHolder[2][1].add(waitonmaxspin);
		panelHolder[2][2].add(stoponmaxcheck);
		panelHolder[2][3].add(activatecheck);

		panelHolder[3][0].add(labelActivation);
		panelHolder[3][1].add(activationCombo);
		panelHolder[3][2].add(activateSt);
		panelHolder[3][3].add(slicest);

		panelHolder[4][0].add(zdevicelabel);
		panelHolder[4][1].add(zdevices);
		panelHolder[4][2].add(disablefocuslock);
		panelHolder[4][3].add(flonlyatz0);

		panelHolder[5][0].add(numbLabel);
		panelHolder[5][1].add(numberloops);
		panelHolder[5][2].add(numberslice);
		panelHolder[5][3].add(deltaz);

		return pane;
	}

	public void setUseActivation(boolean b) {
		if (!nullActivation_) {
			useactivation_ = b;
		} else {
			useactivation_ = false;
		}
	}

	public void setUseStopOnMaxUV(boolean b) {
		stoponmax_ = b;
	}

	public void setUseStopOnMaxUVDelay(int delay) {
		stoponmaxdelay_ = delay;
	}

	private void setActivation(String act){
		activationName_ = act;
	}
	
	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		if (pane.getName().equals(getPanelName())) {
			Component[] pancomp = pane.getComponents();

			for (int j = 0; j < pancomp.length; j++) {
				if (pancomp[j] instanceof JPanel) {
					Component[] comp = ((JPanel) pancomp[j]).getComponents();
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
								zdevice_ = ((String) ((JComboBox<String>) comp[i]).getSelectedItem());
							} else if (comp[i].getName().equals(LABEL_FLATZ0) && comp[i] instanceof JCheckBox) {
								focusLockAtZ0_ = ((JCheckBox) comp[i]).isSelected();
							} else if (comp[i].getName().equals(LABEL_DISABLEFL) && comp[i] instanceof JCheckBox) {
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
								actAtSt = ((JCheckBox) comp[i]).isSelected();
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
		if (zstabProperty_ == null) {
			return new NoPropertyFilter();
		}
		return new SinglePropertyFilter(zstabProperty_.getPropertyLabel());
	}

	@Override
	public String[] getHumanReadableSettings() {
		String[] s = new String[15];
		s[0] = "Exposure = " + params_.getExposureTime() + " ms";
		s[1] = "Interval = " + params_.getIntervalMs() + " ms";
		s[2] = "Number of frames = " + params_.getNumberFrames();
		s[3] = "Use activation = " + useactivation_;
		s[4] = "Stop on max UV = " + stoponmax_;
		s[5] = "Stop on max delay = " + stoponmaxdelay_ + " s";
		s[6] = "Focus stage = " + zdevice_;
		s[7] = "Use FL at St = " + focusLockAtZ0_;
		s[8] = "Disable focus-lock = " + disableFocusLock_;
		s[9] = "Number of loops = " + nLoops;
		s[10] = "Number of slices = " + nSlices;
		s[11] = "Z difference = " + deltaZ + " um";
		s[12] = "Slice St = " + sliceSt;
		s[13] = "Activate at St = " + actAtSt;
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
		parameters[0][1] = String.valueOf(useactivation_);
		parameters[1][0] = KEY_STOPONMAX;
		parameters[1][1] = String.valueOf(stoponmax_);
		parameters[2][0] = KEY_STOPDELAY;
		parameters[2][1] = String.valueOf(stoponmaxdelay_);
		parameters[3][0] = KEY_ZDEVICE;
		parameters[3][1] = zdevice_;
		parameters[4][0] = KEY_FLATZ0;
		parameters[4][1] = String.valueOf(focusLockAtZ0_);
		parameters[5][0] = KEY_DISABLEFL;
		parameters[5][1] = String.valueOf(disableFocusLock_);
		parameters[6][0] = KEY_NLOOPS;
		parameters[6][1] = String.valueOf(nLoops);
		parameters[7][0] = KEY_NSLICES;
		parameters[7][1] = String.valueOf(nSlices);
		parameters[8][0] = KEY_DELTAZ;
		parameters[8][1] = String.valueOf(deltaZ);
		parameters[9][0] = KEY_SLICEST;
		parameters[9][1] = String.valueOf(sliceSt);
		parameters[10][0] = KEY_ACTATST;
		parameters[10][1] = String.valueOf(actAtSt);
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

		useactivation_ = Boolean.parseBoolean(parameters[0][1]);
		stoponmax_ = Boolean.parseBoolean(parameters[1][1]);
		stoponmaxdelay_ = Integer.parseInt(parameters[2][1]);
		zdevice_ = parameters[3][1];
		focusLockAtZ0_ = Boolean.parseBoolean(parameters[4][1]);
		disableFocusLock_ = Boolean.parseBoolean(parameters[5][1]);
		nLoops = Integer.parseInt(parameters[6][1]);
		nSlices = Integer.parseInt(parameters[7][1]);
		deltaZ = Double.parseDouble(parameters[8][1]);
		sliceSt = Integer.parseInt(parameters[9][1]);
		actAtSt = Boolean.parseBoolean(parameters[10][1]);
		activationName_ = parameters[11][1];
	}

	private int getActivationIndex(){
		final String[] acts = activationController_.getActivationPropertiesName();
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
		if (zstabProperty_ != null) {
			if (b) {
				zstabProperty_.setPropertyValue(TwoStateUIProperty.getOnStateLabel());
			} else {
				zstabProperty_.setPropertyValue(TwoStateUIProperty.getOffStateLabel());
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
		} else if (!disableFocusLock_) {
			stabilizeFocus(true);
		}

		if (useactivation_) {
			activationController_.initializeTask(getActivationIndex());
			activationController_.resumeTask();
		}

		double z0 = 0;
		try {
			z0 = core.getPosition(zdevice_);
		} catch (Exception e1) {
			running_ = false;
			e1.printStackTrace();
		}

		if (running_) {
			for (int i = 0; i < nLoops; i++) {
				for (int j = 0; j < nSlices; j++) {
					if (useactivation_ && ((actAtSt && j == sliceSt) || !actAtSt)) {
						activationController_.resumeTask();
					}

					// set z
					double z = z0 + j * deltaZ;

					try {
						// moves the stage
						core.setPosition(zdevice_, z);

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
							if (useactivation_ && stoponmax_ && ((actAtSt && j == sliceSt) || !actAtSt)
									&& activationController_.isCriterionReached()) {
								Thread.sleep(1000 * stoponmaxdelay_);

								stopAcquisition();
							}

							// checks if exit requested
							if (stopAcq_) {
								studio.logs().logDebugMessage("[htSMLM] Multislice interruption during slice " + j + ".");
								break;
							}

							Thread.sleep(1000);
						}

						// disable focus-lock and update z0
						if (j == 0 && !disableFocusLock_ && focusLockAtZ0_) {
							// updates z0 for the next iterations
							z0 = core.getPosition(zdevice_);

							stabilizeFocus(false);
						}

						// pause activation
						if (useactivation_ && ((actAtSt && j == sliceSt) || !actAtSt)) {
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
			core.setPosition(zdevice_, z0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// if focus locked disabled, set to ON state
		if (zstabProperty_ != null && zstabProperty_.isAssigned()) {
			if (zstabProperty_.isOffState(zstabProperty_.getPropertyValue())) {
				stabilizeFocus(true);
			}
		}
		
		if (useactivation_) {
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
		while (EmuUtils.isInteger(name.substring(ind - 1, ind)) && ind > 0) {
			ind--;
		}

		if (ind == name.length()) {
			if (name.substring(ind - 1, ind).equals("_")) {
				newName = name + "1";
			} else {
				newName = name + "_1";
			}
		} else {
			if (name.substring(ind - 1, ind).equals("_")) {
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
		private final Datastore.SaveMode savemode;
		private final String path, directory, name;
		private boolean isRunning_;

		public RunnableAcq(Studio studio, Datastore.SaveMode savemode, String path, String directory, String name) {
			this.studio = studio;
			this.savemode = savemode;
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
				if (Datastore.SaveMode.MULTIPAGE_TIFF == savemode) {
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
