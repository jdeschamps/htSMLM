package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;


import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.micromanager.Studio;
import org.micromanager.data.Datastore;

import de.embl.rieslab.htsmlm.acquisitions.AcquisitionController;
import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.AcquisitionFactory.AcquisitionType;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.NoPropertyFilter;
import de.embl.rieslab.htsmlm.acquisitions.uipropertyfilters.PropertyFilter;

public class ManualInterventionAcquisition implements Acquisition{
	
	private GenericAcquisitionParameters params_;
	
	private final static String PANE_NAME = "Pause panel";
	private final AcquisitionController acqController_;
		
	public ManualInterventionAcquisition(AcquisitionController acqController) {
		acqController_ = acqController;
		params_ = new GenericAcquisitionParameters(AcquisitionType.MANUALINTER, 
				0, 0, 0, 0, new HashMap<String,String>(), new HashMap<String,String>());
	}

	@Override
	public JPanel getPanel() {
		JPanel pane = new JPanel();
		pane.setName(getPanelName());

		return pane;
	}

	@Override
	public void readOutAcquisitionParameters(JPanel pane) {
		// Do nothing
	}

	@Override
	public PropertyFilter getPropertyFilter() {
		return new NoPropertyFilter();
	}

	@Override
	public String[] getHumanReadableSettings() {
		String[] s = new String[0];
		return s;
	}

	@Override
	public String[][] getAdditionalParameters() {
		return new String[0][0];
	}

	@Override
	public void setAdditionalParameters(String[][] parameters) {
		// Do nothing
	}

	@Override
	public GenericAcquisitionParameters getAcquisitionParameters() {
		return params_;
	}
	
	@Override
	public void performAcquisition(Studio studio, String name, String path, Datastore.SaveMode savemode) throws InterruptedException, IOException {
		AtomicBoolean pressedOk = new AtomicBoolean(false);
		
		// show user a window
		SwingUtilities.invokeLater(new Runnable()
	    {
	      public void run()
	      {    	  
	    	  JOptionPane.showMessageDialog(acqController_.getAcquisitionPanel(), "Click \"OK\" when ready.", "Manual intervention", JOptionPane.QUESTION_MESSAGE);
	    	  pressedOk.set(true);
	      }
	    });


		while(!pressedOk.get()) {				
			// sleep
			Thread.sleep(200);
		}
		
	}

	@Override
	public void stopAcquisition() {
		// Do nothing, the JOptionPane is blocking the EDT
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
	public String getPanelName() {
		return PANE_NAME;
	}
	
	@Override
	public AcquisitionType getType() {
		return AcquisitionType.MANUALINTER;
	}

	@Override
	public String getShortName() {
		return "Pause";
	}
}