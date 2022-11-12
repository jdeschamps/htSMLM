package de.embl.rieslab.htsmlm.activation;

import java.util.List;

import org.micromanager.data.ProcessorConfigurator;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.ActivationPanel;
import de.embl.rieslab.htsmlm.activation.processor.ActivationProcessorConfigurator;
import de.embl.rieslab.htsmlm.activation.processor.ReadImagePairsPlugin;
import de.embl.rieslab.htsmlm.activation.utils.ActivationParameters;
import de.embl.rieslab.htsmlm.activation.utils.ActivationResults;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class ActivationController {

	public final static String TASK_NAME = "Activation task";

	private SystemController systemController_;
	private ActivationPanel activationPanel_;
	private ActivationTask task_;
	
	
	private ImagePlus im_;
	private ImageProcessor ip_;
	private int nmsCounter_;
	
	
	public ActivationController(SystemController systemController, ActivationPanel activationPanel) {
		
		systemController_ = systemController;
		activationPanel_ = activationPanel;
		task_ = new ActivationTask(this, systemController_.getStudio(), activationPanel_.getIdleTime());


    	ip_ = new ShortProcessor(200,200);
		im_ = new ImagePlus("NMS result");
		nmsCounter_ = 0;
	}

	public void runActivation(boolean b){
		if(b && !task_.isRunning()){
			// add processor to the on-the-fly pipeline
			addReadImagePairsProcessor();
			
			task_.startTask();
		} else if(!b && task_.isRunning()){
			task_.stopTask();
		}
	}
		
	private void addReadImagePairsProcessor() {
		// TODO this will stop working for compiled htSMLM !!

		// list processors
		List<ProcessorConfigurator> configurator_list = systemController_.getStudio().data().getLivePipelineConfigurators(false);
		int n_act = 0;
		int act_hash = ActivationProcessorConfigurator.getInstance(null).hashCode();
		for(ProcessorConfigurator o: configurator_list) {
			// check if it is the activation processor
			if(o.hashCode() == act_hash) {
				// count the number of activation processors
				n_act++;
			}
		}
		
		// if there are no processor or multiple
		if(n_act == 0) { 
			// add processor
			systemController_.getStudio().data().addAndConfigureProcessor(new ReadImagePairsPlugin());
		} else if(n_act > 1) {
			// remove them and only add one
			systemController_.getStudio().data().clearPipeline();
			systemController_.getStudio().data().addAndConfigureProcessor(new ReadImagePairsPlugin());
		}
	}
	
	private void removeReadImagePairsProcessor() {
		// remove everything
		systemController_.getStudio().data().clearPipeline();
	}
	
	public void showNMS(boolean b){
		if(b){
			im_.setProcessor(ip_);
			im_.setDisplayRange(im_.getStatistics().min, im_.getStatistics().max);
			im_.show();
		} else {
			im_.close();
		}
	}
	
	public void updateResults(final ActivationResults output) {
		if(activationPanel_.isNMSSelected() && nmsCounter_ % 4 == 0){
			ImageProcessor imp = task_.getNMSResult();
			if(imp != null && imp.getPixels() != null){
				ip_ = imp;
				im_.setProcessor(ip_);
				im_.setDisplayRange(im_.getStatistics().min, im_.getStatistics().max);
				im_.updateAndRepaintWindow();
			}
		} else if(nmsCounter_ == Integer.MAX_VALUE){
			nmsCounter_ = 0;
		}
		
		// update UI with the results
		activationPanel_.updateResults(output);
		
		nmsCounter_++;
	}

	public ActivationParameters retrieveAllParameters() {
		return activationPanel_.getActivationParameters();
	}

	public boolean isActivationRunning() {
		return task_.isRunning();
	}

	/**
	 * Update the idle time (ms) of the activation task.
	 * 
	 * @param idleTime New idle time (ms)
	 */
	public void updateIdleTime(int idleTime) {
		if(task_ != null) {
			task_.setIdleTime(idleTime);
		}
	}

	/**
	 * Shut down activation.
	 * 
	 */
	public void shutDown() {
		// stop activation
		task_.stopTask();
		
		// close NMS window
		this.showNMS(false);
		
		// remove on-the-fly processor
		removeReadImagePairsProcessor();
	}

	/**
	 * Select the activation property by index.
	 * 
	 * @param index
	 */
	public void initializeTask(int index) {
		activationPanel_.setSelectedActivation(index);
		
		// zero property
		activationPanel_.zeroProperty();
	}
	
	/**
	 * Called from other threads, e.g. acquisition task.
	 * 
	 * @return
	 */
	public boolean startTask() {
		if (!isActivationRunning()) { 
			runActivation(true);
		}

		// update UI
		return activationPanel_.startTask();
	}

	public void pauseTask() {
		activationPanel_.pauseTask();
	}

	public boolean isCriterionReached(){
		String val = activationPanel_.getCurrentActivationValue();
		if(EmuUtils.isNumeric(val)){
			if(Double.parseDouble(val) >= activationPanel_.getMaxPulse()){
				return true;
			}
		}
		return false;
	}

	public void resumeTask() {
		startTask();
	}

	public String[] getActivationPropertiesName() {
		return activationPanel_.getPropertiesName();
	}
}
