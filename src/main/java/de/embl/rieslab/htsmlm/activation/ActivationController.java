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

/**
 * Class controlling the activation task.
 *
 */
public class ActivationController {

	private final SystemController systemController_;
	private final ActivationPanel activationPanel_;
	private ActivationTask task_;
	
	private ImagePlus im_;
	private ImageProcessor ip_;
	private int nmsCounter_;

	private static final String WINDOW_TITLE = "NMS results";
	
	public ActivationController(SystemController systemController, ActivationPanel activationPanel) {
		
		systemController_ = systemController;
		activationPanel_ = activationPanel;
		task_ = new ActivationTask(this, systemController_.getStudio(), activationPanel_.getIdleTime());


    	ip_ = new ShortProcessor(200,200);
		im_ = null;
		nmsCounter_ = 0;
	}

	/**
	 * Start or stop the activation task.
	 *
	 * @param start Start the task if true
	 */
	public void runActivation(boolean start){
		if(start && !task_.isRunning()){
			// add processor to the on-the-fly pipeline
			addReadImagePairsProcessor();
			
			task_.startTask();
		} else if(!start && task_.isRunning()){
			task_.stopTask();
		}
	}
		
	private void addReadImagePairsProcessor() {
		// list processors
		List<ProcessorConfigurator> configurator_list = systemController_.getStudio().data().getLivePipelineConfigurators(false);
		int numberOfProcessors = 0;
		int activationProcessorHash = ActivationProcessorConfigurator.getInstance(null).hashCode();

		// count the number of activation processors
		for(ProcessorConfigurator proc: configurator_list) {
			if(proc.hashCode() == activationProcessorHash) {
				numberOfProcessors++;
			}
		}
		
		// make sure there is a single Activation processor
		if(numberOfProcessors == 0) {
			// add processor
			systemController_.getStudio().data().addAndConfigureProcessor(new ReadImagePairsPlugin());
		} else if(numberOfProcessors > 1) {
			// remove them and only add one
			// TODO: this prevents any other processor to be used
			removeAllProcessors();
			systemController_.getStudio().data().addAndConfigureProcessor(new ReadImagePairsPlugin());
		}
	}
	
	private void removeAllProcessors() {
		// remove everything
		systemController_.getStudio().data().clearPipeline();
	}

	/**
	 * Show the NMS result in a separate window.
	 * @param showNMS Show NMS if true
	 */
	public void showNMS(boolean showNMS){
		if(showNMS){
			im_ = new ImagePlus(WINDOW_TITLE, ip_);
			im_.setDisplayRange(im_.getStatistics().min, im_.getStatistics().max);
			im_.show();
		} else {
			im_.close();
		}
	}

	/**
	 * Update results in the activation panel.
	 * @param activationResults Results of the last activation iteration
	 */
	public void updateResults(final ActivationResults activationResults) {
		// show NMS only once every four iteration
		if(activationPanel_.isNMSSelected() && nmsCounter_ % 4 == 0){
			ImageProcessor imp = task_.getNMSResult();
			if(imp != null && imp.getPixels() != null){
				ip_ = imp;

				if(im_ != null) {
					im_.setProcessor(ip_);

					// make sure the display range is tailored to the image
					im_.setDisplayRange(im_.getStatistics().min, im_.getStatistics().max);

					im_.updateAndRepaintWindow();
				}
			}
		} else if(nmsCounter_ == Integer.MAX_VALUE){
			nmsCounter_ = 0;
		}
		
		// update UI with the results
		activationPanel_.updateResults(activationResults);

		// increase NMS counter
		nmsCounter_++;
	}

	/**
	 * Retrieve user-defined activation parameters.
	 * @return User-defined parameters
	 */
	public ActivationParameters retrieveAllParameters() {
		return activationPanel_.getActivationParameters();
	}

	/**
	 * Return whether the activation script is running.
	 * @return True if it is
	 */
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
	 */
	public void shutDown() {
		// stop activation
		task_.stopTask();
		
		// close NMS window
		this.showNMS(false);
		
		// remove on-the-fly processors
		removeAllProcessors();
	}

	/**
	 * Select the activation property by index.
	 * 
	 * @param activationIndex Index of the activation.
	 */
	public void initializeTask(int activationIndex) {
		activationPanel_.setSelectedActivation(activationIndex);
		
		// zero property
		activationPanel_.zeroProperty();
	}
	
	/**
	 * Start task.
	 *
	 * Called from other threads, e.g. acquisition task.
	 */
	public void startTask() {
		if (!isActivationRunning()) { 
			runActivation(true);
		}

		// update UI
		activationPanel_.activationHasStarted();
	}

	/**
	 * Pause activation task.
	 */
	public void pauseTask() {
		activationPanel_.stopActivationUpdate();
	}

	/**
	 * Check if the current activation value has reached the maximum allowed.
	 * @return True if it has
	 */
	public boolean isCriterionReached(){
		String val = activationPanel_.getCurrentActivationValue();
		if(EmuUtils.isNumeric(val)){
			return Double.parseDouble(val) >= activationPanel_.getMaxPulse();
		}
		return false;
	}

	/**
	 * Return a list of activation properties friendly name.
	 *
	 * @return List of names
	 */
	public String[] getActivationPropertiesFriendlyName() {
		return activationPanel_.getFriendlyActivationNames();
	}
}
