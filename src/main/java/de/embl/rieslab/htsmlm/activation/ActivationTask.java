package de.embl.rieslab.htsmlm.activation;

import de.embl.rieslab.htsmlm.activation.processor.ActivationProcessor;
import de.embl.rieslab.htsmlm.activation.utils.ActivationParameters;
import de.embl.rieslab.htsmlm.activation.utils.ActivationResults;
import de.embl.rieslab.htsmlm.activation.utils.NMSUtils;
import de.embl.rieslab.htsmlm.utils.Pair;
import de.embl.rieslab.htsmlm.utils.Peak;

import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import de.embl.rieslab.htsmlm.utils.NMS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingWorker;

import org.micromanager.Studio;
import org.micromanager.data.Image;

import mmcorej.CMMCore;

/**
 * Computes the density of emitters and update to the activation laser
 * power.
 *
 * In brief, the script takes polls two images from the processing
 * pipeline and subtract them. It then runs a non-maximum suppression
 * algorithm. The ratio between required and measure number of molecules
 * is finally used to update the activation laser power.
 *
 * @author Joran Deschamps
 *
 */
public class ActivationTask {

	private final static double LOW_QUANTILE = 0.2;
	private final static double HIGH_QUANTILE = 0.8;

	private final CMMCore core_;
	private final ActivationController activationController_;
	private AutomatedActivation worker_;
	private AtomicBoolean running_;
	private ActivationResults output_;
	private ActivationProcessor processor_;
	private ImageProcessor ip_;
	private int idleTime_;
	
	private double dp;

	public ActivationTask(ActivationController activationController, Studio studio, int idle){
		running_ = new AtomicBoolean(false);
		
		core_ = studio.core();
		idleTime_ = idle;

		activationController_ = activationController;
		
		dp = 0; // previous activation laser pulse (note: can be power)
		output_ = new ActivationResults();
		processor_ = ActivationProcessor.getInstance();
	}

	/**
	 * Start activation task.
	 */
	public void startTask() {		
		// start activation
		worker_ = new AutomatedActivation();
		worker_.execute();
		running_.set(true);
	}

	/**
	 * Stop activation task.
	 */
	public void stopTask() {
		running_.set(false);
	}

	/**
	 * Check if the activation task is running.
	 *
	 * @return True if it is running.
	 */
	public boolean isRunning() {
		return running_.get();
	}

	/**
	 * Change the idle time between each iteration of the task.
	 *
	 * @param idle Idle time in ms.
	 */
	public void setIdleTime(int idle){
		idleTime_ = idle;
	}

	/**
	 * Grab two images from the processor queue.
	 *
	 * @return Pair of images
	 */
	protected Pair<Image, Image> getTwoImages(){
		processor_.startQueueing();
		
		// wait until the queue is full
		while(processor_.getQueueSize() < 2) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
		
		processor_.stopQueueing();
		
		// return two frames
		return new Pair<Image, Image>(processor_.poll(), processor_.poll());
	}

	/**
	 * Blur a FloatProcessor using fixed htSMLM constants.
	 *
	 * @param fp FloatProcessor to be blurred.
	 */
	static protected void blur(FloatProcessor fp){
		GaussianBlur gau = new GaussianBlur();
		gau.blurGaussian(
				fp,
				HTSMLMConstants.gaussianMaskSize,
				HTSMLMConstants.gaussianMaskSize,
				HTSMLMConstants.gaussianMaskPrecision
		);
	}
	

	private float[] subtract8bits(Image image1, Image image2) {
		// get the two pixel arrays
		byte[] pixels1 = (byte[]) image1.getRawPixels();
		byte[] pixels2 = (byte[]) image2.getRawPixels();

		float[] pixels_sub = new float[pixels1.length];
		for(int i=0; i<pixels1.length; i++){
			float sub = (float) (pixels1[i] - pixels2[i]);

			// set negative pixels to 0
			pixels_sub[i] = Math.max(sub, 0f);
		}
		return pixels_sub;
	}
	
	private float[] subtract16bits(Image image1, Image image2) {
		// get the two pixel arrays
		short[] pixels1 = (short[]) image1.getRawPixels();
		short[] pixels2 = (short[]) image2.getRawPixels();

		float[] pixels_sub = new float[pixels1.length];
		for(int i=0; i<pixels1.length; i++){
			float sub = (float) (pixels1[i] - pixels2[i]);

			// set negative pixels to 0
			pixels_sub[i] = Math.max(sub, 0f);
		}
		return pixels_sub;
	}

	/**
	 * Compute a Gaussian blurred subtraction between a pair of Images.
	 *
	 * @param taggedPair Pair of Images
	 * @return Blurred difference image
	 */
	protected FloatProcessor computeGaussianSubtraction(Pair<Image, Image> taggedPair){
		Image image1 = taggedPair.getFirst();
		Image image2 = taggedPair.getSecond();
		
		// get subtraction
		float[] pixels_sub;
		if(image1.getBytesPerPixel() == 2) {
			pixels_sub = subtract16bits(image1, image2);
		} else if (image1.getBytesPerPixel() == 1) {
			pixels_sub = subtract8bits(image1, image2);
		} else {
			throw new IllegalArgumentException("Only 8 and 16 bits images are supported.");
		}

		// create FloatProcessor
		int width = (int) core_.getImageWidth();
		int height = (int) core_.getImageHeight();
		FloatProcessor fp = new FloatProcessor(width, height);
		fp.setPixels(pixels_sub);

		// Gaussian filter
		blur(fp);

		return fp;
	}

	/**
	 * Run maximum suppression on an image.
	 *
	 * @param image image
	 * @return NMS object
	 */
	static protected NMS runNMS(FloatProcessor image){
		return new NMS(image, HTSMLMConstants.nmsMaskSize);
	}

