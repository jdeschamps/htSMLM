package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.micromanager.Studio;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.DefaultAcquisitionManager;
import org.micromanager.data.Datastore;

import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.SinglePropertyFilter;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;
import mmcorej.CMMCore;

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
	private final static String LABEL_ACTATST = "activate only at slice:";
	
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
	private final static String KEY_ACTATST = "Activation at St";
		
	@SuppressWarnings("rawtypes")
	private TaskHolder activationTask_;
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
	
	@SuppressWarnings("rawtypes")
	public MultiSliceAcquisition(TaskHolder activationtask, double exposure, String[] zdevices, String defaultzdevice, TwoStateUIProperty zStabilizationProperty) {
		
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
		actAtSt = false;
		stoponmax_ = true;
		stoponmaxdelay_ = 5;

		if(zStabilizationProperty != null && zStabilizationProperty.isAssigned()) {
			zstabProperty_ = zStabilizationProperty;
			disableFocusLock_ = true;
		} else {
			zstabProperty_ = null;
			disableFocusLock_ = false;
		}
		focusLockAtZ0_ = false;
		
		// default values
		deltaZ=0.3;
		nSlices=4;
		nLoops=5;
		sliceSt = 0;

		zdevice_ = defaultzdevice;
		zdevices_ = zdevices;
		
		params_ = new GenericAcquisitionParameters(AcquisitionType.MULTISLICELOC, 
				exposure, 0, 3, 50000, new HashMap<String,String>(), new HashMap<String,String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		
		pane.setName(getPanelName());
		
		JLabel exposurelab, waitinglab, numframelab, intervallab, waitonmaxlab;
		JLabel zdevicelabel, numbLabel;
		JSpinner numberslice, deltaz, numberloops, slicest;
		JSpinner exposurespin, waitingspin, numframespin, intervalspin, waitonmaxspin;
		JCheckBox activatecheck, stoponmaxcheck, disablefocuslock, flonlyatz0, activateSt;
		
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
		numframespin.setToolTipText("Number of frames per slice.");
		
		intervalspin = new JSpinner(new SpinnerNumberModel(params_.getIntervalMs(), 0, 10000000, 1));
		intervalspin.setName(LABEL_INTERVAL);
		intervalspin.setToolTipText("Interval between frames (ms).");
		
		waitonmaxspin = new JSpinner(new SpinnerNumberModel(stoponmaxdelay_, 0, 10000, 1));
		waitonmaxspin.setName(LABEL_MAXUVTIME);
		waitonmaxspin.setToolTipText("Time (s) before stopping the acquisition after reaching the maximum activation value.");

		
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

		
		activateSt = new JCheckBox(LABEL_ACTATST);
		activateSt.setEnabled(!nullActivation_);
		activateSt.setName(LABEL_ACTATST);
		activateSt.setSelected(actAtSt);
		slicest = new JSpinner(new SpinnerNumberModel(sliceSt, 0, 100, 1)); 
		slicest.setEnabled(!nullActivation_);
		slicest.setName(LABEL_SLICEST);
		
		activatecheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (!selected) {
					stoponmaxcheck.setEnabled(false);
					stoponmaxcheck.setSelected(false);
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
		disablefocuslock.setToolTipText("Check to use the focus stabilization only during the first slice of every loop.");
		
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
		pane.setLayout(new GridLayout(nrow,ncol));
	
		for(int m = 0; m < nrow; m++) {
		   for(int n = 0; n < ncol; n++) {
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

	public void setUseActivation(boolean b){
		if(!nullActivation_){
			useactivation_  = b;
		} else {
			useactivation_  = false;
		}
	}

	public void setUseStopOnMaxUV(boolean b){
		stoponmax_ = b;
	}
	
	public void setUseStopOnMaxUVDelay(int delay){
		stoponmaxdelay_ = delay;
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
							} else if(comp[i].getName().equals(LABEL_ZDEVICE) && comp[i] instanceof JComboBox){
								zdevice_ = ((String) ((JComboBox) comp[i]).getSelectedItem());
							} else if(comp[i].getName().equals(LABEL_FLATZ0) && comp[i] instanceof JCheckBox){
								focusLockAtZ0_ = ((JCheckBox) comp[i]).isSelected();
							} else if(comp[i].getName().equals(LABEL_DISABLEFL) && comp[i] instanceof JCheckBox){
								disableFocusLock_ = ((JCheckBox) comp[i]).isSelected();
							} else if(comp[i].getName().equals(LABEL_NSLICES) && comp[i] instanceof JSpinner){
								nSlices = ((Integer) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_NLOOPS) && comp[i] instanceof JSpinner){
								nLoops = ((Integer) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_DELTAZ) && comp[i] instanceof JSpinner){
								deltaZ = ((Double) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_SLICEST) && comp[i] instanceof JSpinner){
								sliceSt = ((Integer) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_ACTATST) && comp[i] instanceof JCheckBox){
								actAtSt = ((JCheckBox) comp[i]).isSelected();
							}
						}
					}
				}
			}	
		}
		
		if(sliceSt >= nSlices) {
			sliceSt = nSlices-1;
		}
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		if(zstabProperty_ == null){
			return new NoPropertyFilter();
		}
		return new SinglePropertyFilter(zstabProperty_.getPropertyLabel());
	}

	@Override
	public String[] getHumanReadableSettings() {
		String[] s = new String[14];
		s[0] = "Exposure = "+params_.getExposureTime()+" ms";
		s[1] = "Interval = "+params_.getIntervalMs()+" ms";
		s[2] = "Number of frames = "+params_.getNumberFrames();
		s[3] = "Use activation = "+useactivation_;
		s[4] = "Stop on max UV = "+stoponmax_;
		s[5] = "Stop on max delay = "+stoponmaxdelay_+" s";
		s[6] = "Focus stage = "+zdevice_;
		s[7] = "Use FL at St = "+focusLockAtZ0_;
		s[8] = "Disable focus-lock = "+disableFocusLock_;
		s[9] = "Number of loops = "+nLoops;
		s[10] = "Number of slices = "+nSlices;
		s[11] = "Z difference = "+deltaZ+" um";
		s[12] = "Slice St = "+sliceSt;
		s[14] = "Activate at St = "+actAtSt;
		return s;
	}

	@Override
	public String getPanelName() {
		return PANE_NAME;
	}
	
	@Override
	public String[][] getAdditionalParameters() {
		String[][] parameters = new String[11][2];

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

		return parameters;
	}
	
	@Override
	public void setAdditionalParameters(String[][] parameters) {
		if(parameters.length != 11 || parameters[0].length != 2) {
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
	}

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path) throws InterruptedException, IOException{
		
		CMMCore core  = studio.core();

		stopAcq_ = false;
		running_ = true;

		if(disableFocusLock_ || (!disableFocusLock_ && focusLockAtZ0_)) {
			zstabProperty_.setPropertyValue(TwoStateUIProperty.getOffStateLabel());
		}
		
		SequenceSettings settings = new SequenceSettings();
		settings.save = true;
		settings.timeFirst = true;
		settings.usePositionList = false;
		settings.root = path;
		settings.numFrames = params_.getNumberFrames();
		settings.intervalMs = 0;
		settings.shouldDisplayImages = true;
		
		double z0 = 0;
		try {
			z0 = core.getPosition(zdevice_);
		} catch (Exception e1) {
			running_=false;
			e1.printStackTrace();
		}
		
		if(running_) {
			for(int i=0;i<nLoops;i++) {
				for(int j=0;j<nSlices;j++) {
					
					if(useactivation_ && ((actAtSt && j==sliceSt) || !actAtSt) ){			
						//activationTask_.initializeTask();
						activationTask_.resumeTask();
					}					
					
					// set z
					double z = z0 + j*deltaZ;
					
					try {
						// moves the stage
						core.setPosition(zdevice_, z);
						
						Thread.sleep(1000); 
						
						if(i>0 && j==sliceSt && !disableFocusLock_ && focusLockAtZ0_) {
							zstabProperty_.setPropertyValue(TwoStateUIProperty.getOnStateLabel());
							Thread.sleep(10000); // ten seconds hard coded waiting time for focus-lock
							zstabProperty_.setPropertyValue(TwoStateUIProperty.getOffStateLabel());
							
							// updates z0 for the next iterations
							z0 = core.getPosition(zdevice_);
						}
						
						// sets-up name
						settings.prefix = "L"+i+"S"+j+"_"+name;
						
						if(stopAcq_){
							System.out.println("[htSMLM] Multislice interruption before slice "+j+".");
							interruptAcquisition(studio);
						}
						
						// runs acquisition
						Datastore store = studio.acquisitions().runAcquisitionWithSettings(settings, false);

						// loops to check if needs to be stopped or not
						while(studio.acquisitions().isAcquisitionRunning()) {
							
							// checks if reached stop criterion
							if(useactivation_ && stoponmax_ && ((actAtSt && j==sliceSt) || !actAtSt) && activationTask_.isCriterionReached()){
								Thread.sleep(1000*stoponmaxdelay_);
												
								interruptAcquisition(studio);
							}
									
							// checks if exit requested
							if(stopAcq_){
								System.out.println("[htSMLM] Multislice interruption during slice "+j+".");
								interruptAcquisition(studio);
							}
							
							Thread.sleep(1000);
						}

						// close store
						studio.displays().closeDisplaysFor(store);
						store.close();

						if(i>0 && j==sliceSt && !disableFocusLock_ && focusLockAtZ0_) {
							// updates z0 for the next iterations
							z0 = core.getPosition(zdevice_);
							
							zstabProperty_.setPropertyValue(TwoStateUIProperty.getOffStateLabel());
						}
						
						
						// pause activation
						if(useactivation_){			
							activationTask_.pauseTask();
						}
						
					} catch (Exception e) {
						running_ = false;						
						System.out.println("[htSMLM] Failed to move stage or query position after slice "+j+".");
						e.printStackTrace();
					}
					
					if(stopAcq_) {
						System.out.println("[htSMLM] Multislice interruption after slice "+j+".");
						break;
					}
				}
				
				if(stopAcq_) {
					System.out.println("[htSMLM] Multislice interruption after loop "+i+".");
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
		if(disableFocusLock_ || (!disableFocusLock_ && focusLockAtZ0_)) {				
			zstabProperty_.setPropertyValue(TwoStateUIProperty.getOnStateLabel());
		}
			
		if(useactivation_) {
			activationTask_.initializeTask();
		}
		
		running_ = false;
	}

	private void interruptAcquisition(Studio studio) {
		//if(interruptionRequested_ == false) {
			try {
				// not pretty but I could not find any other way to stop the acquisition without getting a JDialog popping up and requesting user input
				((DefaultAcquisitionManager) studio.acquisitions()).getAcquisitionEngine().stop(true);
				
				//((DefaultAcquisitionManager) studio.acquisitions()).getAcquisitionEngine().abortRequested();
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
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
		return AcquisitionType.MULTISLICELOC;
	}

	@Override
	public String getShortName() {
		return "MultiSliceLoc";
	}
}
