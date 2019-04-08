package main.java.embl.rieslab.htsmlm.tasks;

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
import org.micromanager.data.Datastore;
import org.micromanager.data.Datastore.SaveMode;

import main.java.embl.rieslab.emu.controller.SystemController;
import main.java.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;
import main.java.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;
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
		private Datastore currAcqStore;
		private Acquisition currAcq;
		private boolean stop_ = false;

		public AcquisitionRun(Experiment exp) {
			exp_ = exp;
		}

		@Override
		protected Integer doInBackground() throws Exception {
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

						// perform acquisitions
						performAcquisitions(i);
						
						if (stop_) {
							System.out.println("Stop is true in position");
							break;
						}

						// show progress
						publish(i);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else { // perform on current position
				System.out.println("Position list empty");
				
				// TODO show message?
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

				// set-up system
				system_.setUpSystem(currAcq.getAcquisitionParameters().getPropertyValues());

				// set configuration settings
				if (!currAcq.getAcquisitionParameters().getMMConfigurationGroupValues().isEmpty()) {
					HashMap<String, String> configs = currAcq.getAcquisitionParameters().getMMConfigurationGroupValues();
					Iterator<String> it = configs.keySet().iterator();
					while (it.hasNext()) {
						String group = it.next();
						try {
							core_.setConfig(group, configs.get(group));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				// build name
				String name = "Pos"+String.valueOf(pos)+"_"+expname_+"_"+acqShortName[k];
				
				// run acquisition
				SaveMode sm = studio_.data().getPreferredSaveMode();
				if(sm == SaveMode.MULTIPAGE_TIFF){
					try {
						currAcqStore = studio_.data().createMultipageTIFFDatastore(exppath_+name, true, true);
						currAcq.performAcquisition(studio_, currAcqStore);
						currAcqStore.close();

					} catch (IOException e) {
						stop_ = true;
						System.out.println("Failed to create multi page TIFF");
						e.printStackTrace();
					}
				} else {
					try {
						currAcqStore = studio_.data().createSinglePlaneTIFFSeriesDatastore(exppath_+name);
						currAcq.performAcquisition(studio_, currAcqStore);
						currAcqStore.close();

					} catch (IOException e) {
						stop_ = true;
						System.out.println("Failed to create single page TIFF");
						e.printStackTrace();
					}
				}
								
				if (stop_) {
					System.out.println("Stop is true in acquisition");
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
			return null;
		}

		private void addToSetWithIncrementalName(LinkedHashSet<String> set, String element, int increment) {
			if(!set.add(element+"_"+increment)) {
				addToSetWithIncrementalName(set, element, increment++);
			}
		}
		
		/*
		private String createAcqName(Acquisition acq, int i){
			String acqname;
			if (i < 10) {
				acqname = "00" + i + "_" + expname_ + "_"+ acq.getType();
			} else if (i < 100) {
				acqname = "0" + i + "_" + expname_ + "_"+ acq.getType();
			} else {
				acqname = i + "_" + expname_ + "_"+ acq.getType();
			}
			return acqname;
		}*/
		
		public void stop() {
			stop_ = true;
			interruptAcquistion();
		}

		private void interruptAcquistion() {
			while (currAcq != null && currAcq.isRunning()) {
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

}
