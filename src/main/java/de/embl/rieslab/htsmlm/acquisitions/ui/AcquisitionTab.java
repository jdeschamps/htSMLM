package de.embl.rieslab.htsmlm.acquisitions.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.micromanager.internal.utils.ReportingUtils;
import org.micromanager.internal.utils.SliderPanel;

import de.embl.rieslab.emu.micromanager.mmproperties.MMProperty;
import de.embl.rieslab.emu.micromanager.presetgroups.MMPresetGroup;
import de.embl.rieslab.emu.micromanager.presetgroups.MMPresetGroupRegistry;
import de.embl.rieslab.emu.ui.uiproperties.MultiStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.SingleStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIProperty;
import de.embl.rieslab.emu.ui.uiproperties.UIPropertyType;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.AllocatedPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.AntiFlagPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.FlagPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NonPresetGroupPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.ReadOnlyPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.TwoStatePropertyFilter;
import de.embl.rieslab.htsmlm.uipropertyflags.FilterWheelFlag;
import de.embl.rieslab.htsmlm.uipropertyflags.FocusLockFlag;
import de.embl.rieslab.htsmlm.uipropertyflags.FocusStabFlag;
import de.embl.rieslab.htsmlm.uipropertyflags.LaserFlag;

/**
 * This class is super messy and difficult to read... Need to go over it again.
 * 
 */
public class AcquisitionTab extends JPanel {

	private static final long serialVersionUID = 1L;

	public final static String KEY_IGNORED = "Ignored";
	private final static String KEY_MMCONF = "Preset groups";

	private AcquisitionWizard wizard_;
	private AcquisitionFactory factory_;
	private JPanel acqcard_; // card layout containing all acqTab
	private JPanel[] acqTab_; // tab for each acquisition
	private JPanel[] acqSettingsPanels_; // specific acquisition settings in the acq tab
	private JComboBox<String> acqTypeComboBox_;
	private String[] acqTypesArray_;
	private int currind;
	private HashMap<String, UIProperty> props_;
	private HashMap<String, String> cachedPropertyValues_;
	private HashMap<String, String> propsfriendlyname_;

