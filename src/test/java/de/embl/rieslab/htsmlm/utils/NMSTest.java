package de.embl.rieslab.htsmlm.utils;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class NMSTest {
    double epsilon = 0.000001d;

    private static final double[] VALS = {1.4, 85.6, 4, 7.8};

    @Test
    public void testQuantile(){
        // create peaks
        int n = VALS.length;
        ArrayList<Peak> peaks = new ArrayList<>();
        for(int i=0; i<n; i++){
            peaks.add(new Peak(i,i,VALS[i]));
        }

        VoidNMS nms = new VoidNMS(peaks);
        double pmin = 0.5/n; // smaller element
        double pmax = (n-0.5)/n; // larger element
        double p = (0.5+1)/n;
        double median = 0.5;

        double interpol = VALS[2]+(median-0.375)*(VALS[3]-VALS[2])/(0.625-0.375);

        assertEquals(VALS[0], nms.getQuantile(pmin-0.001), epsilon);
        assertEquals(VALS[1], nms.getQuantile(pmax+0.001), epsilon);
        assertEquals(VALS[2], nms.getQuantile(p), epsilon);
        assertEquals(interpol, nms.getQuantile(median), epsilon);
    }

    private class VoidNMS extends NMS{
        public VoidNMS(ArrayList<Peak> peaks){
            this.peaks = peaks;
        }
    }
}
