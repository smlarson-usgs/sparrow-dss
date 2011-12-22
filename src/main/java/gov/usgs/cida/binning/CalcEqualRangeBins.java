package gov.usgs.cida.binning;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgs.cida.binning.domain.InProcessBinSet;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.DeliveryFractionMap;

public class CalcEqualRangeBins extends Action<BinSet> {

	private SparrowColumnSpecifier dataColumn;
	private BigDecimal minValue;
	private BigDecimal maxValue;
	private int binCount;
	private BigDecimal detectionLimit;
	private Integer maxDecimalPlaces;
	private boolean bottomUnbounded;
	private boolean topUnbounded;
	
	/** A hash of row numbers that are in the reaches to be mapped. **/
	private DeliveryFractionMap inclusionMap;
	
	@Override
	public BinSet doAction() throws Exception {
		
		if (minValue == null || maxValue == null) {
			Double min = findMinDouble(dataColumn, inclusionMap);
			Double max = findMaxDouble(dataColumn, inclusionMap);
			
			//Filter out possible exceptional values
			if (min == null || max == null || min.isNaN() || max.isNaN()) {
				min = 0d;
				max = 0d;
			} else {
				if (min.isInfinite()) min = Double.MIN_VALUE * -1d;
				if (max.isInfinite()) max = Double.MAX_VALUE;
			}
			
			minValue = new BigDecimal(min).stripTrailingZeros();
			maxValue = new BigDecimal(max).stripTrailingZeros();
		}


		
		BinSet binSet = getEqualRangeBins(
				minValue, maxValue, binCount, detectionLimit, maxDecimalPlaces);
		
		return binSet;

	}
	
	/**
	 * This method basically does some preprocessing to get from the variables
	 * in request to the actual parameters needed to generate equal range bins.
	 * 
	 * Much/all of the pass variables are available as part of the binning request,
	 * however, splitting it out makes the method a bit more testable.
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param binCount
	 * @param detectionLimit
	 * @param maxDecimalPlaces
	 * @param binType
	 * @return
	 */
	protected BinSet getEqualRangeBins(
			BigDecimal minValue, BigDecimal maxValue, int binCount,
			BigDecimal detectionLimit, Integer maxDecimalPlaces) {
		
		BigDecimal cuv = getCharacteristicUnitValue(
				minValue, maxValue, binCount,
				maxDecimalPlaces);
		
			
		InProcessBinSet ipbs = getEqualRangeBins(
				minValue, maxValue, binCount,
				detectionLimit, cuv);
		
		ipbs.functional = createFunctionalPosts(ipbs.posts, 
				ipbs.actualMin, ipbs.actualMax,
				ipbs.characteristicUnitValue); 
		
		//Min and max values used to determine formatting.
		//Adjusted if the extreme top & bottom values are not visible b/c of
		//unlimited top/bottom bounds.
		BigDecimal formatMin = minValue;
		BigDecimal formatMax = maxValue;
		
		if (bottomUnbounded) formatMin = ipbs.posts[1];
		if (topUnbounded) formatMax = ipbs.posts[ipbs.posts.length - 2];
		
		
		DecimalFormat formatter = getFormat(formatMin, formatMax, getScaleOfMostSignificantDigit(cuv), false);
		DecimalFormat functionalFormatter = getFormat(formatMin, formatMax, getScaleOfMostSignificantDigit(cuv), true);
		String formattedNonDetectLimit = "";
		
		if (ipbs.usesDetectionLimit) {
			DecimalFormat ndFormat = getFormat(detectionLimit, detectionLimit, getScaleOfMostSignificantDigit(detectionLimit), false);
			formattedNonDetectLimit = ndFormat.format(detectionLimit);
		}
		
		BinSet binSet = BinSet.createBins(ipbs, formatter, functionalFormatter,
				bottomUnbounded, topUnbounded, formattedNonDetectLimit, BinType.EQUAL_RANGE);
		
		return binSet;
	}
	


