package de.embl.rieslab.htsmlm.activation.utils;

public class ActivationParameters {
    private double dynamicFactor_ = 0.05;
    private double feedbackParameter_ = 0.2;
    private boolean autoCutoff_ = true;
    private double cutoff_ = 0;
    private double dT_ = 1.;
    private double N0_ = 0;
    private double currentPulse_ = 0;
    private double maximumPulse_ = 10000;
    private boolean activate_ = false;
    
    public void setDynamicFactor(double factor) {
    	this.dynamicFactor_ = factor;
    }
    
    public double getDynamicFactor() {
    	return this.dynamicFactor_;
    }
    
    public void setFeedbackParameter(double parameter) {
    	this.feedbackParameter_ = parameter;
    }
    
    public double getFeedbackParameter() {
    	return this.feedbackParameter_;
    }
    
    public void setAutoCutoff(boolean isAutoCutoff) {
    	this.autoCutoff_ = isAutoCutoff;
    }
    
    public boolean getAutoCutoff() {
    	return this.autoCutoff_;
    }
    
    public void setCutoff(double cutoff) {
    	this.cutoff_ = cutoff;
    }
    
    public double getCutoff() {
    	return this.cutoff_;
    }
    
    public void setdT(double dT) {
    	this.dT_ = dT;
    }
    
    public double getdT() {
    	return this.dT_;
    }
    
    public void setN0(double N0) {
    	this.N0_ = N0;
    }
    
    public double getN0() {
    	return this.N0_;
    }
    
    public void setCurrentPulse(double currentPulse) {
    	this.currentPulse_ = currentPulse;
    }
    
    public double getCurrentPulse() {
    	return this.currentPulse_;
    }
    
    public void setMaxPulse(double maximumPulse) {
    	this.maximumPulse_ = maximumPulse;
    }
    
    public double getMaxPulse() {
    	return this.maximumPulse_;
    }
    
    public void setActivate(boolean activate) {
    	this.activate_ = activate;
    }
    
    public boolean getActivate() {
    	return this.activate_;
    }  
}
