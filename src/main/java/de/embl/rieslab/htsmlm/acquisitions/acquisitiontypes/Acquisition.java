package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.io.IOException;

import javax.swing.JPanel;

import org.micromanager.Studio;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;

public interface Acquisition {
	
	/**
	 * Returns the general acquisition parameters used in the Micro-Manager MDA.
	 * 
	 * @return A GenericAcquisitionParameters object. 
	 */
	public abstract GenericAcquisitionParameters getAcquisitionParameters();
	
	/**
	 * Performs the acquisition.
	 * 
	 * @param studio Micro-Manager studio.
	 * @param name Name of the acquisition.
	 * @param path Path where to save the images.
	 * @return True if the acqusition could be started, false otherwise.
	 * @throws InterruptedException 
	 * @throws  
	 */
	public abstract void performAcquisition(Studio studio, String name, String path) throws InterruptedException, IOException;
	
	/**
	 * Requests stop acqusition.
	 */
	public abstract void stopAcquisition(); 
	
	/**
	 * Checks if the acquisition is running.
	 * @return True if it is, false otherwise.
	 */
	public abstract boolean isRunning();
	
	/**
	 * Checks if the position should be skipped.
	 * @return True if the position is to be skipped, false otherwise.
	 */
	public abstract boolean skipPosition();
	
	/**
	 * Returns the JPanel corresponding to the acquisition settings. used in the 
	 * acquisition configuration panel.
	 * @return Acquisition's panel.
	 */
	public abstract JPanel getPanel();
	
	/**
	 * Returns the name of the acquisition JPanel.
	 * 
	 * @return JPanel's name
	 */
	public abstract String getPanelName();
		
	/**
	 * Extracts the acquisition parameters from the JPanel (obtained by getPanel()).
	 * 
	 * @param pane Acquisition's JPanel.
	 */
	public abstract void readOutAcquisitionParameters(JPanel pane);
	
	/**
	 * Returns a PropertyFilter in order to filter out UIProperties already taken into
	 * account by the acquisition.
	 * 
	 * @return PropertyFilter
	 */
	public abstract PropertyFilter getPropertyFilter();
	
	/**
	 * Returns an array of relevant human readable acquisition settings in order to 
	 * display them in the acquisition tree. 
	 * @return Array of human readable settings. 
	 */
	public abstract String[] getHumanReadableSettings();

	/**
	 * Returns an array of keys and parameter values. The array is used to serialize the 
	 * acquisition. 
	 * 
	 * @return String array of depth 2 (Key, Value).
	 */
	public abstract String[][] getAdditionalParameters();
	
	/**
	 * Takes a String array as obtained from getAdditionalParameters() and extract the values.
	 * This is used during deserialization to set the parameters back.
	 * 
	 * @param parameters String array obtained from getAdditionalParameters().
	 */
	public abstract void setAdditionalParameters(String[][] parameters);
	
	/**
	 * Returns the acquisition short name. To be used in feedback to the user.
	 * @return Acquisition short name.
	 */
	public abstract String getShortName();

	/**
	 * Returns the acquisition type.
	 * 
	 * @return AcquisitionType.
	 */
	public abstract AcquisitionType getType();

}