	/**
	 * Builds equal range bins using the characteristUnitValue.
	 * 
	 * Requirements<ul>
	 * <li>maxValue must be greater or equal to minValue.
	 * <li>detect limit should only be used for non-comparisons who's values
	 * are positive.  Other situation may result in unexpected adjustments being
	 * made to the bin extremes.
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param binCount
	 * @param detectionLimit
	 * @param characteristicUnitValue
	 * @return
	 */
	protected InProcessBinSet getEqualRangeBins(
			BigDecimal minValue, BigDecimal maxValue, int binCount,
			BigDecimal detectionLimit, BigDecimal characteristicUnitValue) {
		
		InProcessBinSet processBinSet = new InProcessBinSet();	//result
		processBinSet.requestedBinCount = binCount;
		
		BigDecimal cuv = characteristicUnitValue.stripTrailingZeros();	//shorthand & we need true scale
		processBinSet.characteristicUnitValue = cuv;
		
		//Clean
		minValue = minValue.stripTrailingZeros();
		maxValue = maxValue.stripTrailingZeros();
		
		processBinSet.actualMin = minValue;
		processBinSet.actualMax = maxValue;
		
		//Extreme values are the min and max values that are bumped out (away
		//from each other) to the nearest CUV increment.  If that results in
		//a extreme min below the detection limit, it is forced back to the
		//detection limit.  The 'WODetectLimit' min value does not include the
		//detection limit adjustement.
		BigDecimal extremeMinValue = minValue.stripTrailingZeros();
		BigDecimal extremeMinValueWODetectLimit = null;
		BigDecimal extremeMaxValue = maxValue.stripTrailingZeros();
		BigDecimal extremeRange = null;	//Distance b/t the two
		
		//The amount by which the extreme range can be stretch outward or
		//inward to create the 'nice' min/max set of numbers.
		BigDecimal allowedOutwardRangeStretch = null;
		BigDecimal allowedInwardRangeStretch = null;
		
		//These 'nice' values may be bumped outward or inward to find
		//nicer (read more like a whole number) limit values.  From these values
		//the niceRange is created (simply max-min).  The niceRange is divided
		//into bins and used for the middle post values.  The outermost may
		//not use these nicer values, depending if they are inside or
		//outside the extreme bounds and/or detect limit.
		BigDecimal niceMinValue = null;
		BigDecimal niceMaxValue = null;
		BigDecimal niceRange = null;
		
		//Calculated posts.  An additional bottom post may be added if the
		//detection limit is in effect.
		BigDecimal[] posts = new BigDecimal[binCount + 1];
		
		
		//Bump the extremes out (away from each other) to the nearest CUV
		extremeMinValue = roundToCUVBelow(extremeMinValue, cuv);
		extremeMinValueWODetectLimit = extremeMinValue;
		extremeMaxValue = roundToCUVAbove(extremeMaxValue, cuv);
		if (detectionLimit != null && extremeMinValue.compareTo(detectionLimit) < 0) {
			extremeMinValue = detectionLimit;
			processBinSet.usesDetectionLimit = true;
		}
		if (detectionLimit != null && extremeMaxValue.compareTo(detectionLimit) < 0) {
			//Should not happen, but who knows?
			extremeMaxValue = detectionLimit;
		}
		
		//Calc full range
		extremeRange = extremeMaxValue.subtract(extremeMinValue);
		
		//Its reasonable to stretch a bit to find a 'nicer' value.
		//This defines how much in each direction from the extremes
		allowedOutwardRangeStretch = getAllowedOutwardRangeStretch(extremeRange, binCount);
		allowedInwardRangeStretch = getAllowedInwardRangeStretch(extremeRange, binCount);
		
		//Set our 'nice' values	
		niceMinValue = roundToNiceIfPossible(
				extremeMinValue,
				extremeMinValue.subtract(allowedOutwardRangeStretch),
				extremeMinValue.add(allowedInwardRangeStretch),
				getScaleOfMostSignificantDigit(cuv));

		niceMaxValue = roundToNiceIfPossible(
				extremeMaxValue,
				extremeMaxValue.subtract(allowedInwardRangeStretch),
				extremeMaxValue.add(allowedOutwardRangeStretch),
				getScaleOfMostSignificantDigit(cuv));
		
		//Total range, using nice values
		niceRange = niceMaxValue.subtract(niceMinValue).stripTrailingZeros();
		
		//IN the case that range is zero (all one value), we can force it to
		//be the CUV, which will result in bins increasing from the min value
		//in CUV increments
		if (niceRange.compareTo(BigDecimal.ZERO) == 0) {
			niceRange = cuv;
		}
		
		
		BigDecimal binWidth = findNiceBinWidth(niceRange, binCount, cuv);

		//Set all the posts except the first and last
		for (int i = 0; i < posts.length; i++) {
			if (i == 0) {
				//Bottom bin
				if (processBinSet.usesDetectionLimit) {
					posts[0] = detectionLimit;
				} else {
					//OK to use the nice value if it is lower than the extreme value
					posts[0] = extremeMinValue.min(niceMinValue);
				}
			} else if (i == (posts.length - 1)) {
				//set topmost post
				//ordered b/f i==1 b/c if there is exactly 1 bin, the top must be set.
				posts[i] = extremeMaxValue.max(niceMaxValue);
				
				if (posts[i].compareTo(posts[i-1]) <= 0) {
					//Only happens in cases where all values are under the detect limit
					posts[i] = posts[i] = posts[i - 1].add(binWidth);
				}
			} else if (i == 1) {
				//First bin above the bottom
				//Offset from the 'nice' value, not the actual btm value.
				posts[i] = niceMinValue.add(binWidth);
			} else {
				//All middle posts
				posts[i] = posts[i - 1].add(binWidth);
			}
		}
		
		//slip in a extreme bottom post if we are using the detection limit
		if (processBinSet.usesDetectionLimit) {
			BigDecimal[] postsWithBtm = new BigDecimal[posts.length + 1];
			postsWithBtm[0] = extremeMinValueWODetectLimit;
			System.arraycopy(posts, 0, postsWithBtm, 1, posts.length);
			posts = postsWithBtm;
		}
		
		processBinSet.posts = posts;
		return processBinSet;
	}
	
