package main.embl.rieslab.emu.uiexamples.htsmlm.acquisitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JPanel;

import main.embl.rieslab.emu.ui.uiproperties.filters.PropertyFilter;
import main.embl.rieslab.emu.utils.utils;

import org.micromanager.api.SequenceSettings;

public abstract class Acquisition {


	public final static String[] EMPTY = {"Empty"};

	private double exposure_, intervalMs_;
	private int waitingtime_, numFrames_;
	private ArrayList<Double> slices_;
	private AcquisitionType type_;
	private boolean useconfig_ = false;
	private String group_, configname_, expname_, path_; 
	private HashMap<String,String> propvalues_;
	private HashMap<String,String[]> configgroups_;
	
	public static final String ACQ_SETTINGS = "Acquisition settings";
	
	public Acquisition(AcquisitionType type, double exposure, HashMap<String,String[]> configgroups){
		type_ = type;
		
		expname_ = "";
		
		exposure_ = exposure;
		waitingtime_ = 3;
		intervalMs_ = 0;
		numFrames_ = 1;
		
		configgroups_ = configgroups;
		configname_ = EMPTY[0];
		group_ = EMPTY[0];
		
		path_ = "";
		expname_ = "";
	}


	public void setSlices(double zstart, double zend, double zstep){
		slices_ = new ArrayList<Double>();
		double z = utils.round(zstart-zstep,2);
		
		while (z<=zend){
			z += zstep;
			slices_.add(utils.round(z,2));
		}
	}
	
	public SequenceSettings getSettings(){
		SequenceSettings settings = new SequenceSettings();
		
		settings.save = true;
		settings.timeFirst = true;
		settings.usePositionList = false;
		
		if(slices_ != null){
			settings.slices = slices_;
		}

		settings.root = path_;
		settings.numFrames = numFrames_;
		settings.intervalMs = intervalMs_;
		
		return settings;
	}
	
	public String getType(){
		return type_.getTypeValue();
	}
	
	public void setPath(String path){
		path_ = path;
	}

	public void setProperties(HashMap<String,String> propvalues){
		propvalues_ = propvalues;
	}

	public void setNumberFrames(int numframes){
		numFrames_ = numframes;
	}

	public int getNumberFrames(){
		return numFrames_;
	}

	public void setIntervalMs(double interval){
		intervalMs_ = interval;
	}

	public double getIntervalMs(){
		return intervalMs_;
	}
	
	public void setExposureTime(double exp){
		exposure_ = exp;
	}	
	
	public double getExposure(){
		return exposure_;
	}
	
	public int getWaitingTime(){
		return waitingtime_;
	}
	
	public void setWaitingTime(int waiting){
		waitingtime_ = waiting;
	}
	
	public boolean useConfig(){
		return useconfig_;
	}
	
	public void setConfigurationGroup(String group, String configname){
		if(group != null && configgroups_.containsKey(group)){
			group_ = group;
			configname_  = configname;
			useconfig_ = true;
		}
	}
	
	public HashMap<String,String> getPropertyValues(){
		return propvalues_;
	}
	
	protected void setType(AcquisitionType type){
		type_ = type;
	}
	
	public String getPath() {
		return path_;
	}

	public String getExperimentName() {
		return expname_;
	}	
	
	public void setName(String name) {
		expname_ = name;
	}
	
	public String getConfigGroup(){
		if(group_ != null){
			return group_;
		}
		return EMPTY[0];
	}
	
	public String getConfigName(){
		return configname_;
	}
	
	public String[] getConfigGroupList(){
		int size = configgroups_.size();
		String[] s = new String[size+1];
		String[] temp = configgroups_.keySet().toArray(new String[0]);
		Arrays.sort(temp);
		
		s[0] = EMPTY[0];
		for(int i=0;i<size;i++){
			s[i+1] = temp[i];
		}
		return s;
	}
	
	public String[] getConfigGroupNames(String group){
		if(!configgroups_.containsKey(group)){
			return EMPTY;
		}
		
		String[] array = configgroups_.get(group).clone();
		Arrays.sort(array);
		
		return array;
	}
	
	public abstract void preAcquisition();
	public abstract void postAcquisition();
	public abstract boolean stopCriterionReached();
	public abstract JPanel getPanel();
	public abstract String getPanelName();
	public abstract void readOutParameters(JPanel pane);
	public abstract PropertyFilter getPropertyFilter();
	public abstract String[] getSpecialSettings();
	public abstract String[][] getAdditionalJSONParameters();
	

}