package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.SinglePropertyFilter;

import org.micromanager.Studio;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.internal.DefaultAcquisitionManager;
import org.micromanager.data.Datastore;

public class ZStackAcquisition implements Acquisition {
	
	// Convenience constants	
	private final static String PANE_NAME = "Autofocus panel";
	private final static String LABEL_EXPOSURE = "Exposure (ms):";
	private final static String LABEL_PAUSE = "Pause (s):";
	private final static String LABEL_ZSTART = "Z start / Z end / Z step:";
	private final static String LABEL_ZEND = "Z end (um):";
	private final static String LABEL_ZSTEP = "Z step (um):"; // just pretend it is um but there is no check...
	private final static String LABEL_ZDEVICE = "Z stage:";
	private final static String LABEL_CHECK = "disable focus-lock";
		
	public final static String KEY_ZSTART = "Z start";
	public final static String KEY_ZEND = "Z end";
	public final static String KEY_ZSTEP = "Z step";
	public final static String KEY_ZDEVICE = "Z stage";
	public final static String KEY_DISABLEFL = "Disable focus-lock";
	
	// UI property
	private TwoStateUIProperty zstabProperty_;

	private String zdevice_;
	private String[] zdevices_;
	private double zstart, zend, zstep;
	private boolean disableZStab_; 
	private GenericAcquisitionParameters params_;
	private volatile boolean stopAcq_, running_;
	
	public ZStackAcquisition(double exposure, String[] zdevices, String defaultzdevice, TwoStateUIProperty zStabilizationProperty) {

		if(zStabilizationProperty != null && zStabilizationProperty.isAssigned()) {
			zstabProperty_ = zStabilizationProperty;
			disableZStab_ = true;
		} else {
			zstabProperty_ = null;
			disableZStab_ = false;
		}
		
		zstart=-2;
		zend=2;
		zstep=0.05;

		zdevice_ = defaultzdevice;
		zdevices_ = zdevices;
		
		stopAcq_ = false;
		running_ = false;
		
		params_ = new GenericAcquisitionParameters(AcquisitionType.ZSTACK, 
				exposure, 0, 3, 1, new HashMap<String,String>(), new HashMap<String,String>(), getSlices(zstart, zend, zstep));
	}
	
	public ArrayList<Double> getSlices(double zstart, double zend, double zstep){
		ArrayList<Double> slices = new ArrayList<Double>();
		
		boolean invert;
		if(zstart < zend) {
			invert = false;
		} else {
			invert = true;
		}
		slices.add(zstart);

		double z = zstart;
		if(invert) {
			while (z>=zend){
				z -= zstep;
				slices.add(EmuUtils.round(z,2));
			}
		} else {
			while (z<=zend){
				z += zstep;
				slices.add(EmuUtils.round(z,2));
			}
		}

		return slices;
	}
	
