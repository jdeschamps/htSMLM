package de.embl.rieslab.htsmlm.acquisitions.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.micromanager.data.Datastore;
import org.micromanager.data.Datastore.SaveMode;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.micromanager.presetgroups.MMPresetGroupRegistry;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.acquisitions.AcquisitionController;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;

public class AcquisitionWizard {

	private AcquisitionController owner_;
	private JFrame frame_;
	private ArrayList<AcquisitionTab> tabs_;
	private JTabbedPane tabbedPane_;
	private JTextField waitField;
	private JTextField numPosField;
	private JRadioButton single;
	private JRadioButton multi;
	private final SystemController controller_;
	private HashMap<String, String> propertyValues_;
	
	public AcquisitionWizard(SystemController controller, AcquisitionController owner, HashMap<String, String> propertyValues){
		owner_ = owner;
		controller_ = controller;
		propertyValues_ = propertyValues;
		tabs_ = new ArrayList<AcquisitionTab>();
		
		Datastore.SaveMode saveMode = controller_.getStudio().data().getPreferredSaveMode();
		setUpFrame(3, 0, saveMode, new ArrayList<Acquisition>());
	}
	
	public AcquisitionWizard(SystemController controller, AcquisitionController owner, HashMap<String, String> propertyValues, Experiment exp) {
		owner_ = owner;
		controller_ = controller;
		propertyValues_ = propertyValues;
		tabs_ = new ArrayList<AcquisitionTab>();

		setUpFrame(exp.getPauseTime(),exp.getNumberPositions(), exp.getSaveMode(), exp.getAcquisitionList());
	}
	
	private void setUpFrame(int waitingtime, int numpos, Datastore.SaveMode savemode, ArrayList<Acquisition> acqlist) {
		frame_ = new JFrame("Acquisition wizard");
		JPanel contentpane = new JPanel();
		contentpane.setLayout(new BoxLayout(contentpane,BoxLayout.LINE_AXIS));

		contentpane.add(setUpLeftPanel(waitingtime, numpos, savemode));
		contentpane.add(setUpRightPanel(acqlist));
		
		frame_.add(contentpane);
		
		frame_.pack();
		frame_.setLocationRelativeTo(null);
		frame_.setVisible(true);
	}

