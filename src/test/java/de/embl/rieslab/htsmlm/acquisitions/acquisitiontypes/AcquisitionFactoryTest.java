package de.embl.rieslab.htsmlm.acquisitions.acquisitiontypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.micromanager.data.Datastore;

import de.embl.rieslab.htsmlm.acquisitions.wrappers.Experiment;
import de.embl.rieslab.htsmlm.acquisitions.wrappers.ExperimentWrapper;
import de.embl.rieslab.htsmlm.constants.HTSMLMConstants;

public class AcquisitionFactoryTest {
    double epsilon = 0.000001d;
    

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    /**
     * Create dummy experiment.
     * 
     * @return experiment.
     */
    private Experiment getExperiment() {
    	ArrayList<Acquisition> acqList = new ArrayList();
    	acqList.add(new TimeAcquisition(4.));
    	acqList.add(new SnapAcquisition(6.));
    	
    	return new Experiment(1, 3, Datastore.SaveMode.MULTIPAGE_TIFF, acqList);
    }
    
    
    @Test
    public void testWriteAcquisitionList() throws IOException {
    	// create experiment
    	Experiment e = getExperiment();
    	
    	// get folder and name
    	File file = folder.newFolder("myfolder");
    	String parentFolder = file.getAbsolutePath();
    	String fileName = "myexperiment";
    	
    	// write acquisition 
    	AcquisitionFactory.writeAcquisitionList(e, parentFolder, fileName);
    	
    	// check that files exists
    	String filePath = parentFolder + File.separator + fileName + '.' + HTSMLMConstants.ACQ_EXT;
    	assertTrue( (new File(filePath)).exists() );
    	
    	// make wrapper
    	ExperimentWrapper written_experiment = new ExperimentWrapper(fileName, parentFolder, e);
    	
    	// read acquisition 
    	ExperimentWrapper read_experiment = AcquisitionFactory.readExperiment(filePath);

    	// check that it worked
    	assertEquals(written_experiment.pauseTime, read_experiment.pauseTime);
    	assertEquals(written_experiment.numberPositions, read_experiment.numberPositions);
    	assertEquals(written_experiment.savemode, read_experiment.savemode);
    	assertEquals(written_experiment.acquisitionList.size(), written_experiment.acquisitionList.size());
    	assertEquals(written_experiment.acquisitionList.get(0).type, written_experiment.acquisitionList.get(0).type);
    	assertEquals(written_experiment.acquisitionList.get(1).type, written_experiment.acquisitionList.get(1).type);
    	assertEquals(written_experiment.acquisitionList.get(0).exposure, written_experiment.acquisitionList.get(0).exposure, epsilon);
    	assertEquals(written_experiment.acquisitionList.get(1).exposure, written_experiment.acquisitionList.get(1).exposure, epsilon);
    }
}
