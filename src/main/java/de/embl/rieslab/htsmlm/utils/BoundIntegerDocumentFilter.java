package de.embl.rieslab.htsmlm.utils;


public class BoundIntegerDocumentFilter extends IntegerDocumentFilter {

	public final int min_, max_;
	
	public BoundIntegerDocumentFilter(int min, int max) {
		this.min_ = min;
		this.max_ = max;
	}
	
	@Override
	protected boolean validateString(String text) {
		if("-".equals(text)) {
			// allow to start writing with a hyphen
			return true;
		}
		try {
			int value = Integer.parseInt(text);
			return min_ <= value && value <= max_;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
