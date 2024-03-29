package de.embl.rieslab.htsmlm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.*;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.ui.ConfigurableMainFrame;
import de.embl.rieslab.emu.utils.settings.BoolSetting;
import de.embl.rieslab.emu.utils.settings.Setting;
import de.embl.rieslab.emu.utils.settings.StringSetting;
import de.embl.rieslab.htsmlm.activation.ActivationController;

/**
 * Main frame.
 */
public class MainFramehtSMLM extends ConfigurableMainFrame{

	private static final long serialVersionUID = 1L;

	// settings
	private static final String SETTING_USE_POWERMETER = "Powermeter tab";
	private static final String SETTING_USE_TRIGGER = "Trigger tab";
	private static final String SETTING_USE_ADDFW = "Additional FW tab";
	private static final String SETTING_USE_SINGLEFW = "Single FW panel";
	private static final String SETTING_USE_QPD = "QPD tab";
	private static final String SETTING_USE_IBS2 = "iBeamSmart #2";
	private static final String SETTING_USE_IBS1 = "iBeamSmart #1";
	private static final String SETTING_NAME_IBS2 = "iBeamSmart #2 name";
	private static final String SETTING_NAME_IBS1 = "iBeamSmart #1 name";
	private static final String SETTING_NAME_ADDFILT = "Additional FW tab title";
	private static final String SETTING_SET_RESIZABLE = "Resizable";
	
	// configurable panels and other components
	private AdditionalFiltersPanel addFiltersPanel;
	private FocusPanel focusPanel;
	private QPDPanel qpdPanel;
	private PowerMeterPanel powerPanel;
	private IBeamSmartPanel focusLockPanel, focusLockPanel2;
	private AbstractFiltersPanel filterPanel;
	private LaserControlPanel[] controlPanels;
	private LaserPulsingPanel pulsePanel;
	private LaserTriggerPanel[] triggerPanels;
	private ActivationPanel activationPanel;
	private AdditionalControlsPanel addcontrolPanel;
	private AcquisitionPanel acqPanel;
	private JPanel lowerpanel;
	private JTabbedPane tab;

	
	public MainFramehtSMLM(String title, SystemController controller, TreeMap<String, String> pluginSettings) {
		super(title, controller, pluginSettings);
	}

	@Override
    protected void initComponents() {    	
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFramehtSMLM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

		setupPanels();
    }

