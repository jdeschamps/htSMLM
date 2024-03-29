package de.embl.rieslab.htsmlm;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.IntegerUIParameter;
import de.embl.rieslab.emu.ui.uiparameters.StringUIParameter;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.emu.utils.exceptions.IncorrectUIParameterTypeException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIPropertyException;
import de.embl.rieslab.htsmlm.graph.TimeChart;

/**
 * A panel showing device properties value (laser power) over time.
 *
 * The values can be scaled using a linear calibration (panel parameter).
 */
public class PowerMeterPanel extends ConfigurablePanel{

	private static final long serialVersionUID = 1L;

	// components
	private TimeChart graph_;
	private final JLabel label_;
	private final JComboBox<String> comboBox_;
	private final JToggleButton toggleButton_;
	private final JPanel panelGraph_;
	
	// parameters and properties
	public static final String PARAM_WAVELENGTHS = "wavelengths";
	public static final String PARAM_SLOPES = "slopes";
	public static final String PARAM_OFFSETS = "offsets";
	public static final String PARAM_IDLE = "idle time (ms)";
	public static final String PARAM_NPOS = "number of points";
	
	private final ArrayList<Double> slopes;
	private final ArrayList<Double> offsets;
	private int idleTime_, nPos_;
	private boolean monitoring_ = false;
	private Processor monitorThread;
	private int selectedWavelength_;
	
	public static final String PROP_POWER = "Laser powermeter";
	
