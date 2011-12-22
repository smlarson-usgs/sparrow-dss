package gov.usgs.cida.binning.domain;

import java.math.BigDecimal;

/**
 * Contains data similar to a BinSet, but is used during the construction
 * of the BinSet.
 * 
 * @author eeverman
 */
public class InProcessBinSet implements Cloneable {
	
	/** The nice posts to be formated for the user */
	public BigDecimal[] posts;
	
	/** The functional posts used behind the scenes to actually bin */
	public BigDecimal[] functional;
	
	/** True if a detection limit is used for the lowest bin */
	public boolean usesDetectionLimit;
	
	/** Actual Max - mostly for debug */
	public BigDecimal actualMax;
	
	/** Actual Min - mostly for debug */
	public BigDecimal actualMin;
	
	/** 
	 * The actual CUV used in building the posts.  Used later for
	 * formatting.
	 */
	public BigDecimal characteristicUnitValue;
	
	/** The requested number of bins */
	public Integer requestedBinCount;
	

	public InProcessBinSet() {
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Returns the value of the most bottom post that was calculated.
	 * 
	 * If a detection limit is in use this will be post at index 2.
	 * If no detection limit is in use, this is the post at index 0.
	 * @return
	 */
	public BigDecimal getBottomPostValueAboveDetectionLimit() {
		if (usesDetectionLimit) {
			return posts[2];
		} else {
			return posts[0];
		}
	}
	
	/**
	 * Returns the index of the most bottom post that was calculated.
	 * 
	 * If a detection limit is in use this will be 2.
	 * If no detection limit is in use, this will be 0.
	 * @return
	 */
	public int getBottomPostIndexAboveDetectionLimit() {
		if (usesDetectionLimit) {
			return 2;
		} else {
			return 0;
		}
	}
	
	/**
	 * Returns the value of the topmost post.
	 * @return
	 */
	public BigDecimal getTopPostValue() {
		return posts[posts.length - 1];
	}
	
}