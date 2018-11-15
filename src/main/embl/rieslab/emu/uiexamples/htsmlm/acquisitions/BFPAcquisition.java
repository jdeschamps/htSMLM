package main.embl.rieslab.emu.uiexamples.htsmlm.acquisitions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import main.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import main.embl.rieslab.emu.ui.uiproperties.UIProperty;
import main.embl.rieslab.emu.ui.uiproperties.filters.PropertyFilter;
import main.embl.rieslab.emu.ui.uiproperties.filters.SinglePropertyFilter;

public class BFPAcquisition extends Acquisition {
	
	// Convenience constants		
	private final static String PANE_NAME = "BFP panel";
	private final static String LABEL_GROUP = "Group:";
	private final static String LABEL_GROUPNAME = "GroupName";
	private final static String LABEL_EXPOSURE = "Exposure (ms):";
	private final static String LABEL_PAUSE = "Pause (s):";
		
	// UI property
	private TwoStateUIProperty bfpprop_;
	

	public BFPAcquisition(double exposure, HashMap<String,String[]> configgroups, UIProperty bfpprop) {
		super(AcquisitionType.BFP, exposure, configgroups);
		
		if(bfpprop instanceof TwoStateUIProperty){
			bfpprop_ = (TwoStateUIProperty) bfpprop;
		} else {
			bfpprop_ = null;
		}
		
		this.setNumberFrames(1);
		this.setIntervalMs(0);
	}

	@Override
	public void preAcquisition() {
		if(bfpprop_ != null){
			bfpprop_.setPropertyValue(TwoStateUIProperty.getOnStateName());
		}
	}

	@Override
	public void postAcquisition() {
		if(bfpprop_ != null){
			bfpprop_.setPropertyValue(TwoStateUIProperty.getOffStateName());
		}
	}

	@Override
	public boolean stopCriterionReached() {
		return false;
	}

	@Override
	public String getPanelName(){
		return PANE_NAME;
	}
	
	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		pane.setName(getPanelName());

		pane.setBorder(BorderFactory.createTitledBorder(null, ACQ_SETTINGS, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0,0,0)));
		((TitledBorder) pane.getBorder()).setTitleFont(((TitledBorder) pane.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
				
		JLabel channellab, exposurelab, waitinglab;
		JComboBox channelgroup;
		final JComboBox channelname;
		JSpinner exposurespin, waitingspin;
		
		channellab = new JLabel(LABEL_GROUP);
		exposurelab = new JLabel(LABEL_EXPOSURE);
		waitinglab = new JLabel(LABEL_PAUSE);
		
		channelgroup = new JComboBox(this.getConfigGroupList());
		channelgroup.setName(LABEL_GROUP);
		channelgroup.getModel().setSelectedItem(this.getConfigGroup());
		
		channelname = new JComboBox(this.getConfigGroupNames(this.getConfigGroup()));
		channelname.setName(LABEL_GROUPNAME);
		channelgroup.addActionListener(
	            new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
	                    JComboBox combo = (JComboBox)e.getSource();
	                    String current = (String)combo.getSelectedItem();
	
	                    DefaultComboBoxModel model = new DefaultComboBoxModel(getConfigGroupNames(current));
	                    channelname.setModel( model );
					}
	            }            
	    );
		
		channelname.getModel().setSelectedItem(this.getConfigName());		

		exposurespin = new JSpinner(new SpinnerNumberModel(Math.max(this.getExposure(),1), 1, 10000000, 1));
		exposurespin.setName(LABEL_EXPOSURE);
		waitingspin = new JSpinner(new SpinnerNumberModel(this.getWaitingTime(), 0, 10000000, 1)); 
		waitingspin.setName(LABEL_PAUSE);

		channelgroup.setPreferredSize(exposurespin.getPreferredSize());
		channelname.setPreferredSize(exposurespin.getPreferredSize());
		
		int nrow = 2;
		int ncol = 4;
		JPanel[][] panelHolder = new JPanel[nrow][ncol];    
		pane.setLayout(new GridLayout(nrow,ncol));

		for(int m = 0; m < nrow; m++) {
		   for(int n = 0; n < ncol; n++) {
		      panelHolder[m][n] = new JPanel();
		      pane.add(panelHolder[m][n]);
		   }
		}

		panelHolder[0][0].add(exposurelab);
		panelHolder[1][0].add(waitinglab);
		panelHolder[0][1].add(exposurespin);
		panelHolder[1][1].add(waitingspin);
		panelHolder[0][2].add(channellab);
		panelHolder[0][3].add(channelgroup);
		panelHolder[1][3].add(channelname);
		
		return pane;
	}

	@Override
	public void readOutParameters(JPanel pane) {
		if(pane.getName().equals(getPanelName())){
			Component[] pancomp = pane.getComponents();
			String groupname="", groupmember="";
			for(int j=0;j<pancomp.length;j++){
				if(pancomp[j] instanceof JPanel){
					Component[] comp = ((JPanel) pancomp[j]).getComponents();
					for(int i=0;i<comp.length;i++){
						if(!(comp[i] instanceof JLabel) && comp[i].getName() != null){
							if(comp[i].getName().equals(LABEL_GROUP) && comp[i] instanceof JComboBox){
								groupname = (String) ((JComboBox) comp[i]).getSelectedItem();
							}else if(comp[i].getName().equals(LABEL_GROUPNAME) && comp[i] instanceof JComboBox){
								groupmember = (String) ((JComboBox) comp[i]).getSelectedItem();
							}else if(comp[i].getName().equals(LABEL_EXPOSURE) && comp[i] instanceof JSpinner){
								this.setExposureTime((Double) ((JSpinner) comp[i]).getValue());
							}else if(comp[i].getName().equals(LABEL_PAUSE) && comp[i] instanceof JSpinner){
								this.setWaitingTime((Integer) ((JSpinner) comp[i]).getValue());
							}
						}
					}
				}
			}	
			this.setConfigurationGroup(groupname, groupmember);
		}
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		return new SinglePropertyFilter(bfpprop_.getName());
	}

	@Override
	public String[] getSpecialSettings() {
		String[] s = new String[1];
		s[0] = "Exposure = "+this.getExposure()+" ms";
		return s;
	}

	@Override
	public String[][] getAdditionalJSONParameters() {
		return null;
	}
}