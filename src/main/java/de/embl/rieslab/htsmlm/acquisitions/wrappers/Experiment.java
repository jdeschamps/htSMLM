package de.embl.rieslab.htsmlm.acquisitions.wrappers;

import java.util.ArrayList;

import org.micromanager.data.Datastore;
import org.micromanager.data.Datastore.SaveMode;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;

public class Experiment {

	public static String SINGLEPLANE  = "separate images";
	public static String MULTITIFFS  = "image stack";
	
	private int pausetime;
	private int numberpositions;
	private Datastore.SaveMode savemode;
	private ArrayList<Acquisition> acqlist;
	
	public Experiment(int pausetime, int numberpositions, Datastore.SaveMode savemode, ArrayList<Acquisition> acqlist){
		
		if(acqlist == null){
			throw new NullPointerException();
		}
		this.savemode = savemode;
		this.pausetime = pausetime;
		this.numberpositions = numberpositions;
		this.acqlist = acqlist;
	}	
	
	public Experiment(int pausetime, int numberpositions, String savemode, ArrayList<Acquisition> acqlist){
		
		if(acqlist == null){
			throw new NullPointerException();
		}
		
		this.savemode = Experiment.getSaveModeFromString(savemode);

		this.pausetime = pausetime;
		this.numberpositions = numberpositions;
		this.acqlist = acqlist;
	}	
	
	public int getPauseTime(){
		return pausetime;
	}

	public String getSaveModeAsString(){
		if(Datastore.SaveMode.SINGLEPLANE_TIFF_SERIES.equals(savemode)) {
			return SINGLEPLANE;
		} else {
			return MULTITIFFS;
		}
	}

	public Datastore.SaveMode getSaveMode(){
		return savemode;
	}
	
	public int getNumberPositions(){
		return numberpositions;
	}
	
	public ArrayList<Acquisition> getAcquisitionList(){
		return acqlist;
	}

	public ArrayList<AcquisitionWrapper> getAcquisitionWrapperList() {
		ArrayList<AcquisitionWrapper> acqwlist = new ArrayList<AcquisitionWrapper>();
	
		for (int i=0;i<acqlist.size();i++){
			acqwlist.add(new AcquisitionWrapper(acqlist.get(i)));
		}
		
		return acqwlist;
	}
	
	public static Datastore.SaveMode getSaveModeFromString(String s){
		if(SINGLEPLANE.equals(s)) {
			return Datastore.SaveMode.SINGLEPLANE_TIFF_SERIES;
		} else {
			return Datastore.SaveMode.MULTIPAGE_TIFF;
		}
	}

	public static String saveModeToString(SaveMode saveMode) {
		if(Datastore.SaveMode.SINGLEPLANE_TIFF_SERIES.equals(saveMode)) {
			return SINGLEPLANE;
		} else {
			return MULTITIFFS;
		}
	}
}
