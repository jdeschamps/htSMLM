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
 * 
 * 
 * @author Joran Deschamps
 *
 */
public class ActivationTask {

	private static double LOW_QUANTILE = 0.2;
	private static double HIGH_QUANTILE = 0.8;

	private CMMCore core_;
	private ActivationController activationController_;
	private int idletime_;
	private AutomatedActivation worker_;
	private AtomicBoolean running_;
	private ActivationResults output_;
	private ImageProcessor ip_;
	private ActivationProcessor processor_;
	
	private double dp;

	public ActivationTask(ActivationController activationController, Studio studio, int idle){
		running_ = new AtomicBoolean(false);
		
		core_ = studio.core();
		idletime_ = idle;

		activationController_ = activationController;
		
		dp = 0;
		output_ = new ActivationResults();
		processor_ = ActivationProcessor.getInstance();
	}

		
	public void startTask() {		
		// start activation
		worker_ = new AutomatedActivation();
		worker_.execute();
		running_.set(true);
	}
	

	public void stopTask() {
		running_.set(false);
	}

	public boolean isRunning() {
		return running_.get();
	}
	
	public void setIdleTime(int idle){
		idletime_ = idle;
	}

	protected Pair<Image, Image> getTwoImages(){
		processor_.startQueueing();
		
		// wait that the queue is full
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
			pixels_sub[i] = sub >= 0f ? sub: 0f;
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
			pixels_sub[i] = sub >= 0f ? sub: 0f;
		}
		return pixels_sub;
	}
	
	protected FloatProcessor computeGaussianSubtraction(Pair<Image, Image> taggedPair, long bitDepth){
		Image image1 = taggedPair.getFirst();
		Image image2 = taggedPair.getSecond();
		
		// get subtraction
		float[] pixels_sub;
		if(image1.getBytesPerPixel() == 2) {
			pixels_sub = subtract16bits(image1, image2);
		} else if (image1.getBytesPerPixel() == 1) {
			pixels_sub = subtract8bits(image1, image2);
		} else {
			throw new IllegalArgumentException("Only 8 and 16 bits are supported.");
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

	static protected NMS runNMS(FloatProcessor image){
		return new NMS(image, HTSMLMConstants.nmsMaskSize);
	}

	static protected double computeCutOff(ArrayList<Peak> peaks, double cutoff, double dynamicFactor, double cutoffWeight){
		// get quantiles
		double q1 = NMSUtils.getQuantile(peaks, LOW_QUANTILE);
		double q2 = NMSUtils.getQuantile(peaks, HIGH_QUANTILE);
		double median = NMSUtils.getQuantile(peaks, 0.5);

		// measure slope
		double slope = (q2 - q1) / (HIGH_QUANTILE - LOW_QUANTILE);
		double tempcutoff = median + slope * dynamicFactor;

		double newcutoff = (1 - cutoffWeight) * cutoff + cutoffWeight * tempcutoff;
		newcutoff = Math.floor(10 * newcutoff + 0.5) / 10;

		return newcutoff;
	}

	private void getN(double dynamicFactor, double cutoff, double dT, boolean autocutoff) {
		if (core_.isSequenceRunning()) {
			// grab two images from the circular buffer
			Pair<Image, Image> pairs = getTwoImages();

			// compute Gaussian blurred difference
			FloatProcessor imp = computeGaussianSubtraction(pairs, core_.getBytesPerPixel());

			// run nms
			NMS nms = runNMS(imp);

			// compute cutoff
			double newcutoff;
			if(autocutoff){
				newcutoff = computeCutOff(nms.getPeaks(), cutoff, dynamicFactor, dT);
			} else {
				newcutoff = cutoff;
			}

			// filter peak list based on the cutoff
			ArrayList<Peak> peaks = NMSUtils.filterPeaks(nms, newcutoff);

			// apply cutoff
			ip_ = NMSUtils.applyCutoff(nms, peaks);

			// create output
			output_.setNewCutoff(newcutoff);
			output_.setNewN(peaks.size());
		}
	}
	
	private void getPulse(double feedbackParameter, double N0, double currentPulse, double maxpulse){
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
		
		if(newPulse > maxpulse){
			// if the new pulse is larger than the maximum one, set it to maximum value
			newPulse = maxpulse;
		} else if (newPulse < 0) {
			newPulse = 0;
		}

		// update the output
		output_.setNewPulse(newPulse);
	}

	public void update(ActivationResults output) {
		activationController_.updateResults(output);
	}

	public ImageProcessor getNMSResult(){
		return ip_;
	}

	private class AutomatedActivation extends SwingWorker<Integer, ActivationResults> {
		
		@Override
		protected Integer doInBackground() throws Exception {
			Thread.currentThread().setName("Activation task");
					
			while(running_.get()){
				
				if(core_.isSequenceRunning()) {
					ActivationParameters parameters = activationController_.retrieveAllParameters();
									
					if(parameters.getAutoCutoff()){
						getN(parameters.getDynamicFactor(), parameters.getCutoff(), parameters.getdT(), true);
					} else {
						getN(parameters.getDynamicFactor(), parameters.getCutoff(), parameters.getdT(), false);
					}
					
					if(parameters.getActivate()){
						getPulse(parameters.getFeedbackParameter(), parameters.getN0(), parameters.getCurrentPulse(), parameters.getMaxPulse());
					} else {
						output_.setNewPulse(parameters.getCurrentPulse());
					}
					
					publish(output_);
				}
				
				Thread.sleep(idletime_);
			}
			return 1;
		}

		@Override
		protected void process(List<ActivationResults> chunks) {
			for(ActivationResults result : chunks){
				update(result);
			}
		}
	}
}