	/**
	 * Finds an appropriate bin width based on the range and bin count, rounding
	 * up or down to the nearest CUV based on which option comes out closest to
	 * matching the range when multiplied by the number of bins.
	 * 
	 * @param range
	 * @param binCount
	 * @param characteristicUnitValue
	 * @return
	 */
	protected static BigDecimal findNiceBinWidth(BigDecimal range, int binCount, BigDecimal characteristicUnitValue) {
		BigDecimal binWidth = divide(range, binCount);
		BigDecimal bdBinCount = new BigDecimal(binCount);
		
		//Round up to the next cuv
		BigDecimal rndUpBinWidth = roundToCUVAbove(binWidth, characteristicUnitValue);
		BigDecimal rndDownBinWidth = roundToCUVBelow(binWidth, characteristicUnitValue);
		
		BigDecimal rndUpTopError = rndUpBinWidth.multiply(bdBinCount).subtract(range).abs();
		BigDecimal rndDownTopError = rndDownBinWidth.multiply(bdBinCount).subtract(range).abs();
		
		if (rndUpTopError.compareTo(rndDownTopError) < 0) {
			return rndUpBinWidth.stripTrailingZeros();
		} else {
			if (rndDownBinWidth.compareTo(BigDecimal.ZERO) > 0) {
				return rndDownBinWidth.stripTrailingZeros();
			} else {
				//Don't allow a zero width to be returned
				return characteristicUnitValue;
			}
		}
	}
	
	/**
	 * The allowed amount to stretch the top or bottom end of the overall
	 * bin range in order to reach 'nicer' outward extreme values.
	 * 
	 * This is currently defined as the ideal bin width (range / binCount) X 2/3,
	 * rounded up to 16 decimal places.
	 * 
	 * @param range The range is expected to be positive.
	 * @return
	 */
	protected static BigDecimal getAllowedOutwardRangeStretch(BigDecimal range, int binCount) {
		BigDecimal fractionTop = range.multiply(new BigDecimal("2"));
		int fractionBtm = 3 * binCount;
		
		BigDecimal allowedRangeStretch = fractionTop.divide(
				new BigDecimal(fractionBtm),
				new MathContext(16, RoundingMode.UP));

		return allowedRangeStretch;
	}
	