    @SuppressWarnings("rawtypes")
	private void setupPanels(){
    	HashMap<String, Setting> settings = this.getCurrentPluginSettings();
    	
		JPanel lasers = new JPanel(); 
		controlPanels = new LaserControlPanel[4];
		lasers.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		//c.ipady = 30;
		c.gridy = 0;
		c.insets = new Insets(2,0,2,0);
		for(int i=0;i<controlPanels.length;i++){
			controlPanels[i] = new LaserControlPanel("Laser "+i);
			c.gridx = i;
			lasers.add(controlPanels[i], c);
		}

		setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		
		JPanel upperpane = new JPanel();
		upperpane.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 1;
		c2.gridheight = 3;
		c2.weightx = 0.2;
		c2.weighty = 0.8;
		pulsePanel = new LaserPulsingPanel("Activation");
		upperpane.add(pulsePanel,c2);

		c2.gridx = 1;
		c2.gridy = 0;
		c2.gridwidth = 3;
		c2.gridheight = 2;
		c2.weightx = 0.8;
		c2.weighty = 0.6;
		c2.fill = GridBagConstraints.VERTICAL;
		upperpane.add(lasers,c2);
				
		c2.gridx = 1;
		c2.gridy = 2;
		c2.gridwidth = 3;
		c2.gridheight = 1;
		c2.weightx = 0.8;
		c2.weighty = 0.2;
		c2.fill = GridBagConstraints.HORIZONTAL;
		if(((BoolSetting) settings.get(SETTING_USE_SINGLEFW)).getValue()) {
			filterPanel = new FiltersPanel("Filters");
		} else {
			filterPanel = new DualFWPanel("Filters");
		}
		upperpane.add(filterPanel,c2);
		this.add(upperpane);
		
		focusPanel = new FocusPanel("Focus");
	/*	c2.gridx = 0;
		c2.gridy = 3;
		c2.gridwidth = 4;
		c2.gridheight = 3;
		c2.weighty = 0.4;
		c2.fill = GridBagConstraints.HORIZONTAL;
		this.add(focusPanel,c2);
*/
		this.add(focusPanel);	
		
		///////////////////////////////////////////////////////////// lower panel
		lowerpanel = new JPanel();
		//GridBagConstraints c3 = new GridBagConstraints();
		lowerpanel.setLayout(new BoxLayout(lowerpanel,BoxLayout.LINE_AXIS));
		tab = new JTabbedPane();
		
		/////////// tab
		
		/// QPD tab
		if(((BoolSetting) settings.get(SETTING_USE_QPD)).getValue()) {
			qpdPanel = new QPDPanel("QPD");
			tab.add("QPD", qpdPanel);
		}

		/// iBeamSmart 1
		if(((BoolSetting) settings.get(SETTING_USE_IBS1)).getValue()) {
			String name = settings.get(SETTING_NAME_IBS1).getStringValue();
			focusLockPanel = new IBeamSmartPanel(name);
			tab.add(name, focusLockPanel);
		}

		/// iBeamSmart 2
		if(((BoolSetting) settings.get(SETTING_USE_IBS2)).getValue()) {
			String name = settings.get(SETTING_NAME_IBS2).getStringValue();
			focusLockPanel2 = new IBeamSmartPanel(name);
			tab.add(name, focusLockPanel2);
		}
		
		/// powermeter
		if(((BoolSetting) settings.get(SETTING_USE_POWERMETER)).getValue()) {
			String name = "Powermeter";
			powerPanel = new PowerMeterPanel(name);
			tab.add(name, powerPanel);
		}
		
		// Activation
		activationPanel = new ActivationPanel("Activation", this.getController());
		tab.add("Activation", activationPanel);
		
		/// laser trigger tab
		if(((BoolSetting) settings.get(SETTING_USE_TRIGGER)).getValue()) {
			JPanel lasertrigg = new JPanel();
			lasertrigg.setLayout(new GridLayout(2,2));
			triggerPanels = new LaserTriggerPanel[4];
			for(int i=0;i<triggerPanels.length;i++){
				triggerPanels[i] = new LaserTriggerPanel("Laser trigger "+i);
				lasertrigg.add(triggerPanels[i]);
			}
			tab.add("Trigger", lasertrigg);
		}
		
		/// Additional filters tab
		if(((BoolSetting) settings.get(SETTING_USE_ADDFW)).getValue()) {			
			String title = settings.get(SETTING_NAME_ADDFILT).getStringValue();
			addFiltersPanel = new AdditionalFiltersPanel(title);
			tab.add(title, addFiltersPanel);
		}
		
		/// Acquisition tab
		acqPanel = new AcquisitionPanel(getController(), this);
		tab.add("Acquisition", acqPanel);
		
		/*c3.gridx = 0;
		c3.gridy = 0;
		c3.gridwidth = 3;
		c3.gridheight = 3;
		c3.fill = GridBagConstraints.HORIZONTAL;*/
		lowerpanel.add(tab);

		////////// rest of the lower panel
		addcontrolPanel = new AdditionalControlsPanel("Controls");
	/*	c3.gridx = 3;
		c3.gridy = 0;
		c3.gridwidth = 1;
		c3.gridheight = 1;*/
		//c3.fill = GridBagConstraints.BOTH;
		lowerpanel.add(addcontrolPanel);
		
		/*c2.gridx = 0;
		c2.gridy = 6;
		c2.gridwidth = 4;
		c2.gridheight = 3;
		c2.weighty = 0.4;
		c2.fill = GridBagConstraints.NONE;

		this.add(lowerpanel,c2);*/
		this.add(lowerpanel);

		// set resizable
		this.pack();
		this.setResizable(((BoolSetting) settings.get(SETTING_SET_RESIZABLE)).getValue());
    }
    
    public Point getAcquisitionPanelLocation(){
    	Point loc = this.getLocation();
    	loc.x += tab.getLocation().x+lowerpanel.getLocation().x+acqPanel.getLocation().x;
    	loc.y += tab.getLocation().y+lowerpanel.getLocation().y+acqPanel.getLocation().y;
    	
    	return loc;
    }
    
	public ActivationController getActivationController(){
    	return activationPanel.getActivationController();
    }
	
