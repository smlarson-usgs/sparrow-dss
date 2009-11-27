package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * This factory class creates a binning array based on a request from EHCache.
 *
 * Binning is the process of creating bins for a set of data.  For instance,
 * this data:<br>
 * <code>1, 2, 2, 9, 20, 29</code><br>
 * could be broken into two bins containing three values each based on Equal Count as:
 * <li>bin 1:  1 to 2 (inclusive)
 * <li>bin 2:  2 to 29
 * Equal Range binning for two bins would result in:
 * <li>bin 1:  1 to 15
 * <li>bin 2:  15 to 29
 *
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 *
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 *
 * @author eeverman
 */
public class BinningFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(BinningFactory.class); //logging for this class

	/**
	 * For equal count bins, this specifies by what fraction of the ideal bin
	 * size the resulting bins may vary.
	 * TODO May need to take into account USGS guidelines on rounding http://phoenix.cr.usgs.gov/www/rounding.html
	 */
	public static final double ALLOWABLE_BIN_SIZE_VARIANCE_RATIO = 1d/10;

	public Object createEntry(Object binningRequest) throws Exception {
		BinningRequest request = (BinningRequest)binningRequest;
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(request.getContextID());

		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}

		PredictionContext.DataColumn dc = context.getDataColumn();

		// Determine type of binning to perform, calling the appropriate method
		switch(request.getBinType()) {
			case EQUAL_COUNT:
				return buildEqualCountBins(dc.getTable(), dc.getColumn(), request.getBinCount(), false);
			case EQUAL_RANGE:
				return getEqualRangeBins(dc.getTable(), dc.getColumn(), request.getBinCount(), false, true);
		}

		return null;
	}

	/**
	 * Builds equal count bins from the data in the specified column of the passed DataTable.
	 * @param data The DataTable containing the data to partion.
	 * @param columnIndex The column (zero based) of the dataTable containing the data or interest.
	 * @param binCount The number of bins to create.  Must be greater than zero.
	 * @return
	 */
	public static BigDecimal[] buildEqualCountBins(DataTable data, int columnIndex, int binCount, boolean keepZeroes) {

		if (binCount < 1) {
			throw new IllegalArgumentException("The binCount must be greater than zero.");
		}

		Double[] sortedValues = extractSortedValues(data, columnIndex, keepZeroes);
		return buildEqualCountBins(sortedValues, binCount, true);
	}


	/**
	 * Builds equal count bins for the passed data.
	 *
	 * The sortedData must not contain any NaN, +Infinity, or -Infinity values
	 * and must be sorted in ascending order.  Empty data is permitted.
	 *
	 * @param sortedData Ascending data with 'special' double values removed.
	 * @param binCount The number of bins to create.  Must be greater than zero.
	 * @param useRounding True if the bins should be rounded, false to use precise bins.
	 * @return
	 */
	public static BigDecimal[] buildEqualCountBins(Double[] sortedData, int binCount, Boolean useRounding) {

		if (binCount < 1) {
			throw new IllegalArgumentException("The binCount must be greater than zero.");
		}


		int totalRows = sortedData.length;	//Total rows of data

		//Number of unique values in data, maxing at at the number of bins.
		int uniqueValues = getUniqueValueCount(sortedData, binCount + 1);

		//Handle small numbers of distinct values as a special case
		if (uniqueValues <= binCount) {
			return buildEqualCountBinsSmallSet(sortedData, binCount, useRounding);
		}

		if (useRounding == null || !useRounding) {
			return getExactEqualCountBins(binCount, sortedData);
		}

		// adjust the bin fence posts
		BigDecimal[] bins = new BigDecimal[binCount + 1];
		double binSize = (double)(totalRows) / (double)(binCount);
		double binVariance = ( binSize * ALLOWABLE_BIN_SIZE_VARIANCE_RATIO )/2;

		// lowest value edge bin needs special handling
		double value = sortedData[0];
		int hiIndex = (int) binSize;	//was binVariance - why?
		if (hiIndex == 0) {
			hiIndex = 1; // if variance is too small, just use nearest value
		} else if (hiIndex == totalRows) {
			hiIndex--;	//Single bin, so 10 rows --> binSize of 10 --> index OoB.
		}
		double hi = sortedData[hiIndex];
		//set lower rounding bound to be one 'unit' lower.  Could result in -Infinity.
		double lo = cleanInfinity(value - (hi - value));
		bins[0] = round(value, lo, value); // must not allow the lowest bin to be rounded up

		for (int i=1; i<binCount; i++) {
			double valueIndex = i * binSize;
			value = ( sortedData[(int) Math.floor(valueIndex)] + sortedData[(int) Math.ceil(valueIndex)])/2; // take the average of the surrounding values
			hiIndex = (int) Math.floor((valueIndex + binVariance));
			int loIndex = (int) Math.ceil(valueIndex - binVariance);
			if (loIndex >= hiIndex) {
				// use the bounding elements if bins are small
				loIndex = (int) Math.floor(valueIndex);
				hiIndex = (int) Math.ceil(valueIndex);
			}
			if (loIndex == hiIndex) {
				// This only happens if valueIndex is an integer

				// use the elements one removed, but scale
				loIndex += -1;
				hiIndex += 1;
				double hiVal = value + binVariance * (sortedData[hiIndex] - value)/2;
				double loVal = value - binVariance * (value - sortedData[loIndex])/2;
				bins[i] = round(value, loVal, hiVal);
			} else {
				// allow middle bins to be tweaked slightly higher or lower
				bins[i] = round(value, sortedData[loIndex], sortedData[hiIndex]);
			}
		}

		// highest value edge bin needs special handling
		int lastIndex = sortedData.length - 1;
		value = sortedData[lastIndex]; // last element
		int loIndex = lastIndex - (int) binVariance;
		if (loIndex == lastIndex) {
			loIndex = lastIndex - 1; // if variance is too small, just use nearest value
		}
		lo = sortedData[loIndex];
		hi = cleanInfinity(value + (value - lo));
		bins[binCount] = round(value, value, hi); // must not allow the highest bin to be rounded down

		return bins;
	}

	/**
	 * Builds equal count bins for situations where the number of values is
	 * equal to or less than the number of bins.
	 *
	 * The values passed in sortedData must be free of NaN values and +/-
	 * Infinity.
	 *
	 * @param sortedData The dataset, which must be sorted and contain no NaN's or +/- Infinity
	 * @param binCount	The number of bins to construct
	 * @param useRounding Round the bin values.
	 * @return
	 */
	protected static BigDecimal[] buildEqualCountBinsSmallSet(
			Double[] sortedData, int binCount, Boolean useRounding) {

		BigDecimal[] bins = new BigDecimal[binCount + 1];

		//Default values incase we have no data
		double highValue = 0;
		double lowValue = 0;

		if (sortedData.length > 0) {
			highValue = sortedData[sortedData.length - 1];
			lowValue = sortedData[0];
		}


		if (highValue == 0 && lowValue == 0) {
			//CASE 1: All the values are zero.  Generate bins as:
			//0 to 1, 1 to 2, etc., for as many bins as requested
			for (int i=0; i<=binCount; i++) {
				bins[i] = new BigDecimal(i);
			}
		} else if (highValue == lowValue) {
			//Case 2: All the values are equal.  Generate bins as:
			//0 to ceil(highValue), ceil(highValue) to 2Xceil(highValue), etc.

			if (highValue > 0) {
				bins = getEqualRangeBins(0, highValue, binCount, useRounding);
			} else {
				bins = getEqualRangeBins(lowValue, 0, binCount, useRounding);
			}

		} else {
			//Case 3:  Values vary, but we have as many or more bins than values.
			//Generate bins as equal range
			bins = getEqualRangeBins(lowValue, highValue, binCount, useRounding);

		}

		return bins;
	}

	/**
	 * Reverses the passed array of BigDecimals.
	 *
	 * @param values
	 * @return
	 */
	public static BigDecimal[] reverseValues(BigDecimal[] values) {
		int lastBin = values.length - 1;
		BigDecimal[] rev = new BigDecimal[values.length];

		for (int i=0; i<values.length; i++ ) {
			rev[lastBin - i] = values[i];
		}

		return rev;
	}

	/**
	 * Returns an equal count set of bins so that the bins define break-point
	 * boundaries with approximately an equal number of values in each bin.
	 *
	 * @param data Table of data containing the column to divide into bins.
	 * @param columnIndex Index of the column to divide into bins.
	 * @param binCount Number of bins to divide the column into.
	 * @return Set of bins such that the bins define break-point boundaries
	 *         with an approximately equal number of values contained within.
	 */
	public static BigDecimal[] getExactEqualCountBins(int binCount, Double[] sortedData) {

		int totalRows = sortedData.length;	//Total rows of data

		//Number of rows 'contained' in each bin.  This likely will not come out even,
		//so use a double to preserve the fractional rows.
		double binSize = (double)(totalRows) / (double)(binCount);


		//The bins, where each value is a fence post w/ values between, thus, there is one more 'post' than bins.
		//The first value is the lowest value in values[], the last value is the largest value.
		BigDecimal[] bins = new BigDecimal[binCount + 1];

		//Assign first and last values for the bins (min and max)
		bins[0] = new BigDecimal(sortedData[0]);
		bins[binCount] = new BigDecimal(sortedData[totalRows - 1]);

		//Assign the middle breaks so that equal numbers of values fall into each bin
		for (int i=1; i<(binCount); i++) {

			//Get the row containing the nearest integer split
			double split = i * binSize;

			//The bin boundary is the value contained at that row.
			double topVal = sortedData[(int) Math.ceil(split)];
			double bottomVal = sortedData[(int) Math.floor(split)];


			if (topVal != bottomVal) {
				// take a rounded value inside of the surrounding range
				bins[i] = round(bottomVal, bottomVal, topVal);
			} else {
				bins[i] = new BigDecimal(bottomVal);
			}
		}

		return bins;
	}

	public static final BigDecimal[] digitAccuracyMultipliers= {
		new BigDecimal(new BigInteger("1"), 0), // 0 digit
		new BigDecimal(new BigInteger("2"), 0), // .5 digit
		new BigDecimal(new BigInteger("10"), 0), // 1 digit
		new BigDecimal(new BigInteger("20"), 0), // 1.5 digits
		new BigDecimal(new BigInteger("100"), 0), // 2 digits
		new BigDecimal(new BigInteger("200"), 0), // 2.5 digits
		new BigDecimal(new BigInteger("1000"), 0)}; // 3 digits

	/**
	 * Rounds to the fewest digits necessary (<= 3.5 digits) within the given lo-hi range
	 *
	 * @param value
	 * @param lo
	 * @param hi
	 * @return the original value if lo-hi range is too narrow
	 */
	public static BigDecimal round(double value, double lo, double hi) {


		if (value == lo && value == hi) {
			//No rounding possible
			return new BigDecimal(value);
		} else if (value ==0 || (lo <=0 && hi>=0)) {
			// round to zero as first option
			if (value ==0 || (lo <=0 && hi>=0)) return BigDecimal.ZERO;
		}

		// round to 1 digit
		int exponent = Double.valueOf(Math.ceil(Math.log10(Math.abs(value)))).intValue();

		for (BigDecimal sigfigs: digitAccuracyMultipliers) {
			BigDecimal result = findClosestInRange(value, lo, hi, exponent, sigfigs);
			if (result != null) {
				// don't return exponents of 3 or less
				if (-3 <= result.scale() && result.scale() < 0) {
					String rep = result.toPlainString();
					if (rep.length()<=4) {
						result = result.setScale(0); // just write it out rather than use exponential notation
					}
				}
				return result;
			}
		}

		return new BigDecimal(value); // range is too narrow so just return original value
	}

	/**
	 * Finds the fraction with the denominator which is within the lo-hi range
	 * and closest to the value
	 *
	 * TODO: This returns zero for the situation:
	 * value = max possible negative Double (one more than -Infinity)
	 * lo = -Infinity
	 * hi = value
	 *
	 * @param value
	 * @param lo
	 * @param hi
	 * @param denominator
	 * @return null if no fraction within range
	 */
	public static BigDecimal findClosestInRange(double value, double lo, double hi, int exponent, BigDecimal sigfigs) {
		assert(lo <= value): "lo " + lo + " should be <= value " + value;
		assert(value <= hi) : "lo " + lo + " should be < hi " + hi;
		double normalizer = sigfigs.doubleValue() / (Math.pow(10, exponent));
		value = value * normalizer;
		// expect Math.abs(value) >= 0, but is ok if it isn't
		lo = lo * normalizer;
		hi = hi * normalizer;

		double up = Math.ceil(value);
		double down = Math.floor(value);
		boolean isUpInRange = (up <= hi);
		boolean isDownInRange = (lo <= down);
		long upL = Double.valueOf(up).longValue();
		long downL = Double.valueOf(down).longValue();

		// to restore value, up /normalizer = up /sigfigs w/exp shift

		if (isUpInRange && isDownInRange) {
			// return the closest of two valid alternatives
			return (Math.abs(up - value) < Math.abs(down - value))?
					makeBigDecimal(upL, sigfigs, exponent):
					makeBigDecimal(downL, sigfigs, exponent);
		}
		if (isUpInRange) {
			return makeBigDecimal(upL, sigfigs, exponent);
		} else if (isDownInRange) {
			return makeBigDecimal(downL, sigfigs, exponent);
		}
		return null;
	}


	public static BigDecimal makeBigDecimal(long numerator, BigDecimal denominatorWithScale, int exp) {
		BigDecimal num = new BigDecimal(numerator, new MathContext(denominatorWithScale.scale()));
		BigDecimal temp = num.divide(denominatorWithScale);
		BigDecimal result = temp.scaleByPowerOfTen(exp);
		return result;
	}

	// ================
	// EQUAL RANGE BINS
	// ================

	/**
	 * Returns an equal range set of bins such that the bins define break-point
	 * boundaries whose values are approximately equally spaced apart.
	 *
	 * @param data Table of data containing the column to divide into bins.
	 * @param columnIndex Index of the column to divide into bins.
	 * @param binCount Number of bins to divide the column into.
	 * @return Set of bins such that the bins define break-point boundaries
	 *         whose values are approximately equally spaced apart.
	 */
	public static BigDecimal[] getEqualRangeBins(DataTable data, int columnIndex, int binCount, boolean keepZeroes, boolean useRounding) {

		//Find min and max values
		//

		//Initial values are inconsistent, but ensure any NAN value will update.
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MAX_VALUE * -1d;
		int totalRows = data.getRowCount();
		boolean minSet = false;	//true if minValue is touched
		boolean maxSet = false;	//true if maxValue is touched

		if (totalRows > 0) {

			for (int r=0; r < totalRows; r++) {
				Double value = data.getDouble(r, columnIndex);

				if (value == null || value.isNaN()) {
					//skip
				} else if (value.equals(Double.POSITIVE_INFINITY)) {
					maxValue = Double.MAX_VALUE;
					maxSet = true;
				} else if (value.equals(Double.NEGATIVE_INFINITY)) {
					minValue = Double.MAX_VALUE * -1d;
					minSet = true;
				} else {
					if (minValue > value) {
						minValue = value;
						minSet = true;
					} else if (maxValue < value) {
						maxValue = value;
						maxSet = true;
					}
				}
			}
		}

		//Three situations are possible:
		//zero non-NAN/null values --> no values being set
		//one non-NAN/null value --> only the min value being set
		//two or more non-NAN/null values --> min & max values being set (good)
		if ((! minSet) && (!maxSet)) {
			minValue = 0d;
			maxValue = 0d;
		} else if (! maxSet) {
			maxValue = minValue;
		}

		return getEqualRangeBins(minValue, maxValue, binCount, useRounding);
	}


	/**
	 * Returns an equal range set of bins such that the bins define break-point
	 * boundaries whose values are approximately equally spaced apart.
	 *
	 * @param data Table of data containing the column to divide into bins.
	 * @param columnIndex Index of the column to divide into bins.
	 * @param binCount Number of bins to divide the column into.
	 * @return Set of bins such that the bins define break-point boundaries
	 *         whose values are approximately equally spaced apart.
	 */
	@SuppressWarnings("cast")
	public static BigDecimal[] getEqualRangeBins(double minValue, double maxValue, int binCount, boolean useRounding) {
		//int totalRows = data.getRowCount(); // Total rows of data

		/**
		 * Based on the scale of the value range (maxValue - minValue),
		 * this is the number of digits to the right of the ranges most significant
		 * digit that should be rounded to.  Cannot be < 0.
		 * Example:
		 * The range (max - min) = 1,234,567.890123
		 * A relativeScale of 2 means that we should round to the 10,000 place
		 * (two digits to the right from the '1').
		 */
		int relativeScale = 2;

		/**
		 * Add to the relativeScale and follow the same calculation to determine
		 * what digit should be considered so small that we would not consider
		 * it in rounding.  Cannot be < 0.
		 */
		int relativeSmallValueScale = 5;

		BigDecimal adjustedMinValue = null;
		BigDecimal adjustedMaxValue = null;



		// Calculate the width of a single bin
		BigDecimal binWidth = null;

		if (useRounding) {

			double range = Math.abs(maxValue - minValue);
			int scale = 0;	//The most significant digit's scale (1000 == 3, .001 == -3)

			if (range != 0) {
				scale = (int) Math.log10(range);
			}

			scale = scale - relativeScale;


			adjustedMinValue = roundToScale(new BigDecimal(minValue), scale, relativeSmallValueScale, RoundingMode.FLOOR);
			adjustedMaxValue = roundToScale(new BigDecimal(maxValue), scale, relativeSmallValueScale, RoundingMode.CEILING);

			//Precisely calc binWidth (34 digits of precision) - round later
			binWidth = adjustedMaxValue.subtract(adjustedMinValue).divide(new BigDecimal(binCount), MathContext.DECIMAL128);

			//round the binWidth, since it could be an odd decimal
			binWidth = roundToScale(binWidth, scale, relativeSmallValueScale, RoundingMode.UP);
		} else {
			binWidth = new BigDecimal((maxValue - minValue) / (double)(binCount));
			adjustedMinValue = new BigDecimal(minValue);
			adjustedMaxValue = new BigDecimal(maxValue);
		}

		// We track the bins via their dividing values (imagine each bin being surrounded by two bin posts), thus
		// there is one more post than bins.  The first value is the minimum bin value
		BigDecimal[] binPosts = new BigDecimal[binCount + 1];
		binPosts[0] = adjustedMinValue; // Just using the low candidate

		// Calculate the breakpoints so that each bin is of equal width
		for (int i = 1; i <= binCount; i++) {
			binPosts[i] = binPosts[i-1].add(binWidth);
		}

		return binPosts;
	}

	/**
	 * Calculates the number of unique values in a set of sorted numbers.
	 * If maxCount is greater than zero, it will 'give up' counting when it
	 * reaches that max count and will return the maxValue.  For instance,
	 * if maxCount is 5, as soon as five unique values are found, 5 is returned.
	 *
	 * @param sortedData The data to inspect, sorted low to high.
	 * @param maxCount The number of values to stop counting at.
	 * 		Set 0 or less to disable.
	 * @return The number of unique values or maxCount if reached.
	 */
	protected static int getUniqueValueCount(Double[] sortedData, int maxCount) {


		if (sortedData.length == 0) {
			//Special case of no values
			return 0;
		} else if (sortedData.length == 1) {
			return 1;
		}

		if (maxCount < 1) {
			//force maxCount large enough to be ignored
			maxCount = sortedData.length + 1;
		}

		int i = 1;		//index in sortedData, start at 2nd value
		int cnt = 1;	//count of unique values (1st value is unique)
		double last = sortedData[0];	//last value was 1st value

		while (i < sortedData.length && cnt < maxCount) {
			if (last != sortedData[i]) {
				cnt++;
				last = sortedData[i];
			}
			i++;
		}

		return cnt;
	}


	/**
	 * Does a simple round to the nearest increment of the specified scale.
	 *
	 * For instance, a roundToScale of 3 (thousands place) would round to the
	 * nearest thousand.  The roundMode specifies rounding up (away from zero)
	 * or down (towards zero).  Other RoundingModes are not supported.
	 *
	 * reallySmallRelativeScale allows rounding to a 'bigger' value to be
	 * ignored for very small overages. For instance, if rounding 2000.000001
	 * UP to the nearest thousand, the result would nominally be 3000.  In this
	 * case it may be preferable to not round up, rounding instead to 2000.
	 *
	 * reallySmallRelativeScale is relative to roundToScale.  If roundToScale
	 * is 3 (nearest thousand), a reallySmallRelativeScale of two would mean
	 * that the digit place two to the right (tens) are considered 'really
	 * small'.  Continuing this UP rounding example (rTS = 3, rSRS = 2), 1011
	 * rounds to 2000, 1010 rounds to 1000, 1010.00001 rounds to 2000.
	 *
	 * @param value
	 * @param roundToScale
	 * @param reallySmallRelativeScale
	 * @param roundMode  RoundingModes UP, DOWN, CEILING, and FLOOR are supported.
	 * @return
	 */
	public static BigDecimal roundToScale(BigDecimal value, int roundToScale, int reallySmallRelativeScale, RoundingMode roundMode) {
		//Handle values that are 'really close' to an interval of the roundToScale
		//eg: scale is 3, reallySmallThresholdScale is 4, 1000.00001 --> 1000.

		if (reallySmallRelativeScale < 0) {
			throw new IllegalArgumentException("The reallySmallRelativeScale value cannot be less than 1.");
		}

		//The value to round by (i.e., nearest '1000') with the same sign as the value.
		BigDecimal roundingUnitValue = BigDecimal.ONE.scaleByPowerOfTen(roundToScale);

		//copy value sign to roundingUnitValue.  If value is zero, leave positive.
		if (value.compareTo(BigDecimal.ZERO) != 0) {
			roundingUnitValue = roundingUnitValue.multiply(new BigDecimal(value.signum()));
		}


		BigDecimal reallySmallThreshold = BigDecimal.ONE.scaleByPowerOfTen(roundToScale - reallySmallRelativeScale);
		BigDecimal invertSmallThreshold = roundingUnitValue.abs().subtract(reallySmallThreshold);
		BigDecimal roundRemainder = value.remainder(roundingUnitValue);	//negative if val is negative
		BigDecimal absRemainder = roundRemainder.abs();
		boolean isPositive = (value.compareTo(BigDecimal.ZERO) == 1);
		BigDecimal truncatedValue =
			value.divideToIntegralValue(roundingUnitValue).multiply(roundingUnitValue);
		BigDecimal roundedValue = null;

		switch (roundMode) {
			case CEILING: {
				//Rounding mode to round towards positive infinity.

				//Pos vals go up unless *just* over the last round interval
				//Neg vals round toward zero unless *almost* to next round interval (-.9999 --> -1)

				//(roundRemainder is Neg for neg values)
				if (roundRemainder.compareTo(reallySmallThreshold) == 1) {
					//roundRemainder is greater than reallySmallThreshold (happens for pos vals only)
					roundedValue = truncatedValue.add(roundingUnitValue);
				} else if (!isPositive && absRemainder.compareTo(invertSmallThreshold) == 1) {
					//roundRemainder is greater than reallySmallThreshold (happens for neg vals only)
					roundedValue = truncatedValue.add(roundingUnitValue);	//adding a neg value
				} else {
					roundedValue = truncatedValue;
				}
			}
			break;
			case DOWN: {
				//Rounding mode to round towards zero.
				//Normally this would be the trunkated value, however,
				//if the value is *really close* to the next step, round UP.

				if (absRemainder.compareTo(invertSmallThreshold) == 1) {
					//For neg numbers, we are adding a neg number
					roundedValue = truncatedValue.add(roundingUnitValue);
				} else {
					roundedValue = truncatedValue;
				}
			}
			break;
			case FLOOR: {
				//Rounding mode to round towards negative infinity.

				//Pos vals truncate unless *almost* to the next round interval (1.9999 --> 2)
				//Neg vals round away from zero unless *just* over that last round interval

				//(roundRemainder is Neg for neg values)
				if (roundRemainder.compareTo(invertSmallThreshold) == 1) {
					//roundRemainder is greater than invertSmallThreshold (happens for pos vals only)
					roundedValue = truncatedValue.add(roundingUnitValue);
				} else if (!isPositive && absRemainder.compareTo(reallySmallThreshold) == 1) {
					//absRemainder is greater than reallySmallThreshold (happens for neg vals only)
					roundedValue = truncatedValue.add(roundingUnitValue);	//adding a neg value
				} else {
					roundedValue = truncatedValue;
				}
			}
			break;
			case HALF_DOWN: {
				//Rounding mode to round towards "nearest neighbor" unless both
				//neighbors are equidistant, in which case round down.
				//Neg values are rounded away from zero, Pos values are trunkated.

				//Untested, so unsupported.
				throw new UnsupportedOperationException("The HALF_DOWN rounding option is not supported.");


				//Equadistant or close to trunkated number so round down (trunkate)
//				if (absRemainder.compareTo(fiveXSmallThreshold) < 1) {
//					roundedValue = trunkatedValue;
//				} else {
//					//For neg numbers, we are adding a neg number
//					roundedValue = trunkatedValue.add(roundingUnitValue);
//				}
			}
			//break;	//unreachable
			case HALF_EVEN: {
				//Rounding mode to round towards the "nearest neighbor" unless
				//both neighbors are equidistant, in which case,
				//round towards the even neighbor.
				throw new UnsupportedOperationException("The HALF_EVEN rounding option is not supported.");
			}
			//break; //unreachable
			case HALF_UP: {
				//Rounding mode to round towards "nearest neighbor" unless both
				//neighbors are equidistant, in which case round up.
				//Neg values are rounded away from zero, Pos values are trunkated.

				//Untested, so unsupported.
				throw new UnsupportedOperationException("The HALF_DOWN rounding option is not supported.");


				//closeR to trunkated number so round down (trunkate)
//				if (absRemainder.compareTo(fiveXSmallThreshold) == -1) {
//					roundedValue = trunkatedValue;
//				} else {
//					//For neg numbers, we are adding a neg number
//					roundedValue = trunkatedValue.add(roundingUnitValue);
//				}
			}
			//break; //unreachable
			case UNNECESSARY: {
				throw new UnsupportedOperationException("The UNNECESSARY rounding option is not supported.");
			}
			//break; //unreachable
			case UP : {
				//Rounding mode to round away from zero.
				if (absRemainder.compareTo(reallySmallThreshold) == 1) {
					//For neg numbers, we are adding a neg number
					roundedValue = truncatedValue.add(roundingUnitValue);
				} else {
					roundedValue = truncatedValue;
				}
			}
			break;
			default: {
				throw new IllegalArgumentException("Unreacognized RoundingMode");
			}

		}

		if (roundedValue.compareTo(BigDecimal.ZERO) == 0) {
			//Bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539
			//results in trailing zeros not being trimmed from '0.000000'.
			return BigDecimal.ZERO;
		} else {
			return roundedValue.stripTrailingZeros();
		}
	}

	/**
	 * Extracts a column from a DataTable as a sorted (ascending) double[].
	 *
	 * NaN values are skipped, so the resulting array may have fewer rows than
	 * the DataTable. Positive and negative infinity are converted to the
	 * largest and smallest double values, respectively.
	 *
	 * TODO: put into DataTableUtils
	 *
	 * @param data
	 *            The source DataTable
	 * @param columnIndex
	 *            The column to extract data from
	 * @return sorted data, 'cleaned' of NANs and +/- infinity.
	 *
	 *         TODO This incorrectly replaced the other version of
	 *         extractSortedValues() without keeping the capability to remove
	 *         zeroes. Must reexamine
	 */
