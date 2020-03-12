package de.embl.rieslab.htsmlm.acquisitions;


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
import de.embl.rieslab.htsmlm.tasks.Task;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;
import mmcorej.CMMCore;

public class AcquisitionTask implements Task<Integer>{

	private Studio studio_;
	private CMMCore core_;
	private PositionListManager posmanager_;
	private boolean running_ =  false;
	private SystemController system_;
	private AcquisitionRun t;
	private TaskHolder<Integer> holder_;
	private Experiment exp_;
	
	private String expname_, exppath_; 
	
	public AcquisitionTask(TaskHolder<Integer> holder, SystemController system, Experiment exp, String expname, String exppath){
		system_ = system;
		studio_ = system_.getStudio();
		core_ = studio_.getCMMCore();
		posmanager_ = studio_.getPositionListManager();
				
		exp_ = exp;
		expname_ = expname;
		exppath_ = exppath+"/"+expname+"/";
		
		registerHolder(holder);
	}
	
	@Override
	public void registerHolder(TaskHolder<Integer> holder) {
		holder_ = holder;		
	}

	@Override
	public void notifyHolder(Integer[] outputs) {
		holder_.update(outputs);
	}

	@Override
	public void startTask() {
		if(!exp_.getAcquisitionList().isEmpty()){
			t = new AcquisitionRun(exp_);
			t.execute();
			running_ = true;
		}
	}

	@Override
	public void stopTask() {
		if(t != null){
			t.stop();
		}
	}

	@Override
	public boolean isRunning() {
		return running_;
	}

	@Override
	public boolean isPausable() {
		return false;
	}

	@Override
	public void pauseTask() {
		// do nothing
	}

	@Override
	public void resumeTask() {
		// do nothin
	}
	
	class AcquisitionRun extends SwingWorker<Integer, Integer> {

		private Experiment exp_;
		private Acquisition currAcq;
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
			
			Integer[] param = holder_.retrieveAllParameters();
		
			PositionList poslist = posmanager_.getPositionList();
			int numPosition = poslist.getNumberOfPositions();

			if (numPosition > 0) {
				MultiStagePosition currPos;

				// retrieve max number of positions
				int maxNumPosition = numPosition;
				if (exp_.getNumberPositions() > 0) {
					maxNumPosition = exp_.getNumberPositions();
				}

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
						Thread.sleep(param[0] * 1000);
						
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
						System.out.println("[htSMLM] InterruptedException when performing pos "+i+".");
						e.printStackTrace();
					} catch (Exception e) {
						System.out.println("[htSMLM] Exception when performing pos "+i+".");
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

				currAcq = exp_.getAcquisitionList().get(k);

				if (stop_) {
					System.out.println("[htSMLM] Acquisition interrupted before carrying "+currAcq.getShortName()+" out and setting config groups.");
					break;
				}
				// set-up system
				setUpSystem(currAcq.getAcquisitionParameters().getPropertyValues());

				// set preset group settings
				if (!currAcq.getAcquisitionParameters().getMMConfigurationGroupValues().isEmpty()) {
					HashMap<String, String> configs = currAcq.getAcquisitionParameters().getMMConfigurationGroupValues();
					Iterator<String> it = configs.keySet().iterator();
					while (it.hasNext()) {
						String group = it.next();

						MMPresetGroup configgroup = system_.getMMPresetGroupRegistry().getMMPresetGroups().get(group);
						if(configgroup.hasPreset(configs.get(group))) {	// if the preset is known
							try {
								core_.setConfig(group, configs.get(group));
							} catch (Exception e) {
								System.out.println("[htSMLM] Exception when setting configuration preset group "+group+".");
								e.printStackTrace();
							}
						} else if(!configs.get(group).equals(AcquisitionTab.KEY_IGNORED)) { // else if it is not ignored and a single presets with a single property, try to set it
							if(configgroup.getGroupSize() == 1 && configgroup.getNumberOfMMProperties() == 1) {
								configgroup.getAffectedProperties().get(0).setValue(configs.get(group), null);
							}
						}
					}
				}
				
				// set exposure time
				setExposure(currAcq.getAcquisitionParameters().getExposureTime());
				
				// pause time in ms
				try {
					Thread.sleep(1000*currAcq.getAcquisitionParameters().getWaitingTime());
				} catch (InterruptedException e) {
					System.out.println("[htSMLM] InterruptedException when sleeping before acquisition.");
					e.printStackTrace();
				}
				
				if (stop_) {
					System.out.println("[htSMLM] Acquisition interrupted before carrying "+currAcq.getShortName()+" out.");
					break;
				}
				
				// build name
				String name = "Pos"+String.valueOf(pos)+"_"+expname_+"_"+acqShortName[k];
				
				// run acquisition
				try {
					currAcq.performAcquisition(studio_, name, exppath_);
				} catch (InterruptedException | IOException e) {
					System.out.println("[htSMLM] Failed to perform "+currAcq.getShortName()+" acquisition.");
					e.printStackTrace();
				} 				
				
				if (stop_) {
					System.out.println("[htSMLM] Acquisition interrupted after carrying "+currAcq.getShortName()+" out.");
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
			if (currAcq != null && currAcq.isRunning()) {
				currAcq.stopAcquisition();
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
				if(result>=0){
					Integer[] results = {result};
					notifyHolder(results);
				} else if(result == -1){
					running_ = false;
					holder_.taskDone();
				}
			}
		}
	}

	public void setUpSystem(HashMap<String, String> propertyValues) {
		HashMap<String, UIProperty> uiproperties = system_.getPropertiesMap();
		Iterator<String> it = propertyValues.keySet().iterator();
		String s;
		while(it.hasNext()){
			s = it.next();
			if(uiproperties.containsKey(s)){
				uiproperties.get(s).setPropertyValue(propertyValues.get(s));
			}
		}
	}

}
