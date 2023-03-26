package de.embl.rieslab.htsmlm.updaters;

import java.util.List;

import javax.swing.SwingWorker;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.graph.Chart;

/**
 * A class updating a 2D chart with the values of two device properties.
 */
public class ChartUpdater {

	private Chart chart_;
	private UIProperty propertyX_, propertyY_;
	private volatile boolean running_ = false;
	private boolean initialised_ = false;
	private UIUpdater task_;
	private int idleTime_;
	
	public ChartUpdater(Chart chart, UIProperty xProp, UIProperty yProp, int idleTime){
		
		if(chart == null || xProp == null || yProp == null) {
			throw new NullPointerException();
		}
		
		chart_ = chart;
		propertyX_ = xProp;
		propertyY_ = yProp;
		
		idleTime_ = idleTime;
	}
	
	public boolean isInitialised(){
		return initialised_;
	}
	
	public boolean isRunning(){
		return running_;
	}
	
	public void startUpdater(){
		// performs sanity check
		if(!initialised_ && propertyX_ != null && propertyY_ != null) {
			if(propertyX_.isAssigned() && EmuUtils.isNumeric(propertyX_.getPropertyValue()) 
					&& propertyY_.isAssigned() && EmuUtils.isNumeric(propertyY_.getPropertyValue())) {
				initialised_ = true;
			}
		}
		
		if(!running_ && initialised_){
			running_ = true;
			task_ = new UIUpdater( );
			task_.execute();
		}
	}
	
	public void stopUpdater(){
		running_ = false;
	}
	
	public void changeIdleTime(int newTime){
		idleTime_ = newTime;
	}

	public void changeChart(Chart newChart){
		chart_ = newChart;
	}
	
	private class UIUpdater extends SwingWorker<Integer, Double[]> {

		@Override
		protected Integer doInBackground() throws Exception {
			Double[] value = new Double[2];
			while(running_){

				value[0] = Double.parseDouble(propertyX_.getPropertyValue());
				value[1] = Double.parseDouble(propertyY_.getPropertyValue());
				publish(value);
				
				Thread.sleep(idleTime_);
			}
			return 1;
		}

		@Override
		protected void process(List<Double[]> chunks) {
			for(Double[] result : chunks){
				chart_.addPoint(result[0],result[1]);
			}
		}
	}
}