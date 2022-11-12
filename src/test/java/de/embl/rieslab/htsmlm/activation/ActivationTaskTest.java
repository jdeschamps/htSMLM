package de.embl.rieslab.htsmlm.activation;

import de.embl.rieslab.htsmlm.activation.utils.NMSUtils;
import de.embl.rieslab.htsmlm.utils.NMS;
import de.embl.rieslab.htsmlm.utils.Peak;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.junit.Test;

import java.util.ArrayList;

import static de.embl.rieslab.htsmlm.activation.ActivationTask.blur;
import static de.embl.rieslab.htsmlm.activation.ActivationTask.runNMS;
import static org.junit.Assert.assertEquals;

public class ActivationTaskTest {
    double epsilon = 0.000001d;

    @Test
    public void testConversion() {
        /**
         * Check value of short subtractions converted to floats.
         */
        int size = 3;
        short[] pixels1 = {
                Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE + 200,
                Short.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE,
                Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE
        };
        short[] pixels2 = {
                Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE,
                Short.MIN_VALUE, Short.MAX_VALUE - 200, Short.MIN_VALUE,
                Short.MIN_VALUE + 1000, Short.MIN_VALUE, Short.MIN_VALUE
        };

        float[] pixels_sub = new float[pixels1.length];
        for (int i = 0; i < pixels1.length; i++) {
            float sub = (float) (pixels1[i] - pixels2[i]);

            pixels_sub[i] = sub >= 0f ? sub : 0f;
        }

        // sanity check
        assertEquals(0f, pixels_sub[0], epsilon);
        assertEquals(200f, pixels_sub[2], epsilon);
        assertEquals(200f, pixels_sub[4], epsilon);
        assertEquals(0f, pixels_sub[6], epsilon);

        // float processor
        FloatProcessor fp = new FloatProcessor(size, size);
        fp.setPixels(pixels_sub);

        // sanity check
        assertEquals(0f, fp.getf(0, 0), epsilon);
        assertEquals(200f, fp.getf(2, 0), epsilon);
        assertEquals(200f, fp.getf(1, 1), epsilon);
        assertEquals(0f, fp.getf(0, 2), epsilon);
    }


    @Test
    public void testComputeCutOffNoPeaks(){
        ArrayList<Peak> peaks = new ArrayList<>();

        double cutoff = 20;
        double cutoffFeedback = 0.5;
        double expectedCutoff = cutoff * (1 - cutoffFeedback);
        assertEquals(expectedCutoff, ActivationTask.computeCutOff(peaks, cutoff, 0.1, cutoffFeedback), epsilon);
    }


    @Test
    public void testComputeCutOff(){
        double cutoff = 0.;
        double cutoffParameter = 0.2;
        double cutoffFeedback = 1.; // no averaging
        double high_intensity = 2000.;
        double low_intensity = 100.;
        int n_high = 10;
        int n_low = 10;

        // create peaks
        ArrayList<Peak> peaks = new ArrayList<>(n_high+n_low);
        for(int i=0; i<n_high; i++){
            peaks.add(new Peak(0, 0, high_intensity));
        }
        for(int i=0; i<n_low; i++){
            peaks.add(new Peak(0, 0, low_intensity));
        }

        // compute cut off
        double new_cutoff = ActivationTask.computeCutOff(peaks, cutoff, cutoffParameter, cutoffFeedback);
        System.out.println(new_cutoff);

        // count peaks
        int n = (int) peaks.stream().filter(p->p.getValue()>new_cutoff).count();
        assertEquals(n_high, n);
    }


    @Test
    public void testNMSWindow() throws InterruptedException {
        // create test image
        short intensity = 20_000;
        int size = 30;
        float[] float_pix = new float[size*size];

        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                if(i>=size/2-5 && i<=size/2-5 && j>=size/2-5 && j<=size/2-5){
                    float_pix[i*size+j] = intensity;
                } else {
                    float_pix[i*size+j] = 0f;
                }
            }
        }

        // create FloatProcessor
        FloatProcessor fp = new FloatProcessor(size, size);
        fp.setPixels(float_pix);

        // Gaussian filter
        blur(fp);

        // run NMS
        NMS nms = runNMS(fp);

        // filtered peaks
        ArrayList<Peak> peaks = NMSUtils.filterPeaks(nms, 0);

        // get image
        ImageProcessor ip = NMSUtils.applyCutoff(nms, peaks);

        // show result
        ImagePlus im = new ImagePlus("NMS result");
        im.setProcessor(ip);
        im.setDisplayRange(im.getStatistics().min, im.getStatistics().max);
        im.show();

        Thread.sleep(100);
    }
}
