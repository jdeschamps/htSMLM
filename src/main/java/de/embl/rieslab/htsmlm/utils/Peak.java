package de.embl.rieslab.htsmlm.utils;

public class Peak {
	private int x_,y_;
	private double value_;
	
	public Peak(int x, int y, double value){
		x_ = x;
		y_ = y;
		value_ = value;
	}
	
	public void set(int x, int y, int value){
		x_ = x;
		y_ = y;
		value_ = value;
	}

	public int getX(){
		return x_;
	}
	
	public int getY(){
		return y_;
	}
	
	public double getValue(){
		return value_;
	}

	public void print(){
		System.out.println("["+x_+","+y_+","+value_+"]");
	}
	
	public String toString(){
		String s = "["+x_+","+y_+","+value_+"]";
		return s;
	}
	
}
