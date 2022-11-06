package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.PropertyMap;
import org.micromanager.data.ProcessorConfigurator;

public class ActivationProcessorConfigurator implements ProcessorConfigurator {

	@Override
	public void showGUI() {
	
	}

	@Override
	public void cleanup() {

	}

	@Override
	public PropertyMap getSettings() {
		return new ActivationPropertyMap();
	}

}
