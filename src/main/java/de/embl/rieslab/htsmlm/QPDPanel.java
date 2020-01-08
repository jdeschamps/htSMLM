package de.embl.rieslab.htsmlm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.IntegerUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.graph.Chart;
import de.embl.rieslab.htsmlm.updaters.ChartUpdater;
import de.embl.rieslab.htsmlm.updaters.JProgressBarUpdater;

public class QPDPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;
	
	//////// Thread
	private ChartUpdater chartupdater_;
	private JProgressBarUpdater progressbarupdater_;
	
	//////// Properties
	private final static String QPD_X = "QPD X";
	private final static String QPD_Y = "QPD Y";
	private final static String QPD_Z = "QPD Z";
	
	//////// Parameters
	private final static String PARAM_XYMAX = "XY max";
	private final static String PARAM_ZMAX = "Z max";
	private final static String PARAM_IDLE = "Idle time (ms)";
	
	//////// Default parameters
	private int idle_, xymax_, zmax_; 
	
	//////// Components
	private JProgressBar progressBar_;
	private JToggleButton togglebuttonMonitor_;
	private Chart graph_;
	private JPanel graphpanel_;
	
	public QPDPanel(String label) {
		super(label);
		
		setupPanel();
	}

	private void setupPanel() {
		this.setLayout(new GridBagLayout());
		
		graph_ = newGraph();
		try {
			chartupdater_ = new ChartUpdater(graph_,getUIProperty(QPD_X),getUIProperty(QPD_Y),idle_);
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		graphpanel_ = new JPanel();
		graphpanel_.add(graph_.getChart());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		//	c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,2,2,2);
		c.gridwidth = 3;
		c.gridheight = 3;
		
		this.add(graphpanel_,c);
		
		c.gridx = 3;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.insets = new Insets(40,2,2,2);
		c.gridwidth = 1;
		c.gridheight = 2;

		progressBar_ = new javax.swing.JProgressBar();
		progressBar_.setOrientation(SwingConstants.VERTICAL);
		progressBar_.setMaximum(zmax_);
		progressBar_.setMinimum(0);
		
		try {
			progressbarupdater_ = new JProgressBarUpdater(progressBar_, getUIProperty(QPD_Z), idle_);
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		this.add(progressBar_,c);
		
		c.gridx = 3;
		c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(2,10,2,10);
		c.gridwidth = 1;
		c.gridheight = 1;
		togglebuttonMonitor_ = new JToggleButton("Monitor");
		togglebuttonMonitor_.setToolTipText("Start/stop monitoring the values of the QPD signals");
		
		this.add(togglebuttonMonitor_,c);

	}
	
	protected void monitorQPD(boolean b) {
		if(b){
			chartupdater_.startUpdater();
			progressbarupdater_.startUpdater();
		} else {
			chartupdater_.stopUpdater();
			progressbarupdater_.stopUpdater();
		}
	}

	private Chart newGraph(){
		return new Chart("QPD","X","Y",1,270,270, xymax_);
	}

	@Override
	protected void initializeProperties() {
		addUIProperty(new UIProperty(this, QPD_X,"X signal of the QPD."));
		addUIProperty(new UIProperty(this, QPD_Y,"Y signal of the QPD."));
		addUIProperty(new UIProperty(this, QPD_Z,"Sum signal of the QPD."));
	}

	@Override
	protected void initializeParameters() {
		xymax_ = 1024;
		zmax_ = 1024;
		idle_ = 100;
		
		addUIParameter(new IntegerUIParameter(this, PARAM_XYMAX,"Maximum X and Y signals value from the QPD.",xymax_));
		addUIParameter(new IntegerUIParameter(this, PARAM_ZMAX,"Maximum Sum signal value from the QPD.",zmax_));
		addUIParameter(new IntegerUIParameter(this, PARAM_IDLE,"Idle time (ms) in between two measurements of the QPD signals.",idle_)); // thread idle time
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		// do nothing
	}

	@Override
	public void parameterhasChanged(String label) {
		if(label.equals(PARAM_XYMAX)){
			try {
				int newval = getIntegerUIParameterValue(PARAM_XYMAX);			
				if(newval != xymax_){
					xymax_ = newval;
					graphpanel_.remove(graph_.getChart());
					graph_ = newGraph();
					graphpanel_.add(graph_.getChart());
					graphpanel_.updateUI();
					chartupdater_.changeChart(graph_);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(label.equals(PARAM_ZMAX)){
			try {
				int newval = getIntegerUIParameterValue(PARAM_ZMAX);
				if(newval != zmax_){
					zmax_ = newval;
					progressBar_.setMaximum(zmax_);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(label.equals(PARAM_IDLE)){
			try {
				int newval = getIntegerUIParameterValue(PARAM_IDLE);
				if(newval != idle_){
					idle_ = newval;
					chartupdater_.changeIdleTime(idle_);
					progressbarupdater_.changeIdleTime(idle_);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutDown() {
		chartupdater_.stopUpdater();
		progressbarupdater_.stopUpdater();
	}

	@Override
	public String getDescription() {
		return "The QPD panel plots the values of three QPD signals. Three signals are displayed: X and Y in a 2D chart and Sum in a progress bar. "
				+ "The monitoring of the plots can be turned on or off using the \"monitor\" button";
	}

	@Override
	protected void initializeInternalProperties() {
		// Do nothing
	}
	
	@Override
	public void internalpropertyhasChanged(String label) {
		// Do nothing
	}

	@Override
	protected void addComponentListeners() {
		SwingUIListeners.addActionListenerToBooleanAction(b -> monitorQPD(b), togglebuttonMonitor_);
	}
}