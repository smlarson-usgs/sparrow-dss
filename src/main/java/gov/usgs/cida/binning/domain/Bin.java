package gov.usgs.cida.binning.domain;


import java.io.Serializable;
import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * A bin used to create a themed map or legend.
 * 
 * @author eeverman
 */
@XStreamAlias("bin")
@XStreamInclude({Bound.class})
public class Bin implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** Formatted String representation of the top bin */
	private final Bound top;
	
	/** True if the top bin is unbounded.  For instance, it may be '>' */
	private final Bound bottom;
	
	/** True if this bin is for values below the detection limit. */
	@XStreamAsAttribute
	private final boolean nonDetect;
	
	/** The number of values included in this bin.  May be null */
	@XStreamAsAttribute
	private final Integer valueCount;
	

	public Bin(Bound bottom, Bound top) {
		this.top = top;
		this.bottom = bottom;
		nonDetect = false;
		valueCount = null;
	}
	
	public Bin(Bound bottom, Bound top, Integer valueCount) {
		this.top = top;
		this.bottom = bottom;
		nonDetect = false;
		this.valueCount = valueCount;
	}
	
	public Bin(Bound bottom, Bound top, boolean isNonDetect) {
		this.top = top;
		this.bottom = bottom;
		nonDetect = isNonDetect;
		valueCount = null;
	}
	
	public Bin(Bound bottom, Bound top, boolean isNonDetect, Integer valueCount) {
		this.top = top;
		this.bottom = bottom;
		nonDetect = isNonDetect;
		this.valueCount = valueCount;
	}


	public Bound getTop() {
		return top;
	}


	public Bound getBottom() {
		return bottom;
	}
	
	/**
	 * Returns true if the formatted bounds are the same, top and bottom.
	 * 
	 * @return
	 */
	public boolean hasDuplicateFormattedBounds() {
		return top.getFormatted().equals(bottom.getFormatted());
	}
	
	/**
	 * Returns true if the actual top bound equals the actual bottom bound.
	 * @return
	 */
	public boolean hasDuplicateActualBounds() {
		return top.getActual().equals(bottom.getActual());
	}
	
	/**
	 * Returns true if the bin contains the passed value.
	 * 
	 * @param inclusiveBottom True to check the bottom bound inclusively.  False is exclusive.
	 * @param inclusiveTop True to check the top bound inclusively.  False is exclusive.
	 * @param val
	 * @return
	 */
	public boolean formattedContains(double val, boolean inclusiveBottom, boolean inclusiveTop) {
		
		BigDecimal bigVal = new BigDecimal(val);
		
		return formattedBottomLessThan(bigVal, inclusiveBottom) &&
				formattedTopGreaterThan(bigVal, inclusiveTop);
	}
	
	public boolean formattedBottomLessThan(double val, boolean inclusive) {
		BigDecimal bigVal = new BigDecimal(val);
		return formattedBottomLessThan(bigVal, inclusive);
	}
	
	public boolean formattedBottomLessThan(BigDecimal val, boolean inclusive) {
		if (! bottom.isUnbounded()) {
			BigDecimal bigBot = new BigDecimal(bottom.getFormatted());
			
			if (inclusive) {
				if (bigBot.compareTo(val) > 0) {
					return false;
				}	
			} else {
				if (bigBot.compareTo(val) >= 0) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean formattedTopGreaterThan(double val, boolean inclusive) {
		BigDecimal bigVal = new BigDecimal(val);
		return formattedTopGreaterThan(bigVal, inclusive);
	}
	
	public boolean formattedTopGreaterThan(BigDecimal val, boolean inclusive) {
		if (! top.isUnbounded()) {
			BigDecimal bigTop = new BigDecimal(top.getFormatted());
			
			if (inclusive) {
				if (bigTop.compareTo(val) < 0) {
					return false;
				}
			} else {
				if (bigTop.compareTo(val) <= 0) {
					return false;
				}
			}
		}
		
		return true;
	}


	public boolean isNonDetect() {
		return nonDetect;
	}

	public Integer getValueCount() {
		return valueCount;
	}
	


}
