package de.embl.rieslab.htsmlm.utils;

/**
 * A peak described by its x and y position, and intensity.
 */
public class Peak {
	private int x_,y_;
	private double intensity_;
	
	public Peak(int x, int y, double intensity){
		x_ = x;
		y_ = y;
		intensity_ = intensity;
	}
	
	public void set(int x, int y, int intensity){
		x_ = x;
		y_ = y;
		intensity_ = intensity;
	}

	public int getX(){
		return x_;
	}
	
	public int getY(){
		return y_;
	}
	
	public double getValue(){
		return intensity_;
	}
	
	public String toString(){
		String s = "["+x_+","+y_+","+ intensity_ +"]";
		return s;
	}
	
}
