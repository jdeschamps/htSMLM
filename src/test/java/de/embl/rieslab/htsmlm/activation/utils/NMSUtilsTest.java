package de.embl.rieslab.htsmlm.activation.utils;

import de.embl.rieslab.htsmlm.activation.ActivationTask;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import de.embl.rieslab.htsmlm.utils.NMS;
import de.embl.rieslab.htsmlm.utils.Peak;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NMSUtilsTest {
    double epsilon = 0.000001d;

    private static final double[] VALS = {1.4, 85.6, 4, 7.8};

    @Test
    public void testFilterPeaks() {
        ArrayList<Peak> peaks = new ArrayList<>();

        // build truth
        final boolean[] truth = {true, false, true, true, false, false, true};
        final int[] x = {0, 5, 7, 12, 15, 23, 25};
        final int[] y = {11, 4, 6, 7, 24, 12, 22};
        final Double[] v = {10., 1., 50., 13., 4., 7., 11.};

        // built list
        for (int i = 0; i < x.length; i++) {
            peaks.add(new Peak(x[i], y[i], v[i]));
        }

        // filter list
        FloatProcessor fp = new FloatProcessor(3, 3);
        ImagePlus ip = new ImagePlus("", fp);
        MonkeyPatchNMS nms = new MonkeyPatchNMS(ip, peaks);
        ArrayList<Peak> filteredPeaks = NMSUtils.filterPeaks(nms, 10.);

        // test filtering
        assertEquals(filteredPeaks.size(), 4);

        List<Double> valueList = Arrays.asList(v);
        for (Peak p : filteredPeaks) {
            assertTrue(truth[valueList.indexOf(p.getValue())]);
        }

    }


    @Test
    public void testQuantile(){
        // create peaks
        int n = VALS.length;
        ArrayList<Peak> peaks = new ArrayList<>();
        for(int i=0; i<n; i++){
            peaks.add(new Peak(i,i,VALS[i]));
        }

        double pmin = 0.5/n; // smaller element
        double pmax = (n-0.5)/n; // larger element
        double p = (0.5+1)/n;
        double median = 0.5;

        double interpol = VALS[2]+(median-0.375)*(VALS[3]-VALS[2])/(0.625-0.375);

        assertEquals(VALS[0], NMSUtils.getQuantile(peaks, pmin-0.001), epsilon);
        assertEquals(VALS[1], NMSUtils.getQuantile(peaks,pmax+0.001), epsilon);
        assertEquals(VALS[2], NMSUtils.getQuantile(peaks, p), epsilon);
        assertEquals(interpol, NMSUtils.getQuantile(peaks, median), epsilon);
    }

    @Test
    public void testApplyCutoff() throws InterruptedException {
        int size = 15;
        float[] pixels = {
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 65535, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
                255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        };
        int index_max_2D = 7;
        int index_max_1D  = index_max_2D * size + index_max_2D;

        FloatProcessor fp = new FloatProcessor(size, size);
        fp.setPixels(pixels);

        // check max value
        assertEquals(pixels[index_max_1D], fp.getStatistics().max, epsilon);

        // create peaks
        ArrayList<Peak> peaks = new ArrayList<>();
        peaks.add(new Peak(index_max_2D, index_max_2D, pixels[index_max_1D]));

        // create MonkeyPatch NMS
        MonkeyPatchNMS nms = new MonkeyPatchNMS(new ImagePlus("", fp), peaks);

        // apply cutoff
        ImageProcessor ip = NMSUtils.applyCutoff(nms, peaks);

        // show image
        // ImagePlus im = new ImagePlus("", ip);
        // final ImageJ ij = new ImageJ();
        // im.show();

        // check that the scaling worked
        assertEquals(ip.getMax(), ip.maxValue(), epsilon);
        assertEquals(ip.getMin(), ip.minValue(), epsilon);

        // check that the roi has been created
        int ind_plus = index_max_2D + NMSUtils.sizeRoi / 2;
        int ind_minus = index_max_2D - NMSUtils.sizeRoi / 2;
        assertEquals(ip.getPixelValue(ind_plus, ind_plus), ip.maxValue(), epsilon);
        assertEquals(ip.getPixelValue(ind_minus, ind_minus), ip.maxValue(), epsilon);
    }

    protected class MonkeyPatchNMS extends NMS {

        public MonkeyPatchNMS(ImagePlus im, ArrayList<Peak> peaks) {
            super(im, 1);

            this.peaks = peaks;
        }

        @Override
        public void process(){
            // do nothing
        }
    }
}
