package de.embl.rieslab.htsmlm.activation;

import de.embl.rieslab.htsmlm.ActivationPanel;
import de.embl.rieslab.htsmlm.activation.processor.ActivationProcessor;
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

import javax.swing.SwingWorker;

import org.micromanager.data.Image;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import mmcorej.org.json.JSONException;

// TODO: imglib2 rather than ImageJ1...

public class ActivationTask {

	/**
	 * 
	 * Now this is super messy as most of the code turned into a hack to work with MM2gamma. Maybe in the future:
	 * 
	 * have a separate thread poll frames and place them in a queue, then this thread can (without while loop) poll
	 * them by checking the size of the queue and taking two frames directly.
	 * 
	 */
	public static int PARAM_SDCOEFF = 0;
	public static int PARAM_FEEDBACK = 1;
	public static int PARAM_CUTOFF = 2;
	public static int PARAM_AUTOCUTOFF = 3;
	public static int PARAM_dT = 4;
	public static int PARAM_N0 = 5;
	public static int PARAM_PULSE = 6;
	public static int PARAM_MAXPULSE = 7;
	public static int PARAM_ACTIVATE = 8;
	public static int OUTPUT_NEWCUTOFF = 0;
	public static int OUTPUT_N = 1;
	public static int OUTPUT_NEWPULSE = 2;
	public static int NUM_PARAMETERS = 9;
	public static int NUM_OUTPUTS = 3;
	private static int MAX_COUNTER = 200;

	private static double LOW_QUANTILE = 0.2;
	private static double HIGH_QUANTILE = 0.8;

	private CMMCore core_;
	private ActivationPanel holder_;
	private int idletime_;
	private AutomatedActivation worker_;
	private boolean running_;
	private Double[] output_;
	private ImageProcessor ip_;
	private ActivationProcessor processor_;
	
	private double dp;

	public ActivationTask(ActivationPanel holder, CMMCore core, int idle){
		running_ = false;
		
		core_ = core;
		idletime_ = idle;

		holder_ = holder;
		
		dp = 0;
		
		output_ = new Double[3];
		output_[0] = 0.;
		output_[1] = 0.;
		output_[2] = 0.;
		
		processor_ = ActivationProcessor.getInstance();
	}

	public void startTask() {
		worker_ = new AutomatedActivation();
		worker_.execute();
		running_ = true;
	}

	public void stopTask() {
		running_ = false;
	}

	public boolean isRunning() {
		return running_;
	}
	
	public void setIdleTime(int idle){
		idletime_ = idle;
	}

	protected TaggedImage getTaggedImage(int exclude_number) {
		int counter = 0;
		boolean abort = false;
		TaggedImage tagged = null;

		// try to extract an image
		while (tagged == null && !abort) {
			try {
				Thread.sleep(2);
				tagged = (TaggedImage) core_.popNextTaggedImage();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				counter++;
			}

			try {
				if (tagged.tags.getInt("ImageNumber") == exclude_number) {
					tagged = null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (counter > MAX_COUNTER) {
				abort = true;
			}
		}
		return tagged;
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
			
		//TaggedImage tagged1 = getTaggedImage(-1);

		/*if (tagged1 != null) {
			try {
				TaggedImage tagged2 = getTaggedImage(tagged1.tags.getInt("ImageNumber"));
				
				return new Pair<TaggedImage, TaggedImage>(tagged1, tagged2);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;*/
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

	static protected double computeCutOff(ArrayList<Peak> peaks, double cutoff, double cutoffParameter, double cutoffFb){
		// get quantiles
		double q1 = NMSUtils.getQuantile(peaks, LOW_QUANTILE);
		double q2 = NMSUtils.getQuantile(peaks, HIGH_QUANTILE);
		double median = NMSUtils.getQuantile(peaks, 0.5);

		// measure slope
		double slope = (q2 - q1) / (HIGH_QUANTILE - LOW_QUANTILE);
		double tempcutoff = median + slope * cutoffParameter;

		double newcutoff = (1 - cutoffFb) * cutoff + cutoffFb * tempcutoff;
		newcutoff = Math.floor(10 * newcutoff + 0.5) / 10;

		return newcutoff;
	}

	private void getN(double sdcoeff, double cutoff, double dT, boolean autocutoff) {
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
				newcutoff = computeCutOff(nms.getPeaks(), cutoff, sdcoeff, dT);
			} else {
				newcutoff = cutoff;
			}

			// filter peak list based on the cutoff
			ArrayList<Peak> peaks = NMSUtils.filterPeaks(nms, newcutoff);

			// TODO change how parameters are passed back to UI
			// apply cutoff
			ip_ = NMSUtils.applyCutoff(nms, peaks);

			// create output
			output_[OUTPUT_NEWCUTOFF] = newcutoff;
			output_[OUTPUT_N] = (double) peaks.size();
		}
	}
	
	private void getPulse(double feedbackParameter, double N0, double currentPulse, double maxpulse){
		double N = output_[OUTPUT_N];
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
		output_[OUTPUT_NEWPULSE] = newPulse;
	}

	public void update(Double[] outputs) {
		holder_.update(outputs);
	}

	public ImageProcessor getNMSResult(){
		return ip_;
	}

	private class AutomatedActivation extends SwingWorker<Integer, Double[]> {
		
		@Override
		protected Integer doInBackground() throws Exception {
			Thread.currentThread().setName("Activation task");
			
			Double[] params;
			
			while(running_){
				
				if(core_.isSequenceRunning()) {
					params = holder_.retrieveAllParameters();
									
					// TODO: sanity checks here?
					if(params[PARAM_AUTOCUTOFF] == 1){
						getN(params[PARAM_SDCOEFF],params[PARAM_CUTOFF],params[PARAM_dT],true);
					} else {
						getN(params[PARAM_SDCOEFF],params[PARAM_CUTOFF],params[PARAM_dT],false);
					}
					
					if(params[PARAM_ACTIVATE] == 1){
						getPulse(params[PARAM_FEEDBACK],params[PARAM_N0],params[PARAM_PULSE],params[PARAM_MAXPULSE]);
					} else {
						output_[OUTPUT_NEWPULSE] = params[PARAM_PULSE];
					}
					
					publish(output_);
				}
				
				Thread.sleep(idletime_);
			}
			return 1;
		}

		@Override
		protected void process(List<Double[]> chunks) {
			for(Double[] result : chunks){
				update(result);
			}
		}
	}
}
