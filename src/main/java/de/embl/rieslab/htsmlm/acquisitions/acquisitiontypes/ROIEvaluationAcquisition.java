package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import javax.swing.JPanel;

import org.micromanager.Studio;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;

public class ROIEvaluationAcquisition implements Acquisition {

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return null;
	}

	@Override
	public boolean performAcquisition(Studio studio, String name, String path) {
		return true;
	}

	@Override
	public void stopAcquisition() {
		
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public boolean skipPosition() {
		return false;
	}

	@Override
	public JPanel getPanel() {
		return null;
	}

	@Override
	public String getPanelName() {
		return null;
	}

	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		return null;
	}

	@Override
	public String[] getSpecialSettings() {
		return null;
	}

	@Override
	public String[][] getAdditionalJSONParameters() {
		return null;
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public AcquisitionType getType() {
		return null;
	}

}
