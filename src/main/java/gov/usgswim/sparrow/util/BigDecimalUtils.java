package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.cachefactory.BinningFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class BigDecimalUtils {

	public static BigDecimal makeBigDecimal(long numerator, BigDecimal denominatorWithScale, int exp) {
		BigDecimal num = new BigDecimal(numerator, new MathContext(denominatorWithScale.scale()));
		BigDecimal temp = num.divide(denominatorWithScale);
		BigDecimal result = temp.scaleByPowerOfTen(exp);
		return result;
	}

	/**
	 * Rounds to the fewest digits necessary (<= 3.5 digits) within the given lo-hi range
	 *
	 * @param value
	 * @param lo
	 * @param hi
	 * @return the original value if lo-hi range is too narrow
	 */
	public static BigDecimal round(double value, double lo, double hi) {
		assert(value >= lo &&  value <= hi): "value " + value + " is not between lo " + lo + " and " + hi + " hi";
		if (value == lo && value == hi) {
			//No rounding possible
			return new BigDecimal(value);
		} else if (value ==0 || (lo <=0 && hi>=0)) {
			// round to zero as first option
			if (value ==0 || (lo <=0 && hi>=0)) return BigDecimal.ZERO;
		}

		// round to 1 digit
		int exponent = Double.valueOf(Math.ceil(Math.log10(Math.abs(value)))).intValue();

		for (BigDecimal sigfigs: BinningFactory.digitAccuracyMultipliers) {
			BigDecimal result = BinningFactory.findClosestInRange(value, lo, hi, exponent, sigfigs);
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

		BigDecimal result = null;
		try {
			result = new BigDecimal(value);
		} catch (NumberFormatException e) {
			System.err.println("Invalid BigDecimal value " + value);
			throw e;
		}
		return result; // range is too narrow so just return original value
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
			}
			return roundedValue.stripTrailingZeros();

		}

}
