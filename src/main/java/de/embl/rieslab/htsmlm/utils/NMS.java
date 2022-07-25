package de.embl.rieslab.htsmlm.utils;


import java.util.ArrayList;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Implementation following: Neubeck, A., & Van Gool, L. (2006, August).
 * Efficient non-maximum suppression. In 18th International Conference on
 * Pattern Recognition (ICPR'06) (Vol. 3, pp. 850-855). IEEE.
 */

public class NMS {
	private ImageProcessor image;
	private int width_, height_;
	private int n_;
	protected ArrayList<Peak> peaks;
	
	public NMS(ImagePlus im, int n){
		peaks = new ArrayList<>();
		image = im.getProcessor();
		n_ = n;

		width_ = im.getWidth();
		height_ = im.getHeight();

		process();
	}

	public ArrayList<Peak> getPeaks(){
		return peaks;
	}

	public ImageProcessor getImageProcessor(){
		return image;
	}

	public long getN(double cutoff){
		if(!(peaks == null)) {
			return peaks.stream().filter(p -> p.getValue()>=cutoff).count();
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
						if(image.get(ii,jj) > image.get(mi,mj)){
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
								if(image.get(ll,kk)> image.get(mi,mj) ){
									failed = true;
									break Outer;
								}
							}
						}
					}
				}
				if(!failed){
					peaks.add(new Peak(mi, mj, image.get(mi,mj)));
				}
			}			
		}
	}
}
