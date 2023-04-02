package de.embl.rieslab.htsmlm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.ui.ConfigurablePanel;
import de.embl.rieslab.emu.ui.swinglisteners.SwingUIListeners;
import de.embl.rieslab.emu.ui.uiparameters.UIPropertyParameter;
import de.embl.rieslab.emu.utils.exceptions.UnknownUIParameterException;
import de.embl.rieslab.htsmlm.acquisitions.AcquisitionController;
import de.embl.rieslab.htsmlm.acquisitions.utils.AcquisitionDialogs;
import de.embl.rieslab.htsmlm.acquisitions.utils.AcquisitionInformationPanel;
import de.embl.rieslab.htsmlm.acquisitions.utils.SummaryTreeController;
import de.embl.rieslab.htsmlm.uipropertyflags.FocusStabFlag;
import de.embl.rieslab.htsmlm.uipropertyflags.TwoStateFlag;


/**
 * Acquisition panel allowing configuring, saving/loading and running acquisitions.
 * 
 * @author Joran Deschamps
 *
 */
public class AcquisitionPanel extends ConfigurablePanel{
		
	private static final long serialVersionUID = 1L;


	private final SystemController systemController_;
	private final AcquisitionController acqController_;
    private final MainFramehtSMLM mainFrame_;
    private final SummaryTreeController summaryTree_;
    
    ///// Parameters
    public final static String PARAM_LOCKING = "Focus stabilization";
    public final static String PARAM_BFP = "BFP lens";
    public final static String PARAM_BRIGHTFIELD = "Bright field";
	
    ///// Convenience variables
	private String paramBFP_, paramLocking_, paramBrightField_;
    
    ///// UI
    private JButton jButton_setPath;
    private JToggleButton jToggle_startStop, jToggle_showSummary;
    private JButton jButton_load,jButton_configAcq,jButton_saveAcq;
    private JLabel jLabel_expName, jLabel_path, jLabel_progress;
    private JProgressBar jProgressBar_progress;
    private JTextField jTextField_expName;
    private JTextField jTextField_path;
    private JTextPane jTextPane_progress;
    	
	public AcquisitionPanel(SystemController systemController, MainFramehtSMLM mainFrame){
		super("Acquisitions");
		
		systemController_ = systemController;
		mainFrame_ = mainFrame;
		
		// instantiate panels
		initPanel();
		
		// create acquisition controller
		acqController_ = new AcquisitionController(systemController,
												   this, 
												   new AcquisitionInformationPanel(jTextPane_progress),
												   mainFrame_.getActivationController());	
		
		// instantiate the summary tree controller
		summaryTree_ = new SummaryTreeController(mainFrame_, systemController_, acqController_, this, jToggle_showSummary);
	}
	
