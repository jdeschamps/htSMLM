package de.embl.rieslab.htsmlm;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.DoubleUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.IntegerUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIPropertyTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.graph.TimeChart;
import de.embl.rieslab.htsmlm.uipropertyflags.FocusStabFlag;
import de.embl.rieslab.htsmlm.updaters.TimeChartUpdater;

/**
 *
 * @author Joran Deschamps
 */
public class FocusPanel extends ConfigurablePanel {

	private static final long serialVersionUID = 1L;

	//////// Components
	private JLabel poslabel_;
	private JLabel smallsteplabel_;
	private JLabel largesteplabel_;
	private JTextField textfieldPosition_;
	private JToggleButton togglebuttonMonitor_;
	private JToggleButton togglebuttonLock_;
	private JButton buttonLargeStepsUp_;
	private JButton buttonSmallStepsUp_;
	private JButton buttonLargeStepsDown_;
	private JButton buttonSmallStepsDown_;
	private JTextField textfieldLargeStep_;
	private JTextField textfieldSmallStep_;
	private JPanel panelLeftControl_;
	private JPanel panelRightControl_;
	private JPanel panelGraph_;
	private TimeChart graph_;
	
	//////// Thread
	private TimeChartUpdater updater_;

	//////// Properties
	private final static String FOCUS_POSITION = "Z stage position";
	private final static String FOCUS_STABILIZATION = "Z stage focus locking";
	
	//////// Parameters
	private final String PARAM_LARGESTEP = "Large step";
	private final String PARAM_SMALLSTEP = "Small step";
	private final String PARAM_IDLE = "Idle time (ms)";
	private final String PARAM_NPOS = "Number of points";
	
	//////// Default parameters
	private double smallstep_, largestep_;
	private int idle_, npos_; 
	private boolean initialised = false; // used only for initial textfield value
	
	public FocusPanel(String label) {
		super(label);
		
		setupPanel();
	}
	
	private void setupPanel() {
		graph_ = newGraph();
		try {
			updater_ = new TimeChartUpdater(graph_,getUIProperty(FOCUS_POSITION),idle_);
		} catch (UnknownUIPropertyException e) {
		}
		
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createTitledBorder(null, getPanelLabel(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0,0,0)));
		((TitledBorder) this.getBorder()).setTitleFont(((TitledBorder) this.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
		
		panelLeftControl_ = new JPanel();
		panelGraph_ = new JPanel();
		panelRightControl_ = new JPanel();
		
		initLeftPanel();
		initCentralPanel();
		initRightPanel();

		this.add(panelLeftControl_);
		this.add(panelGraph_);
		this.add(panelRightControl_);
		
	}

	private void initLeftPanel(){
		panelLeftControl_.setLayout(new GridBagLayout());
		
		poslabel_ = new JLabel("Position:");

		
		textfieldPosition_ = new JTextField();	
		textfieldPosition_.setToolTipText("Sets the position of the stage.");
		textfieldPosition_.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
				String typed = getUserInput();
        	    if(typed == null) {
        	        return;
        	    } 
				try {
					double val = Double.parseDouble(typed.replaceAll(",","."));
					if (val >= 0) {
						if (!togglebuttonLock_.isSelected()) {
							setUIPropertyValue(FOCUS_POSITION, typed.replaceAll(",","."));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
         });
		textfieldPosition_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String typed = getUserInput();
        	    if(typed == null) {
        	        return;
        	    } 
				try {
					double val = Double.parseDouble(typed.replaceAll(",","."));
					if (val >= 0) {
						if (!togglebuttonLock_.isSelected()) {
							setUIPropertyValue(FOCUS_POSITION, typed.replaceAll(",","."));
						}
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
        	}
        });

		togglebuttonMonitor_ = new JToggleButton("Monitor");		
		togglebuttonMonitor_.setToolTipText("Turn on/off the monitoring of the stage position.");

		SwingUIListeners.addActionListenerToBooleanAction(b -> monitorPosition(b), togglebuttonMonitor_);

		togglebuttonLock_ = new JToggleButton("Lock");
		togglebuttonLock_.setToolTipText("Turn on/off the Z stabilization.");
		try {
			SwingUIListeners.addActionListenerToTwoState(this, FOCUS_STABILIZATION, togglebuttonLock_);
		} catch (IncorrectUIPropertyTypeException e1) {
			e1.printStackTrace();
		}
		
		///// grid bag 
		GridBagConstraints c = new GridBagConstraints();
	
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,15,2,15);
		c.gridwidth = 1;
		panelLeftControl_.add(poslabel_, c);

		c.gridy = 1;
		c.insets = new Insets(2,15,40,15);
		panelLeftControl_.add(textfieldPosition_, c);
		
		c.gridy = 4;
		c.insets = new Insets(2,15,2,15);
		panelLeftControl_.add(togglebuttonMonitor_, c);
		
		c.gridy = 5;
		panelLeftControl_.add(togglebuttonLock_, c);
		
	}

	private void initCentralPanel(){
		 panelGraph_.add(graph_.getChart());
	}
	
