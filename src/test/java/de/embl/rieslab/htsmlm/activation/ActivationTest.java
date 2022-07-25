package de.embl.rieslab.htsmlm.activation;

import ij.ImagePlus;
import ij.process.ShortProcessor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActivationTest {
    double epsilon = 0.000001d;

    @Test
    public void testScaleChange(){
        int size = 3;
        short[] pixels = {
                Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE,
                Short.MIN_VALUE, Short.MAX_VALUE, Short.MIN_VALUE,
                Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE
        };

        // create ShortProcessor
        ShortProcessor sp = new ShortProcessor(size, size);
        sp.setPixels(pixels);

        // check value
        assertEquals(65535, sp.getMax(), epsilon);

        // concert to float and wrap in ImagePlus
        ImagePlus imp = new ImagePlus("Image", sp.convertToFloatProcessor());

        // check value
        assertEquals(pixels[4], imp.getStatistics().max, epsilon);
    }
}
