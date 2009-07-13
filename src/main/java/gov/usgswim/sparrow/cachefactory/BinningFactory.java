package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.utils.DataTableUtils;
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
				return getEqualCountBins(dc.getTable(), dc.getColumn(), request.getBinCount());
			case EQUAL_RANGE:
				return getEqualRangeBins(dc.getTable(), dc.getColumn(), request.getBinCount(), true);
		}

		return null;
	}

	/**
	 * Placeholder for original getEqualCountBins() method. TODO remove
	 * @param data
	 * @param columnIndex
	 * @param binCount
	 * @return
	 */
	protected BigDecimal[] getEqualCountBins(DataTable data, int columnIndex, int binCount) {
		boolean isKeepZeroes = false; // TODO accept as parameter
		Float[] sortedValues = extractSortedValues(data, columnIndex, binCount, isKeepZeroes); //sorted Array holding all values
		return getEqualCountBins(sortedValues, binCount, true); // TODO set to true for now. Should take parameter from BinningRequest
	}

	/**
	 * TODO put into DataTableUtils
	 * @param data
	 * @param columnIndex
	 * @param isKeepZeroes
	 * @return
	 */
	public static Float[] extractSortedValues(DataTable data, int columnIndex, int minValueCount, boolean isKeepZeroes) {
		int totalRows = data.getRowCount();

		boolean hasZero = false;
		List<Float> filteredValues = new ArrayList<Float>();

		for (int r=0; r<totalRows; r++) {
			float value = data.getFloat(r, columnIndex);
			if (!Float.isNaN(value) && !Float.isInfinite(value)) {
				if (value == 0F) {hasZero = true;}
				if (isKeepZeroes || value != 0F) {
					filteredValues.add(value);
				}
			}
		}
		if (!isKeepZeroes && hasZero) {
			filteredValues.add(0F); // This ensures that the reaches with 0 values are at least drawn. They will not be drawn if all the nonzero reach values are of the same sign
		}
		// Need to make sure we have at least enough values for bins
		while (filteredValues.size() <= minValueCount) {
			filteredValues.add(0F);
		}

		Float[] values = new Float[filteredValues.size()];
		values = filteredValues.toArray(values);
		Arrays.sort(values);
		return values;
	}

	public static final double ALLOWABLE_BIN_SIZE_VARIANCE_RATIO = 1d/10;
	public static BigDecimal[] getEqualCountBins(Float[] sortedData, int binCount, Boolean useRounding) {
		int totalRows = sortedData.length;	//Total rows of data

		if (useRounding == null || !useRounding) {
			return getEqualCountBins(binCount, sortedData);
		}
		// adjust the bin fence posts
		BigDecimal[] bins = new BigDecimal[binCount + 1];
		double binSize = (double)(totalRows) / (double)(binCount);
		double binVariance = ( binSize * ALLOWABLE_BIN_SIZE_VARIANCE_RATIO )/2;

		// lowest value edge bin needs special handling
		double value = sortedData[0];
		int hiIndex = (int) binVariance;
		if (hiIndex == 0) {
			hiIndex = 1; // if variance is too small, just use nearest value
		}
		double hi = sortedData[hiIndex];
		double lo = value - (hi - value);
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
		hi = value + (value - lo);
		bins[binCount] = round(value, value, hi); // must not allow the highest bin to be rounded down

		return bins;
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
	 *         TODO change name to getExactEqualCountBins
	 */
	public static BigDecimal[] getEqualCountBins(int binCount, Float[] sortedData) {

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
			float topVal = sortedData[(int) Math.ceil(split)];
			float bottomVal = sortedData[(int) Math.floor(split)];


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
		// round to zero as first option
		if (value ==0 || (lo <=0 && hi>=0)) return new BigDecimal(0);
		// round to 1 digit
		int exponent = Double.valueOf(Math.ceil(Math.log10(Math.abs(value)))).intValue();

		for (BigDecimal sigfigs: digitAccuracyMultipliers) {
			// debug
			// System.out.println("lo=" + lo + "; hi=" + hi + "; value=" + value);
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
	@SuppressWarnings("cast")
	public static BigDecimal[] getEqualRangeBins(DataTable data, int columnIndex, int binCount, boolean useRounding) {
		//int totalRows = data.getRowCount(); // Total rows of data

		// TODO: check that this is implemented and then uncomment. Currently unsupported operation exception. I think this is just out of date with the latest jar
		// Grab the min and max values from the DataTable
//		double minValue = data.getMinDouble(columnIndex);
//		double maxValue = data.getMaxDouble(columnIndex);
		double minValue = 0;
		double maxValue = 0;

		{
			float[] sortedValues = DataTableUtils.extractSortedValues(data, columnIndex, false);
			minValue = sortedValues[0];
			maxValue = sortedValues[sortedValues.length - 1];
		}
		// Calculate the width of a single bin
		double binWidth = (maxValue - minValue) / (double)(binCount);

		// We track the bins via their dividing values (imagine each bin being surrounded by two bin posts), thus
		// there is one more post than bins.  The first value is the minimum bin value
		BigDecimal[] binPosts = new BigDecimal[binCount + 1];
		BigDecimal bdBinWidth = null;

		if (useRounding) {
			double maxAllowableRangeExpansion = Math.min(.1, 1.0/(3.0*binCount)); // allow to expand by 10% or 1/3 of a bin, whichever is less
			bdBinWidth = round(binWidth, binWidth, (1 + maxAllowableRangeExpansion) * binWidth); // round up by at most maxAllowableRangeExpansion
			double totalRangeDiff = bdBinWidth.doubleValue() * binCount - (maxValue - minValue); // expansion in size of total range.
			BigDecimal binsMinValue = round(minValue, minValue - totalRangeDiff, minValue); // round down by at most diff
			//BigDecimal binsMaxValue = round(maxValue, maxValue, maxValue + totalRangeDiff); // round up by at most diff
			// TODO determine whether binsMinValue or binsMaxValue is a better
			// rounding value by comparing the candidates for the first and last
			// bin posts.

			binPosts[0] = binsMinValue; // Just using the low candidate

		} else {
			// exact, but we still need BigDecimals rounding for display
			int defaultPrecision = 6; // round to 6 digits by default
			bdBinWidth = new BigDecimal(binWidth, new MathContext(defaultPrecision, RoundingMode.CEILING));// round up to be used as BigDecimal
			//BigDecimal binsMinValue = new BigDecimal(minValue, new MathContext(defaultPrecision, RoundingMode.FLOOR)); // round down
			// Note, there is a slight possibility that the totalRangeDiff is
			// actually smaller than the difference between the "rounded"
			// minValues, so that as a consequence, the top of the bin posts
			// is actually less than the maxValue. But at 6 digit accuracy,
			// I just don't care.
		}

		// Calculate the breakpoints so that each bin is of equal width
		for (int i = 1; i <= binCount; i++) {
			binPosts[i] = binPosts[i-1].add(bdBinWidth);
		}

		return binPosts;
	}

}
