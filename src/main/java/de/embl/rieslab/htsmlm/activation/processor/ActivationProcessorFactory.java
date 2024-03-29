package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorFactory;

/**
 * ActivationProcessor factory.
 */
public class ActivationProcessorFactory implements ProcessorFactory {

	@Override
	public Processor createProcessor() {
		return ActivationProcessor.getInstance();
	}

}
