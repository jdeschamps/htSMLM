package main.java.embl.rieslab.htsmlm.autopilot.utils.math;

public interface Statistic<T>
{

	public void reset();

	public int enter(double pValue);

	public T getStatistic();

	public int getCount();

}