	private void initPanel() {
		// set path button
	    jButton_setPath = new JButton("...");
	    jButton_setPath.setToolTipText("Select the folder to save the experiments to.");

	    // start/stop button
	    jToggle_startStop = new JToggleButton("Start");
	    jToggle_startStop.setToolTipText("Start/stop the experiments.");
	    jToggle_startStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if(selected) {
					// attempt to start acquisitions
					String path = getExperimentPath();
					String name = getExperimentName();

					if(path == null || path.equals("")){
						// invalid path
						AcquisitionDialogs.showNoPathMessage();
						jToggle_startStop.setSelected(false);
					} else if(name == null || name.equals("")){
						// invalid name
						AcquisitionDialogs.showNoNameMessage();	
						jToggle_startStop.setSelected(false);
					} else if(acqController_.isAcquisitionListEmpty()){
						// no acquisition
						AcquisitionDialogs.showNoAcqMessage();
						jToggle_startStop.setSelected(false);
					} else {
						// start acquisitions
						boolean b = acqController_.startAcquisition();
						if(b){
							jToggle_startStop.setText("Stop");
							jProgressBar_progress.setMaximum(acqController_.getNumberOfPositions());
						} else {
							jToggle_startStop.setSelected(false);
						}
					}
				} else {
					// stop acquisitions
					jToggle_startStop.setText("Start");
					acqController_.stopAcquisition();
				}
			}
		});
        
	    // load an experiment
        jButton_load = new JButton("Load");
        jButton_load.setToolTipText("Load an experiment file (.uiacq).");
        
        // show configuration wizard window
        jButton_configAcq = new JButton("Configure");
        jButton_configAcq.setToolTipText("Start the acquisition wizard to create a set of acquisitions.");
        
        // save acquisition button
        jButton_saveAcq = new JButton("Save as");
        jButton_saveAcq.setToolTipText("Save the current acquisition list to a file of your choice.");

        // show summary button
        jToggle_showSummary = new JToggleButton(">>");        
        jToggle_showSummary.setToolTipText("Show the acquisition list summary tree.");
        
        // labels
        jLabel_path = new JLabel("Path");
        jLabel_expName = new JLabel("Experiment name");
        jLabel_progress = new JLabel("Progress");

        // experiment progress bar
	    jProgressBar_progress = new JProgressBar();
	    jProgressBar_progress.setMinimum(0);
        
	    // experiment name and path
	    jTextField_expName = new JTextField();
	    jTextField_expName.setToolTipText("Experiment name.");
	    jTextField_path = new JTextField();
	    jTextField_path.setToolTipText("Experiment path.");
	    
	    // progress panel, where updates are written
		jTextPane_progress = new JTextPane();
	    jTextPane_progress.setBackground(this.getBackground());
	    
	    JScrollPane scroll = new JScrollPane(jTextPane_progress);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    scroll.setBounds(50, 30, 300, 50);

	    
	    /////////////////////////////////////////////////
	    /// Grid bag layout
		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		c.fill = GridBagConstraints.HORIZONTAL;

		// upper part
		c.gridx = 0;
		c.gridy = 1;
		this.add(jLabel_path, c);	    

		c.gridx = 0;
		c.gridy = 2;
		this.add(jLabel_expName, c);

		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		this.add(jTextField_path, c);
		
		c.gridx = 1;
		c.gridy = 2;
		this.add(jTextField_expName, c);

		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx =0.1;
		this.add(jButton_setPath, c);
		
		// progress 
		c.gridx = 0;
		c.gridy = 3;
		this.add(jLabel_progress, c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		this.add(jProgressBar_progress, c);	
		
		c.gridx = 3;
		c.gridy = 3;
		c.gridwidth = 1;
		c.weightx =0.1;
		this.add(jToggle_showSummary, c);	 

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 4;
		c.gridheight = 3;
		c.weightx = 0.8;
		c.weighty = 0.8;
		c.fill = GridBagConstraints.BOTH;
		this.add(scroll, c);	 
		
		// lower part
		JPanel lower = new JPanel();
		GridLayout gridlayout = new GridLayout(0,4);
		lower.setLayout(gridlayout);

		lower.add(jToggle_startStop);
		lower.add(jButton_configAcq);
		lower.add(jButton_saveAcq);
		lower.add(jButton_load);
		
		c.gridx = 0;
		c.gridy = 8;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;		
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(lower,c);
	}
		
	private void showSelectPath(){
    	JFileChooser fc = new JFileChooser();
    	fc.setCurrentDirectory(new java.io.File(".")); 
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	int returnVal = fc.showOpenDialog(this);
    	if(returnVal == JFileChooser.APPROVE_OPTION) {
    	    File folder = fc.getSelectedFile();
    	    
    	    // update path
    	    jTextField_path.setText(folder.getAbsolutePath());  
    	}
	}


	/////////////////////////////////////////////////////////////////////////////
	//////
	////// PropertyPanel methods
	//////
	
	@Override
	protected void initializeProperties() {
		// Do nothing
	}

	@Override
	protected void initializeInternalProperties() {
		// Do nothing
	}
  
	@Override
	protected void initializeParameters() {
		paramBFP_ = UIPropertyParameter.NO_PROPERTY;
		paramLocking_ = UIPropertyParameter.NO_PROPERTY;
		paramBrightField_ = UIPropertyParameter.NO_PROPERTY;

		String descBfp = "Choose among the mapped GUI properties that have two states. Originally aimed for a "
				+ "Bertrand lens, this parameter is used by a specific type of acquisition (BFP). Before a BFP "
				+ "acquisition, the selected GUI property is set to its on state, a single frame is recorded, "
				+ "and the property is finally set to its off state.";

		String descBf = "Choose among the mapped GUI properties that have two states. Originally aimed for a "
				+ "bright-field LED array, this parameter is used by a specific type of acquisition (Bright-field). "
				+ "Before a Bright-field acquisition, the selected GUI property is set to its on state, a single "
				+ "frame is recorded, and the property is finally set to its off state. "; 
		
		String descFl = "Select the \"Z stage focus locking\" property if it has been mapped in the Properties tab. "
				+ "This allows the acquisition controller to turn on or off the focus stabilization depending on the "
				+ "designed experiments.";
		
		addUIParameter(new UIPropertyParameter(this, PARAM_BFP,descBfp, new TwoStateFlag()));
		addUIParameter(new UIPropertyParameter(this, PARAM_LOCKING,descFl, new FocusStabFlag()));
		addUIParameter(new UIPropertyParameter(this, PARAM_BRIGHTFIELD,descBf, new TwoStateFlag()));
	}

	@Override
	public void propertyhasChanged(String name, String newvalue) {
		// Do nothing
	}

	@Override
	public void parameterhasChanged(String label) {
		if(PARAM_BFP.equals(label)){
			try {
				paramBFP_ = getStringUIParameterValue(PARAM_BFP);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_LOCKING.equals(label)){
			try {
				paramLocking_ = getStringUIParameterValue(PARAM_LOCKING);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		} else if(PARAM_BRIGHTFIELD.equals(label)){
			try {
				paramBrightField_ = getStringUIParameterValue(PARAM_BRIGHTFIELD);
			} catch (UnknownUIParameterException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void internalpropertyhasChanged(String label) {
		// Do nothing
	}

	@Override
	public void shutDown() {
		acqController_.shutDown();
		summaryTree_.shutDown();
	}

	@Override
	public String getDescription() {
		return "The acquisition tab gives access to an acquisition tool. By clicking on the \"Configure\" button, you can design a set of experiments "
				+ "to be performed one after the other on each position of Micro-manager position list. The acquisition wizard includes different "
				+ "type of acquisitions, depending on which properties have been allocated. Each acquisition type has specific settings. The wizard "
				+ "also allows the user to set the preset groups and properties to the value of their choice for each acquisition. Note that only the "
				+ "allocated properties are shown. After designing an experimental set, a summary is accessible via the \">>\" button. Acquisition lists "
				+ "can be saved and loaded.";
	}
	

	/**
	 * Return the property name that corresponds to the {@code parameterName}.
	 * 
	 * @param parameterName Parameter name
	 * @return Property name as a string
	 */
	public String getParameterValues(String parameterName) {
		if(PARAM_BFP.equals(parameterName)){
			return paramBFP_;
		} else if(PARAM_LOCKING.equals(parameterName)){
			return paramLocking_;
		} else if(PARAM_BRIGHTFIELD.equals(parameterName)){
			return paramBrightField_;
		}
		return null;
	}

	/**
	 * Update the progress bar with a new value.
	 * 
	 * @param newValue New value
	 */
	public void updateProgressBar(int newValue) {
		jProgressBar_progress.setValue(newValue);
	}

	/**
	 * Get the experiment name from the corresponding text field.
	 * 
	 * @return Experiment name as a string.
	 */
	public String getExperimentName() {
		return jTextField_expName.getText();
	}

	
	/**
	 * Get the experiment path from the corresponding text field.
	 * 
	 * @return Experiment path as a string.
	 */
	public String getExperimentPath() {
		return jTextField_path.getText();
	}
	
	/**
	 * Unselect the start/stop button and set its text to "stop".
	 */
	public void showStop(){
		jToggle_startStop.setSelected(false);
		jToggle_startStop.setText("Start");
	}
	
	public SummaryTreeController getSummaryTreeController() {
		return summaryTree_;
	}

	@Override
	protected void addComponentListeners() {
		// select path
        SwingUIListeners.addActionListenerToUnparametrizedAction(this::showSelectPath, jButton_setPath);
        
        // load experiment
        SwingUIListeners.addActionListenerToUnparametrizedAction(acqController_::loadAcquisitionList, jButton_load);
        
        // save experiment
        SwingUIListeners.addActionListenerToUnparametrizedAction(acqController_::saveAcquisitionList, jButton_saveAcq);
        
        // show configuration wizard
        SwingUIListeners.addActionListenerToUnparametrizedAction(acqController_::startWizard, jButton_configAcq);
        
        // show/hide summary tree
        SwingUIListeners.addActionListenerToBooleanAction(summaryTree_::showSummary, jToggle_showSummary);

		jToggle_showSummary.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if (jToggle_showSummary.isSelected()){
					jToggle_showSummary.setText("<<");
				} else {
					jToggle_showSummary.setText(">>");
				}
			}
		});
	}
}
