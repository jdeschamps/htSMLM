package de.embl.rieslab.htsmlm;

import java.util.TreeMap;

import de.embl.rieslab.emu.controller.SystemController;
import de.embl.rieslab.emu.plugin.UIPlugin;
import de.embl.rieslab.emu.ui.ConfigurableMainFrame;

public class HTSMLM implements UIPlugin{

	@Override
	public ConfigurableMainFrame getMainFrame(SystemController controller, TreeMap<String, String> pluginSettings) {
		return new MainFrame("ht-SMLM control center", controller, pluginSettings);
	}

	@Override
	public String getName() {
		return "ht-SMLM";
	}
}