	public PowerMeterPanel(String label) {
		super(label);

		slopes = new ArrayList<Double>();
		offsets = new ArrayList<Double>();
		
		// components
		String[] temp = new String[1];
		temp[0] = "########";
		
		label_ = new JLabel("1000 mW");
		Font f = label_.getFont();
		label_.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		
		selectedWavelength_ = 0;
		comboBox_ = new JComboBox<String>(temp);
		comboBox_.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
	    		selectedWavelength_ = comboBox_.getSelectedIndex();
		    }
		});
		toggleButton_ = new JToggleButton("Monitor");
		
		panelGraph_ = new JPanel();
		graph_ = getNewGraph();
		panelGraph_.add(graph_.getChart());
		
		// layout
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,10,2,10);
		c.gridwidth = 1;	
		this.add(comboBox_,c);
		
		c.gridx = 1;
		c.insets = new Insets(5,25,2,10);
		this.add(label_,c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 3;	
		c.gridwidth = 4;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1,1,1,1);
		this.add(panelGraph_,c);	
		
		c.gridy = 4;
		c.gridheight = 1;	
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(20,10,2,10);
		this.add(toggleButton_,c);
	}

	private TimeChart getNewGraph() {
		return new TimeChart("power","time","power", nPos_,350,200,false);
	}
	
	private void setLabel(String value) {
		label_.setText(value+" mW");
	}
	
	@Override
	protected void initializeProperties() {
		addUIProperty(new UIProperty(this, PROP_POWER, "Laser power signal value. The device property should "
				+ "be numerical. Slope and offset to convert it to mW can be set in the Parameters tab."));
	}

	@Override
	protected void propertyhasChanged(String propertyName, String newValue) {
		if(PROP_POWER.equals(propertyName)) {
			if(EmuUtils.isNumeric(newValue)){
				// round
				double value = (Math.floor(Double.parseDouble(newValue) * 100) / 100);
				setLabel(String.valueOf(convertPower(value)));
			}
		}
	}

	@Override
	protected void addComponentListeners() {
		SwingUIListeners.addActionListenerToBooleanAction(b -> monitorPower(b), toggleButton_);
	}

	@Override
	protected void initializeInternalProperties() {}

	@Override
	public void internalpropertyhasChanged(String propertyName) {}

	@Override
	protected void initializeParameters() {
		String wlgth = "405,488,561,638";
		String slope = "1,1,1,1";
		String offset = "0,0,0,0";

		String descW = "Comma-separated wavelengths of the different lasers measured by the powermeter property.";
		String descSl = "Comma-separated slopes to convert the measurements to Watts. Make sure to input as "
				+ "many slopes as there are wavelengths, otherwise, a default value of 1 will be applied.";
		String descOf = "Comma-separated offsets to convert the measurements to mW. Make sure to input as "
				+ "many slopes as wavelengths, otherwise a default value of 1 will be applied.";
		String descId = "Idle time (ms) between two updates of the powermeter value.";
		String descN = "Number of laser power measurements displayed in the chart (x axis).";
		
		
		
		addUIParameter(new StringUIParameter(this, PARAM_WAVELENGTHS, descW, wlgth));
		addUIParameter(new StringUIParameter(this, PARAM_SLOPES, descSl, slope));
		addUIParameter(new StringUIParameter(this, PARAM_OFFSETS, descOf, offset));
		

		idleTime_ = 1000;
		nPos_ = 100;
		addUIParameter(new IntegerUIParameter(this, PARAM_IDLE, descId, idleTime_));
		addUIParameter(new IntegerUIParameter(this, PARAM_NPOS, descN, nPos_));
	}

	@Override
	protected void parameterhasChanged(String parameterName) {
		if(PARAM_WAVELENGTHS.equals(parameterName)) {
			try {
				String s = getStringUIParameterValue(PARAM_WAVELENGTHS);
				String[] vals = s.split(",");
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(vals);
				comboBox_.removeAllItems();
				comboBox_.setModel(model);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_SLOPES.equals(parameterName)) {
			try {
				String s = getStringUIParameterValue(PARAM_SLOPES);
				
				String[] vals = s.split(",");
				for(int i=0;i<vals.length;i++) {
					if(!EmuUtils.isNumeric(vals[i])) {
						vals[i] = "1";
					}
				}
				slopes.clear();
				
				for(String v: vals) {
					slopes.add(Double.parseDouble(v));
				}
				
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_OFFSETS.equals(parameterName)) {
			try {
				String s = getStringUIParameterValue(PARAM_OFFSETS);
				
				String[] vals = s.split(",");
				for(int i=0;i<vals.length;i++) {
					if(!EmuUtils.isNumeric(vals[i])) {
						vals[i] = "0";
					}
				}
				offsets.clear();
				
				for(String v: vals) {
					offsets.add(Double.parseDouble(v));
				}
				
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_IDLE.equals(parameterName)){
			try {
				int val = getIntegerUIParameterValue(PARAM_IDLE);
				if(val != idleTime_){
					idleTime_ = val;
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}else if(PARAM_NPOS.equals(parameterName)){
			try {
				int val = getIntegerUIParameterValue(PARAM_NPOS);
				if(val != nPos_){
					nPos_ = val;
					panelGraph_.remove(graph_.getChart());
					graph_ = getNewGraph();
					panelGraph_.add(graph_.getChart());
					panelGraph_.updateUI();
				}
			} catch (IncorrectUIParameterTypeException | UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public String getDescription() {
		return "This panel is meant to plot laser power over time. The laser power is read using the "+PROP_POWER+" property, and can be converted "
				+ "using the slope and offset parameters. Wavelength can be selected (with its own slope and offset) by inputing multiple comma separated"
				+ "wavelengths as a parameter.";
	}

	@Override
	public void shutDown() {
		monitorPower(false);
	}
	
	protected void monitorPower(boolean b) {
		if(b){
			try {
				monitoring_ = true;
				monitorThread = new Processor(this.getUIProperty(PROP_POWER));
				monitorThread.execute();
			} catch (UnknownUIPropertyException e) {
				e.printStackTrace();
				monitoring_ = false;
			}
		} else {
			monitoring_ = false;
		}
	}
	
	public boolean isMonitoring(){
		return monitoring_;
	}
	
	protected int getCurrentWavelength() {
		return selectedWavelength_;
	}
	
	protected double convertPower(double value) {
		int index = getCurrentWavelength();				
		
		double slope, offset;
		if(index < slopes.size()) {
			slope = slopes.get(index);
		} else {
			slope = 1.;
		}

		if(index < offsets.size()) {
			offset = offsets.get(index);
		} else {
			offset = 0.;
		}

		return slope*value+offset;
	}
	
	private class Processor extends SwingWorker<Integer, Double> {

		private UIProperty property_;
		
		public Processor(UIProperty prop) {
			property_ = prop;
		}
		
		@Override
		protected Integer doInBackground() throws Exception {
			Double value;

			while(monitoring_){	
				value = Double.parseDouble(property_.getPropertyValue());
				publish(convertPower(value));

				Thread.sleep(idleTime_);
			}
			return 1;
		}

		@Override
		protected void process(List<Double> chunks) {
			for(Double result : chunks){
				graph_.addPoint(result);
				
				// round
				double value = (Math.floor(result * 100) / 100);
				setLabel(String.valueOf(value));
			}
		}
	}
}