	private JPanel setUpLeftPanel(int waitingtime, int numpos, SaveMode savemode) {
		JPanel leftpane = new JPanel();

		JButton add = new JButton("Add");
		add.setToolTipText("Add a new acquisition to the list.");
		
		JButton remove = new JButton("Remove");
		remove.setToolTipText("Remove the currently selected acquisition.");
		
		JButton left = new JButton("<");
		left.setToolTipText("Move the currently selected acquisition to the left.");
		
		JButton right = new JButton(">");
		right.setToolTipText("Move the currently selected acquisition to the right.");
		
		JButton save = new JButton("Save");
		save.setToolTipText("Save the acquisition list.");
		
		JLabel wait = new JLabel("Waiting (s)");
		waitField = new JTextField(String.valueOf(waitingtime));
		waitField.setPreferredSize(new Dimension(30,20));
		waitField.setToolTipText("Waiting time (s) before starting the experiment (this waiting period occurs only once for each position).");
		
		JLabel pos = new JLabel("Pos number");
		numPosField = new JTextField(String.valueOf(numpos));
		numPosField.setToolTipText("Number of positions to use from the position list (0 = use all).");
		
		add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	createNewTab();
            }
        });
		
		remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	removeTab();
            }
        });
		
		save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	saveAcqList();
            }
        });
		
		right.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveTabRight();
            }
        });
		
		left.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	moveTabLeft();
            }
        });
		
		// radio button group 
        single = new JRadioButton("Separate imgs");
        multi = new JRadioButton("Image stack");
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(single);
        buttonGroup.add(multi);

        if(savemode.equals(Datastore.SaveMode.SINGLEPLANE_TIFF_SERIES)) {
        	single.setSelected(true);
        } else {
        	multi.setSelected(true);
        }		
		
	    /////////////////////////////////////////////////
	    /// Grid bag layout
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,4,2,4);
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		pane.add(add, c);	

		c.gridx = 0;
		c.gridy = 1;
		pane.add(remove, c);	

		JPanel leftright = new JPanel();
		GridLayout gridlayout = new GridLayout(0,2);
		leftright.setLayout(gridlayout);

		leftright.add(left);
		leftright.add(right);
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		pane.add(leftright, c);	
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		pane.add(Box.createHorizontalStrut(10), c);	

		c.gridx = 0;
		c.gridy = 5;
		pane.add(wait, c);	
		
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		pane.add(waitField, c);

		c.gridx = 0;
		c.gridy = 6;
		pane.add(pos, c);	
		
		c.gridx = 1;
		c.gridy = 6;
		c.gridwidth = 2;
		pane.add(numPosField, c);

		c.gridx = 0;
		c.gridy = 7;
		pane.add(single, c);	

		c.gridx = 0;
		c.gridy = 8;
		pane.add(multi, c);	
		
		c.gridx = 0;
		c.gridy = 9;
		c.gridwidth = 2;
		pane.add(save, c);	

		leftpane.add(pane);
		leftpane.add(new JPanel());
		
		return leftpane;
	}

	private JTabbedPane setUpRightPanel(ArrayList<Acquisition> acqlist) {
		tabbedPane_ = new JTabbedPane();
		
		if(acqlist.size() == 0) { // if empty acquisition list
			AcquisitionTab acqtab = new AcquisitionTab(this, new AcquisitionFactory(owner_, controller_), propertyValues_);
			tabbedPane_.add(acqtab.getTypeName(), acqtab);
			tabs_.add(acqtab);
		} else {
			for(int i=0;i<acqlist.size();i++){
				tabs_.add(new AcquisitionTab(this, new AcquisitionFactory(owner_, controller_), propertyValues_, acqlist.get(i)));
		        tabbedPane_.add(tabs_.get(i), i);
			}
			tabbedPane_.setSelectedIndex(0);
		}
		
		return tabbedPane_;
	}
	
	protected void moveTabLeft() {
    	if(tabs_.size()>1){
    		int i = tabbedPane_.getSelectedIndex();

    		if(i>0){
        		AcquisitionTab tab = tabs_.get(i);

        		tabbedPane_.remove(i);
        		tabs_.remove(i);
        		
        		tabs_.add(i-1, tab);
        		tabbedPane_.add(tab, i-1);
        		tabbedPane_.setSelectedIndex(i-1);
    		}
    	} 		
	}

	protected void moveTabRight() {
    	if(tabs_.size()>1){
    		int i = tabbedPane_.getSelectedIndex();

    		if(i<tabs_.size()-1){
    			AcquisitionTab tab = tabs_.get(i);

    			tabbedPane_.remove(i);
        		tabs_.remove(i);
        		
        		tabs_.add(i+1, tab);
        		tabbedPane_.add(tab, i+1);
        		tabbedPane_.setSelectedIndex(i+1);
    		}
    	} 
	}

	protected void removeTab() {
    	if(tabs_.size()>1){
    		int i = tabbedPane_.getSelectedIndex();

    		tabbedPane_.remove(i);
    		tabs_.remove(i);
    	} 
	}

	protected void createNewTab() {
       	tabs_.add(new AcquisitionTab(this, new AcquisitionFactory(owner_, controller_), propertyValues_));
        tabbedPane_.add(tabs_.get(tabs_.size()-1), tabs_.size()-1);
        tabbedPane_.setSelectedIndex(tabs_.size()-1);
	}

	public void changeName(AcquisitionTab acquisitionTab) {
		setNameTab(acquisitionTab.getTypeName());
	}
	
    private void setNameTab(String name){
       	int i = tabbedPane_.getSelectedIndex();
       	if(i>=0 && i< tabbedPane_.getTabCount()){
           	tabbedPane_.setTitleAt(i, name);
       	}
    }
    	
    protected void saveAcqList() {
		owner_.setExperiment(new Experiment(getWaitingTime(), getNumberPositions(), getSaveMode(), getAcquisitionList()));
		shutDown();		
	}
    
	private int getWaitingTime() {
		String s = waitField.getText();
		if(EmuUtils.isInteger(s)){
			return Integer.parseInt(s); 
		}
		return 3000;
	}  
	
	private int getNumberPositions() {
		String s = numPosField.getText();
		if(EmuUtils.isInteger(s)){
			return Integer.parseInt(s); 
		}
		return 0;
	}
	
	private Datastore.SaveMode getSaveMode(){
		if(single.isSelected()) {
			return Datastore.SaveMode.SINGLEPLANE_TIFF_SERIES;
		} else {
			return Datastore.SaveMode.MULTIPAGE_TIFF;
		}
	}
	
	private ArrayList<Acquisition> getAcquisitionList() {
		ArrayList<Acquisition> acqlist = new ArrayList<Acquisition>();
		
		for(int i=0;i<tabs_.size();i++){
			acqlist.add(tabs_.get(i).getAcquisition());
		}
		
		return acqlist;
	}

	public boolean isRunning(){
		return frame_.isActive();
	}
	
	public HashMap<String, UIProperty> getPropertiesMap(){
		return controller_.getPropertiesMap();
	}
	
	public MMPresetGroupRegistry getMMPresetRegistry(){
		return controller_.getMMPresetGroupRegistry();
	}
	
	public void shutDown() {
		if(frame_ != null){
			frame_.dispose();
		}
	}


}