//	public static double[] extractSortedValues(DataTable data, int columnIndex) {
//		int totalRows = data.getRowCount();
//
//		int skippedValueCount = 0;	//Number of values skipped b/c they are Not a Number
//		int addedValueCount = 0;	//Number of values added
//
//		//Initialize to at least a size of 1
//		double[] values = new double[(totalRows > 0)?totalRows:1];
//
//		for (int r=0; r<totalRows; r++) {
//			Double value = data.getDouble(r, columnIndex);
//			if (value.isNaN()) {
//				skippedValueCount++;
//			} else if (value.equals(Double.POSITIVE_INFINITY)) {
//				values[addedValueCount] = Double.MAX_VALUE;
//				addedValueCount++;
//			} else if (value.equals(Double.NEGATIVE_INFINITY)) {
//				values[addedValueCount] = Double.MAX_VALUE * -1d;
//				addedValueCount++;
//			} else {
//				values[addedValueCount] = value;
//				addedValueCount++;
//			}
//		}
//
//		//If no values were added (all were NAN), add one 'fake' zero value.
//		if (addedValueCount == 0) {
//			values[0] = 0d;
//			addedValueCount = 1;
//		}
//
//		double[] trimmedValues = null;
//
//		if (skippedValueCount > 0) {
//			trimmedValues = Arrays.copyOfRange(values, 0, addedValueCount);
//		} else {
//			trimmedValues = values;
//		}
//
//		Arrays.sort(trimmedValues);
//
//		return trimmedValues;
//	}

	/**
	 * TODO put into DataTableUtils
	 * @param data
	 * @param columnIndex
	 * @param isKeepZeroes
	 * @return
	 */
	public static Double[] extractSortedValues(DataTable data, int columnIndex, boolean isKeepZeroes) {
		int totalRows = data.getRowCount();

		boolean hasZero = false;
		List<Double> filteredValues = new ArrayList<Double>();

		for (int r=0; r<totalRows; r++) {
			Double value = data.getDouble(r, columnIndex);

			if (value != null) {
				if (!Double.isNaN(value) && !Double.isInfinite(value)) {
					if (value == 0F) {hasZero = true;}
					if (isKeepZeroes || value != 0D) {
						filteredValues.add(value);
					}
				}
			} /* else {	}
			 Nulls can be safely skipped - they contain no data.
			*/
		}
		if (!isKeepZeroes && hasZero) {
			filteredValues.add(0D); // This ensures that the reaches with 0 values are at least drawn. They will not be drawn if all the nonzero reach values are of the same sign
		}

		Double[] values = new Double[filteredValues.size()];
		values = filteredValues.toArray(values);
		Arrays.sort(values);
		return values;
	}

	/**
	 * Cleans infinite values from doubles, replacing them with the max value
	 * with the appropriate sign.  Values which are NaN are returned as NaN.
	 *
	 * @param value The value to have infinites removed.
	 * @return The cleaned double.
	 */
	public static double cleanInfinity(Double value) {
		if (value.equals(Double.POSITIVE_INFINITY)) {
			return Double.MAX_VALUE;
		} else if (value.equals(Double.NEGATIVE_INFINITY)) {
			return Double.MAX_VALUE * -1d;
		} else {
			//Return normal values and NaNs
			return value;
		}
	}


}
