package de.embl.rieslab.htsmlm.acquisitions;

import java.io.File;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

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
import de.embl.rieslab.htsmlm.acquisitions.utils.AcquisitionDialogs;
import de.embl.rieslab.htsmlm.acquisitions.utils.AcquisitionInformationPanel;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;
import de.embl.rieslab.htsmlm.activation.ActivationController;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;


/**
 * Class controlling the acquisitions.
 * 
 * @author Joran Deschamps
 *
 */
public class AcquisitionController{
	
	private AcquisitionPanel acquisitionPanel_;
	private SystemController systemController_;
	private AcquisitionInformationPanel infoPanel_;
	private Experiment exp_;
	private AcquisitionWizard wizard_;
	private AcquisitionTask task_;
	private ActivationController activationController_;

	public AcquisitionController(SystemController controller, 
								 AcquisitionPanel owner, 
								 AcquisitionInformationPanel infoPanel, 
								 ActivationController activationController){
		systemController_ = controller;
		acquisitionPanel_ = owner;
		activationController_ = activationController;
		
		// initiate info panel
		infoPanel_ = infoPanel;
		infoPanel_.setInitialText();
		
		// placeholder experiment
		exp_ = new Experiment(0, 0, systemController_.getStudio().data().getPreferredSaveMode(), new ArrayList<Acquisition>());
	}
	

