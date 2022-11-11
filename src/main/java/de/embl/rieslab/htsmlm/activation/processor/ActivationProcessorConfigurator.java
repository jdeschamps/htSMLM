package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.PropertyMap;
import org.micromanager.PropertyMaps;
import org.micromanager.data.ProcessorConfigurator;

public class ActivationProcessorConfigurator implements ProcessorConfigurator {
	
	private static ActivationProcessorConfigurator configurator;

	private ActivationProcessorConfigurator(PropertyMap settings) {
		// do nothing
	}
	
    public static ActivationProcessorConfigurator getInstance(PropertyMap settings) {
        if (configurator == null) {
        	configurator = new ActivationProcessorConfigurator(settings);
        }
        return configurator;
    }
	
	@Override
	public void showGUI() {
		// do nothing
	}

	@Override
	public void cleanup() {
		// do nothing
	}

	@Override
	public PropertyMap getSettings() {
		return PropertyMaps.builder().build();
	}
}
