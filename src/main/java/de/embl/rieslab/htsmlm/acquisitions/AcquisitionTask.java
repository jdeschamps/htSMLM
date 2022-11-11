package de.embl.rieslab.htsmlm.acquisitions;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.SwingWorker;

import org.micromanager.MultiStagePosition;
import org.micromanager.PositionList;
import org.micromanager.PositionListManager;
import org.micromanager.Studio;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.micromanager.presetgroups.MMPresetGroup;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;
import de.embl.rieslab.htsmlm.acquisitions.ui.AcquisitionTab;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;
import mmcorej.CMMCore;

/**
 * Class performing an experiment by running a set of acquisitions.
 * 
 * @author Joran Deschamps
 *
 */
public class AcquisitionTask{

	private Studio studio_;
	private CMMCore core_;
	private PositionListManager posmanager_;
	private boolean running_ =  false;
	private SystemController systemController_;
	private AcquisitionRun t;
	private AcquisitionController acqController_;
	private Experiment exp_;
	
	private String expName_, expPath_; 
	
	public AcquisitionTask(AcquisitionController acqController, SystemController systemController, Experiment exp, String expName, String expPath){
		systemController_ = systemController;
		studio_ = systemController_.getStudio();
		core_ = studio_.getCMMCore();
		posmanager_ = studio_.getPositionListManager();
				
		exp_ = exp;
		expName_ = expName;
		expPath_ = expPath+File.separator+expName+File.separator;

		acqController_ = acqController;
	}

	/**
	 * Updates the acquisition controller.
	 * 
	 * @param outputs array 
	 */
	public void notifyAcquisitionController(int acquisitionNumber) {
		acqController_.update(acquisitionNumber);
	}

	/**
	 * Start acquisitions.
	 */
	public void startTask() {
		if(!exp_.getAcquisitionList().isEmpty()){
			t = new AcquisitionRun(exp_);
			t.execute();
			running_ = true;
		}
	}

	/**
	 * Stop acquisitions.
	 */
	public void stopTask() {
		if(t != null){
			t.stop();
		}
	}

	/**
	 * Check if the acquisitions are running.
	 * 
	 * @return True if it is, false otherwise.
	 */
	public boolean isRunning() {
		return running_;
	}

	/**
	 * Worker running the list of acquisitions.
	 * 
	 * @author Joran Deschamps
	 *
	 */
	class AcquisitionRun extends SwingWorker<Integer, Integer> {

		private Experiment exp_;
		private Acquisition currentAcq;
		private boolean stop_ = false;

		public AcquisitionRun(Experiment exp) {
			exp_ = exp;
		}

		@Override
		protected Integer doInBackground() throws Exception {	
			Thread.currentThread().setName("Acquisition task");
			return runExperiment();
		}

		private Integer runExperiment() {
			
			int pauseTime = acqController_.retrievePauseTime();
		
			PositionList poslist = posmanager_.getPositionList();
			int numPosition = poslist.getNumberOfPositions();

			if (numPosition > 0) {
				MultiStagePosition currPos;

				// retrieve max number of positions set in the acquisition wizard
				int maxNumPosition = numPosition;
				if (exp_.getNumberPositions() > 0) {
					maxNumPosition = exp_.getNumberPositions();
				}

				// for each position
				for (int i = 0; i < maxNumPosition; i++) {
					// move to next stage position
					currPos = poslist.getPosition(i);
					try {
						
						int sizePos = currPos.size();
						for(int j=0;j<sizePos;j++) {
							String stage = currPos.get(j).getStageDeviceLabel();
							if(currPos.get(j).is2DStagePosition()) {
								core_.setXYPosition(stage, currPos.get(j).get2DPositionX(), currPos.get(j).get2DPositionY());
							} else if(currPos.get(j).is1DStagePosition()) {
								core_.setPosition(stage, currPos.get(j).get1DPosition());
							}
						}

						// let time for the stage to move to position
						Thread.sleep(pauseTime * 1000);
						
						if (stop_) {
							break;
						}

						// perform acquisitions
						performAcquisitions(i);
						
						if (stop_) {
							break;
						}

						// show progress
						publish(i);
						
					} catch (InterruptedException e) {
						log("[htSMLM] InterruptedException when performing pos "+i+".");
						e.printStackTrace();
					} catch (Exception e) {
						log("[htSMLM] Exception when performing pos "+i+".");
						e.printStackTrace();
					}
				}
			} else { // perform on current position
				performAcquisitions(0);
			}

			publish(-1);
			
			return 0;
		}

