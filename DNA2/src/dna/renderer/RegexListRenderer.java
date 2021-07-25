package dna.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import dna.dataStructures.Regex;

@SuppressWarnings("serial")
public class RegexListRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		label.setText((String)((Regex)value).getLabel());
		label.setForeground((Color)((Regex)value).getColor());
		label.setOpaque(true);
		return label;
	}
}