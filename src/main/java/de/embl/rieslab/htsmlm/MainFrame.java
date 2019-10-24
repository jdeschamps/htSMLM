package de.embl.rieslab.htsmlm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.embl.rieslab.emu.configuration.settings.BoolSetting;
import de.embl.rieslab.emu.configuration.settings.Setting;
import de.embl.rieslab.emu.configuration.settings.StringSetting;
import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.ui.ConfigurableMainFrame;
import de.embl.rieslab.htsmlm.tasks.TaskHolder;

public class MainFrame extends ConfigurableMainFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String SETTING_USE_TRIGGER = "Trigger tab";
	private static final String SETTING_USE_BOOSTER = "Booster tab";
	private static final String SETTING_USE_ADDFW = "Additional FW tab";
	private static final String SETTING_USE_SINGLEFW = "Single FW panel";
	private static final String SETTING_USE_QPD = "QPD tab";
	private static final String SETTING_USE_FL = "Focus-lock tab";
	
	private AdditionalFiltersPanel addFiltersPanel;
	private FocusPanel focusPanel;
	private QPDPanel qpdPanel;
	private FocusLockPanel focuslockpanel;
	private AbstractFiltersPanel filterPanel;
	private LaserControlPanel[] controlPanels;
	private LaserPulsingPanel pulsePanel;
	private LaserTriggerPanel[] triggerPanels;
	private ActivationPanel activationPanel;
	private AdditionalControlsPanel addcontrolPanel;
	private AcquisitionPanel acqPanel;
	private JPanel lowerpanel;
	private JTabbedPane tab;
	@SuppressWarnings("rawtypes")
	private HashMap<String,TaskHolder> taskholders_;

	
	public MainFrame(String title, SystemController controller, TreeMap<String, String> pluginSettings) {
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        setupPanels();
 
        this.pack(); 
        this.setResizable(false);
 	    this.setVisible(true);        
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
		pulsePanel = new LaserPulsingPanel("Laser 0");
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
		
		/// Focus-lock panel
		if(((BoolSetting) settings.get(SETTING_USE_FL)).getValue()) {
			focuslockpanel = new FocusLockPanel("Focus-lock");
			tab.add("Focus-lock", focuslockpanel);
		}
		
		// Activation
		activationPanel = new ActivationPanel("Activation", getCore());
		tab.add("Activation", activationPanel);
		
		/// laser trigger tab
		if(((BoolSetting) settings.get(SETTING_USE_TRIGGER)).getValue()) {
			JPanel lasertrigg = new JPanel();
			lasertrigg.setLayout(new GridLayout(2,2));
			triggerPanels = new LaserTriggerPanel[4];
			for(int i=0;i<triggerPanels.length;i++){
				triggerPanels[i] = new LaserTriggerPanel("Laser "+i); // The "Laser #" is used in the AcquisitionPanel to discriminate lasers
				lasertrigg.add(triggerPanels[i]);
			}
			tab.add("Trigger", lasertrigg);
		}
		
		/// Additional filters tab
		if(((BoolSetting) settings.get(SETTING_USE_ADDFW)).getValue()) {
			addFiltersPanel = new AdditionalFiltersPanel("Additional filters");
			tab.add("Additional filters", addFiltersPanel);
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
		
        // tasks, ignore the acquisition task as it is not supposed to be called by another panel
        taskholders_ = new HashMap<String,TaskHolder>();
        taskholders_.put(activationPanel.getTaskName(), activationPanel);

    }
    
    public Point getAcquisitionPanelLocation(){
    	Point loc = this.getLocation();
    	loc.x += tab.getLocation().x+lowerpanel.getLocation().x+acqPanel.getLocation().x;
    	loc.y += tab.getLocation().y+lowerpanel.getLocation().y+acqPanel.getLocation().y;
    	
    	return loc;
    }
    
    @SuppressWarnings("rawtypes")
	public HashMap<String,TaskHolder> getTaskHolders(){
    	return taskholders_;
    }

	@SuppressWarnings("rawtypes")
	@Override
	public HashMap<String, Setting> getDefaultPluginSettings() {
		HashMap<String, Setting> defaultSettings = new HashMap<String, Setting>();
		defaultSettings.put(SETTING_USE_TRIGGER, new BoolSetting(SETTING_USE_TRIGGER, "Check to use the trigger tab in the plugin.", true));
		defaultSettings.put(SETTING_USE_BOOSTER, new BoolSetting(SETTING_USE_BOOSTER, "Check to use the booster tab in the plugin.", true));
		defaultSettings.put(SETTING_USE_ADDFW, new BoolSetting(SETTING_USE_ADDFW, "Check to use the additional filters tab in the plugin.", true));
		defaultSettings.put(SETTING_USE_SINGLEFW, new BoolSetting(SETTING_USE_SINGLEFW, "Check to use a single FW panel, uncheck for a double FW panel.", true));
		defaultSettings.put(SETTING_USE_QPD, new BoolSetting(SETTING_USE_QPD, "Check to use the QPD tab in the plugin.", true));
		defaultSettings.put(SETTING_USE_FL, new BoolSetting(SETTING_USE_FL, "Check to use the Focus-lock tab in the plugin.", true));
		
		return defaultSettings;
	}
}
