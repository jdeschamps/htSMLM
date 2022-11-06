package de.embl.rieslab.htsmlm.activation.processor;

import java.util.concurrent.LinkedBlockingQueue;

import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;

public class ActivationProcessor implements Processor {

	private static ActivationProcessor processor;
	private final LinkedBlockingQueue<Image> queue;
	private boolean shouldQueue = false;
	
	private ActivationProcessor() {
		queue = new LinkedBlockingQueue<Image>(2);
	}
	
    public static ActivationProcessor getInstance() {
        if (processor == null) {
        	processor = new ActivationProcessor();
        }
        return processor;
    }
	
	public void startQueueing() {
		queue.clear();
		shouldQueue = true;
	}
	
	public void stopQueueing() {
		shouldQueue = false;
	}
	
	public int getQueueSize() {
		return queue.size();
	}
	
	public Image poll() {
		return queue.poll();
	}
	
	@Override
	public void processImage(Image image, ProcessorContext context) {
		context.outputImage(image);
		
		if(shouldQueue && queue.size() < 2) {
			queue.add(image);
		}
	}

}
