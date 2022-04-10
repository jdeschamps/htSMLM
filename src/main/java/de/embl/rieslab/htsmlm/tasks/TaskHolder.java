package de.embl.rieslab.htsmlm.tasks;

public interface TaskHolder<T> {

	void update(T[] output);
	T[] retrieveAllParameters();
	boolean startTask();
	void stopTask();
	void pauseTask();
	void resumeTask();
	boolean isTaskRunning();
	String getTaskName();
	boolean isCriterionReached();
	void taskDone();
	
	/**
	 * To be called before starting the task.
	 * 
	 */
	void initializeTask();
	void initializeTask(T[] input);

}
