package de.embl.rieslab.htsmlm.acquisitions.wrappers;

import java.util.Arrays;
import java.util.HashMap;

import de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes.Acquisition;

public class AcquisitionWrapper {

	public String type;
	public double exposure, interval;
	public int numFrames, waitingTime;
	public String[][] configurations;
	public String[][] properties;
	public String[][] additionalParameters;
	
	public AcquisitionWrapper(){
		// necessary for JSON deserialization
	}
	
	public AcquisitionWrapper(Acquisition acq){
        type = acq.getAcquisitionParameters().getAcquisitionType().getTypeValue();
        exposure=acq.getAcquisitionParameters().getExposureTime();
        interval= acq.getAcquisitionParameters().getIntervalMs();
        numFrames=acq.getAcquisitionParameters().getNumberFrames();
        waitingTime= acq.getAcquisitionParameters().getWaitingTime();
        
        
        HashMap<String,String> conf = acq.getAcquisitionParameters().getMMConfigurationGroupValues();
        String[] confkeys = conf.keySet().toArray(new String[0]);
        Arrays.sort(confkeys);
        configurations = new String[confkeys.length][2];
       	for(int j=0;j<confkeys.length;j++){
       		configurations[j][0] = confkeys[j];
       		configurations[j][1] = conf.get(confkeys[j]);
       	}

        
        HashMap<String,String> prop = acq.getAcquisitionParameters().getPropertyValues();
        String[] propkeys = prop.keySet().toArray(new String[0]);
        Arrays.sort(propkeys);
        properties = new String[propkeys.length][2];
       	for(int j=0;j<propkeys.length;j++){
          	properties[j][0] = propkeys[j];
          	properties[j][1] = prop.get(propkeys[j]);
       	}
       	
       	additionalParameters = acq.getAdditionalParameters();
	}
}
