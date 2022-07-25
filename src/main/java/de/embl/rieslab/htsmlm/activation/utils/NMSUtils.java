package de.embl.rieslab.htsmlm.activation.utils;

import de.embl.rieslab.htsmlm.utils.NMS;
import de.embl.rieslab.htsmlm.utils.Peak;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Arrays;

public class NMSUtils {

    private final static int sizeRoi=10;
    private final static double epsilon = 0.000001d;


    public static ArrayList<Peak> filterPeaks(NMS nms, double cutoff){
        ArrayList<Peak> filtered_peaks = new ArrayList<>();
        for(Peak p: nms.getPeaks()){
            if(p.getValue() >= cutoff){
                filtered_peaks.add(p);
            }
        }

        return filtered_peaks;
    }

    public static ImageProcessor applyCutoff(NMS nms, ArrayList<Peak> filteredPeaks, double cutoff){
        // TODO imglib2
        ImageProcessor impresult = (ImageProcessor) nms.getImageProcessor().clone();
        impresult.setValue(impresult.getStatistics().max);

        Roi roi = new Roi(0,0, sizeRoi, sizeRoi);
        for(Peak p: filteredPeaks){
            int mi = p.getX();
            int mj = p.getY();
            roi.setLocation(mi-sizeRoi/2, mj-sizeRoi/2);
            impresult.draw(roi);
        }

        // TODO: change to more clever contrast
        impresult.multiply(5); // for contrast

        return impresult;
    }

    // algorithm from
    // https://www.mathworks.com/help/matlab/ref/quantile.html;jsessionid=62adf577fa8b77dce03112a70ad5#btf91zm
    // https://www.mathworks.com/help/matlab/ref/quantile.html#btf91wi
    public static double getQuantile(ArrayList<Peak> peaks, double q){
        if(q<0 || q>1){
            throw new IllegalArgumentException("Quantile should be in range [0,1]");
        }

        // construct array with values
        double[] vals = peaks.stream().mapToDouble(p -> p.getValue()).toArray();
        int n = vals.length;

        // sort the array in place
        Arrays.sort(vals);

        if(q < 0.5/n){
            return vals[0];
        } else if(q > (n-0.5)/n){
            return vals[n-1];
        } else {
            // find points to interpolate
            int counter = 0;
            double pmin = 0.5/n;
            double pmax = pmin;
            double max_val = (n-0.5)/n;
            while((Math.abs(q-pmax)<epsilon || pmax < q) &&
                    (Math.abs(max_val-pmax)<epsilon || pmax < max_val)){ // account for double precision error
                pmin = pmax;
                pmax = (0.5+(++counter))/n;
            }

            if((Math.abs(q-pmax)<epsilon)){
                return vals[counter];
            } else {
                // linear interpolation
                double qq =  vals[counter-1]+(q-pmin)*(vals[counter]-vals[counter-1])/(pmax-pmin);
                return qq;
            }
        }
    }
}
