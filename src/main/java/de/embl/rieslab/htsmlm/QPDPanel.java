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
import de.embl.rieslab.htsmlm.updaters.ProgressBarUpdater;

/**
 * A panel with a 2D graph and a progress bar showing live the state
 * of three device properties.
 */
public class QPDPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;
	
	//////// Thread
	private ChartUpdater chartUpdater_;
	private ProgressBarUpdater progressBarUpdater_;
	
	//////// Properties
	private final static String QPD_X = "QPD X";
	private final static String QPD_Y = "QPD Y";
	private final static String QPD_Z = "QPD Z";
	
	//////// Parameters
	private final static String PARAM_XYMAX = "XY max";
	private final static String PARAM_ZMAX = "Z max";
	private final static String PARAM_IDLE = "Idle time (ms)";
	
	//////// Default parameters
	private int idleTime_, xyMax_, zMax_;
	
	//////// Components
	private JProgressBar progressBar_;
	private JToggleButton toggleButtonMonitor_;
	private Chart graph_;
	private JPanel graphPanel_;
	
	public QPDPanel(String label) {
		super(label);
		
		setupPanel();
	}

	private void setupPanel() {
		this.setLayout(new GridBagLayout());
		
		graph_ = newGraph();
		try {
			chartUpdater_ = new ChartUpdater(graph_,getUIProperty(QPD_X),getUIProperty(QPD_Y), idleTime_);
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}
		graphPanel_ = new JPanel();
		graphPanel_.add(graph_.getChart());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		//	c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,2,2,2);
		c.gridwidth = 3;
		c.gridheight = 3;
		
		this.add(graphPanel_,c);
		
		c.gridx = 3;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		c.insets = new Insets(40,2,2,2);
		c.gridwidth = 1;
		c.gridheight = 2;

		progressBar_ = new javax.swing.JProgressBar();
		progressBar_.setOrientation(SwingConstants.VERTICAL);
		progressBar_.setMaximum(zMax_);
		progressBar_.setMinimum(0);
		
		try {
			progressBarUpdater_ = new ProgressBarUpdater(progressBar_, getUIProperty(QPD_Z), idleTime_);
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
		toggleButtonMonitor_ = new JToggleButton("Monitor");
		toggleButtonMonitor_.setToolTipText("Start/stop monitoring the values of the QPD signals");
		
		this.add(toggleButtonMonitor_,c);

	}
	
	protected void monitorQPD(boolean b) {
		if(b){
			chartUpdater_.startUpdater();
			progressBarUpdater_.startUpdater();
		} else {
			chartUpdater_.stopUpdater();
			progressBarUpdater_.stopUpdater();
		}
	}

	private Chart newGraph(){
		return new Chart("QPD","X","Y",1,270,270, xyMax_);
	}

	@Override
	protected void initializeProperties() {
		String desc = " signal of the quadrant photo-diode (QPD). Can alternatively be used to plot "
				+ "any device property (X vs Y and Z as a progress bar).";
		addUIProperty(new UIProperty(this, QPD_X,"X"+desc));
		addUIProperty(new UIProperty(this, QPD_Y,"Y"+desc));
		addUIProperty(new UIProperty(this, QPD_Z,"Z"+desc));
	}

	@Override
	protected void initializeParameters() {
		xyMax_ = 1024;
		zMax_ = 1024;
		idleTime_ = 100;
		
		addUIParameter(new IntegerUIParameter(this, PARAM_XYMAX,"Maximum X and Y signals value in the graph.", xyMax_));
		addUIParameter(new IntegerUIParameter(this, PARAM_ZMAX,"Maximum Z value in the progress bar.", zMax_));
		addUIParameter(new IntegerUIParameter(this, PARAM_IDLE,"Idle time (ms) between two updates of the QPD signals value.", idleTime_)); // thread idle time
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		// do nothing
	}

	@Override
	public void parameterhasChanged(String label) {
		switch (label) {
			case PARAM_XYMAX:
				try {
					int newVal = getIntegerUIParameterValue(PARAM_XYMAX);
					if (newVal != xyMax_) {
						xyMax_ = newVal;
						graphPanel_.remove(graph_.getChart());
						graph_ = newGraph();
						graphPanel_.add(graph_.getChart());
						graphPanel_.updateUI();
						chartUpdater_.changeChart(graph_);
					}
				} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
					e.printStackTrace();
				}
				break;
			case PARAM_ZMAX:
				try {
					int newVal = getIntegerUIParameterValue(PARAM_ZMAX);
					if (newVal != zMax_) {
						zMax_ = newVal;
						progressBar_.setMaximum(zMax_);
					}
				} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
					e.printStackTrace();
				}
				break;
			case PARAM_IDLE:
				try {
					int newVal = getIntegerUIParameterValue(PARAM_IDLE);
					if (newVal != idleTime_) {
						idleTime_ = newVal;
						chartUpdater_.changeIdleTime(idleTime_);
						progressBarUpdater_.updateIdleTime(idleTime_);
					}
				} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
					e.printStackTrace();
				}
				break;
		}
	}

	@Override
	public void shutDown() {
		chartUpdater_.stopUpdater();
		progressBarUpdater_.stopUpdater();
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
		SwingUIListeners.addActionListenerToBooleanAction(b -> monitorQPD(b), toggleButtonMonitor_);
	}
}