	/**
	 * Update the acquisition step.
	 * 
	 * @param acquisitionStep
	 */
	public void update(int acquisitionStep) {
		if (SwingUtilities.isEventDispatchThread()) {
		    acquisitionPanel_.updateProgressBar(acquisitionStep);
		    infoPanel_.setPositionDoneText(acquisitionStep);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    acquisitionPanel_.updateProgressBar(acquisitionStep);
				    infoPanel_.setPositionDoneText(acquisitionStep);
				}
			});
		}
	}

	/**
	 * Get pause time between acquisitions.
	 * 
	 * @return Pause time in seconds.
	 */
	public int retrievePauseTime() {
		return exp_.getPauseTime();
	}

	/**
	 * Start acquisition.
	 * 
	 * @return True if the acquisition started, false if the path or name were invalid.
	 */
	public boolean startAcquisition() {
		// this is running on the EDT
		
		// get path and experiment name
		final String experimentName = acquisitionPanel_.getExperimentName();
		final String folderPath = acquisitionPanel_.getExperimentPath();
		
		if(!isAcquisitionListEmpty() && folderPath != null && experimentName != null && !folderPath.equals("")){	
			task_ = new AcquisitionTask(this, systemController_, exp_, experimentName, folderPath);
	
			Thread t = new Thread("Run acquisitions") {
				public void run() {

					// save the acquisition list to the destination folder
					boolean b = AcquisitionFactory.writeAcquisitionList(exp_, folderPath, experimentName);
	
					if (!b) {
						// report problem saving
						systemController_.getStudio().logs().logDebugMessage("[htSMLM] Error writing acquisition list");
					}
	
					task_.startTask();
				}
	
			};
			t.start();
	
			infoPanel_.setStartText();
			acquisitionPanel_.updateProgressBar(0);
			
			return true;
		}
		return false;
	}	
	
	/**
	 * Stop acquisition.
	 */
	public void stopAcquisition() {
		if(task_ != null){
			task_.stopTask();
		}
	}
	
	/**
	 * Check if acquisition thread is running.
	 * 
	 * @return True if it is, false otherwise.
	 */
	public boolean isAcquisitionRunning() {
		if(task_ != null){
			return task_.isRunning();
		}
		return false;
	}
	
	/**
	 * Notify the controller that the acquisition is done.
	 */
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
		if(!isAcquisitionRunning()){		
			acquisitionPanel_.updateProgressBar(getNumberOfPositions());
			acquisitionPanel_.showStop();
			infoPanel_.setStopText();
			
			// refresh all properties to make sure things are synchronised
			systemController_.forceUpdate();
		}
	}

	/**
	 * Start the acquisition wizard.
	 */
	public void startWizard() {
		// first, let's grab all the current property values
		// create a filter: no read only -> only allocated -> non preset group
		NonPresetGroupPropertyFilter filteredProperties = new NonPresetGroupPropertyFilter(new AllocatedPropertyFilter(new ReadOnlyPropertyFilter()));
		HashMap<String, UIProperty> props = filteredProperties.filterProperties(systemController_.getPropertiesMap());
		
		// create map of all property values
		HashMap<String, String> propValues = new HashMap<String, String>();
		Iterator<String> it = props.keySet().iterator();
		while(it.hasNext()) {
			String s = it.next();
			propValues.put(s, props.get(s).getPropertyValue());
		}
		
		// start the acquisition wizard
		if(!isAcquisitionListEmpty()){
			wizard_ = new AcquisitionWizard(systemController_, this, propValues, exp_);
		} else {
			wizard_ = new AcquisitionWizard(systemController_, this, propValues);	
		}
	}

	/**
	 * Load experiment from json file.
	 * 
	 * @param path Path to file.
	 */
	public void loadExperiment(String path) {
		Experiment exp = loadAcquisitionList(path);

		if(exp.getAcquisitionList() != null){
			exp_ = exp;
			infoPanel_.setAcquisitionLoaded();	
			infoPanel_.setSummaryText(exp_);
		
			// for the moment there is no mechanism to get the expname and exppath
		}
	}
	
	private Experiment loadAcquisitionList(String path) {		
    	return (new AcquisitionFactory(this, systemController_)).readAcquisitionList(path);
	}

	/**
	 * Save experiment to json file.
	 * 
	 * @param parentPath Path to parent folder
	 * @param fileName File name
	 */
	public void saveExperiment(String parentPath, String fileName) {
		String name = fileName;
		if (!fileName.endsWith("." + HTSMLMConstants.ACQ_EXT)) {
			name = fileName + "." + HTSMLMConstants.ACQ_EXT;
		}
		AcquisitionFactory.writeAcquisitionList(exp_, parentPath, name);
	}

	/**
	 * Get current experiment.
	 * 
	 * @return Experiment
	 */
	public Experiment getExperiment() {
		return exp_;
	}
	
	/**
	 * Set experiment and update the UI.
	 * 
	 * @param exp
	 */
	public void setExperiment(Experiment exp){
		exp_ = exp;
		infoPanel_.setSummaryText(exp_);
		acquisitionPanel_.getSummaryTreeController().updateSummary();
	}

	public void loadAcquisitionList(){
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Acquisition list", HTSMLMConstants.ACQ_EXT);
		fileChooser.setFileFilter(filter);
		int result = fileChooser.showOpenDialog(new JFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    String path = selectedFile.getAbsolutePath();
		    
		    // load
		    loadExperiment(path);
	    }
	}

	public void saveAcquisitionList() {
		if(isAcquisitionListEmpty()){
			AcquisitionDialogs.showNoAcqMessage();
		} else {	
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Acquisition list", HTSMLMConstants.ACQ_EXT);
			fileChooser.setFileFilter(filter);
			int result = fileChooser.showSaveDialog(new JFrame());
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String parentFolder = selectedFile.getParent();
				String fileName = selectedFile.getName();
				
				// save
				saveExperiment(parentFolder, fileName);
			}
		}
	}
	
	
	/**
	 * Shut down. 
	 */
	public void shutDown() {
		stopAcquisition();
		if(wizard_ != null){
			wizard_.shutDown();
		}
	}

	public int getNumberOfPositions() {
		return systemController_.getStudio().getPositionListManager().getPositionList().getNumberOfPositions();
	}

	public int getCurrentPositionIndex() {
		if(task_ != null){
			return task_.getCurrentPosition();
		}
		return -1;
	}
	
	public boolean isAcquisitionListEmpty() {
		return exp_.getAcquisitionList().isEmpty();
	}

	public boolean isAcquistionPropertyEnabled(AcquisitionType type) {
		if(type.equals(AcquisitionType.BFP) && !acquisitionPanel_.getParameterValues(AcquisitionPanel.PARAM_BFP).equals(UIPropertyParameter.NO_PROPERTY)){
			return true;
		} else if(type.equals(AcquisitionType.BF) && !acquisitionPanel_.getParameterValues(AcquisitionPanel.PARAM_BRIGHTFIELD).equals(UIPropertyParameter.NO_PROPERTY)){
			return true;
		}
		return false;  
	}
	
	public String getAcquisitionParameterValue(String param){
		return acquisitionPanel_.getParameterValues(param);
	}

	public ActivationController getActivationController() {
		return activationController_;
	}

	public AcquisitionPanel getAcquisitionPanel() {
		return acquisitionPanel_;
	}

}