	/**
	 * Creates a default acquisition tab panel.
	 *  
	 * @param wizard Current acquisition wizard.
	 * @param factory Acquisition factory.
	 */
	public AcquisitionTab(AcquisitionWizard wizard, AcquisitionFactory factory, HashMap<String, String> propertyValues) {
		factory_ = factory;
		wizard_ = wizard;
		cachedPropertyValues_ = propertyValues;

		// Get the array of acquisition types and create a JComboBox
		acqTypesArray_ = factory_.getAcquisitionTypeList();
		acqTypeComboBox_ = new JComboBox<String>(acqTypesArray_);
		acqTypeComboBox_.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		acqTypeComboBox_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeAcquisition((String) acqTypeComboBox_.getSelectedItem());
			}
		});

		// Set the current tab acquisition
		currind = 0;
		this.setName(acqTypesArray_[currind]);

		// Filter out read-only properties from the system properties
		props_ = (new NonPresetGroupPropertyFilter(new AllocatedPropertyFilter(new ReadOnlyPropertyFilter())))
				.filterProperties(wizard_.getPropertiesMap());
		propsfriendlyname_ = new HashMap<String, String>();
		Iterator<String> it = props_.keySet().iterator();
		String s;
		while (it.hasNext()) {
			s = it.next();
			propsfriendlyname_.put(props_.get(s).getFriendlyName(), s); // get each property friendly name
		}

		// Create acquisition panels
		acqcard_ = new JPanel(new CardLayout());
		acqTab_ = new JPanel[acqTypesArray_.length];
		acqSettingsPanels_ = new JPanel[acqTypesArray_.length];
		for (int i = 0; i < acqTypesArray_.length; i++) {

			Acquisition acq = factory_.getAcquisition(acqTypesArray_[i]);
			JPanel pane = acq.getPanel();

			pane.setBorder(BorderFactory.createTitledBorder(null, "Acquisition Settings",
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
			((TitledBorder) pane.getBorder())
					.setTitleFont(((TitledBorder) pane.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
			
			acqSettingsPanels_[i] = pane;
			
			// creates the default panel
			acqTab_[i] = createPanel(acqSettingsPanels_[i], acq.getPropertyFilter(), new HashMap<String, String>(),
					new HashMap<String, String>());
			acqcard_.add(acqTab_[i], acqTypesArray_[i]);
		}

		setUpPanel();
	}

	/**
	 * Creates an acquisition tab panel from an acquisition.
	 * 
	 * @param wizard Current acquisition wizard.
	 * @param factory Acquisition factory.
	 * @param acquisition Current acquisition.
	 */
	public AcquisitionTab(AcquisitionWizard wizard, AcquisitionFactory factory, HashMap<String, String> propertyValues, Acquisition acquisition) {
		factory_ = factory;
		wizard_ = wizard;
		cachedPropertyValues_ = propertyValues;

		// Get the array of acquisition types and create a JComboBox
		acqTypesArray_ = factory_.getAcquisitionTypeList();
		acqTypeComboBox_ = new JComboBox<String>(acqTypesArray_);
		acqTypeComboBox_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeAcquisition((String) acqTypeComboBox_.getSelectedItem());
			}
		});

		// Set current acquisition to the acquisition passed as parameter
		currind = 0;
		for (int i = 0; i < acqTypesArray_.length; i++) {
			if (acqTypesArray_[i].equals(acquisition.getType().getTypeValue())) {
				currind = i;
				break;
			}
		}
		this.setName(acqTypesArray_[currind]);
		acqTypeComboBox_.setSelectedIndex(currind);

		// Filter out read-only properties from the system properties
		props_ = (new NonPresetGroupPropertyFilter(new AllocatedPropertyFilter(new ReadOnlyPropertyFilter())))
				.filterProperties(wizard_.getPropertiesMap());
		propsfriendlyname_ = new HashMap<String, String>();
		Iterator<String> it = props_.keySet().iterator();
		String s;
		while (it.hasNext()) {
			s = it.next();
			propsfriendlyname_.put(props_.get(s).getFriendlyName(), s);
		}

		// Create acquisition panels and set the property values for the current
		// acquisition tab
		acqcard_ = new JPanel(new CardLayout());
		acqTab_ = new JPanel[acqTypesArray_.length];
		acqSettingsPanels_ = new JPanel[acqTypesArray_.length];
		for (int i = 0; i < acqTypesArray_.length; i++) {
			if (i == currind) {
				JPanel pane = acquisition.getPanel();

				pane.setBorder(BorderFactory.createTitledBorder(null, "Acquisition Settings",
						TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
				((TitledBorder) pane.getBorder())
						.setTitleFont(((TitledBorder) pane.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
				acqSettingsPanels_[i] = pane;
				
				// creates a panel by passing the presets
				acqTab_[i] = createPanel(acqSettingsPanels_[i], acquisition.getPropertyFilter(),
						acquisition.getAcquisitionParameters().getMMConfigurationGroupValues(),
						acquisition.getAcquisitionParameters().getPropertyValues());
				acqcard_.add(acqTab_[i], acqTypesArray_[i]);
			} else {
				acqSettingsPanels_[i] = factory_.getAcquisition(acqTypesArray_[i]).getPanel();
				acqTab_[i] = createPanel(acqSettingsPanels_[i], factory_.getAcquisition(acqTypesArray_[i]).getPropertyFilter(),
						new HashMap<String, String>(), new HashMap<String, String>());
				acqcard_.add(acqTab_[i], acqTypesArray_[i]);
			}
		}

		setUpPanel();

		CardLayout cl = (CardLayout) (acqcard_.getLayout());
		cl.show(acqcard_, acqTypesArray_[currind]);

	}

	/**
	 * Set up sub-panels.
	 * 
	 * @param acqpane
	 * @param filter
	 * @param mmPresetGroupValues
	 * @param uipropertyValues
	 * @return
	 */
	private JPanel createPanel(JPanel acqpane, PropertyFilter filter, HashMap<String, String> mmPresetGroupValues,
			HashMap<String, String> uipropertyValues) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		pane.add(Box.createVerticalStrut(10));

		// acquisition panel
		pane.add(acqpane);

		pane.add(Box.createVerticalStrut(10));

		// MM preset groups
		JPanel mmconfig = createMMPresetGroupsTable(wizard_.getMMPresetRegistry(), mmPresetGroupValues);
		mmconfig.setBorder(BorderFactory.createTitledBorder(null, KEY_MMCONF, TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
		((TitledBorder) mmconfig.getBorder())
				.setTitleFont(((TitledBorder) mmconfig.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
		pane.add(mmconfig);

		pane.add(Box.createVerticalStrut(10));

		// get properties
		HashMap<String, UIProperty> props = filter.filterProperties(props_);
		String[] temp;
		PropertyFilter filt;

		// focus stabilization
		filt = new FlagPropertyFilter(new FocusStabFlag());
		temp = filt.filterStringProperties(props);
		props = filt.filteredProperties(props);

		if (temp.length > 0) {
			JPanel focstab = createPropertyTable(temp, true, uipropertyValues);
			focstab.setBorder(BorderFactory.createTitledBorder(null, "Focus stabilization",
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
			((TitledBorder) focstab.getBorder())
					.setTitleFont(((TitledBorder) focstab.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
			pane.add(focstab);

			pane.add(Box.createVerticalStrut(10));
		}

		// filterwheel
		filt = new FlagPropertyFilter(new FilterWheelFlag());
		temp = filt.filterStringProperties(props);
		props = filt.filteredProperties(props);

		if (temp.length > 0) {
			JPanel fw = createPropertyTable(temp, false, uipropertyValues);
			fw.setBorder(BorderFactory.createTitledBorder(null, "Filter wheel", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
			((TitledBorder) fw.getBorder())
					.setTitleFont(((TitledBorder) fw.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
			pane.add(fw);

			pane.add(Box.createVerticalStrut(10));
		}

		// lasers
		filt = new FlagPropertyFilter(new LaserFlag());
		temp = filt.filterStringProperties(props);
		props = filt.filteredProperties(props);

		/////////This works on the assumption that all lasers are called "Laser #" to mark a border between them
		// and I guess that it is ordered alphabetically
		if (temp.length > 0) {

			// find laser number of the first String
			int ind = 0;
			for (int i = 0; i < temp[0].length() - 1; i++) {
				if (Character.isDigit(temp[0].charAt(i)) && Character.isDigit(temp[0].charAt(i + 1))) {
					ind = Integer.valueOf(temp[0].substring(i, i + 1));
					break;
				} else if (Character.isDigit(temp[0].charAt(i))) {
					ind = Integer.valueOf(temp[0].substring(i, i + 1));
					break;
				}
			}
			
			JPanel lasertab = new JPanel();
			lasertab.setBorder(BorderFactory.createTitledBorder(null, "Lasers", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
			lasertab.setLayout(new BoxLayout(lasertab, BoxLayout.PAGE_AXIS));
			((TitledBorder) lasertab.getBorder())
					.setTitleFont(((TitledBorder) lasertab.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));

			ArrayList<String> templaser = new ArrayList<String>();
			templaser.add(temp[0]);

			if(temp.length  == 1) {
				JPanel subpanel = createPropertyTable(templaser.toArray(new String[0]), false, uipropertyValues);
				lasertab.add(subpanel);
			} else {			
				for (int j = 1; j < temp.length; j++) {
					int ind2 = 0;
	
					// extract the laser number of the jth entry in temp
					for (int i = 0; i < temp[j].length() - 1; i++) {
						if (Character.isDigit(temp[j].charAt(i)) && Character.isDigit(temp[j].charAt(i + 1))) {
							ind2 = Integer.valueOf(temp[j].substring(i, i + 1));
							break;
						} else if (Character.isDigit(temp[j].charAt(i))) {
							ind2 = Integer.valueOf(temp[j].substring(i, i + 1));
							break;
						}
					}
	
					if (ind2 == ind && j < temp.length - 1) { // if the jth entry relates to the same laser as ind
						templaser.add(temp[j]); // just add it to templaser
					} else if (ind2 == ind && j == temp.length - 1) { // if it relates to the indth laser but is the last entry, create a subpanel with a table
						// create a jpanel
						templaser.add(temp[j]);
						JPanel subpanel = createPropertyTable(templaser.toArray(new String[0]), false, uipropertyValues);
						lasertab.add(subpanel);
					} else if (ind2 != ind && j == temp.length - 1) { //it relates to another laser and it the last entry
						// create a jpanel with templaser
						JPanel subpanel = createPropertyTable(templaser.toArray(new String[0]), false, uipropertyValues);
						lasertab.add(subpanel);
	
						// empty templaser and add the new entry
						templaser.clear();
						templaser.add(temp[j]);
	
						// then create a subpanel for this new entry
						subpanel = createPropertyTable(templaser.toArray(new String[0]), false, uipropertyValues);
						lasertab.add(subpanel);
					} else { // it is from a different laser but is not the last entry
						// then set ind to this new laser
						ind = ind2;
	
						// create a jpanel for the previous laser and empty templaser
						JPanel subpanel = createPropertyTable(templaser.toArray(new String[0]), false, uipropertyValues);
						lasertab.add(subpanel);
	
						templaser.clear();
						templaser.add(temp[j]);
					}
				}
			}

			pane.add(lasertab);
			pane.add(Box.createVerticalStrut(10));
		}

		// Two-state
		filt = new TwoStatePropertyFilter(new AntiFlagPropertyFilter(new FocusLockFlag()));
		temp = filt.filterStringProperties(props);
		props = filt.filteredProperties(props);

		if (temp.length > 0) {
			JPanel twostate = createPropertyTable(temp, false, uipropertyValues);
			twostate.setBorder(BorderFactory.createTitledBorder(null, "Two-state", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, null, new Color(0, 0, 0)));
			((TitledBorder) twostate.getBorder())
					.setTitleFont(((TitledBorder) twostate.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
			pane.add(twostate);

			pane.add(Box.createVerticalStrut(10));
		}

		// others
		/*
		 * filt = new NoPropertyFilter(); temp = filt.filterStringProperties(props);
		 * 
		 * if(temp.length>0){ JPanel others = createTable(temp,false);
		 * others.setBorder(BorderFactory.createTitledBorder(null, "Other",
		 * TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new
		 * Color(0,0,0))); ((TitledBorder)
		 * others.getBorder()).setTitleFont(((TitledBorder)
		 * others.getBorder()).getTitleFont().deriveFont(Font.BOLD, 12));
		 * pane.add(others); }
		 */

		pane.add(new JPanel());

		return pane;
	}

	private void setUpPanel() {
		JPanel contentpane = new JPanel();

		contentpane.setLayout(new BoxLayout(contentpane, BoxLayout.PAGE_AXIS));

		contentpane.add(Box.createVerticalStrut(5));

		JPanel combopanel = new JPanel(new GridLayout(0, 4));
		combopanel.add(acqTypeComboBox_);
		contentpane.add(combopanel);

		contentpane.add(acqcard_);

		this.add(new JScrollPane(contentpane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

	}

	/**
	 * Creates a JTable holding the MMPresetGroup values.
	 * 
	 * @param mmPresetRegistry Object holding the reference to all configuration groups
	 * @param mmPresetGroupValues Map of the values to set in the table, if there are any
	 * @return
	 */
	private JPanel createMMPresetGroupsTable(MMPresetGroupRegistry mmPresetRegistry,
			HashMap<String, String> mmPresetGroupValues) {
		
		JPanel pane = new JPanel();

		// Defines table model
		DefaultTableModel model = new DefaultTableModel(new Object[] { "Property", "Value" }, 0);

		final HashMap<String, String[]> channels = mmPresetRegistry.getMMPresetGroupChannels();// keys = preset group's name, object = list of presets
		final HashMap<String, MMPresetGroup> groups = mmPresetRegistry.getMMPresetGroups(); // hashmap of the preset group objects

		String[] keys = channels.keySet().toArray(new String[0]);
		Arrays.sort(keys);

		/*
		 * Preset groups:
		 * If one state only and one affected property, then currentvalue is drawn from the property directly
		 * If more than one state, then currentvalue is drawn from mmPresetRegistry.getCurrentMMPresetGroupChannel()
		 * If currentvalue is not empty, then if there is a value set in the acquisition settings, use the latter
		 * If not value is set in the acquisition settings, then use currentvalue
		 * If currentvalue is an empty String but an acquisition setting exist, then use the latter
		 * Should currentvalue be an empty String and no acquisition setting set, then value is "Ignored"
		 */
		
		// For each preset group
		for (int i = 0; i < keys.length; i++) {
	
			if (channels.get(keys[i]) != null && channels.get(keys[i]).length > 0 && !keys[i].equals("System")) { // if the String[] is not null and not empty, ignore the special System preset
				
				String currentValue = "";
				if(channels.get(keys[i]).length == 1 && groups.get(keys[i]).getAffectedProperties().size() == 1){
					currentValue = groups.get(keys[i]).getAffectedProperties().get(0).getStringValue();
				} else { 
					currentValue = mmPresetRegistry.getCurrentMMPresetGroupChannel(keys[i]);
				}
				
				if(mmPresetGroupValues.containsKey(keys[i])) { // if sets in acq
					if(groups.get(keys[i]).getAffectedProperties().get(0).isStringAllowed(mmPresetGroupValues.get(keys[i]))){ // acq value allowed ad single prop + single channel
						model.addRow(new Object[] { keys[i], mmPresetGroupValues.get( keys[i]) }); // sets the acq value
					} else if(groups.get(keys[i]).hasPreset(mmPresetGroupValues.get(keys[i]))){
						model.addRow(new Object[] { keys[i], mmPresetGroupValues.get(keys[i]) }); // sets the acq value
					} else if(mmPresetGroupValues.get(keys[i]).equals(KEY_IGNORED)) {
						model.addRow(new Object[] { keys[i], KEY_IGNORED }); // use ignored
					} else {
						model.addRow(new Object[] { keys[i], currentValue }); // sets the current value
					}
				} else { // not set in acq
					if(!currentValue.equals("")) {
						model.addRow(new Object[] { keys[i], currentValue }); // sets the current value
					} else {
						model.addRow(new Object[] { keys[i], KEY_IGNORED }); // use ignored
					}
				}
				
			}
		}

		JTable table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				switch (column) {
				case 0:
					return new BoldTableCellRenderer(); // first column is written in bold font
				case 1:
					if (column == 1 && groups.get((String) this.getValueAt(row, 0)).getNumberOfMMProperties() == 1 && groups.get((String) this.getValueAt(row, 0)).getGroupSize() == 1) {
						if (groups.get((String) this.getValueAt(row, 0)).getAffectedProperties().get(0)
								.hasLimits()) {
							return new LimitedPropertyTableCellRenderer(groups.get((String) this.getValueAt(row, 0)).getAffectedProperties().get(0));
						}
					}
					return new DefaultTableCellRenderer(); // column 1 takes a default renderer
				default:
					return super.getCellRenderer(row, column);
				}
			}

			@Override
			public TableCellEditor getCellEditor(int row, int column) {
				String s = (String) this.getValueAt(row, 0);

				if (column == 1 && groups.get((String) this.getValueAt(row, 0)).getNumberOfMMProperties() == 1
						 && groups.get((String) this.getValueAt(row, 0)).getGroupSize() == 1) {
					if (groups.get((String) this.getValueAt(row, 0)).getAffectedProperties().get(0)
							.hasAllowedValues()) {
						String[] states = groups.get((String) this.getValueAt(row, 0)).getAffectedProperties().get(0)
								.getStringAllowedValues();
						return new DefaultCellEditor(new JComboBox<String>(states));
					} else if (groups.get((String) this.getValueAt(row, 0)).getAffectedProperties().get(0)
							.hasLimits()) {
						return new PropertyValueCellEditor(groups.get((String) this.getValueAt(row, 0)).getAffectedProperties().get(0));
					} else {
						return new DefaultCellEditor(new JTextField());
					}
				} else {
					String[] states = channels.get(s);
					String[] states_ig = new String[states.length + 1];
					states_ig[0] = KEY_IGNORED;
					for (int i = 0; i < states.length; i++) {
						states_ig[i + 1] = states[i];
					}

					return new DefaultCellEditor(new JComboBox<String>(states_ig));
				}
			}

			@Override
			public boolean isCellEditable(int row, int col) { // first column is non-editable and second as well if it
																// is a field value row
				if (col == 0) {
					return false;
				}
				return true;
			}

		};
		table.setName(KEY_MMCONF);
		table.setAutoCreateRowSorter(false);
		table.setRowHeight(23);
		table.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		table.setRowSelectionAllowed(false);
		pane.setLayout(new GridLayout());
		pane.add(table);

		return pane;
	}

	/**
	 * Creates a JTable of the filtered properties with the current values set for the acquisition.
	 * 
	 * @param filteredProperties Properties to add to the table
	 * @param twostatedefault_   Default value for TwoStateProperties
	 * @param acqPropertyValues     Property values of the current acquisition
	 * @return JPanel with the filtered UIProperties JTable.
	 */
	private JPanel createPropertyTable(String[] filteredProperties, boolean twostatedefault_,
			HashMap<String, String> acqPropertyValues) {
		JPanel pane = new JPanel();

		// Defines table model
		DefaultTableModel model = new DefaultTableModel(new Object[] { "Property", "Value" }, 0);

		// For each property of the UI
		for (int i = 0; i < filteredProperties.length; i++) {
			UIProperty prop = props_.get(filteredProperties[i]);
			if (acqPropertyValues.containsKey(filteredProperties[i])) { // if the property is found in the acquisition
																		// properties
				if (prop instanceof TwoStateUIProperty) {
					if (acqPropertyValues.get(filteredProperties[i]).equals(TwoStateUIProperty.getOnStateLabel())) {
						model.addRow(new Object[] { prop.getFriendlyName(), true });
					} else {
						model.addRow(new Object[] { prop.getFriendlyName(), false });
					}
				} else {
					model.addRow(new Object[] { prop.getFriendlyName(), acqPropertyValues.get(filteredProperties[i]) });
				}
			} else { // if not, set by cached value
				if (prop.getType().equals(UIPropertyType.TWOSTATE)) {
					if(cachedPropertyValues_.containsKey(filteredProperties[i])) { // take it from the cached value
						model.addRow(new Object[] { prop.getFriendlyName(), ((TwoStateUIProperty) prop).isOnState(cachedPropertyValues_.get(filteredProperties[i])) });
					} else {
						model.addRow(new Object[] { prop.getFriendlyName(), twostatedefault_ });
					}
				} else if (prop.getType().equals(UIPropertyType.SINGLESTATE)) {
					model.addRow(new Object[] { prop.getFriendlyName(), ((SingleStateUIProperty) prop).getStateValue() });
				} else if (prop.getType().equals(UIPropertyType.MULTISTATE)) {
					if(cachedPropertyValues_.containsKey(filteredProperties[i])) { // take it from the cached value
						model.addRow(new Object[] { prop.getFriendlyName(),
							((MultiStateUIProperty) prop).getStateNameFromValue(cachedPropertyValues_.get(filteredProperties[i])) });
					} else {
						model.addRow(new Object[] { prop.getFriendlyName(),
								((MultiStateUIProperty) prop).getStateNameFromValue(prop.getPropertyValue()) });
					}
				} else {
					if(cachedPropertyValues_.containsKey(filteredProperties[i])) { // take it from the cached value
						model.addRow(new Object[] { prop.getFriendlyName(), cachedPropertyValues_.get(filteredProperties[i]) });
					} else {
						model.addRow(new Object[] { prop.getFriendlyName(), prop.getPropertyValue() });

					}
				}
			}
		}

		JTable table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				switch (column) {
				case 0:
					return new BoldTableCellRenderer(); // first column is written in bold font
				case 1:
					if (getValueAt(row, column) instanceof Boolean) {
						return super.getDefaultRenderer(Boolean.class);
					}
					return new DefaultTableCellRenderer(); // column 1 takes a default renderer
				default:
					return super.getCellRenderer(row, column);
				}
			}

			@Override
			public TableCellEditor getCellEditor(int row, int column) {
				String s = (String) this.getValueAt(row, 0);

				if (column == 1) {
					if (getValueAt(row, column) instanceof Boolean) {
						return super.getDefaultEditor(Boolean.class);
					} else if (props_.get(propsfriendlyname_.get(s)) instanceof MultiStateUIProperty) {
						return new DefaultCellEditor(new JComboBox<String>(
								((MultiStateUIProperty) props_.get(propsfriendlyname_.get(s))).getStatesName()));
					} else if (props_.get(propsfriendlyname_.get(s)).hasMMPropertyAllowedValues()) {
						return new DefaultCellEditor(
								new JComboBox<String>(props_.get(propsfriendlyname_.get(s)).getAllowedValues()));
					} else {
						super.getCellEditor(row, column);
					}
				}

				return super.getCellEditor(row, column);
			}

			@Override
			public boolean isCellEditable(int row, int col) { // first column is non-editable and second as well if it
																// is a field value row
				if (col == 0) {
					return false;
				}
				return true;
			}

		};

		table.setAutoCreateRowSorter(false);
		table.setRowHeight(23);
		table.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		table.setRowSelectionAllowed(false);
		pane.setLayout(new GridLayout());
		pane.add(table);

		return pane;
	}

	/**
	 * Extract the properties.
	 * 
	 * @param c
	 * @param properties
	 * @return
	 */
	private HashMap<String, String> extractPropertyValues(Component c, HashMap<String, String> properties) {
		if (c instanceof JTable && (c.getName() == null || !c.getName().equals(KEY_MMCONF))) {
			if (((JTable) c).isEditing()) {
				((JTable) c).getCellEditor().stopCellEditing();
			}
			TableModel model = ((JTable) c).getModel();
			int nrow = model.getRowCount();
			for (int k = 0; k < nrow; k++) { // loop through the rows
				String s = (String) model.getValueAt(k, 0);
				if (!(model.getValueAt(k, 1) instanceof Boolean)) { // if second row is not a boolean property
					properties.put(propsfriendlyname_.get(s), (String) model.getValueAt(k, 1));
				} else {
					if ((Boolean) model.getValueAt(k, 1)) {
						properties.put(propsfriendlyname_.get(s), TwoStateUIProperty.getOnStateLabel());
					} else {
						properties.put(propsfriendlyname_.get(s), TwoStateUIProperty.getOffStateLabel());
					}
				}
			}
		} else if (c instanceof JPanel) {
			Component[] subcomps = ((JPanel) c).getComponents();
			for (int l = 0; l < subcomps.length; l++) {
				extractPropertyValues(subcomps[l], properties);
			}
		}
		return properties;
	}

	/**
	 * Extract the MMConfGroups values.
	 * 
	 * @param c
	 * @param confgroups
	 * @return
	 */
	private HashMap<String, String> extractMMConfigurationGroupValues(Component c, HashMap<String, String> confgroups) {
		if (c instanceof JTable && c.getName() != null && c.getName().equals(KEY_MMCONF)) {
			if (((JTable) c).isEditing()) {
				((JTable) c).getCellEditor().stopCellEditing();
			}
			TableModel model = ((JTable) c).getModel();
			int nrow = model.getRowCount();
			for (int k = 0; k < nrow; k++) { // loop through the rows
				String group = (String) model.getValueAt(k, 0);
				String val = String.valueOf(model.getValueAt(k, 1));

				confgroups.put(group, val);
			}
		} else if (c instanceof JPanel) {
			Component[] subcomps = ((JPanel) c).getComponents();
			for (int l = 0; l < subcomps.length; l++) {
				extractMMConfigurationGroupValues(subcomps[l], confgroups);
			}
		}
		return confgroups;
	}

	public String getTypeName() {
		return acqTypesArray_[currind];
	}

	/**
	 * Change to the card panel corresponding to the newly selected Acquisition.
	 * 
	 * @param type
	 */
	private void changeAcquisition(String type) {
		int temp = currind;
		for (int i = 0; i < acqTypesArray_.length; i++) {
			if (acqTypesArray_[i].equals(type)) {
				currind = i;
				break;
			}
		}

		if (temp == currind) {
			return;
		}

		this.setName(type);
		wizard_.changeName(this);

		CardLayout cl = (CardLayout) (acqcard_.getLayout());
		cl.show(acqcard_, type);
	}

	/**
	 * Retrieves the selected Acquisition from the AcquisitionTab.
	 * 
	 * @return
	 */
	public Acquisition getAcquisition() {
		// get acquisition from factory with the right type
		Acquisition acq = factory_.getAcquisition(acqTypesArray_[currind]);

		// set mm configuration groups
		acq.getAcquisitionParameters()
				.setMMConfigurationGroupValues(extractMMConfigurationGroupValues(acqTab_[currind], new HashMap<String, String>()));

		// set properties value in the acquisition
		acq.getAcquisitionParameters()
				.setPropertyValues(extractPropertyValues(acqTab_[currind], new HashMap<String, String>()));

		// read out the JPanel related to the acquisition
		acq.readOutAcquisitionParameters(acqSettingsPanels_[currind]);

		return acq;
	}

	/**
	 * Renders cell text with a bold font. Adapted from:
	 * https://stackoverflow.com/questions/22325138/cellrenderer-making-text-bold
	 */
	public final class BoldTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component compo = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (column == 0) {
				compo.setFont(compo.getFont().deriveFont(Font.BOLD));
			} else {
				compo.setFont(compo.getFont().deriveFont(Font.PLAIN));
			}

			return compo;
		}
	}

	/**
	 * Adapted from org.micromanager.internal.utils.PropertyValueCellRenderer. 
	 */
	public final class LimitedPropertyTableCellRenderer implements TableCellRenderer {

		JLabel lab_ = new JLabel();
		@SuppressWarnings("rawtypes")
		MMProperty prop;

		public LimitedPropertyTableCellRenderer(@SuppressWarnings("rawtypes") MMProperty prop) {
			this.prop = prop;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			lab_.setOpaque(true);
			lab_.setHorizontalAlignment(JLabel.LEFT);

			Component comp;

			SliderPanel slider = new SliderPanel();
			
			if (prop.getType() == MMProperty.MMPropertyType.INTEGER) {
				slider.setLimits((int) prop.getMin(), (int) prop.getMax());
			} else if (prop.getType() == MMProperty.MMPropertyType.FLOAT) {
				slider.setLimits((float) prop.getMin(), (float) prop.getMax());
			} else {
				slider.setLimits(Double.parseDouble((String) prop.getMin()), Double.parseDouble((String) prop.getMin()));
			}
			
			try {
				slider.setText(String.valueOf(value));
			} catch (ParseException ex) {
				ReportingUtils.logError(ex);
			}
			slider.setToolTipText(String.valueOf(value));
			comp = slider;

			comp.setEnabled(true);

			return comp;
		}

	}

	/**
	 * Adapted from org.micromanager.internal.utils.PropertyValueCellEditor. 
	 */
	public final class PropertyValueCellEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long serialVersionUID = 1L;
		SliderPanel slider_ = new SliderPanel();
	    @SuppressWarnings("rawtypes")
		MMProperty prop;

	    public PropertyValueCellEditor(@SuppressWarnings("rawtypes") MMProperty prop) {
	        this(false);
	        this.prop = prop;
	    }
	    
	    public PropertyValueCellEditor(boolean disableExcluded) {
	        super();


	        slider_.addEditActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                fireEditingStopped();
	            }
	        });

	        slider_.addSliderMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseReleased(MouseEvent e) {
	                fireEditingStopped();
	            }
	        });
	    }

	    // This method is called when a cell value is edited by the user.
	    @Override
	    public Component getTableCellEditorComponent(JTable table, Object value,
	            boolean isSelected, int rowIndex, int colIndex) {

	    	if (prop.getType() == MMProperty.MMPropertyType.INTEGER) {
	    		slider_.setLimits((int) prop.getMin(), (int) prop.getMax());
			} else if (prop.getType() == MMProperty.MMPropertyType.FLOAT) {
				slider_.setLimits((float) prop.getMin(), (float) prop.getMax());
			} else {
				slider_.setLimits(Double.parseDouble((String) prop.getMin()), Double.parseDouble((String) prop.getMin()));
			}
	    	
			try {
				slider_.setText(String.valueOf(value));
			} catch (ParseException ex) {
				ReportingUtils.logError(ex);
			}
	    	
			return slider_;  
	    }

	    // This method is called when editing is completed.
	    // It must return the new value to be stored in the cell.
	    @Override
	    public Object getCellEditorValue() {
	    	return slider_.getText();
	    }
	}
}
