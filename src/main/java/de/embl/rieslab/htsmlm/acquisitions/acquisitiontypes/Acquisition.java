package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import javax.swing.JPanel;

import org.micromanager.Studio;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.filters.PropertyFilter;

public interface Acquisition {
	
	public abstract GenericAcquisitionParameters getAcquisitionParameters();
	
	public abstract boolean performAcquisition(Studio studio, String name, String path);
	
	public abstract void stopAcquisition(); 
	
	public abstract boolean isRunning();
	
	public abstract boolean skipPosition();
	
	public abstract JPanel getPanel();
	
	public abstract String getPanelName();
		
	public abstract void readOutAcquisitionParameters(JPanel pane);
	
	public abstract PropertyFilter getPropertyFilter();
	
	public abstract String[] getSpecialSettings();
	
	public abstract String[][] getAdditionalJSONParameters();
	
	public abstract String getShortName();

	public abstract AcquisitionType getType();

}
