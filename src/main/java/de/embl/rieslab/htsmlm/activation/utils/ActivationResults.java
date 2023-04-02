package de.embl.rieslab.htsmlm.activation.utils;


/**
 * Class holding the results of an activation script iteration.
 */
public class ActivationResults {

    // cutoff
    private double newCutoff = 0.;

    // number of detected molecules
    private int N = 0;

    // new pulse
    private double newPulse = 0;
    
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
}
