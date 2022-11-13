package de.embl.rieslab.htsmlm.activation.processor;

import org.micromanager.data.Image;
import org.micromanager.data.ProcessorContext;
import org.micromanager.data.SummaryMetadata;

public class ActivationContext implements ProcessorContext{

	private final SummaryMetadata summaryMetaData; // useless
	private Image image;
	
	public ActivationContext(SummaryMetadata summaryMetaData) {
		this.summaryMetaData = summaryMetaData;
	}

	@Override
	public void outputImage(Image image) {
		this.image = image; 
	}

	@Override
	public SummaryMetadata getSummaryMetadata() {
		return this.summaryMetaData;
	}
}