	/**
	 * The allowed amount to stretch the top or bottom end of the overall
	 * bin range in order to reach 'nicer' inward extreme values.
	 * 
	 * This is currently defined as the ideal bin width (range / binCount) X 1/2,
	 * rounded up.
	 * 
	 * @param range The range is expected to be positive.
	 * @return
	 */
	protected static BigDecimal getAllowedInwardRangeStretch(BigDecimal range, int binCount) {
		
		BigDecimal allowedRangeStretch = range.divide(
				new BigDecimal(2 * binCount),
				new MathContext(16, RoundingMode.UP));

		return allowedRangeStretch;
	}
	
	/**
	 * The characteristic unit value (CUV) is a value that represents a
	 * 'reasonable' interval on which to base the bins and by which to bump
	 * bounding values up or down during the binning process.
	 * 
	 * The CUV is always a positive, non-zero '1' times ten to some power.
	 * For example:  100, .01, or .000001.
	 * The CUV's BigDecimal representation is always stripped of trailing zeros.
	 * 
	 * The CUV is calculated as 1% of the range of an idealize bin, rounded towards
	 * the floor of the nearest '1'.  For example:
	 * For a total range of 3 (max - min) and 5 bins:
	 * 3/5 = .6 (idealized bin range)
	 * 1% of .6 = .006
	 * Convert to .001 (round to nearest 1 floor).
	 * 
	 * If the decimalPlaces param is non-null, this is the number of decimal
	 * places the caller wants to use.  So, 2 would result in a unit value
	 * of .01.  However, the LARGER of the calculated CUV and the one
	 * specified by the decimal places will be used.
	 * 
	 * If range is zero and decimalPlaces is null, the '1 floor' of 
	 * 		.01 X (maxValue/binCount) is returned.
	 * If range is zero and decimalPlaces is null and the maxValue is zero, .01 is returned.
	 * 
	 * The maxValue must be greater than or equal to the minValue
	 * 
	 * @param minValue Actual max value of the values to be binned
	 * @param maxValue Actual min value of the values to be binned
	 * @param binCount The number of bins that will be generated
	 * @param decimalPlaces Non-null to indicate the caller has a specific
	 * 	decimal place they want to use.  >0 == places behind the decimal.  <0 == left of decimal.
	 *  Same as BigDecimal scale concept.
	 * @return
	 */
	public static BigDecimal getCharacteristicUnitValue(
			BigDecimal minValue, BigDecimal maxValue,
			int binCount, Integer decimalPlaces) {
		
		
		BigDecimal range = maxValue.subtract(minValue);
		BigDecimal perfectBinSize = divide(range, binCount);
		BigDecimal forcedCUV = null;
		
		if (decimalPlaces != null) {
			forcedCUV = oneTimesTenToThePower(-1 * decimalPlaces);
		}
		
		//Intercept condition where range == 0
		if (range.compareTo(BigDecimal.ZERO) == 0) {
			if (forcedCUV != null) {
				return forcedCUV;
			} else {
				if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
					//all values are zero
					return oneTimesTenToThePower(-2);
				} else {
					//Use the max value as the total range & fall through
					perfectBinSize = divide(maxValue, binCount);
				}
			}
		}
		
		
		//TODO:  The CUV should probably be rounded up in cases like 999.0 -->
		//CUV of 1% of 1000, not 100.  Not a big deal, since it just means
		//the values are a bit more precise.
		//1% of the average bin size seems reasonable....
		BigDecimal unroundedCUV = perfectBinSize.divide(new BigDecimal(100)).stripTrailingZeros();
		BigDecimal calcedCUV = oneTimesTenToThePower(-1 * getScaleOfMostSignificantDigit(unroundedCUV));
		
