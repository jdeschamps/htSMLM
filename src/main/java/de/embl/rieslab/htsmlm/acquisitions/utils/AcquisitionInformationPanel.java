package de.embl.rieslab.htsmlm.acquisitions.utils;

import java.util.ArrayList;

import javax.swing.JTextPane;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;

/**
 * A class updating a text panel with information concerning the acquisitions.
 * 
 * @author Joran Deschamps
 *
 */
public class AcquisitionInformationPanel {

    private final static String TEXT_INIT = "No configured acquisition list.\n";
    private final static String TEXT_START = "Starting acquisition.\n";
    private final static String TEXT_NEWPOS = "Stage position done: ";
    private final static String TEXT_FINISHED = "Acquisition finished.";
    private final static String TEXT_STOP = "Stopping acquisition. \n";
    private final static String TEXT_SUMMARY = "Acquisition summary: \n";
    private final static String TEXT_LOADED = "Acquisition list loaded.\n";
	
	private JTextPane panel_;
	
	public AcquisitionInformationPanel(JTextPane panel){
		panel_ = panel;
	}

	public void setInitialText(){
		addText(TEXT_INIT);
	}
	
	public void setStopText(){
		addText(TEXT_STOP);
	}

	public void setStartText(){
		addText(TEXT_START);
	}

	public void setAcquisitionLoaded(){
		addText(TEXT_LOADED);
	}

	public void setPositionDoneText(int i){
		addText(TEXT_NEWPOS+i+".\n");
	}
	
	public void setSummaryText(Experiment exp){
		if(exp.getAcquisitionList().size() > 0){
			ArrayList<Acquisition> acqList = exp.getAcquisitionList();
			if(acqList.size()>0){
				String s = TEXT_SUMMARY;
				s += "Acquisitions: ";
				for(int i=0;i<acqList.size()-1;i++){
					s = s+acqList.get(i).getType()+", ";
				}
				s = s+acqList.get(acqList.size()-1).getType()+".\n";
				panel_.setText(s);
			}
		}
	}

	private void addText(String message){
		String s = panel_.getText();
		s = s + message;
		panel_.setText(s);
	}

}
