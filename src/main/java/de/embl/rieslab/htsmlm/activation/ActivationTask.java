package de.embl.rieslab.htsmlm.activation;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import de.embl.rieslab.htsmlm.tasks.Task;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;
import de.embl.rieslab.htsmlm.utils.NMS;

import java.util.List;

import javax.swing.SwingWorker;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

public class ActivationTask implements Task<Double> {

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
	
	private CMMCore core_;
	private TaskHolder<Double> holder_;
	private int idletime_;
	private AutomatedActivation worker_;
	private boolean running_;
	private Double[] output_;
	private ImageProcessor ip_;
	
	private double dp;
	
	public ActivationTask(TaskHolder<Double> holder, CMMCore core, int idle){
		running_ = false;
		
		core_ = core;
		idletime_ = idle;

		registerHolder(holder);
		
		dp = 0;
		
		output_ = new Double[3];
		output_[0] = 0.;
		output_[1] = 0.;
		output_[2] = 0.;
	}
	
	@Override
	public void registerHolder(TaskHolder<Double> holder) {
		holder_ = holder;
	}

	@Override
	public void startTask() {
		worker_ = new AutomatedActivation();
		worker_.execute();
		running_ = true;
	}

	@Override
	public void stopTask() {
		running_ = false;
	}

	@Override
	public boolean isRunning() {
		return running_;
	}
	
	public void setIdleTime(int idle){
		idletime_ = idle;
	}

	private void getN(double sdcoeff, double cutoff, double dT, boolean autocutoff) {
		if (core_.isSequenceRunning() && core_.getBytesPerPixel() == 2) {			
			int width, height;
			double tempcutoff;
			boolean abort = false;
			int counter1 = 0, counter2 = 0;

			TaggedImage tagged1 = null, tagged2 = null;
			ShortProcessor ip, ip2;
			ImagePlus imp, imp2;
			ImageCalculator calcul = new ImageCalculator();
			ImagePlus imp3;
			GaussianBlur gau = new GaussianBlur();
			NMS NMSuppr = new NMS();

			width = (int) core_.getImageWidth();
			height = (int) core_.getImageHeight();
						
			// try to extract two images
			while(tagged1 == null && abort == false) {
				//System.out.println("Try tagged 1");
				
				try {
					Thread.sleep(2);
					tagged1 = core_.getLastTaggedImage();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					counter1++;
				}
				
				if(counter1 > MAX_COUNTER) {
					abort = true;
				}
			}
			
			while(tagged2 == null && abort == false) {
				
				try {
					Thread.sleep(2);
					
					TaggedImage temp = core_.getLastTaggedImage();
					if(temp.tags.getInt("ImageNumber") != tagged1.tags.getInt("ImageNumber")) {
						tagged2 = temp;
					} else {
						counter2++;
					}
				} catch (InterruptedException e) {
					//e.printStackTrace();
				} catch (Exception e) { // buffer empty
					counter2++;
					//e.printStackTrace();
				}
				
				if(counter2 > MAX_COUNTER) {
					abort = true;
				}
			}

			if (!abort) {
				try {
					ip = new ShortProcessor(width, height);
					ip2 = new ShortProcessor(width, height);
	
					ip.setPixels(tagged1.pix);
					ip2.setPixels(tagged2.pix);
	
					imp = new ImagePlus("", ip);
					imp2 = new ImagePlus("", ip2);
	
					// Subtraction
					imp3 = calcul.run("Substract create", imp, imp2);
	
					// Gaussian filter
					gau.blurGaussian(imp3.getProcessor(),
							HTSMLMConstants.gaussianMaskSize,
							HTSMLMConstants.gaussianMaskSize,
							HTSMLMConstants.gaussianMaskPrecision);
	
					try {
						tempcutoff = imp3.getStatistics().mean + sdcoeff
								* imp3.getStatistics().stdDev;
					} catch (Exception e) {
						tempcutoff = cutoff;
					}
	
					double newcutoff;
					if (autocutoff) {
						newcutoff = (1 - 1 / dT) * cutoff + tempcutoff / dT;
						newcutoff = Math.floor(10 * newcutoff + 0.5) / 10;
					} else {
						newcutoff = cutoff;
						if (newcutoff == 0) {
							newcutoff = Math.floor(10 * tempcutoff + 0.5) / 10;;
						}
					}
					
					ip_ = NMSuppr.run(imp3, HTSMLMConstants.nmsMaskSize, newcutoff);
					output_[OUTPUT_NEWCUTOFF] = newcutoff;
					output_[OUTPUT_N] = (double) NMSuppr.getN();
				} catch (Exception e) {
					// exit?
					e.printStackTrace();
				}
			}
		}
	}
	
	private void getPulse(double feedback, double N0, double currentPulse, double maxpulse){
		double N = output_[OUTPUT_N];
		double newPulse;
		
		if(core_.isSequenceRunning()){		
			if(N0 > 0) {
				dp = dp + 0.1 + currentPulse*feedback*(1-N/N0);

				newPulse = currentPulse+dp;

				if(dp*dp > 1) {
					dp = 0;
				}
				
				if(newPulse == 0 && dp<0) {
					dp = 0;
				}
				
			} else {
				newPulse = 0;
			}
		} else {
			newPulse = currentPulse;
		}
		
		if(newPulse > maxpulse){
			newPulse = maxpulse;
		}		
		
		output_[OUTPUT_NEWPULSE] = newPulse;
	}

	@Override
	public void notifyHolder(Double[] outputs) {
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
									
					// sanity checks here?
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
				notifyHolder(result);
			}
		}
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
		// do nothing
	}

}
