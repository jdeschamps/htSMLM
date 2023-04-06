package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.micromanager.Studio;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.SequenceSettings.Builder;
import org.micromanager.acquisition.internal.DefaultAcquisitionManager;
import org.micromanager.data.Datastore;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;


public class TimeAcquisition implements Acquisition{
	
	private GenericAcquisitionParameters params_;
	
	private final static String PANE_NAME = "Time panel";
	private final static String LABEL_EXPOSURE = "Exposure (ms):";
	private final static String LABEL_PAUSE = "Pause (s):";
	private final static String LABEL_NUMFRAME = "Number of frames:";
	private final static String LABEL_INTERVAL = "Interval (ms):";
	
	private volatile boolean stopAcq_, running_;
	
	public TimeAcquisition(double exposure) {
		stopAcq_ = false;
		running_ = false;

		params_ = new GenericAcquisitionParameters(AcquisitionType.TIME, 
				exposure, 0, 3, 30000, new HashMap<String,String>(), new HashMap<String,String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		
		pane.setName(getPanelName());
		
		JLabel exposurelab, waitinglab, numframelab, intervallab;
		JSpinner exposurespin, waitingspin, numframespin, intervalspin;
		
		exposurelab = new JLabel(LABEL_EXPOSURE);
		waitinglab = new JLabel(LABEL_PAUSE);
		numframelab = new JLabel(LABEL_NUMFRAME);
		intervallab = new JLabel(LABEL_INTERVAL);
		
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
		
		
		int nrow = 2;
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
		
		panelHolder[0][1].add(exposurespin);
		panelHolder[1][1].add(waitingspin);
		
		panelHolder[0][2].add(numframelab);
		panelHolder[1][2].add(intervallab);
		
		panelHolder[0][3].add(numframespin);
		panelHolder[1][3].add(intervalspin);

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
							}else if(comp[i].getName().equals(LABEL_NUMFRAME) && comp[i] instanceof JSpinner){
								params_.setNumberFrames((Integer) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_INTERVAL) && comp[i] instanceof JSpinner){
								params_.setIntervalMs((Double) ((JSpinner) comp[i]).getValue());
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
		String[] s = new String[3];
		s[0] = "Exposure = "+params_.getExposureTime()+" ms";
		s[1] = "Number of frames = "+params_.getNumberFrames();
		s[2] = "Interval = "+params_.getIntervalMs()+" ms";
		return s;
	}

	@Override
	public String getPanelName() {
		return PANE_NAME;
	}

	@Override
	public String[][] getAdditionalParameters() {
		String[][] s = new String[0][0];
		return s;
	}

	@Override
	public void setAdditionalParameters(String[][] parameters) {
		// do nothing
	}

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}

	@Override
	public void performAcquisition(Studio studio, String name, String path, Datastore.SaveMode savemode) throws InterruptedException, IOException {		
		stopAcq_ = false;
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
			// check if exit
			if(stopAcq_){
				interruptAcquisition(studio);
			}
			
			Thread.sleep(500);
		}
		
		studio.displays().closeDisplaysFor(store);
		store.close();
		
		running_ = false;
	}

	private void interruptAcquisition(Studio studio) {
		try {
			studio.acquisitions().abortAcquisition();
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
	public AcquisitionType getType() {
		return AcquisitionType.TIME;
	}

	@Override
	public String getShortName() {
		return "t";
	}

	/*
	@Override
	public Builder getBuilder(String path, String name, GenericAcquisitionParameters params) {
		
		Builder seqBuilder = new SequenceSettings.Builder();
		
		
		seqBuilder.save(true);
		seqBuilder.timeFirst(true);
		seqBuilder.usePositionList(false);
		seqBuilder.root(path);
		seqBuilder.prefix(name);
		seqBuilder.numFrames(params_.getNumberFrames());
		seqBuilder.intervalMs(0);
		seqBuilder.shouldDisplayImages(true);
		seqBuilder.useFrames(true);
		
		
		//seqBuilder.acqOrderMode(0) ;
		//seqBuilder.cameraTimeout(1000) ;
		//seqBuilder.channelGroup("") ;
		//seqBuilder.channels(null) ;
		//seqBuilder.comment(java.lang.String c) ;
		//seqBuilder.customIntervalsMs(java.util.ArrayList<java.lang.Double> c) ;
		seqBuilder. 	displayTimeUnit(int d) ;
		seqBuilder. 	intervalMs(double d) ;
		seqBuilder. 	keepShutterOpenChannels(boolean k) ;
		seqBuilder. 	keepShutterOpenSlices(boolean k) ;
		seqBuilder. 	numFrames(int nFrames) ;
		seqBuilder. 	prefix(java.lang.String p) ;
		seqBuilder. 	relativeZSlice(boolean r) ;
		seqBuilder. 	root(java.lang.String r) ;
		seqBuilder. 	save(boolean s) ;
		seqBuilder. 	saveMode(Datastore.SaveMode s) ;
		seqBuilder. 	shouldDisplayImages(boolean s) ;
		seqBuilder. 	skipAutofocusCount(int s) ;
		seqBuilder. 	slices(java.util.ArrayList<java.lang.Double> s) ;
		seqBuilder. 	slicesFirst(boolean s) ;
		seqBuilder. 	sliceZBottomUm(double s) ;
		seqBuilder. 	sliceZStepUm(double s) ;
		seqBuilder. 	sliceZTopUm(double s) ;
		seqBuilder. 	timeFirst(boolean t) ;
		seqBuilder. 	useAutofocus(boolean u) ;
		seqBuilder. 	useChannels(boolean u) ;
		seqBuilder. 	useCustomIntervals(boolean use) ;
		seqBuilder. 	useFrames(boolean u) ;
		seqBuilder. 	usePositionList(boolean u) ;
		seqBuilder. 	useSlices(boolean u) ;
		seqBuilder. 	zReference(double z);
		return seqBuilder;
	}*/

}