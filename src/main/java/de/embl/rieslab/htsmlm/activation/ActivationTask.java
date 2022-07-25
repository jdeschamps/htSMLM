package de.embl.rieslab.htsmlm.activation;

import de.embl.rieslab.htsmlm.ActivationPanel;
import de.embl.rieslab.htsmlm.activation.utils.NMSUtils;
import de.embl.rieslab.htsmlm.utils.Pair;
import de.embl.rieslab.htsmlm.utils.Peak;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import de.embl.rieslab.htsmlm.utils.NMS;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import mmcorej.org.json.JSONException;
import org.micromanager.internal.utils.imageanalysis.ImageUtils;

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
				tagged = core_.getLastTaggedImage();
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

	protected Pair<TaggedImage, TaggedImage> getTwoImages(){
		TaggedImage tagged1 = getTaggedImage(-1);

		if (tagged1 != null) {
			try {
				TaggedImage tagged2 = getTaggedImage(tagged1.tags.getInt("ImageNumber"));

				return new Pair(tagged1, tagged2);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	protected ImagePlus computeGaussianSubtraction(Pair<TaggedImage, TaggedImage> taggedPair){
		TaggedImage tagged1 = taggedPair.getFirst();
		TaggedImage tagged2 = taggedPair.getSecond();

		// create ShortProcessors and copy pixel values from the tagged images
		int width = (int) core_.getImageWidth();
		int height = (int) core_.getImageWidth();
		ShortProcessor ip = new ShortProcessor(width, height);
		ShortProcessor ip2 = new ShortProcessor(width, height);
		ip.setPixels(tagged1.pix);
		ip2.setPixels(tagged2.pix);

		// convert to Float and wrap in ImagePlus
		ImagePlus imp = new ImagePlus("Image1", ip.convertToFloatProcessor());
		ImagePlus imp2 = new ImagePlus("Image2", ip2.convertToFloatProcessor());

		// Subtraction
		ImageCalculator calculator = new ImageCalculator();
		ImagePlus imp3 = calculator.run("Subtract create", imp, imp2);

		// Gaussian filter
		GaussianBlur gau = new GaussianBlur();
		gau.blurGaussian(
				imp3.getProcessor(),
				HTSMLMConstants.gaussianMaskSize,
				HTSMLMConstants.gaussianMaskSize,
				HTSMLMConstants.gaussianMaskPrecision
		);

		// set negative values to 0
		for(int i=0;i<imp3.getWidth();i++){
			for(int j=0;j<imp3.getHeight();j++){
				if(imp3.getProcessor().get(i, j) < 0){
					imp3.getProcessor().setf(i,j,0.f);
				}
			}
		}

		return imp3;
	}

	protected NMS runNMS(ImagePlus image){
		return new NMS(image, HTSMLMConstants.nmsMaskSize);
	}

	protected double computeCutOff(NMS nms, double cutoff, double cutoffParameter, double dT){
		double q1 = NMSUtils.getQuantile(nms.getPeaks(), LOW_QUANTILE);
		double q2 = NMSUtils.getQuantile(nms.getPeaks(), HIGH_QUANTILE);
		double median = NMSUtils.getQuantile(nms.getPeaks(), 0.5);

		double slope = (q2 - q1) / (HIGH_QUANTILE - LOW_QUANTILE);
		double tempcutoff = median + slope * cutoffParameter;

		double newcutoff = (1 - 1 / dT) * cutoff + tempcutoff / dT;
		newcutoff = Math.floor(10 * newcutoff + 0.5) / 10;

		// TODO inverse transform to original image pixel depth (ShortProc)

		return newcutoff;
	}

	private void getN(double sdcoeff, double cutoff, double dT, boolean autocutoff) {
		if (core_.isSequenceRunning() && core_.getBytesPerPixel() == 2) {
			// grab two images from the circular buffer
			Pair<TaggedImage, TaggedImage> pairs = getTwoImages();

			// try to extract two images
			ImagePlus imp = computeGaussianSubtraction(pairs);

			// run nms
			NMS nms = runNMS(imp);

			// compute cutoff
			double newcutoff;
			if(autocutoff){
				newcutoff = computeCutOff(nms, cutoff, sdcoeff, dT);
			} else {
				// TODO scale cutoff to the Float value?
				newcutoff = cutoff;
			}

			// filter peak list based on the cutoff
			ArrayList<Peak> peaks = NMSUtils.filterPeaks(nms, newcutoff);

			// apply cutoff
			ip_ = NMSUtils.applyCutoff(nms, peaks, newcutoff);

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