		private void performAcquisitions(int pos){
			
			// create acq names
			String[] acqShortName = createAcqShortNameSet(exp_.getAcquisitionList()).toArray(new String[0]);
			
			// perform each acquisition sequentially
			for (int k = 0; k < exp_.getAcquisitionList().size(); k++) {

				currentAcq = exp_.getAcquisitionList().get(k);

				// check for abort
				if (stop_) {
					log("[htSMLM] Acquisition interrupted before carrying out "+currentAcq.getShortName()+" and setting config groups.");
					break;
				}
				
				// set-up system
				setUpSystem(currentAcq.getAcquisitionParameters().getPropertyValues());

				// set preset group settings
				setUpConfigGroups(currentAcq);
				
				// set exposure time
				setExposure(currentAcq.getAcquisitionParameters().getExposureTime());
				
				// pause time in ms
				try {
					Thread.sleep(1_000 * currentAcq.getAcquisitionParameters().getWaitingTime());
				} catch (InterruptedException e) {
					log("[htSMLM] InterruptedException when sleeping before acquisition.");
					e.printStackTrace();
				}
				
				if (stop_) {
					log("[htSMLM] Acquisition interrupted before carrying out "+currentAcq.getShortName()+".");
					break;
				}
				
				// build name
				String name = "Pos"+String.valueOf(pos)+"_"+expName_+"_"+acqShortName[k];
				
				// run acquisition
				try {
					currentAcq.performAcquisition(studio_, name, expPath_, exp_.getSaveMode());
				} catch (InterruptedException | IOException e) {
					log("[htSMLM] Failed to perform "+currentAcq.getShortName()+" acquisition.");
					e.printStackTrace();
				} 				
				
				if (stop_) {
					log("[htSMLM] Acquisition interrupted after carrying out "+currentAcq.getShortName()+".");
					break;
				}
			}
		}

		private LinkedHashSet<String> createAcqShortNameSet(ArrayList<Acquisition> acquisitionList) {
			LinkedHashSet<String> names = new LinkedHashSet<String>();
			for (int k = 0; k < exp_.getAcquisitionList().size(); k++) {
				if(!names.add(exp_.getAcquisitionList().get(k).getShortName())) {
					addToSetWithIncrementalName(names, exp_.getAcquisitionList().get(k).getShortName(), 2);
				}
			}			
			return names;
		}

		private void addToSetWithIncrementalName(LinkedHashSet<String> set, String element, int increment) {
			if(!set.add(element+"_"+increment)) {
				addToSetWithIncrementalName(set, element, increment+1);
			}
		}
		
		public void stop() {
			stop_ = true;
			interruptAcquistion();
		}

		private void setExposure(double exposure){
			try {
				core_.setExposure(exposure);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		
		private void interruptAcquistion() {
			if (currentAcq != null && currentAcq.isRunning()) {
				currentAcq.stopAcquisition();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		protected void process(List<Integer> chunks) {
			for(Integer result : chunks){
				if(result >= 0){
					notifyAcquisitionController(result);
				} else if(result == -1){
					running_ = false;
					acqController_.taskDone();
				}
			}
		}
	}

	/**
	 * Set the state of all relevant properties to the values in {@code peropertyValues}.
	 * 
	 * @param propertyValues Map of the properties and values.
	 */
	private void setUpSystem(HashMap<String, String> propertyValues) {
		// set property values
		HashMap<String, UIProperty> uiproperties = systemController_.getPropertiesMap();
		Iterator<String> it = propertyValues.keySet().iterator();
		String s;
		while(it.hasNext()){
			s = it.next();
			if(uiproperties.containsKey(s)){
				uiproperties.get(s).setPropertyValue(propertyValues.get(s));
			}
		}
	}
	
	/**
	 * Set the state of the configuration setting groups specified by {@code currentAcquisition}.
	 * 
	 * @param currentAcquisition Current acquisition.
	 */
	private void setUpConfigGroups(Acquisition currentAcquisition) {
		// if it has configuration group values
		if (!currentAcquisition.getAcquisitionParameters().getMMConfigurationGroupValues().isEmpty()) {
			// extract configuration settings groups
			HashMap<String, String> configs = currentAcquisition.getAcquisitionParameters().getMMConfigurationGroupValues();
			
			// for each configuration settings group
			Iterator<String> it = configs.keySet().iterator();
			while (it.hasNext()) {
				String group = it.next();

				// extract the preset group
				MMPresetGroup configGroup = systemController_.getMMPresetGroupRegistry().getMMPresetGroups().get(group);
				if(configGroup.hasPreset(configs.get(group))) {	// if the preset is known
					try {
						core_.setConfig(group, configs.get(group));
					} catch (Exception e) {
						log("[htSMLM] Exception when setting configuration preset group "+group+".");
						e.printStackTrace();
					}
				} else if(!configs.get(group).equals(AcquisitionTab.KEY_IGNORED)) { // else if it is not ignored and a single preset with a single property, try to set it
					if(configGroup.getGroupSize() == 1 && configGroup.getNumberOfMMProperties() == 1) {
						configGroup.getAffectedProperties().get(0).setValue(configs.get(group), null);
					}
				}
			}
		}
	}
	
	private void log(String message) {
		studio_.logs().logMessage(message);
	}

}
