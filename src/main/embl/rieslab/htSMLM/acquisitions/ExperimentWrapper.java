package main.embl.rieslab.htSMLM.acquisitions;

import java.util.ArrayList;

public class ExperimentWrapper {

	public int pausetime;
	public int numberpositions;
	public ArrayList<AcquisitionWrapper> acqwlist;
	
	public ExperimentWrapper(){
		
	}
	
	public ExperimentWrapper(int pausetime, int numberpositions, ArrayList<AcquisitionWrapper> acqwlist){
		this.pausetime = pausetime;
		this.numberpositions = numberpositions;
		this.acqwlist = acqwlist;
	}
	
	
}
