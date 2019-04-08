package main.java.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import javax.swing.JPanel;

import org.micromanager.Studio;
import org.micromanager.data.Datastore;

import main.java.embl.rieslab.htsmlm.acquisitions.AcquisitionFactory.AcquisitionType;
import main.java.embl.rieslab.htsmlm.filters.PropertyFilter;

public interface Acquisition {
	
	public abstract GenericAcquisitionParameters getAcquisitionParameters();
	
	public abstract void performAcquisition(Studio studio, Datastore store); // should make sure that the store is empty
	
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
