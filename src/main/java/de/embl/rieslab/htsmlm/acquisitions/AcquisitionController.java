package de.embl.rieslab.htsmlm.acquisitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.ui.uiparameters.UIPropertyParameter;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.htsmlm.AcquisitionPanel;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.ui.AcquisitionWizard;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.AllocatedPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NonPresetGroupPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.ReadOnlyPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.utils.AcquisitionInformationPanel;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import de.embl.rieslab.htsmlm.tasks.Task;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;

public class AcquisitionController implements TaskHolder<Integer>{

	private static String TASK_NAME = "Unsupervised acquisitions";
	
	private AcquisitionPanel owner_;
	private SystemController controller_;
	private AcquisitionInformationPanel infopanel_;
	private Experiment exp_;
	private AcquisitionWizard wizard_;
	private AcquisitionTask task_;

	public AcquisitionController(SystemController controller, AcquisitionPanel owner, AcquisitionInformationPanel infopane){
		controller_ = controller;
		owner_ = owner;
		infopanel_ = infopane;
		
		infopanel_.setInitialText();
		
		// placeholder experiment
		exp_ = new Experiment(0, 0, new ArrayList<Acquisition>());
	}
	
	////////////////////////////////////////////////////////////////////
	///// Taskholder methods
	
	@Override
	public void update(Integer[] output) {
		if (SwingUtilities.isEventDispatchThread()) {
		    owner_.updateProgressBar(output[0]);
		    infopanel_.setPositionDoneText(output[0]);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    owner_.updateProgressBar(output[0]);
				    infopanel_.setPositionDoneText(output[0]);
				}
			});
		}
	}

	@Override
	public Integer[] retrieveAllParameters() {
		Integer[] params = new Integer[1];
		params[0] = exp_.getPauseTime();
		return params;
	}

	@Override
	public boolean startTask() {
		// this is running on the EDT
		
		// set path and experiment name in acquisition
		final String experimentName = owner_.getExperimentName();
		final String folderPath = owner_.getExperimentPath();
		
		if(!isAcquisitionListEmpty() && folderPath != null && experimentName != null && !folderPath.equals("")){	
			final AcquisitionFactory factory = new AcquisitionFactory(this,controller_);
			task_ = new AcquisitionTask(this, controller_, exp_, experimentName, folderPath);
	
			Thread t = new Thread("Set-up acquisition") {
				public void run() {

					// save the acquisition list to the destination folder
					boolean b = factory.writeAcquisitionList(exp_, folderPath, experimentName);
	
					if (!b) {
						// report problem saving
						System.out.println("Error writting acquisition list");
					}
	
					task_.startTask();
				}
	
			};
			t.start();
	
			infopanel_.setStartText();
			owner_.updateProgressBar(0);
			
			return true;
		}
		return false;
	}	
	
	@Override
	public void stopTask() {
		if(task_ != null){
			task_.stopTask();
		}
	}

	@Override
	public boolean isPausable() {
		return false;
	}

	@Override
	public void pauseTask() {
		// Do nothing		
	}

	@Override
	public void resumeTask() {
		// Do nothing
	}

	@Override
	public boolean isTaskRunning() {
		return task_.isRunning();
	}

	@Override
	public String getTaskName() {
		return TASK_NAME;
	}

	@Override
	public boolean isCriterionReached() {
		return false;
	}

	@Override
	public void initializeTask() {
		// Do nothing
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Task getTask() {
		return task_;
	}
	
	@Override
	public void taskDone() {
		if (SwingUtilities.isEventDispatchThread()) {
			done();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					done();
				}
			});
		}
	}

	private void done(){
		if(!isTaskRunning()){		
			owner_.updateProgressBar(getNumberOfPositions());
			owner_.setStateButtonToStop();
			infopanel_.setStopText();
			
			// refresh all properties to make sure things are synchronized
			controller_.forceUpdate();
		}
	}

	public void startWizard() {
		// first, let's grab all the current property values
		HashMap<String, UIProperty> props = (new NonPresetGroupPropertyFilter(new AllocatedPropertyFilter(new ReadOnlyPropertyFilter())))
				.filterProperties(controller_.getPropertiesMap());
		HashMap<String, String> propValues = new HashMap<String, String>();
		
		Iterator<String> it = props.keySet().iterator();
		while(it.hasNext()) {
			String s = it.next();
			propValues.put(s, props.get(s).getPropertyValue());
		}
		
		if(!isAcquisitionListEmpty()){
			wizard_ = new AcquisitionWizard(controller_, this, propValues, exp_);
		} else {
			wizard_ = new AcquisitionWizard(controller_, this, propValues);	
		}
	}

	public void loadExperiment(String path) {
		Experiment exp = loadAcquisitionList(path);	// maybe try catch? to get the exception of null acquitisionlist

		if(exp.getAcquisitionList() != null){
			exp_ = exp;
			infopanel_.setAcquisitionLoaded();	
			infopanel_.setSummaryText(exp_);
		
			// for the moment there is no mechanism to get the expname and exppath
		}
	}
	
	private Experiment loadAcquisitionList(String path) {		
    	return (new AcquisitionFactory(this, controller_)).readAcquisitionList(path);
	}

	public void saveExperiment(String parentPath, String fileName) {
		String name = fileName;
		if (!fileName.endsWith("." + HTSMLMConstants.ACQ_EXT)) {
			name = fileName + "." + HTSMLMConstants.ACQ_EXT;
		}
		(new AcquisitionFactory(this, controller_)).writeAcquisitionList(exp_, parentPath, name);
	}

	public Experiment getExperiment() {
		return exp_;
	}
	
	public void setExperiment(Experiment exp){
		exp_ = exp;
		infopanel_.setSummaryText(exp_);
	}

	public void shutDown() {
		stopTask();
		if(wizard_ != null){
			wizard_.shutDown();
		}
	}

	public int getNumberOfPositions() {
		return controller_.getStudio().getPositionListManager().getPositionList().getNumberOfPositions();
	}

	public boolean isAcquisitionListEmpty() {
		return exp_.getAcquisitionList().isEmpty();
	}

	public boolean isAcquistionPropertyEnabled(AcquisitionType type) {
		if(type.equals(AcquisitionType.BFP) && !owner_.getParameterValues(AcquisitionPanel.PARAM_BFP).equals(UIPropertyParameter.NO_PROPERTY)){
			return true;
		} else if(type.equals(AcquisitionType.BF) && !owner_.getParameterValues(AcquisitionPanel.PARAM_BRIGHTFIELD).equals(UIPropertyParameter.NO_PROPERTY)){
			return true;
		}
		return false;  
	}
	
	public String getAcquisitionParameterValue(String param){
		return owner_.getParameterValues(param);
	}

	@SuppressWarnings("rawtypes")
	public TaskHolder getTaskHolder(String taskName) {
		return owner_.getTaskHolders().get(taskName);
	}
}
