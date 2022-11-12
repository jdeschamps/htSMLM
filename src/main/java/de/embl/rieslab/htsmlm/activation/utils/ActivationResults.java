package de.embl.rieslab.htsmlm.activation.utils;

import ij.process.ImageProcessor;

public class ActivationResults {

    private double newCutoff = 0.;
    private int N = 0;
    private double newPulse = 0;
    private ImageProcessor filteredImage = null;
    
    public void setNewCutoff(double newCutoff) {
    	this.newCutoff = newCutoff;
    }
    
    public double getNewCutOff() {
    	return this.newCutoff;
    }
    
    public void setNewN(int N) {
    	this.N = N;
    }
    
    public int getNewN() {
    	return this.N;
    }
    
    public void setNewPulse(double newPulse) {
    	this.newPulse = newPulse;
    }
    
    public double getNewPulse() {
    	return this.newPulse;
    }
    
    public void setFilteredImage(ImageProcessor filteredImage) {
    	this.filteredImage = filteredImage;
    }
    
    public ImageProcessor getFilteredImage() {
    	return this.filteredImage;
    }
    
}