	private void initRightPanel(){
		panelRightControl_.setLayout(new GridBagLayout());
		
		buttonLargeStepsUp_ = new JButton("^^");
		buttonLargeStepsUp_.setToolTipText("Move the stage up by one large step.");
		buttonLargeStepsUp_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(largestep_);
            }
        });

		buttonSmallStepsUp_ = new JButton("^");
		buttonSmallStepsUp_.setToolTipText("Move the stage up by one small step.");
		buttonSmallStepsUp_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(smallstep_);
            }
        });

		largesteplabel_ = new JLabel(">>");
		textfieldLargeStep_ = new JTextField(String.valueOf(largestep_));	
		textfieldLargeStep_.setToolTipText("Large step size (um).");

		SwingUIListeners.addActionListenerToDoubleAction(d -> largestep_ = d, textfieldLargeStep_);

		smallsteplabel_ = new JLabel(">");
		textfieldSmallStep_ = new JTextField(String.valueOf(smallstep_));
		textfieldSmallStep_.setToolTipText("Small step size (um).");
		SwingUIListeners.addActionListenerToDoubleAction(d -> smallstep_ = d, textfieldSmallStep_);
				
		buttonSmallStepsDown_ = new JButton("v");
		buttonSmallStepsDown_.setToolTipText("Move the stage down by one small step.");
		buttonSmallStepsDown_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(-smallstep_);
            }
        });
		buttonLargeStepsDown_ = new JButton("vv");
		buttonLargeStepsDown_.setToolTipText("Move the stage down by one large step.");
		buttonLargeStepsDown_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(-largestep_);
            }
        });
		
		
		// grid bag constraints
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,10,2,10);
		c.gridwidth = 2;		
		panelRightControl_.add(buttonLargeStepsUp_, c);
		
		c.gridy = 1;		
		panelRightControl_.add(buttonSmallStepsUp_, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(2,1,2,1);
		panelRightControl_.add(largesteplabel_, c);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(2,10,2,10);
		panelRightControl_.add(textfieldLargeStep_, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(2,1,2,1);
		panelRightControl_.add(smallsteplabel_, c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		c.insets = new Insets(2,10,2,10);
		panelRightControl_.add(textfieldSmallStep_, c);
		
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(2,10,2,10);
		panelRightControl_.add(buttonSmallStepsDown_, c);

		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		panelRightControl_.add(buttonLargeStepsDown_, c);
	}
	
	protected void moveRelativePosition(double step) {
		try {
			String s = getUIPropertyValue(FOCUS_POSITION);
    	
	    	if (EmuUtils.isNumeric(s)) {
	    		double val = Double.parseDouble(s)+step;
	    		setUIPropertyValue(FOCUS_POSITION,String.valueOf(val));
	    	}	
		} catch (UnknownUIPropertyException e) {
			e.printStackTrace();
		}	
	}

	protected void monitorPosition(boolean b) {
		if(b){
			updater_.startUpdater();
		} else {
			updater_.stopUpdater();
		}
	}
	
	public boolean isMonitoring(){
		return togglebuttonMonitor_.isSelected();
	}
	
	private String getUserInput(){
		String s = textfieldPosition_.getText();
		if(EmuUtils.isNumeric(s)){
			return s;
		}
		return null;
	}
	
	private TimeChart newGraph(){
		return new TimeChart("position","time","position",npos_,310,150,false);
	}

	@Override
	protected void initializeProperties() {
		addUIProperty(new UIProperty(this, FOCUS_POSITION,"Position of the stage, used to move the stage and monitor its position."));
		addUIProperty(new TwoStateUIProperty(this, FOCUS_STABILIZATION,"Property used for focus stabilization.", new FocusStabFlag()));
	}
	
	@Override
	protected void initializeParameters() {
		smallstep_ = 0.2;
		largestep_ = 2;
		idle_ = 100;
		npos_ = 30; 
		
		addUIParameter(new DoubleUIParameter(this, PARAM_LARGESTEP,"Default value for large z stage step.",largestep_));
		addUIParameter(new DoubleUIParameter(this, PARAM_SMALLSTEP,"Default value for small z stage step.",smallstep_));
		addUIParameter(new IntegerUIParameter(this, PARAM_IDLE,"Idle time (ms) of the stage position monitoring.",idle_)); // thread idle time
		addUIParameter(new IntegerUIParameter(this, PARAM_NPOS,"Number of stage positions displayed in the chart (x axis).",npos_)); // number of points in the graph
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(FOCUS_POSITION.equals(name)){
			if(EmuUtils.isNumeric(newvalue)){
				if(!initialised){
					initialised = true;
					textfieldPosition_.setText(newvalue);
				}
			}
		} else if(FOCUS_STABILIZATION.equals(name)){
			try {
				togglebuttonLock_.setSelected(((TwoStateUIProperty) getUIProperty(FOCUS_STABILIZATION)).isOnState(newvalue));
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
			}
		}
	}

	

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_LARGESTEP.equals(label)){
			try {
				largestep_ = getDoubleUIParameterValue(PARAM_LARGESTEP);
				textfieldLargeStep_.setText(String.valueOf(largestep_));
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_SMALLSTEP.equals(label)){
			try {
				smallstep_ = getDoubleUIParameterValue(PARAM_SMALLSTEP);
				textfieldSmallStep_.setText(String.valueOf(smallstep_));
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_IDLE.equals(label)){
			try {
				int val = getIntegerUIParameterValue(PARAM_IDLE);
				if(val != idle_){
					idle_ = val;
					updater_.changeIdleTime(idle_);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_NPOS.equals(label)){
			try {
				int val = getIntegerUIParameterValue(PARAM_NPOS);
				if(val != npos_){
					npos_ = val;
					panelGraph_.remove(graph_.getChart());
					graph_ = newGraph();
					panelGraph_.add(graph_.getChart());
					panelGraph_.updateUI();
					updater_.changeChart(graph_);
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutDown() {
		updater_.stopUpdater();
		setUIPropertyValue(FOCUS_STABILIZATION,TwoStateUIProperty.getOffStateLabel());
	}

	@Override
	public String getDescription() {
		return "The focus panel controls the Z stage of the microscope. It allows monitoring its position (select/unselect the monitoring button). In addition, small and large steps buttons can move the stage up and down. "
				+ "The locking property corresponds to focus stabilization.";
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
		// Do nothing
	}
}