		if (decimalPlaces == null) {
			return calcedCUV;
		} else {
			//return the larger of the two
			if (calcedCUV.compareTo(forcedCUV) > 0) {
				return calcedCUV;
			} else {
				return forcedCUV;
			}
		}
	}
	
	/**
	 * Builds a '1' times ten to the specified power with any trailing zeros stripped.
	 * 
	 * Examples:<ul>
	 * <li>0 == 1
	 * <li>-1 == .1
	 * <li>3 = 1000
	 * </ul>
	 * 
	 * @param scale
	 * @return
	 */
	protected static BigDecimal oneTimesTenToThePower(int power) {
		if (power == 0) {
			return BigDecimal.ONE.stripTrailingZeros();
		} else if (power < 0) {
			power = power * -1;
			String numStr;
			numStr = "." + StringUtils.repeat("0", power - 1);
			numStr = numStr + "1";
			return new BigDecimal(numStr).stripTrailingZeros();
		} else {
			String numStr;
			numStr = StringUtils.repeat("0", power);
			numStr = "1" + numStr;
			return new BigDecimal(numStr).stripTrailingZeros();
		}

	}
	
	
	/**
	 * Finds the next characteristicUnitValue down from the min value, which 
	 * can be used as the bottom bin.  If the bottom value is evenly divisible by
	 * the CUV, it is returned.  All return values have extra zeros removed.
	 * 
	 * The CUV is expected to be a ONE X 10 to the something.
	 * 
	 * Example:  min value is -4.2345, CharUnitValue is .01
	 * Bottom bin would be -4.24
	 * 
	 * Example:  min value is .2945, CharUnitValue is .01
	 * Bottom bin would be .29
	 * 
	 * @param roundMeDown 
	 * @param characteristicUnitValue
	 * @return
	 */
	protected static BigDecimal roundToCUVBelow(BigDecimal roundMeDown, BigDecimal characteristicUnitValue) {
		BigDecimal[] intDivideAndRemainder = roundMeDown.divideAndRemainder(characteristicUnitValue, MathContext.DECIMAL64);
		BigDecimal numOfCharUnitValues = intDivideAndRemainder[0];
		BigDecimal remainder = intDivideAndRemainder[1];
		
		if (remainder.compareTo(BigDecimal.ZERO) < 0) {
			//remainder is negative, meaning we need to bump one cuv down from zero
			//See example in javadoc above.
			numOfCharUnitValues = numOfCharUnitValues.subtract(BigDecimal.ONE);
		}
		
		BigDecimal btmPost = characteristicUnitValue.multiply(numOfCharUnitValues).stripTrailingZeros();
		return btmPost;
	}
	
	
	/**
	 * Finds the next characteristicUnitValue up from the max value, which 
	 * can be used as the top bin.  If the max value is evenly divisible by
	 * the CUV, it is returned.  All return values have extra zeros removed.
	 * 
	 * The CUV is expected to be a ONE X 10 to the something.
	 * 
	 * Example:  max value is -4.2345, CharUnitValue is .01
	 * Bottom bin would be -4.23
	 * 
	 * Example:  min value is .2945, CharUnitValue is .01
	 * Bottom bin would be .30
	 * 
	 * @param roundMeUp 
	 * @param characteristicUnitValue
	 * @return
	 */
	protected static BigDecimal roundToCUVAbove(BigDecimal roundMeUp, BigDecimal characteristicUnitValue) {
		BigDecimal[] intDivideAndRemainder = roundMeUp.divideAndRemainder(characteristicUnitValue, MathContext.DECIMAL64);
		BigDecimal numOfCharUnitValues = intDivideAndRemainder[0];
		BigDecimal remainder = intDivideAndRemainder[1];
		
		if (remainder.compareTo(BigDecimal.ZERO) > 0) {
			//remainder is positive, meaning we need to bump one cuv up from zero
			//See example in javadoc above.
			numOfCharUnitValues = numOfCharUnitValues.add(BigDecimal.ONE);
		}
		
		BigDecimal topPost = characteristicUnitValue.multiply(numOfCharUnitValues).stripTrailingZeros();
		return topPost;
	}
	
	/**
	 * Rounds to the specified scale with constraints and rounding preferences.
	 * 
	 * Scale is basically decimal place.  A scale of 1 means to round to one
	 * decimal place.  0 means to round to the ones place.  -2 rounds to the
	 * hundreds place.  This is the same concept of scale that BigDecimal uses.
	 * 
	 * First, rounding is done by three methods:  HALF_EVEN, FLOOR and CEILING.
	 * Any round value that goes outside the floor or ceiling value is eliminated.
	 * Then of the remaining values, preference is given in the following order:
	 * <ul>
	 * <li>The result using the least precision (ie the least number of digits)
	 * <li>The result ending in a 5
	 * <li>The result that is even
	 * <li>One of the remaining values
	 * <li>If no values remain (all outside the bounds), the original value is
	 * returned.
	 * 
	 * @param roundMe
	 * @param floorValue  Possible round values cannot be less than this number.
	 * @param ceilingValue  Possible round values cannot be greater than this nubmer.
	 * @param finestScaleToRoundTo Finest (most toward positive) scale to round to.
	 * 	This is the BigDecimal concept of scale - see getScaleOfMostSignificantDigit
	 *  for discussion.
	 * @return
	 */
	protected BigDecimal roundToNiceIfPossible(
			BigDecimal roundMe, BigDecimal floorValue, BigDecimal ceilingValue,
			int finestScaleToRoundTo) {
		
		roundMe = roundMe.stripTrailingZeros();	//required for calcs
		
		//Array of rounded values to try, rounding up & down at different scales
		ArrayList<BigDecimal> rounds = new ArrayList<BigDecimal>(6);
		
		//Try rounding to one digit left of the most significant digit (MSD)
		int startScale = getScaleOfMostSignificantDigit(roundMe) - 1;
		int endScale = finestScaleToRoundTo;
		
		//For the MSD if right of the decimal (.0000001), always start by try
		//to round to the ones place.
		if (startScale > 0) { 
			startScale = 0;
		}
		
		if (finestScaleToRoundTo < startScale) {
			endScale = startScale;
			startScale = finestScaleToRoundTo;
		}
		
		
		//Create a list of rounded values, rounding from one digit left of the
		//most significant digit (i.e., 9 rounds to 10) down to the digit specified
		//by the scale of the CUV.  Try up & down for each.
		for (int currentScale = startScale; currentScale <= endScale; currentScale++) {
			BigDecimal up = round(roundMe, currentScale, RoundingMode.CEILING);
			BigDecimal down = round(roundMe, currentScale, RoundingMode.FLOOR);
			
			if (up.compareTo(floorValue) >= 0 && up.compareTo(ceilingValue) <= 0) {
				rounds.add(up.stripTrailingZeros());
			}
			
			if (down.compareTo(floorValue) >= 0 && down.compareTo(ceilingValue) <= 0) {
				rounds.add(down.stripTrailingZeros());
			}
		}
		
		if (rounds.size() > 0) {
			//
			//Find the most desirable round
			BigDecimal leastPrecision = null;
			int numberOfOptionsWGreaterPrecision = 0;
			BigDecimal endsInFive = null;
			BigDecimal isEven = null;
			Iterator<BigDecimal> roundIt = rounds.iterator();
			while (roundIt.hasNext()) {
				BigDecimal bd = roundIt.next();
				
				if (bd.compareTo(BigDecimal.ZERO) == 0) {
					//Stop searching - a zero is always good
					return bd;
				} else if (leastPrecision == null) {
					leastPrecision = bd;
				} else if (bd.precision() < leastPrecision.precision()) {
					leastPrecision = bd;
					numberOfOptionsWGreaterPrecision++;
				} else if (leastPrecision.precision() < bd.precision()) {
					numberOfOptionsWGreaterPrecision++;
				}
				
				String intStr = bd.unscaledValue().toString();
				String lastDigit = intStr.substring(intStr.length() - 1);
				if (lastDigit.equals("5")) {
					endsInFive = bd;
				}
				
				//Skip 0 b/c it will be less precision (handled above)
				if ("2468".indexOf(lastDigit) > 0) {
					isEven = bd;
				}
				
			}
			
			if (numberOfOptionsWGreaterPrecision > 0) {
				return leastPrecision;
			} else if (endsInFive != null) {
				return endsInFive;
			} else if (isEven != null) {
				return isEven;
			} else {
				return rounds.get(0);	//Good enough
			}
		} else {
			//all rounded values were outside the constraints
			return roundMe;
		}

	}
	
	/**
	 * Rounds to the specified decimal place using BigDecimal scale/decimal
	 * nomenclature where the ones place is zero, decimal places to the
	 * right are positive and powers of ten to the left are negative.
	 * 
	 * Examples:
	 * <ul>
	 * <li>Rounding .06 to the 1 scale rounds to .1 (depending on the rnd mode)
	 * <li>Rounding 99 to the -2 scale round to 100 (depending on the rdn mode).
	 * 
	 * @param roundMe
	 * @param scale
	 * @param roundingMethod
	 * @return
	 */
	public static BigDecimal round(BigDecimal roundMe, int scale, RoundingMode roundingMethod) {
		return roundMe.setScale(scale, roundingMethod).stripTrailingZeros();
	}
	
	/**
	 * Returns the scale (ie the BigDecimal concept of scale) of the most
	 * significant digit of the value.
	 * 
	 * Examples:<ul>
	 * <li>The scale of 1.49523 is 0
	 * <li>The scale of 0.149523 is 1
	 * <li>The scale of 14.9523 is -1
	 * <li>The scale of 0 is 0
	 * 
	 * @param value
	 * @return
	 */
	public static int getScaleOfMostSignificantDigit(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) == 0) {
			//Some multi-decimal place forms of zero return a non-zero result
			//for the equation below, even when stripped.
			return 0;
		} else {
			return value.scale() - value.precision() + 1;
		}
	}
	
	/**
	 * Divides w/o the possibility of arithmetic errors from infinite expansions.
	 * 
	 * The MathContext DECIMAL64 is used, which has 16 digits of precision and
	 * rounds HALF_EVEN.
	 * 
	 * Returned values are stripped of trailing zeros.
	 * 
	 * @param numerator
	 * @param demoninator
	 * @return
	 */
	protected static BigDecimal divide(BigDecimal numerator, int demoninator) {
		BigDecimal result = null;
		result = numerator.divide(
				new BigDecimal(demoninator), MathContext.DECIMAL64).stripTrailingZeros();
		return result;
	}
	
	/**
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param scale
	 * @param isForFunctionalBin Functional bins should always be non-sci notation.
	 * @return
	 */
	public static DecimalFormat getFormat(BigDecimal minValue, BigDecimal maxValue, int scale, boolean isForFunctionalBin) {
		
		//Find number of digits left of decimal
		BigDecimal maxAbs = minValue.abs();
		maxAbs = maxAbs.max(maxValue.abs());
		int digitsLeftOfDecimal = Math.abs(maxAbs.precision() - maxAbs.scale());
		
		boolean useSciNotation = (digitsLeftOfDecimal > 4 || scale > 4) && ! isForFunctionalBin;
		
		String formatStr = null;
		
		if (useSciNotation) {
			if (scale > 0) {
				//Sig figs beyond the decimal (decimal fraction number)
				formatStr = "0." + StringUtils.repeat("0", scale) + "E0";
			} else {
				//will contain no decimal number
				formatStr = "##0.##E0";
			}
		} else {
			if (scale > 0) {
				//Sig figs beyond the decimal (decimal fraction number)
				formatStr = "0." + StringUtils.repeat("0", scale);
			} else {
				//will contain no decimal number
				formatStr = "0";
			}
		}
		
		return new DecimalFormat(formatStr);
	}
	
	/**
	 * Creates functional bins (bins actually used for the binning, not shown
	 * to the user) which are be bumped out by one CUV to allow for rounding
	 * errors if they are too close to the actual min and max values.
	 * 
	 * For instance, if the top post is 50 and the actual max value is 49.9999,
	 * the functional top post would be bumped to 50.01 if the cuv is .01.
	 * 
	 * @param posts
	 * @param actualMin
	 * @param actualMin
	 * @param characteristicUnitValue
	 * @return
	 */
	public static BigDecimal[] createFunctionalPosts(BigDecimal[] posts, 
			BigDecimal actualMin, BigDecimal actualMax,
			BigDecimal characteristicUnitValue) {
		
		BigDecimal roundErrThreshold = new BigDecimal(".0001");
		roundErrThreshold = roundErrThreshold.min(characteristicUnitValue);
		
		//Start with the original top and bottom values
		BigDecimal min = posts[0];	
		BigDecimal max = posts[posts.length - 1];
		
		if (actualMin.subtract(min).compareTo(roundErrThreshold) < 0) {
			min = min.subtract(characteristicUnitValue);
		}
		
		if (max.subtract(actualMax).compareTo(roundErrThreshold) < 0) {
			max = max.add(characteristicUnitValue);
		}
		
		BigDecimal[] functional = Arrays.copyOf(posts, posts.length);
		functional[0] = min;
		functional[posts.length - 1] = max;
		
		return functional;
	}
	

	public static Double findMaxDouble(SparrowColumnSpecifier sparrowColumn, DeliveryFractionMap inclusionMap) {
		
		ColumnData column = sparrowColumn.getTable().getColumn(sparrowColumn.getColumn());
		Double result = Double.NEGATIVE_INFINITY;
		boolean found = false;
		int rowCount = column.getRowCount();
		
		if (inclusionMap != null) {
			
			//Find max, excluding values not in the inclussionMap
			for (int r=0; r<rowCount; r++) {
				Double current = column.getDouble(r);
				
				if (current != null && ! Double.isNaN(current) && !current.isInfinite() && inclusionMap.hasRowNumber(r)) {
					result = Math.max(result, current);
					found = true;
				}
			}

		} else {

			//No inclusionMap, so don't do that part of check
			for (int r=0; r<rowCount; r++) {
				Double current = column.getDouble(r);
				
				if (current != null && ! Double.isNaN(current) && !current.isInfinite()) {
					result = Math.max(result, current);
					found = true;
				}
			}
		}
		
		if (!found) {
			return null;
		} else {
			return result;
		}
	}
	
	public static Double findMinDouble(SparrowColumnSpecifier sparrowColumn, DeliveryFractionMap inclusionMap) {
		
		ColumnData column = sparrowColumn.getTable().getColumn(sparrowColumn.getColumn());
		Double result = Double.POSITIVE_INFINITY;
		boolean found = false;
		int rowCount = column.getRowCount();
		
		if (inclusionMap != null) {
			
			//Find max, excluding values not in the inclussionMap
			for (int r=0; r<rowCount; r++) {
				Double current = column.getDouble(r);
				
				if (current != null && ! Double.isNaN(current) && !current.isInfinite() && inclusionMap.hasRowNumber(r)) {
					result = Math.min(result, current);
					found = true;
				}
			}

		} else {

			//No inclusionMap, so don't do that part of check
			for (int r=0; r<rowCount; r++) {
				Double current = column.getDouble(r);
				
				if (current != null && ! Double.isNaN(current) && !current.isInfinite()) {
					result = Math.min(result, current);
					found = true;
				}
			}
		}
		
		if (!found) {
			return null;
		} else {
			return result;
		}
	}

	public SparrowColumnSpecifier getDataColumn() {
		return dataColumn;
	}

	public void setDataColumn(SparrowColumnSpecifier dataColumn) {
		this.dataColumn = dataColumn;
	}

	public BigDecimal getMinValue() {
		return minValue;
	}

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	public BigDecimal getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	public int getBinCount() {
		return binCount;
	}

	public void setBinCount(int binCount) {
		this.binCount = binCount;
	}

	public BigDecimal getDetectionLimit() {
		return detectionLimit;
	}

	public void setDetectionLimit(BigDecimal detectionLimit) {
		this.detectionLimit = detectionLimit;
	}

	public Integer getMaxDecimalPlaces() {
		return maxDecimalPlaces;
	}

	public void setMaxDecimalPlaces(Integer maxDecimalPlaces) {
		this.maxDecimalPlaces = maxDecimalPlaces;
	}

	public boolean isBottomUnbounded() {
		return bottomUnbounded;
	}

	public void setBottomUnbounded(boolean bottomUnbounded) {
		this.bottomUnbounded = bottomUnbounded;
	}

	public boolean isTopUnbounded() {
		return topUnbounded;
	}

	public void setTopUnbounded(boolean topUnbounded) {
		this.topUnbounded = topUnbounded;
	}

	public void setInclusionMap(DeliveryFractionMap inclusionMap) {
		this.inclusionMap = inclusionMap;
	}

	public DeliveryFractionMap getInclusionMap() {
		return inclusionMap;
	}
	
}
