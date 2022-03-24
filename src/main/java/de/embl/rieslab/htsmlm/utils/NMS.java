package de.embl.rieslab.htsmlm.utils;


import java.util.ArrayList;
import java.util.Arrays;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.process.ImageProcessor;

/**
 * Implementation following: Neubeck, A., & Van Gool, L. (2006, August).
 * Efficient non-maximum suppression. In 18th International Conference on
 * Pattern Recognition (ICPR'06) (Vol. 3, pp. 850-855). IEEE.
 */

public class NMS {
	ImageProcessor imp;
	ImagePlus im_, imtemp_;
	ImageWindow iw;
	int width_, height_;
	int n_;
	int sizeRoi=10;
	double epsilon = 0.000001d;
	ArrayList<Peak> peaks;
	
	public NMS(){
		peaks = new ArrayList<>();
		imtemp_ = new ImagePlus();
		imtemp_.setTitle("NMS");
	}
	
	public void run(ImagePlus im, int n){
		im_ = im;		
		width_ = im.getWidth();
		height_ = im.getHeight();
		imp = im.getProcessor();
		n_ = n;
		peaks.clear();
		
		process();
	}

	public ImageProcessor getNMSDetections(ImagePlus im, double cutoff){
		ImageProcessor impresult = (ImageProcessor) imp.clone();
		impresult.setValue(65535);		// white

		Peak[] filt_peaks = (Peak[]) peaks.stream().filter(p -> p.getValue()>= cutoff).toArray();

		Roi roi = new Roi(0,0,sizeRoi,sizeRoi);
		for(Peak p: filt_peaks){
			int mi = p.getX();
			int mj = p.getY();
			roi.setLocation(mi-sizeRoi/2, mj-sizeRoi/2);
			impresult.draw(roi);
		}
		impresult.multiply(5);

		return impresult;
	}

	// algorithm from
	// https://www.mathworks.com/help/matlab/ref/quantile.html;jsessionid=62adf577fa8b77dce03112a70ad5#btf91zm
	// https://www.mathworks.com/help/matlab/ref/quantile.html#btf91wi
	public double getQuantile(double q){
		if(q<0 || q>1){
			throw new IllegalArgumentException("Quantile should be in range [0,1]");
		}

		// construct array with values
		double[] vals = peaks.stream().mapToDouble(p -> p.getValue()).toArray();
		int n = vals.length;

		// sort the array in place
		Arrays.sort(vals);

		if(q < 0.5/n){
			return vals[0];
		} else if(q > (n-0.5)/n){
			return vals[n-1];
		} else {
			// find points to interpolate
			int counter = 0;
			double pmin = 0.5/n;
			double pmax = pmin;
			double max_val = (n-0.5)/n;
			while((Math.abs(q-pmax)<epsilon || pmax < q) &&
					(Math.abs(max_val-pmax)<epsilon || pmax < max_val)){ // account for double precision error
				pmin = pmax;
				pmax = (0.5+(++counter))/n;
			}

			if((Math.abs(q-pmax)<epsilon)){
				return vals[counter];
			} else {
				// linear interpolation
				double qq =  vals[counter-1]+(q-pmin)*(vals[counter]-vals[counter-1])/(pmax-pmin);
				return qq;
			}
		}
	}

	public long getN(double cutoff){
		if(!(peaks == null)) {
			long n = 0;
			return peaks.stream().filter(p -> p.getValue()>= cutoff).count();
		}

		return -1;
	}
	
	public int getN(){
		int N=0;
		
		if(!(peaks == null)){
			N = peaks.size();
		}
		
		return N;
	}
	
	public void process(){
		int i,j,ii,jj,ll,kk;
		int mi,mj;
		boolean failed = false;
	
		for(i=0;i<width_-n_-1;i+=n_+1){	// Loop over (n+1)x(n+1)
			for(j=0;j<height_-n_-1;j+=n_+1){
				mi = i;
				mj = j;
				for(ii=i;ii<=i+n_;ii++){	
					for(jj=j;jj<=j+n_;jj++){
						if(imp.get(ii,jj) > imp.get(mi,mj)){	
							mi = ii;
							mj = jj;
						}
					}
				}
				failed = false;
				
				Outer:
				for(ll=mi-n_;ll<=mi+n_;ll++){	
					for(kk=mj-n_;kk<=mj+n_;kk++){
						if((ll<i || ll>i+n_) || (kk<j || kk>j+n_)){
							if(ll<width_ && ll>0 && kk<height_ && kk>0){		
								if(imp.get(ll,kk)>imp.get(mi,mj) ){
									failed = true;
									break Outer;
								}
							}
						}
					}
				}
				if(!failed){
					peaks.add(new Peak(mi, mj, imp.get(mi,mj)));
				}
			}			
		}
	}
}
