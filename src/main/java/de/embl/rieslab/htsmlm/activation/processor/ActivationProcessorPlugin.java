package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.PropertyMap;
import org.micromanager.Studio;
import org.micromanager.data.ProcessorConfigurator;
import org.micromanager.data.ProcessorFactory;
import org.micromanager.data.ProcessorPlugin;

public class ActivationProcessorPlugin implements ProcessorPlugin{

	private static ActivationProcessorPlugin plugin;

	private ActivationProcessorPlugin() {}
	
    public static ActivationProcessorPlugin getInstance() {
        if (plugin == null) {
        	plugin = new ActivationProcessorPlugin();
        }
        return plugin;
    }
	
	@Override
	public void setContext(Studio studio) {
		// Do nothing
	}

	@Override
	public String getName() {
		return "htSMLM activation";
	}

	@Override
	public String getHelpText() {
		return "Places frames into a queue when requested by htSMLM activation script.";
	}

	@Override
	public String getVersion() {
		return "0";
	}

	@Override
	public String getCopyright() {
		return "";
	}

	@Override
	public ProcessorConfigurator createConfigurator(PropertyMap settings) {
		return new ActivationProcessorConfigurator();
	}

	@Override
	public ProcessorFactory createFactory(PropertyMap settings) {
		return new ActivationProcessorFactory();
	}

}
