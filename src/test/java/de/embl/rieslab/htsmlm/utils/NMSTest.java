package de.embl.rieslab.htsmlm.utils;

import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;
import ij.process.FloatProcessor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class NMSTest {
    double epsilon = 0.000001d;

    @Test
    public void testPipeline() {
        int width = 32;
        int height = 16;

        int x1 = 19;
        int y1 = 9;
        float i1 = 2000f;
        int x2 = 9;
        int y2 = 11;
        float i2 = 4000f;

        // set maxima
        FloatProcessor fp = new FloatProcessor(width, height);
        fp.setf(x1, y1, i1);
        fp.setf(x2, y2, i2);

        // run NMS
        NMS nms = new NMS(fp, 3);

        // get filtered peaks
        List<Peak> peaks = nms.getPeaks().stream().filter(p -> p.getValue() > 0).collect(Collectors.toList());
        assertEquals(2, peaks.size());

        boolean firstfirst = peaks.get(0).getX() == x1;
        assertEquals(firstfirst ? x1: x2, peaks.get(0).getX());
        assertEquals(firstfirst ? y1: y2, peaks.get(0).getY());
        assertEquals(firstfirst ? i1: i2, peaks.get(0).getValue(), epsilon);
        assertEquals(firstfirst ? x2: x1, peaks.get(1).getX());
        assertEquals(firstfirst ? y2: y1, peaks.get(1).getY());
        assertEquals(firstfirst ? i2: i1, peaks.get(1).getValue(), epsilon);
    }
}
