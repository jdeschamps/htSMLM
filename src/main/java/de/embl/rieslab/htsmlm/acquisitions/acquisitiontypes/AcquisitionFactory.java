package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.ui.uiproperties.TwoStateUIProperty;
import de.embl.rieslab.emu.utils.EmuUtils;
import de.embl.rieslab.htsmlm.AcquisitionPanel;
import de.embl.rieslab.htsmlm.acquisitions.AcquisitionController;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.AcquisitionWrapper;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.ExperimentWrapper;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import mmcorej.DeviceType;
import mmcorej.StrVector;

public class AcquisitionFactory {
	
	private AcquisitionController acqController_;
	private SystemController systemController_;
	private String[] acqTypeList_;
	private String[] zDevices_;
	
	public AcquisitionFactory(AcquisitionController acqController, SystemController systemController){
		acqController_ = acqController;
		systemController_ = systemController;
		zDevices_ = getZDevices();
		acqTypeList_ = getEnabledAcquisitionList();
	}
	
	private String[] getZDevices(){
		StrVector devices = systemController_.getCore().getLoadedDevicesOfType(DeviceType.StageDevice);
		return devices.toArray();
	}

	private String[] getEnabledAcquisitionList(){
		ArrayList<String> list = new ArrayList<>(Arrays.asList(AcquisitionType.getList()));
				
		if(!acqController_.isAcquistionPropertyEnabled(AcquisitionType.BF)){
			list.remove(AcquisitionType.BF.getTypeValue());
		}
		if(!acqController_.isAcquistionPropertyEnabled(AcquisitionType.BFP)){
			list.remove(AcquisitionType.BFP.getTypeValue());
		}
		
		if(zDevices_ == null || zDevices_.length == 0) {
			list.remove(AcquisitionType.ZSTACK.getTypeValue());
			list.remove(AcquisitionType.MULTISLICELOC.getTypeValue());
		}
		
		return list.toArray(new String[0]);
	}
	
	public String[] getAcquisitionTypeList(){
		return acqTypeList_;
	}
	 
	public Acquisition getAcquisition(String type){
		if (type.equals(AcquisitionType.LOCALIZATION.getTypeValue())) {
			return new LocalizationAcquisition(acqController_.getActivationController(), getExposure(),
					systemController_.getStudio());
		} else if (type.equals(AcquisitionType.MULTISLICELOC.getTypeValue())) {
			
			return new MultiSliceAcquisition(acqController_, getExposure(),
					zDevices_, systemController_.getCore().getFocusDevice(), (TwoStateUIProperty) systemController_
							.getProperty(acqController_.getAcquisitionParameterValue(AcquisitionPanel.PARAM_LOCKING)));
			
		} else if (type.equals(AcquisitionType.TIME.getTypeValue())) {
			
			return new TimeAcquisition(getExposure());
			
		} else if (type.equals(AcquisitionType.SNAP.getTypeValue())) {
			
			return new SnapAcquisition(getExposure());
			
		} else if (type.equals(AcquisitionType.ZSTACK.getTypeValue())) {
			
			return new ZStackAcquisition(getExposure(), zDevices_, systemController_.getCore().getFocusDevice(),
					(TwoStateUIProperty) systemController_
							.getProperty(acqController_.getAcquisitionParameterValue(AcquisitionPanel.PARAM_LOCKING)));
			
		} else if (type.equals(AcquisitionType.BFP.getTypeValue())) {
			
			return new BFPAcquisition(getExposure(), (TwoStateUIProperty) systemController_
					.getProperty(acqController_.getAcquisitionParameterValue(AcquisitionPanel.PARAM_BFP)));
			
		} else if (type.equals(AcquisitionType.BF.getTypeValue())) {
			
			return new BrightFieldAcquisition(getExposure(), (TwoStateUIProperty) systemController_
					.getProperty(acqController_.getAcquisitionParameterValue(AcquisitionPanel.PARAM_BRIGHTFIELD)));
			
		} else if (type.equals(AcquisitionType.MANUALINTER.getTypeValue())) {
			
			return new ManualInterventionAcquisition(acqController_);
			
		}
			
		return getDefaultAcquisition();
	}
	