	public JTabbedPane getTab() {
		return tab;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public HashMap<String, Setting> getDefaultPluginSettings() {
		HashMap<String, Setting> defaultSettings = new HashMap<String, Setting>();

		String descPow = "Check to use the powermeter tab in the plugin. Causes a new tab to appear, allowing "
				+ "users to read a Micro-manager property and linearly transform it to represent a power in mW. "
				+ "Users can choose a number of names for the channels (wavelengths), as well as the slopes and "
				+ "offsets for the linear transformation.  ";
		String descTri = "Check to use the trigger tab in the plugin. The trigger tab can be used with MicroFPGA, "
				+ "an FPGA board aimed, among other things, at triggering lasers in a flexible manner.  MicroFPGA "
				+ "can be found on Github (jdeschamps/MicroFPGA).";
		String descAFW = "Check to use the additional filters tab in the plugin. When selected, a new tab "
				+ "containing two sliders with four positions each. All slider position names and colors can "
				+ "be customized.";
		String descNameAFW = "Title of the additional filters tab, if the \"Additional FW tab\" setting is "
				+ "selected.";
		String descSFW = "Check to use a single filter wheel panel. When unchecked, the filter wheel panel "
				+ "contains two rows of filters, with customizable names and colors, corresponding to two "
				+ "different filter wheels.";
		String descQPD = "Check to use the QPD tab in the plugin. The QPD tab is aimed at plotting three "
				+ "signals (X, Y, Z) from a quadrant photo-diode in time. The tab contains one graph (X vs Y) "
				+ "and one progress bar (Z). This is part of a focus-locking system. ";
		String descUseIB2 = "Check to use the iBeamSmart #2 tab. Originally aimed at controlling an "
				+ "iBeamSmart laser from Toptica, the Toptica specific aspects can be disabled to correspond to "
				+ "a simple laser. ";
		String descUseIB1 = "Check to use the iBeamSmart #1 tab. Originally aimed at controlling an "
				+ "iBeamSmart laser from Toptica, the Toptica specific aspects can be disabled to correspond to "
				+ "a simple laser. ";
		String descNIB2 = "Name of iBeamSmart #2 tab, if the \"iBeamSmart #2\" setting is selected. The "
				+ "corresponding properties and parameters will bear the same name.";
		String descNIB1 = "Name of iBeamSmart #1 tab, if the \"iBeamSmart #1\" setting is selected. The "
				+ "corresponding properties and parameters will bear the same name.";
		String descResize = "Set the GUI resizable.";
		
		defaultSettings.put(SETTING_USE_POWERMETER, new BoolSetting(SETTING_USE_POWERMETER, descPow, true));
		defaultSettings.put(SETTING_USE_TRIGGER, new BoolSetting(SETTING_USE_TRIGGER, descTri, true));
		defaultSettings.put(SETTING_USE_ADDFW, new BoolSetting(SETTING_USE_ADDFW, descAFW, true));
		defaultSettings.put(SETTING_NAME_ADDFILT, new StringSetting(SETTING_NAME_ADDFILT, descNameAFW, "Additional filters"));
		defaultSettings.put(SETTING_USE_SINGLEFW, new BoolSetting(SETTING_USE_SINGLEFW, descSFW, true));
		defaultSettings.put(SETTING_USE_QPD, new BoolSetting(SETTING_USE_QPD, descQPD, true));
		defaultSettings.put(SETTING_USE_IBS2, new BoolSetting(SETTING_USE_IBS2, descUseIB2, true));
		defaultSettings.put(SETTING_USE_IBS1, new BoolSetting(SETTING_USE_IBS1, descUseIB1, true));
		defaultSettings.put(SETTING_NAME_IBS2, new StringSetting(SETTING_NAME_IBS2, descNIB2, "iBeamSmart #2"));
		defaultSettings.put(SETTING_NAME_IBS1, new StringSetting(SETTING_NAME_IBS1, descNIB1, "iBeamSmart #1"));
		defaultSettings.put(SETTING_SET_RESIZABLE, new BoolSetting(SETTING_SET_RESIZABLE, descResize, false));
		
		return defaultSettings;
	}

	@Override
	protected String getPluginInfo() {
		return "htSMLM was developped by Joran Deschamps, EMBL (2016-2019). It is intended as an intuitive Micro-Manager "
				+ "interface for a localization microscope, capable of long-term unsupervising imaging and automated localization microscopy."
				+ " For more details, visit the github repository: https://github.com/jdeschamps/htSMLM. \n";
	}
}
