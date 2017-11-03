package main.embl.rieslab.htSMLM.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import main.embl.rieslab.htSMLM.threads.TimeChartUpdater;
import main.embl.rieslab.htSMLM.ui.graph.TimeChart;
import main.embl.rieslab.htSMLM.ui.uiparameters.DoubleUIParameter;
import main.embl.rieslab.htSMLM.ui.uiparameters.IntUIParameter;
import main.embl.rieslab.htSMLM.ui.uiproperties.TwoStateUIProperty;
import main.embl.rieslab.htSMLM.ui.uiproperties.UIProperty;
import main.embl.rieslab.htSMLM.util.utils;

/**
 *
 * @author Joran Deschamps
 */
public class FocusPanel extends PropertyPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6190484716500047549L;

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
	public static String FOCUS_POSITION = "Z stage position";
	public static String FOCUS_STABILIZATION = "Z stage focus locking";
	
	//////// Parameters
	public static String PARAM_LARGESTEP = "Large step";
	public static String PARAM_SMALLSTEP = "Small step";
	public static String PARAM_IDLE = "Idle time";
	public static String PARAM_NPOS = "Number of points";
	
	//////// Default parameters
	private double smallstep_, largestep_;
	private int idle_, npos_; 
	private boolean initialised = false; // used only for initial textfield value
	
	public FocusPanel(String label) {
		super(label);
	}
	
	public void setupPanel() {
		graph_ = new TimeChart("position","time","position",npos_,300,200,false);
		updater_ = new TimeChartUpdater(graph_,getUIProperty(FOCUS_POSITION),idle_);
		
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder(null, getLabel(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0,0,0)));
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
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,15,2,15);
		c.gridwidth = 1;
		poslabel_ = new JLabel("Position:");
		panelLeftControl_.add(poslabel_, c);

		c.gridy = 1;
		c.insets = new Insets(2,15,40,15);
		textfieldPosition_ = new JTextField();
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
					double val = Double.parseDouble(typed);
					if (val >= 0) {
						if (!togglebuttonLock_.isSelected()) {
							changeProperty(FOCUS_POSITION, typed);
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
					double val = Double.parseDouble(typed);
					if (val >= 0) {
						if (!togglebuttonLock_.isSelected()) {
							changeProperty(FOCUS_POSITION, typed);
						}
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
        	}
        });
		panelLeftControl_.add(textfieldPosition_, c);

		c.gridy = 4;
		c.insets = new Insets(2,15,2,15);
		togglebuttonMonitor_ = new JToggleButton("Monitor");
		togglebuttonMonitor_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					monitorPosition(true);
				} else if(e.getStateChange()==ItemEvent.DESELECTED){
					monitorPosition(false);
				}
			}
        });
		panelLeftControl_.add(togglebuttonMonitor_, c);
		
		c.gridy = 5;
		togglebuttonLock_ = new JToggleButton("Lock");
		togglebuttonLock_.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					lockPosition(true);
				} else if(e.getStateChange()==ItemEvent.DESELECTED){
					lockPosition(false);
				}
			}
        });
		panelLeftControl_.add(togglebuttonLock_, c);
	}

	private void initCentralPanel(){
		 panelGraph_.add(graph_.getChart());
	}
	
	private void initRightPanel(){
		panelRightControl_.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,10,2,10);
		c.gridwidth = 2;
		buttonLargeStepsUp_ = new JButton("^^");
		buttonLargeStepsUp_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(largestep_);
            }
        });
		panelRightControl_.add(buttonLargeStepsUp_, c);
		
		c.gridy = 1;
		buttonSmallStepsUp_ = new JButton("^");
		buttonSmallStepsUp_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(smallstep_);
            }
        });
		panelRightControl_.add(buttonSmallStepsUp_, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(2,1,2,1);
		largesteplabel_ = new JLabel(">>");
		panelRightControl_.add(largesteplabel_, c);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(2,10,2,10);
		textfieldLargeStep_ = new JTextField(String.valueOf(largestep_));
		textfieldLargeStep_.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
        	    if(!utils.isNumeric(textfieldLargeStep_.getText())) {
        	        return;
        	    } 
				try {
					largestep_ = Double.parseDouble(textfieldLargeStep_.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
         });
		textfieldPosition_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
        	    if(!utils.isNumeric(textfieldLargeStep_.getText())) {
        	        return;
        	    } 
				try {
					largestep_ = Double.parseDouble(textfieldLargeStep_.getText());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
        	}
        });
		panelRightControl_.add(textfieldLargeStep_, c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(2,1,2,1);
		smallsteplabel_ = new JLabel(">");
		panelRightControl_.add(smallsteplabel_, c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		c.insets = new Insets(2,10,2,10);
		textfieldSmallStep_ = new JTextField(String.valueOf(smallstep_));
		textfieldSmallStep_.addFocusListener(new FocusListener() {  
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
        	    if(!utils.isNumeric(textfieldLargeStep_.getText())) {
        	        return;
        	    } 
				try {
					largestep_ = Double.parseDouble(textfieldLargeStep_.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
         });
		textfieldSmallStep_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
        	    if(!utils.isNumeric(textfieldLargeStep_.getText())) {
        	        return;
        	    } 
				try {
					largestep_ = Double.parseDouble(textfieldLargeStep_.getText());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
        	}
        });
		panelRightControl_.add(textfieldSmallStep_, c);
		
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(2,10,2,10);
		buttonSmallStepsDown_ = new JButton("v");
		buttonSmallStepsDown_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(-smallstep_);
            }
        });
		panelRightControl_.add(buttonSmallStepsDown_, c);

		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		buttonLargeStepsDown_ = new JButton("vv");
		buttonLargeStepsDown_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveRelativePosition(-largestep_);
            }
        });
		panelRightControl_.add(buttonLargeStepsDown_, c);
	}
	
	protected void moveRelativePosition(double step) {
    	String s = getUIPropertyValue(FOCUS_POSITION);
    	
    	if (utils.isNumeric(s)) {
    		double val = Double.parseDouble(s)+step;
    		changeProperty(FOCUS_POSITION,String.valueOf(val));
    	}		
	}

	protected void lockPosition(boolean b) {
		if(b){
			changeProperty(FOCUS_STABILIZATION,TwoStateUIProperty.ON);
		} else {
			changeProperty(FOCUS_STABILIZATION,TwoStateUIProperty.OFF);
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
		if(utils.isNumeric(s)){
			return s;
		}
		return null;
	}

	@Override
	protected void initializeProperties() {
		addUIProperty(new UIProperty(this, FOCUS_POSITION,"Position property of the stage, used for movements and monitoring."));
		addUIProperty(new TwoStateUIProperty(this, FOCUS_STABILIZATION,"Property used for focus stabilization."));
	}
	
	@Override
	protected void initializeParameters() {
		smallstep_ = 0.2;
		largestep_ = 2;
		idle_ = 100;
		npos_ = 30; 
		
		addUIParameter(new DoubleUIParameter(this, PARAM_LARGESTEP,"Default value for large z stage step.",largestep_));
		addUIParameter(new DoubleUIParameter(this, PARAM_SMALLSTEP,"Default value for large z stage step.",smallstep_));
		addUIParameter(new IntUIParameter(this, PARAM_IDLE,"Idle time of the stage position monitoring.",idle_)); // thread idle time
		addUIParameter(new IntUIParameter(this, PARAM_NPOS,"Number of stage positions displayed in the chart.",npos_)); // number of point in the graph
	}
	
	@Override
	protected void changeProperty(String name, String value) {
		if(name.equals(FOCUS_POSITION) || name.equals(FOCUS_STABILIZATION)){
			getUIProperty(name).setPropertyValue(value);
		}
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		if(name.equals(FOCUS_POSITION)){
			if(utils.isNumeric(newvalue)){
				if(!initialised){
					initialised = true;
					textfieldPosition_.setText(newvalue);
				}
			}
		} else if(name.equals(FOCUS_STABILIZATION)){
			if(newvalue.equals(TwoStateUIProperty.ON)){
				togglebuttonLock_.setSelected(true);
			} else {
				togglebuttonLock_.setSelected(false);
			}
		}
	}



	@Override
	public void parameterhasChanged(String label) {
		if(label.equals(PARAM_LARGESTEP)){
			largestep_ = ((DoubleUIParameter) getUIParameter(PARAM_LARGESTEP)).getValue();
			textfieldLargeStep_.setText(String.valueOf(largestep_));
		} else if(label.equals(PARAM_SMALLSTEP)){
			smallstep_ = ((DoubleUIParameter) getUIParameter(PARAM_SMALLSTEP)).getValue();
			textfieldSmallStep_.setText(String.valueOf(smallstep_));
		}else if(label.equals(PARAM_IDLE)){
			if(((IntUIParameter) getUIParameter(PARAM_IDLE)).getValue() != idle_){
				idle_ = ((IntUIParameter) getUIParameter(PARAM_IDLE)).getValue();
				updater_.changeIdleTime(idle_);
			}
		}else if(label.equals(PARAM_NPOS)){
			if(((IntUIParameter) getUIParameter(PARAM_NPOS)).getValue() != npos_){
				npos_ = ((IntUIParameter) getUIParameter(PARAM_NPOS)).getValue();
				panelGraph_.remove(graph_.getChart());
				graph_ = new TimeChart("position","time","position",npos_,300,200,false);
				panelGraph_.add(graph_.getChart());
				panelGraph_.updateUI();
				updater_.changeChart(graph_);
			}
		}
	}

	@Override
	public void shutDown() {
		updater_.stopUpdater();
	}
}
