package main.test;


import java.io.IOException;

import org.micromanager.Studio;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.data.Coords;
import org.micromanager.data.Datastore;
import org.micromanager.data.Image;
import org.micromanager.data.internal.DefaultCoords;

public class TestAcq {
	
	public static void testAcquisitionSpeed(Studio studio) {
		int[] numFrames = {500,1000,2000};
		double[] exposures = {10,30,50};

		double[][] snaps = new double[numFrames.length][exposures.length];
		double[][] acqs = new double[numFrames.length][exposures.length];
		
		Thread thread = new Thread(){
		    public void run(){
		
	
			    for(int n=0;n<numFrames.length;n++) {	
			    	for(int e=0;e<exposures.length;e++) {
				    	try {
							studio.core().setExposure(exposures[e]);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
			    		
				    	long start, end;
						String path = "C:\\Program Files\\Micro-Manager-2.0gamma\\test\\";
						
						
						////////////////////////////////////////////////////////
						////////////////////////////////////////////////////////
						// snap acquisition
						start = System.currentTimeMillis(); 
						Datastore store;
						try {
							store = studio.data().createMultipageTIFFDatastore(path+"snap_"+exposures[e]+System.currentTimeMillis(), true, true);	
							
							studio.displays().createDisplay(store);
						
							Image image;
							Coords.CoordsBuilder builder = new DefaultCoords.Builder();
							builder.channel(0).z(0).stagePosition(0);
							
							for(int i=0;i<numFrames[n];i++){
								
								builder = builder.time(i);
								image = studio.live().snap(false).get(0);
								image = image.copyAtCoords(builder.build());
								
								try {
									store.putImage(image);
								} catch (IOException exp) {
									exp.printStackTrace();
								}
					
							}
							store.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						end = System.currentTimeMillis(); 
						snaps[n][e] = (end-start)/1000.;
				
					
						
						////////////////////////////////////////////////////////
						////////////////////////////////////////////////////////
						// core acquisition
						start = System.currentTimeMillis(); 
				
						SequenceSettings settings = new SequenceSettings();
						settings.save = true;
						settings.timeFirst = true;
						settings.usePositionList = false;
						settings.root = "D:\\Micromanager\\Micro-Manager-2.0gamma\\test";
						settings.numFrames = numFrames[n];
						settings.intervalMs = 0;
						settings.shouldDisplayImages = true;
						
						// set acquisition settings
						studio.acquisitions().setAcquisitionSettings(settings);
				
						// run acquisition
						studio.acquisitions().runAcquisition("acq_"+exposures[e], path);
				
						end = System.currentTimeMillis(); 
						acqs[n][e] = (end-start)/1000.;			
				   	}
		    	}
			    
		    	for(int j=0;j<numFrames.length;j++) {
			    	for(int i=0;i<exposures.length;i++) {
						System.out.println(snaps[j][i]);
					}
		    	}
		    	
		    	for(int j=0;j<numFrames.length;j++) {
			    	for(int i=0;i<exposures.length;i++) {
						System.out.println(acqs[j][i]);
					}
		    	}
		    }
		};
		thread.start();
	}
}
