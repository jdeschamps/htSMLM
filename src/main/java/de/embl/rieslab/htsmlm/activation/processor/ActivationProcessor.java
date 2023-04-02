package de.embl.rieslab.htsmlm.activation.processor;

import java.util.concurrent.LinkedBlockingQueue;

import org.micromanager.data.Image;
import org.micromanager.data.Processor;
import org.micromanager.data.ProcessorContext;

/**
 * Image processor storing two consecutive images for use in the activation
 * task.
 */
public class ActivationProcessor implements Processor {

	private static ActivationProcessor processor;
	private final LinkedBlockingQueue<Image> queue;
	private boolean shouldQueue = false;
	
	private ActivationProcessor() {
		queue = new LinkedBlockingQueue(2);
	}
	
    public static ActivationProcessor getInstance() {
        if (processor == null) {
        	processor = new ActivationProcessor();
        }
        return processor;
    }

	/**
	 * Empty the queue and start over.
	 */
	public void startQueueing() {
		queue.clear();
		shouldQueue = true;
	}

	/**
	 * Stop queueing.
	 */
	public void stopQueueing() {
		shouldQueue = false;
	}

	/**
	 * Return the queue size.
	 *
	 * @return Queue size
	 */
	public int getQueueSize() {
		return queue.size();
	}

	/**
	 * Poll the queue.
	 *
	 * @return Image at the head of the queue
	 */
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
