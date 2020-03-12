package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.SinglePropertyFilter;

import org.micromanager.Studio;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Datastore;

public class BFPAcquisition implements Acquisition{
	
	private GenericAcquisitionParameters params_;
	
	private final static String PANE_NAME = "BFP panel";
	private final static String LABEL_EXPOSURE = "Exposure (ms):";
	private final static String LABEL_PAUSE = "Pause (s):";
	
	private TwoStateUIProperty bfpprop_;
		
	public BFPAcquisition(double exposure, TwoStateUIProperty bfpprop) {
		if(bfpprop == null){
			throw new NullPointerException();
		}
		bfpprop_ = bfpprop;
		
		params_ = new GenericAcquisitionParameters(AcquisitionType.BFP, 
				exposure, 0, 3, 1, new HashMap<String,String>(), new HashMap<String,String>());
	}

	@Override
	public String getPanelName(){
		return PANE_NAME;
	}
	
	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		pane.setName(getPanelName());
				
		JLabel exposurelab, waitinglab;
		JSpinner exposurespin, waitingspin;
		
		exposurelab = new JLabel(LABEL_EXPOSURE);
		waitinglab = new JLabel(LABEL_PAUSE);

		exposurespin = new JSpinner(new SpinnerNumberModel(Math.max(params_.getExposureTime(),1), 1, 10000000, 1));
		exposurespin.setName(LABEL_EXPOSURE);
		exposurespin.setToolTipText("Camera exposure (ms).");
		
		waitingspin = new JSpinner(new SpinnerNumberModel(params_.getWaitingTime(), 0, 10000000, 1)); 
		waitingspin.setName(LABEL_PAUSE);
		waitingspin.setToolTipText("Waiting time (s) to allow device state changes before this acquisition.");
		
		int nrow = 1;
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
		panelHolder[0][2].add(waitinglab);
		panelHolder[0][1].add(exposurespin);
		panelHolder[0][3].add(waitingspin);
		
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
							}else if(comp[i].getName().equals(LABEL_PAUSE) && comp[i] instanceof JSpinner){
								params_.setWaitingTime((Integer) ((JSpinner) comp[i]).getValue());
							}
						}
					}
				}
			}	
		}
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		return new SinglePropertyFilter(bfpprop_.getPropertyLabel());
	}

	@Override
	public String[] getHumanReadableSettings() {
		String[] s = new String[1];
		s[0] = "Exposure = "+params_.getExposureTime()+" ms";
		return s;
	}

	@Override
	public String[][] getAdditionalParameters() {
		return new String[0][0];
	}

	@Override
	public void setAdditionalParameters(String[][] parameters) {
		
	}

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path) throws InterruptedException, IOException {
		// turn on BF
		bfpprop_.setPropertyValue(TwoStateUIProperty.getOnStateLabel());

		SequenceSettings settings = new SequenceSettings();
		settings.save = true;
		settings.timeFirst = true;
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
			Thread.sleep(100);
		}
		
		studio.displays().closeDisplaysFor(store);

		// turn off BF
		bfpprop_.setPropertyValue(TwoStateUIProperty.getOffStateLabel());
		
		store.close();
	}

	@Override
	public void stopAcquisition() {
		// do nothing
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public boolean skipPosition() {
		return false;
	}

	@Override
	public AcquisitionType getType() {
		return AcquisitionType.BFP;
	}

	@Override
	public String getShortName() {
		return "BFP";
	}
}