package de.embl.rieslab.htsmlm.updaters;


import java.util.List;

import javax.swing.SwingWorker;

import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.graph.TimeChart;

public class TimeChartUpdater {

	private TimeChart chart_;
	private UIProperty property_;
	private volatile boolean running_ = false;
	private boolean initialised_ = false;
	private UIUpdater task_;
	private int idleTime_;
	
	public TimeChartUpdater(TimeChart chart, UIProperty prop, int idleTime){
		
		if(chart == null || prop == null) {
			throw new NullPointerException();
		}
		
		chart_ = chart;
		property_ = prop;
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
		if(!initialised_ && property_ != null) {
			if(property_.isAssigned() && EmuUtils.isNumeric(property_.getPropertyValue())) {
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

	public void changeChart(TimeChart newChart){
		chart_ = newChart;
	}
	
	private class UIUpdater extends SwingWorker<Integer, Double> {

		@Override
		protected Integer doInBackground() throws Exception {
			Double value;

			while(running_){	
				value = Double.parseDouble(property_.getPropertyValue());
				
				// round
				value = (Math.floor(value * 100) / 100);

				publish(value);

				Thread.sleep(idleTime_);
			}
			return 1;
		}

		@Override
		protected void process(List<Double> chunks) {
			for(Double result : chunks){
				chart_.addPoint(result);
			}
		}
	}
}
