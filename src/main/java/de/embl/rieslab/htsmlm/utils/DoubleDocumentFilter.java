package de.embl.rieslab.htsmlm.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Adapted from
 * https://stackoverflow.com/questions/11093326/restricting-jtextfield-input-to-integers
 */
public class DoubleDocumentFilter extends DocumentFilter {

	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
			throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.insert(offset, string);

		if (validateString(sb.toString())) {
			super.insertString(fb, offset, string, attr);
		} else {
			// Do nothing
		}
	}

	private boolean validateString(String text) {
		if("-".equals(text)) {
			// allow to start writing with a hyphen
			return true;
		}
		try {
			Double.parseDouble(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
			throws BadLocationException {

		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.replace(offset, offset + length, text);

		if (validateString(sb.toString())) {
			super.replace(fb, offset, length, text, attrs);
		} else {
			// Do nothing
		}

	}

	@Override
	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
		Document doc = fb.getDocument();
		StringBuilder sb = new StringBuilder();
		sb.append(doc.getText(0, doc.getLength()));
		sb.delete(offset, offset + length);

		if (sb.toString().length() == 0) {
			super.replace(fb, offset, length, "", null);
		} else {
			if (validateString(sb.toString())) {
				super.remove(fb, offset, length);
			} else {
				// Do nothing
			}
		}

	}
}
