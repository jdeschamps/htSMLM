package de.embl.rieslab.htsmlm.activation.utils;

public class ActivationParameters {
    public double cutoffParameter = 0.05;
    public double feedbackParameter = 0.2;
    public boolean autoCutoff = true;
    public double fixedCutoff = 0;
    public double dT = 1.;
    public double N0 = 0;
    public double currentPulse = 0;
    public double maximumPulse = 10000;
    public boolean activate = false;
}
