package gov.usgs.cida.sparrow.validation;

/**
 *
 * @author eeverman
 */
public class BasicComparator implements Comparator {
	
	
	private double allowedFractionalVarianceForValuesLessThan10 = 0d;
	private double allowedFractionalVarianceForValuesLessThan1K = 0d;
	private double allowedFractionalVarianceForValuesLessThan100K = 0d;
	private double allowedFractionalVariance = 0d;

	private double maxAbsVarianceForValuesLessThanOne = 0d;
	private double maxAbsVariance = 0d;

	public BasicComparator() {
		
		allowedFractionalVarianceForValuesLessThan10 = .0001d;
		allowedFractionalVarianceForValuesLessThan1K = .0001d;
		allowedFractionalVarianceForValuesLessThan100K = .0001d;
		allowedFractionalVariance = .0001d;
		
		//Max abs variation for values less than one
		maxAbsVarianceForValuesLessThanOne = .00001d;
		
		//Max abs value for all values (must be true for all other tests)
		maxAbsVariance = 100d;
	}
	
	public BasicComparator(double allowedFractionalVariance, 
			double allowedFractionalVarianceLessThan10,
			double allowedFractionalVarianceLessThan1K,
			double allowedFractionalVarianceLessThan100K,
			double maxAbsVarianceForLessThanOne, double maxAbsVariance) {
		
		this.allowedFractionalVariance = allowedFractionalVariance;
		this.allowedFractionalVarianceForValuesLessThan10 = allowedFractionalVarianceLessThan10;
		this.allowedFractionalVarianceForValuesLessThan1K = allowedFractionalVarianceLessThan1K;
		this.allowedFractionalVarianceForValuesLessThan100K = allowedFractionalVarianceLessThan100K;
		
		this.maxAbsVarianceForValuesLessThanOne = maxAbsVarianceForLessThanOne;
		this.maxAbsVariance = maxAbsVariance;
	}
	
	
	
	public double getAllowedFractionalVariance() {
		return allowedFractionalVariance;
	}

	public void setAllowedFractionalVariance(double allowedFractionalVariance) {
		this.allowedFractionalVariance = allowedFractionalVariance;
	}

	public double getAllowedFractionalVarianceForValuesLessThan10() {
		return allowedFractionalVarianceForValuesLessThan10;
	}

	public void setAllowedFractionalVarianceForValuesLessThan10(double allowedFractionalVarianceForValuesLessThan10) {
		this.allowedFractionalVarianceForValuesLessThan10 = allowedFractionalVarianceForValuesLessThan10;
	}

	public double getAllowedFractionalVarianceForValuesLessThan100K() {
		return allowedFractionalVarianceForValuesLessThan100K;
	}

	public void setAllowedFractionalVarianceForValuesLessThan100K(double allowedFractionalVarianceForValuesLessThan100K) {
		this.allowedFractionalVarianceForValuesLessThan100K = allowedFractionalVarianceForValuesLessThan100K;
	}

	public double getAllowedFractionalVarianceForValuesLessThan1K() {
		return allowedFractionalVarianceForValuesLessThan1K;
	}

	public void setAllowedFractionalVarianceForValuesLessThan1K(double allowedFractionalVarianceForValuesLessThan1K) {
		this.allowedFractionalVarianceForValuesLessThan1K = allowedFractionalVarianceForValuesLessThan1K;
	}

	public double getMaxAbsVariance() {
		return maxAbsVariance;
	}

	public void setMaxAbsVariance(double maxAbsVariance) {
		this.maxAbsVariance = maxAbsVariance;
	}

	public double getMaxAbsVarianceForValuesLessThanOne() {
		return maxAbsVarianceForValuesLessThanOne;
	}

	public void setMaxAbsVarianceForValuesLessThanOne(double maxAbsVarianceForValuesLessThanOne) {
		this.maxAbsVarianceForValuesLessThanOne = maxAbsVarianceForValuesLessThanOne;
	}

	
	
	
	/**
	 * Compares two values and returns true if they are considered equal.
	 * Note that only positive values are expected.  If a negative value
	 * is received for any value, false is returned.
	 * 
	 * For expected values less than zero:
	 * False is always returned.  No negative numbers are expected.
	 * 
	 * For expected values of 1 or less:
	 * The actual value must have an absolute difference of less than 
	 * maxAbsVarianceForLessThanOne OR a fractional difference of less than 
	 * allowedFractionalVarianceLessThanOneK.
	 * 
	 * For expected values of 1000 or less:
	 * The actual value must have an absolute difference of less than 
	 * maxAbsVariance AND a fractional difference of less than 
	 * allowedFractionalVarianceLessThanOneK.
	 * 
	 * For expected values of greater than 1000:
	 * The actual value must have an absolute difference of less than 
	 * maxAbsVariance AND a fractional difference of less than 
	 * allowedFractionalVariance.
	 * 
	 * 
	 * 
	 * @param expect  Expected Value
	 * @param actual Value to compare
	 * @return 
	 */
	public boolean comp(double expect, double actual) {
		
		if (expect < 0 || actual < 0) {
			return false;
		}
		
		double diff = Math.abs(actual - expect);
		double frac = 0;
		
		if (diff == 0) {
			return true;	//no further comparison required
		} else {
			frac = diff / expect;	//we are sure at this point that baseValue > 0
		}
		
		if (expect <= 1d) {
			return (diff <= maxAbsVarianceForValuesLessThanOne || frac < allowedFractionalVarianceForValuesLessThan1K);
		} else if (expect <= 10) {
			return (frac <= allowedFractionalVarianceForValuesLessThan10 && diff <= maxAbsVariance);
		} else if (expect <= 1000d) {
			return (frac <= allowedFractionalVarianceForValuesLessThan1K && diff <= maxAbsVariance);
		} else if (expect <= 100000d) {
			return (frac <= allowedFractionalVarianceForValuesLessThan100K && diff <= maxAbsVariance);
		} else {
			return (frac <= allowedFractionalVariance && diff <= maxAbsVariance);
		}

	}
}
