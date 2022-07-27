package de.embl.rieslab.htsmlm.utils;


import java.util.ArrayList;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Implementation following: Neubeck, A., & Van Gool, L. (2006, August).
 * Efficient non-maximum suppression. In 18th International Conference on
 * Pattern Recognition (ICPR'06) (Vol. 3, pp. 850-855). IEEE.
 */

public class NMS {
	private ImageProcessor image;
	private int width_, height_;
	private int maskSize_;
	protected ArrayList<Peak> peaks;
	
	public NMS(FloatProcessor imp, int maskSize){
		peaks = new ArrayList<>();
		image = imp;
		maskSize_ = maskSize;

		width_ = imp.getWidth();
		height_ = imp.getHeight();

		process();
	}

	public ArrayList<Peak> getPeaks(){
		return peaks;
	}

	public ImageProcessor getImageProcessor(){
		return image;
	}

	public void process(){
		int i,j,ii,jj,ll,kk;
		int mi,mj;
		boolean failed = false;
	
		for(i=0; i<width_- maskSize_ -1; i+= maskSize_ +1){	// Loop over (n+1)x(n+1)
			for(j=0; j<height_- maskSize_ -1; j+= maskSize_ +1){
				mi = i;
				mj = j;
				for(ii=i; ii<=i+ maskSize_; ii++){
					for(jj=j; jj<=j+ maskSize_; jj++){
						if(image.getf(ii,jj) > image.getf(mi,mj)){
							mi = ii;
							mj = jj;
						}
					}
				}
				failed = false;
				
				Outer:
				for(ll=mi- maskSize_; ll<=mi+ maskSize_; ll++){
					for(kk=mj- maskSize_; kk<=mj+ maskSize_; kk++){
						if((ll<i || ll>i+ maskSize_) || (kk<j || kk>j+ maskSize_)){
							if(ll<width_ && ll>0 && kk<height_ && kk>0){		
								if(image.getf(ll,kk)> image.getf(mi,mj) ){
									failed = true;
									break Outer;
								}
							}
						}
					}
				}
				if(!failed){
					peaks.add(new Peak(mi, mj, image.getf(mi,mj)));
				}
			}			
		}
	}
}
