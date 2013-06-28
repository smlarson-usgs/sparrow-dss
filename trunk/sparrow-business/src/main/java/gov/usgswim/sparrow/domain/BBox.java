package gov.usgswim.sparrow.domain;

import org.apache.commons.lang.StringUtils;

/**
 * An immutable bounding box.
 * @author eeverman
 *
 */
public class BBox {
	private double leftLongBound;
	private double rightLongBound;
	private double upperLatBound;
	private double lowerLatBound;
	
	/**
	 * Constructor that does bounds checking.
	 * @param leftLongBound
	 * @param lowerLatBound
	 * @param rightLongBound
	 * @param upperLatBound
	 * @throws Exception
	 */
	public BBox(double leftLongBound, double lowerLatBound,
			double rightLongBound, double upperLatBound) throws Exception {
		
		this.leftLongBound = leftLongBound;
		this.lowerLatBound = lowerLatBound;
		this.rightLongBound = rightLongBound;
		this.upperLatBound = upperLatBound;
		
		
		if (! validateBounds(leftLongBound, lowerLatBound, rightLongBound, upperLatBound)) {
			throw new Exception("The left bound is greater than the right bound, or the lower bound is greater than the upper.");
		}
	}
	
	/**
	 * Constructor that does bounds checking.
	 * The passed bounds string is expected to be in this format:
	 * left,lower,right,upper
	 * Extra spaces are permitted.
	 * 
	 * @param boundsSring
	 * @throws Exception
	 */
	public BBox(String boundsSring) throws Exception {
		
		double[] bounds = splitBoundsString(boundsSring);
		
		this.leftLongBound = bounds[0];
		this.lowerLatBound = bounds[1];
		this.rightLongBound = bounds[2];
		this.upperLatBound = bounds[3];
		
		if (! validateBounds(leftLongBound, lowerLatBound, rightLongBound, upperLatBound)) {
			throw new Exception("The left bound is greater than the right bound, or the lower bound is greater than the upper.");
		}
	}
	
	/**
	 * Constructor that does bounds checking.
	 * The passed bounds array is expected to be in this format:
	 * [0] left, [1] lower, [2] right, [3] upper
	 * @param bounds
	 * @throws Exception
	 */
	public BBox(double[] bounds) throws Exception {
		this(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	
	/**
	 * Parses a bounds string into a bounds array.
	 * The passed bounds string is expected to be in this format:
	 * left,lower,right,upper
	 * Extra spaces are permitted.
	 * @param bounds
	 * @return
	 * @throws Exception
	 */
	public static double[] splitBoundsString(String bounds) throws Exception {
		String[] boundArray = bounds.split(",");
		
		if (boundArray.length != 4) {
			throw new Exception("The bounds string should be of the form 'left,lower,right,upper'.");
		}
		
		double[] boundDArray = new double[4];
		
		boundDArray[0] = Double.parseDouble(StringUtils.trimToNull(boundArray[0]));
		boundDArray[1] = Double.parseDouble(StringUtils.trimToNull(boundArray[1]));
		boundDArray[2] = Double.parseDouble(StringUtils.trimToNull(boundArray[2]));
		boundDArray[3] = Double.parseDouble(StringUtils.trimToNull(boundArray[3]));
		
		return boundDArray;
	}
	
	/**
	 * Validates the bounds to ensure that the left bound is less than the right bound
	 * and the lower bound is less than the upper bound.
	 * @param leftLongBound
	 * @param lowerLatBound
	 * @param rightLongBound
	 * @param upperLatBound
	 * @return
	 */
	public static boolean validateBounds(double leftLongBound, double lowerLatBound, double rightLongBound, double upperLatBound) {
		if (leftLongBound >= rightLongBound) return false;
		if (lowerLatBound >= upperLatBound) return false;
		return true;
	}

	/**
	 * @return the leftLongBound
	 */
	public double getLeftLongBound() {
		return leftLongBound;
	}

	/**
	 * @return the rightLongBound
	 */
	public double getRightLongBound() {
		return rightLongBound;
	}

	/**
	 * @return the upperLatBound
	 */
	public double getUpperLatBound() {
		return upperLatBound;
	}

	/**
	 * @return the lowerLatBound
	 */
	public double getLowerLatBound() {
		return lowerLatBound;
	}
}