	private double getExposure(){
		double i = 0;
		try {
			i = systemController_.getCore().getExposure();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	
	private Acquisition getDefaultAcquisition() {
		return new LocalizationAcquisition(acqController_.getActivationController(), getExposure(), systemController_.getStudio());
	}

	/**
	 * Write the acquisition list to the disk.
	 * 
	 * @param experiment
	 * @param fileName
	 * @param parentFolder
	 * @return
	 */
	static public boolean writeAcquisitionList(Experiment experiment, String parentFolder, String fileName){
		
		String fullpath, shortname;
		if(fileName.endsWith("."+HTSMLMConstants.ACQ_EXT)){
			if(parentFolder.endsWith(File.separator)) {
				fullpath = parentFolder+fileName;	
				shortname = fileName.substring(fileName.length()-2-HTSMLMConstants.ACQ_EXT.length());
			} else {
				fullpath = parentFolder+File.separator+fileName;	
				shortname = fileName.substring(fileName.length()-2-HTSMLMConstants.ACQ_EXT.length());		
			}
		} else {
			if(parentFolder.endsWith(File.separator)) {
				fullpath = parentFolder+fileName+"."+HTSMLMConstants.ACQ_EXT;	
				shortname = fileName;
			} else {
				fullpath = parentFolder+File.separator+fileName+"."+HTSMLMConstants.ACQ_EXT;	
				shortname = fileName;		
			}
		}
		
		boolean fileExists = true;
		while(fileExists){
			File f = new File(fullpath);
			if(f.exists()) { 
			    fullpath = incrementAcquisitionFileName(fullpath);
			} else {
				fileExists = false;
			}
		}
		
		File f = new File(parentFolder);
		if(!f.exists()) {
			f.mkdirs();
		}
		
		// wrap experiment into object that can be exported to JSON
		ExperimentWrapper expw = new ExperimentWrapper(shortname, parentFolder, experiment);
		
		// gson object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();	
        
		// write experiment to json
		try {
			String json_str = gson.toJson(expw);
			
			FileWriter file = new FileWriter(fullpath);
			file.write(json_str);
			file.close();
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private static String incrementAcquisitionFileName(String name) {
		String newname = name.substring(0, name.length()-HTSMLMConstants.ACQ_EXT.length()-1);
		int ind = 0;
		for(int i=0;i<newname.length();i++){
			if(newname.charAt(i) == '_'){
				ind = i;
			}
		}
		
		if(ind == 0){
			newname = newname+"_1."+HTSMLMConstants.ACQ_EXT;
		} else {
			if(EmuUtils.isInteger(newname.substring(ind+1))){
				int num = Integer.valueOf(newname.substring(ind+1))+1;
				newname = newname.substring(0, ind+1)+String.valueOf(num)+"."+HTSMLMConstants.ACQ_EXT;
			} else {
				newname = newname+"_1."+HTSMLMConstants.ACQ_EXT;
			}
		}
		
		return newname;
	}

	static public ExperimentWrapper readExperiment(String path) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		// gson object
		Gson gson = new Gson();
				
		// read experiment
		return gson.fromJson(new FileReader(path), ExperimentWrapper.class);
	}
	
	
	public Experiment readAcquisitionList(String path){	
		ArrayList<Acquisition> acqlist = new ArrayList<Acquisition>();
		int waitingTime = 3;
		int numPos = 0;
		String saveMode = Experiment.MULTITIFFS;

		// read experiment
		try {
			ExperimentWrapper expw = readExperiment(path);
		
			// read acquisition list
			ArrayList<AcquisitionWrapper> acqwlist = expw.acquisitionList;	

			// for the moment ignore name and path Strings
			waitingTime = expw.pauseTime;
			numPos = expw.numberPositions;
			saveMode = expw.savemode;
			
			if(acqwlist != null && !acqwlist.isEmpty()){
				for(int i=0;i<acqwlist.size();i++){
					AcquisitionWrapper acqw = acqwlist.get(i);
					if(acqw.type.equals(AcquisitionType.BFP.getTypeValue())){
						BFPAcquisition acq = (BFPAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);
					
						acqlist.add(acq);
						
					} else if(acqw.type.equals(AcquisitionType.BF.getTypeValue())){
						BrightFieldAcquisition acq = (BrightFieldAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);
						
						acqlist.add(acq);
						
					} else if(acqw.type.equals(AcquisitionType.ZSTACK.getTypeValue())){
						ZStackAcquisition acq = (ZStackAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);
						acq.setAdditionalParameters(acqw.additionalParameters);
						
						acqlist.add(acq);

					} else if(acqw.type.equals(AcquisitionType.LOCALIZATION.getTypeValue())){
						LocalizationAcquisition acq = (LocalizationAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);
						acq.setAdditionalParameters(acqw.additionalParameters);
						
						acqlist.add(acq);

					} else if(acqw.type.equals(AcquisitionType.MULTISLICELOC.getTypeValue())){
						MultiSliceAcquisition acq = (MultiSliceAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);
						acq.setAdditionalParameters(acqw.additionalParameters);
										
						acqlist.add(acq);

					} else if(acqw.type.equals(AcquisitionType.TIME.getTypeValue())){
						TimeAcquisition acq = (TimeAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);
						
						acqlist.add(acq);
					} else if(acqw.type.equals(AcquisitionType.SNAP.getTypeValue())){
						SnapAcquisition acq = (SnapAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);		
						
						acqlist.add(acq);
					} else if(acqw.type.equals(AcquisitionType.MANUALINTER.getTypeValue())){
						ManualInterventionAcquisition acq = (ManualInterventionAcquisition) getAcquisition(acqw.type);
						configureGeneralAcquistion(acq, acqw);		
						
						acqlist.add(acq);
					}
				}
			}
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new Experiment(waitingTime, numPos, Experiment.getSaveModeFromString(saveMode), acqlist);
	}
	
	static private void configureGeneralAcquistion(Acquisition acq, AcquisitionWrapper acqw){
		acq.getAcquisitionParameters().setExposureTime(acqw.exposure);
		acq.getAcquisitionParameters().setWaitingTime(acqw.waitingTime);
		
		HashMap<String,String> confs = new HashMap<String,String>();
		if(acqw.configurations != null){
			for(int j=0;j<acqw.configurations.length;j++){
				confs.put(acqw.configurations[j][0], acqw.configurations[j][1]);
			}
		}
		acq.getAcquisitionParameters().setMMConfigurationGroupValues(confs);
		
		HashMap<String,String> props = new HashMap<String,String>();
		if(acqw.properties != null){
			for(int j=0;j<acqw.properties.length;j++){
				props.put(acqw.properties[j][0], acqw.properties[j][1]);
			}
		}
		acq.getAcquisitionParameters().setPropertyValues(props);
		
		acq.getAcquisitionParameters().setNumberFrames(acqw.numFrames);
		acq.getAcquisitionParameters().setIntervalMs(acqw.interval);		
	}
	
	public enum AcquisitionType { 
		TIME("Time"), 
		BFP("BFP"), 
		BF("Bright-field"), 
		SNAP("Snapshot"), 
		LOCALIZATION("Localization"), 
		ZSTACK("Z-stack"),
		MULTISLICELOC("Multislice localization"),
		MANUALINTER("Manual intervention");
		
		private String value; 
		
		private AcquisitionType(String value) { 
			this.value = value; 
		}

		public String getTypeValue() {
			return value;
		} 
		
		public static String[] getList() {
			String[] s = { AcquisitionType.LOCALIZATION.getTypeValue(), 
						   AcquisitionType.BFP.getTypeValue(),
						   AcquisitionType.BF.getTypeValue(), 
						   AcquisitionType.ZSTACK.getTypeValue(),
						   AcquisitionType.SNAP.getTypeValue(), 
						   AcquisitionType.TIME.getTypeValue(),
						   AcquisitionType.MULTISLICELOC.getTypeValue(),
						   AcquisitionType.MANUALINTER.getTypeValue() };
			return s;
		}
	}; 
}
