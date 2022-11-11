package de.embl.rieslab.htsmlm;

import java.util.TreeMap;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.plugin.UIPlugin;
import de.embl.rieslab.emu.ui.ConfigurableMainFrame;
import de.embl.rieslab.htsmlm.activation.processor.ActivationProcessorPlugin;

public class HTSMLM implements UIPlugin{

	@Override
	public ConfigurableMainFrame getMainFrame(SystemController controller, TreeMap<String, String> pluginSettings) {
		controller.getStudio().data().addAndConfigureProcessor(new ActivationProcessorPlugin());

		return new MainFrame("ht-SMLM control center", controller, pluginSettings);
	}

	@Override
	public String getName() {
		return "ht-SMLM";
	}
}
