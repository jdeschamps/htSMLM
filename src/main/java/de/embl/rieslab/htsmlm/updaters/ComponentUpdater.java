package de.embl.rieslab.htsmlm.updaters;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import com.nativelibs4java.opencl.library.IOpenCLLibrary;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;

/**
 * Abstract class updating the state of a Swing component
 * based on a UIProperty.
 *
 * @param <T> Type of Swing component
 */
public abstract class ComponentUpdater<T extends JComponent> {

	private T component_;
	private final UIProperty property_;
	private volatile boolean running_ = false;
	private boolean initialised_ = false;
	private UIUpdater task_;
	private int idleTime_;
	
	public ComponentUpdater(T component, UIProperty prop, int idleTime){
		component_ = component;
		property_ = prop;
		
		idleTime_ = idleTime;

		// check whether the property is compatible with the updater
		initialised_ = sanityCheck(property_);
		if(!sanityCheck(property_)){
			throw new IllegalArgumentException(
					"Property" + property_.getFriendlyName() +
							" is not compatible with "+ this.getClass().getName() + ".");
		}
		
	}

	/**
	 * Check whether the updater is running.
	 * @return True if it is
	 */
	public boolean isRunning(){
		return running_;
	}

	/**
	 * Start updater.
	 */
	public void startUpdater(){
		if(!running_ && initialised_){
			running_ = true;
			task_ = new UIUpdater( );
			task_.execute();
		}
	}

	protected T getComponent(){
		return component_;
	}

	/**
	 * Stop updater.
	 */
	public void stopUpdater(){
		running_ = false;
	}

	/**
	 * Change the idle time between updates.
	 * @param newIdleTime New idle time
	 */
	public void updateIdleTime(int newIdleTime){
		idleTime_ = newIdleTime;
	}

	/**
	 * Check whether the property is compatible with the updater.
	 * @param prop Property to check
	 * @return True if it is
	 */
	public abstract boolean sanityCheck(UIProperty prop);

	/**
	 * Update the component
	 * @param val Value with which to update the component
	 */
	public abstract void updateComponent(String val);
		
	private class UIUpdater extends SwingWorker<Integer, String> {

		@Override
		protected Integer doInBackground() throws Exception {
			while(running_){
				
				String s = property_.getPropertyValue();
				if(s != null && !s.isEmpty()){
					publish(property_.getPropertyValue());
				}
				Thread.sleep(idleTime_);
			}
			return 1;
		}

		@Override
		protected void process(List<String> chunks) {
			for(String result : chunks){
				updateComponent(result);
			}
		}
	}
}