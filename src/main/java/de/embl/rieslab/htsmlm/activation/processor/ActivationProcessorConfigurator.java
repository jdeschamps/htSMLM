package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.PropertyMap;
import org.micromanager.data.ProcessorConfigurator;

public class ActivationProcessorConfigurator implements ProcessorConfigurator {

	private static ActivationProcessorConfigurator configurator;

	private ActivationProcessorConfigurator() {}
	
    public static ActivationProcessorConfigurator getInstance() {
        if (configurator == null) {
        	configurator = new ActivationProcessorConfigurator();
        }
        return configurator;
    }
	
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