	/**
	 * Compute an intensity cutoff by approximating the quantile distribution of molecule
	 * intensities using a linear relationship and taking the intensity corresponding to a
	 * quantile equal to that of the median + a dynamic factor ("quantile over median").
	 *
	 * The resulting cutoff is a running average.
	 *
	 * @param peaks List of peak intensities.
	 * @param cutoff Current cutoff.
	 * @param dynamicFactor Factor determining the "quantile over median" corresponding to the
	 *                      cutoff intensity.
	 * @param cutoffWeight Running average weight in favor of the new cutoff.
	 * @return New intensity cutoff value.
	 */
	static protected double computeCutOff(ArrayList<Peak> peaks, double cutoff, double dynamicFactor, double cutoffWeight){
		// get quantiles corresponding to high, low and median
		double q1 = NMSUtils.getQuantile(peaks, LOW_QUANTILE); // 0.2
		double q2 = NMSUtils.getQuantile(peaks, HIGH_QUANTILE); // 0.8
		double median = NMSUtils.getQuantile(peaks, 0.5);

		// approximate linear slope
		double slope = (q2 - q1) / (HIGH_QUANTILE - LOW_QUANTILE);

		// compute cutoff as the intensity of the median plus an additional quantity
		// corresponding to a "quantile over median" value, a.k.a the dynamic factor
		double tempCutoff = median + slope * dynamicFactor;

		// compute new cutoff using a weighted average
		double newCutoff = (1 - cutoffWeight) * cutoff + cutoffWeight * tempCutoff;

		// limit the new cutoff to one decimal value
		newCutoff = Math.floor(10 * newCutoff + 0.5) / 10;

		return newCutoff;
	}

	private void getN(double dynamicFactor, double cutoff, double averagingWeight, boolean autoCutoff) {
		if (core_.isSequenceRunning()) {
			// grab two images from processor pipeline
			Pair<Image, Image> pairs = getTwoImages();

			// compute Gaussian blurred difference
			FloatProcessor imp = computeGaussianSubtraction(pairs);

			// run nms
			NMS nms = runNMS(imp);

			// compute cutoff
			double newCutoff;
			if(autoCutoff){
				newCutoff = computeCutOff(nms.getPeaks(), cutoff, dynamicFactor, averagingWeight);
			} else {
				// keep user defined cutoff
				newCutoff = cutoff;
			}

			// filter peak list based on the cutoff
			ArrayList<Peak> peaks = NMSUtils.filterPeaks(nms, newCutoff);

			// apply cutoff
			ip_ = NMSUtils.applyCutoff(nms, peaks);

			// create output
			output_.setNewCutoff(newCutoff);
			output_.setNewN(peaks.size());
		}
	}
	
	private void getPulse(double feedbackParameter, double N0, double currentPulse, double maxPulse){
		double N = output_.getNewN();
		double newPulse;
		
		if(core_.isSequenceRunning()){		
			if(N0 > 0) {

				// compute the change in pulse based on N, N0, the current pulse and a feedback parameter
				// note that we cumulate the changes for small values (see next `if` clause)
				dp = dp + 0.1 + currentPulse * feedbackParameter * (1-N/N0);

				// compute the new pulse
				newPulse = currentPulse+dp;

				// if the change in pulse is larger than 1, then we do not remember it for the next iteration
				if(dp*dp > 1) {
					dp = 0;
				}

				// prevent dp from becoming negative when N >> N0
				if(newPulse == 0 && dp<0) {
					dp = 0;
				}
				
			} else {
				// if 0 or negative N, then pulse is set to 0
				newPulse = 0;
			}
		} else {
			// if the camera is not running, don't change the pulse
			newPulse = currentPulse;
		}
		
		if(newPulse > maxPulse){
			// if the new pulse is larger than the maximum one, set it to maximum value
			newPulse = maxPulse;
		} else if (newPulse < 0) {
			newPulse = 0;
		}

		// update the output
		output_.setNewPulse(newPulse);
	}

	/**
	 * Update results in the controller.
	 *
	 * @param output Activation results to pass on to the controller
	 */
	public void update(ActivationResults output) {
		activationController_.updateResults(output);
	}

	/**
	 * Return the results of the last NMS iteration: the blurred difference
	 * of images with detection highlighted.
	 *
	 * @return Image with highlighted detections
	 */
	public ImageProcessor getNMSResult(){
		return ip_;
	}

	private class AutomatedActivation extends SwingWorker<Integer, ActivationResults> {
		
		@Override
		protected Integer doInBackground() throws Exception {
			Thread.currentThread().setName("Activation task");
					
			while(running_.get()){
				// if the camera is running
				if(core_.isSequenceRunning()) {
					// get most recent user defined parameters
					ActivationParameters parameters = activationController_.retrieveAllParameters();

					// compute the number of molecules
					getN(
							parameters.getDynamicFactor(),
							parameters.getCutoff(),
							parameters.getAveragingWeight(),
							parameters.getAutoCutoff()
					);

					// set new activation laser pulse
					if(parameters.getActivate()){
						getPulse(parameters.getFeedbackParameter(), parameters.getN0(), parameters.getCurrentPulse(), parameters.getMaxPulse());
					} else {
						output_.setNewPulse(parameters.getCurrentPulse());
					}

					// push the results
					publish(output_);
				}

				// sleep until next iteration
				Thread.sleep(idleTime_);
			}
			return 1;
		}

		@Override
		protected void process(List<ActivationResults> chunks) {
			for(ActivationResults result : chunks){
				// update the controller with most recent results
				update(result);
			}
		}
	}
}
