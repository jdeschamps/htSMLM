package main.java.de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import javax.swing.JPanel;

import org.micromanager.Studio;
import org.micromanager.data.Datastore;

import main.java.de.embl.rieslab.htsmlm.acquisitions.AcquisitionFactory.AcquisitionType;
import main.java.de.embl.rieslab.htsmlm.filters.PropertyFilter;

public class ROIEvaluationAcquisition implements Acquisition {

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performAcquisition(Studio studio, Datastore store) {
		// TODO Auto-generated method stub
	}

	@Override
	public void stopAcquisition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean skipPosition() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPanelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSpecialSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[][] getAdditionalJSONParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcquisitionType getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