	private void setSlices(double zstart, double zend, double zstep){
		params_.setZSlices(getSlices(zstart,zend,zstep));
	}
	
	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path) throws InterruptedException, IOException {
		stopAcq_ = false;
		running_ = true;
		
		String default_device = studio.getCMMCore().getFocusDevice();
		try {
			studio.getCMMCore().setFocusDevice(zdevice_);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(zstabProperty_!= null && disableZStab_){ // if there is a stabilization property and 
			zstabProperty_.setPropertyValue(TwoStateUIProperty.getOffStateLabel()); // turn it off
		}
				
		SequenceSettings settings = new SequenceSettings();
		settings.save = true;
		settings.slicesFirst = true;
		settings.relativeZSlice = true;
		settings.slices = params_.getZSlices();
		
		double z0;
		try {
			z0 = studio.getCMMCore().getPosition(zdevice_);
			settings.zReference = z0;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		settings.usePositionList = false;
		settings.root = path;
		settings.prefix = name;
		settings.numFrames = 1;
		settings.intervalMs = 0;
		settings.shouldDisplayImages = true;

		// run acquisition
		Datastore store = studio.acquisitions().runAcquisitionWithSettings(settings, false);

		// loop to check if needs to be stopped or not
		while(studio.acquisitions().isAcquisitionRunning()) {
			// check if exit
			if(stopAcq_){
				interruptAcquisition(studio);
			}
			
			Thread.sleep(500);
		}
		
		studio.displays().closeDisplaysFor(store);
		
		store.close();
		
		if(zstabProperty_!=null && disableZStab_){
			zstabProperty_.setPropertyValue(TwoStateUIProperty.getOnStateLabel()); // sets on at the end
		}

		try {
			studio.getCMMCore().setFocusDevice(default_device);
		} catch (Exception e) {
			e.printStackTrace();
		}
					
		running_ = false;
	}
	
	private void interruptAcquisition(Studio studio) {
		try {
			// not pretty but I could not find any other way to stop the acquisition without getting a JDialog popping up and requesting user input
			((DefaultAcquisitionManager) studio.acquisitions()).getAcquisitionEngine().stop(true);;
		} catch (Exception e) {
			e.printStackTrace();
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
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		
		pane.setName(getPanelName());
		
		JLabel exposurelab, waitinglab, zstartlab, zdevicelabel;
		JSpinner exposurespin, waitingspin, zstartspin, zendspin, zstepspin;
		
		// focus-lock
		JCheckBox disableFocusLock = new JCheckBox(LABEL_CHECK);
		disableFocusLock.setName(LABEL_CHECK);
		disableFocusLock.setToolTipText("Select to disable Z stabilization (if applicable).");
		if(zstabProperty_ == null) {
			disableFocusLock.setEnabled(true);
		}
		disableFocusLock.setSelected(disableZStab_);
		
		exposurelab = new JLabel(LABEL_EXPOSURE);
		waitinglab = new JLabel(LABEL_PAUSE);
		zstartlab = new JLabel(LABEL_ZSTART);
		zdevicelabel = new JLabel(LABEL_ZDEVICE);
		
		// exposure and waiting time
		exposurespin = new JSpinner(new SpinnerNumberModel(Math.max(params_.getExposureTime(),1), 1, 10000, 1));
		exposurespin.setName(LABEL_EXPOSURE);
		exposurespin.setToolTipText("Camera exposure (ms).");

		waitingspin = new JSpinner(new SpinnerNumberModel(params_.getWaitingTime(), 0, 10000, 1)); 
		waitingspin.setName(LABEL_PAUSE);
		waitingspin.setToolTipText("Waiting time (s) to allow device state changes before this acquisition.");

		
		// z related
		zstartspin = new JSpinner(new SpinnerNumberModel(zstart, -1000, 1000, 0.05)); 
		zstartspin.setName(LABEL_ZSTART);
		zstartspin.setToolTipText("Relative position in um of the first slice.");
		
		zendspin = new JSpinner(new SpinnerNumberModel(zend, -1000, 1000, 1)); 
		zendspin.setName(LABEL_ZEND);
		zendspin.setToolTipText("Relative position in um of the last slice.");
		
		zstepspin = new JSpinner(new SpinnerNumberModel(zstep, -1000, 1000, 0.01));
		zstepspin.setName(LABEL_ZSTEP);
		zstepspin.setToolTipText("Step size (um) between slices.");
		
		
		JComboBox<String> zdevices = new JComboBox<String>(zdevices_);
		zdevices.setSelectedItem(zdevice_);
		zdevices.setName(LABEL_ZDEVICE);
		zdevices.setToolTipText("Device performing the Z stack.");
		

		int nrow = 3;
		int ncol = 4;
		JPanel[][] panelHolder = new JPanel[nrow][ncol];    
		pane.setLayout(new GridLayout(nrow,ncol));

		for(int m = 0; m < nrow; m++) {
		   for(int n = 0; n < ncol; n++) {
		      panelHolder[m][n] = new JPanel();
		      pane.add(panelHolder[m][n]);
		   }
		}

		panelHolder[0][0].add(zdevicelabel);
		panelHolder[0][1].add(zdevices);
		panelHolder[0][2].add(disableFocusLock);
		
		panelHolder[1][0].add(exposurelab);
		panelHolder[2][0].add(zstartlab);
		
		panelHolder[1][1].add(exposurespin);
		panelHolder[2][1].add(zstartspin);
		
		panelHolder[1][2].add(waitinglab);
		panelHolder[2][2].add(zendspin);
		
		panelHolder[1][3].add(waitingspin);
		panelHolder[2][3].add(zstepspin);

		return pane;
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
							} else if(comp[i].getName().equals(LABEL_ZSTART) && comp[i] instanceof JSpinner){
								zstart = ((Double) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_ZEND) && comp[i] instanceof JSpinner){
								zend = ((Double) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_ZSTEP) && comp[i] instanceof JSpinner){
								zstep = ((Double) ((JSpinner) comp[i]).getValue());
							} else if(comp[i].getName().equals(LABEL_ZDEVICE) && comp[i] instanceof JComboBox){
								zdevice_ = ((String) ((JComboBox) comp[i]).getSelectedItem());
							}else if(comp[i].getName().equals(LABEL_CHECK) && comp[i] instanceof JCheckBox){
								disableZStab_ = ((JCheckBox) comp[i]).isSelected();
							}
						}
					}
				}
			}	
			
			this.setSlices(zstart, zend, zstep);
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
		String[] s = new String[6];
		s[0] = "Exposure = "+params_.getExposureTime()+" ms";
		s[1] = "Stage = "+zdevice_;
		s[2] = "Zstart = "+zstart+" um";
		s[3] = "Zend = "+zend+" um";
		s[4] = "Zstep = "+zstep+" um";
		s[5] = "Disable focus-lock = "+String.valueOf(disableZStab_);
		return s;
	}

	@Override
	public String getPanelName() {
		return PANE_NAME;
	}
	
	@Override
	public String[][] getAdditionalParameters() {
		String[][] s = new String[5][2];

		s[0][0] = KEY_ZSTART;
		s[0][1] = String.valueOf(zstart);
		s[1][0] = KEY_ZEND;
		s[1][1] = String.valueOf(zend);
		s[2][0] = KEY_ZSTEP;
		s[2][1] = String.valueOf(zstep);
		s[3][0] = KEY_ZDEVICE;
		s[3][1] = zdevice_;
		s[4][0] = KEY_DISABLEFL;
		s[4][1] = String.valueOf(disableZStab_);
		
		return s;
	}	
	
	@Override
	public void setAdditionalParameters(String[][] parameters) {
		if(parameters.length != 5 || parameters[0].length != 2) {
			throw new IllegalArgumentException("The parameters array has the wrong size: expected (5,2), got ("
					+ parameters.length + "," + parameters[0].length + ")");
		}		
		zstart = Double.parseDouble(parameters[0][1]);
		zend = Double.parseDouble(parameters[1][1]);
		zstep = Double.parseDouble(parameters[2][1]);
		zdevice_ = parameters[3][1];
		disableZStab_ = Boolean.parseBoolean(parameters[4][1]);
	}

	public void setZDevice(String zdevice){
		zdevice_ = zdevice;
	}

	public void setDisableFocusLock(boolean zstabuse){
		disableZStab_ = zstabuse;
	}
	
	@Override
	public AcquisitionType getType() {
		return AcquisitionType.ZSTACK;
	}

	@Override
	public String getShortName() {
		return "Z";
	}
}
