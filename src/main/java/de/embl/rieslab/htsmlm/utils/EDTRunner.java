package de.embl.rieslab.htsmlm.utils;

import javax.swing.*;
import java.awt.*;

public class EDTRunner {

    public static void runOnEDT(Runnable runnable){
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }
}
