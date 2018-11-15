package main.embl.rieslab.emu.configuration.ui.misc;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import main.embl.rieslab.emu.utils.ColorRepository;

public class IconTableRenderer implements TableCellRenderer{
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		//return new IconAdapter(new ColorIcon(ColorRepository.getColor((String) value)), (String) value);
		JLabel label = new JLabel((String) value);
		label.setIcon(new ColorIcon(ColorRepository.getColor((String) value)));
		return label;
	}
	
}