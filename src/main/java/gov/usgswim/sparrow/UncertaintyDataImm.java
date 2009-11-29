package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

/**
 * An immutable implementation of UncertaintyData using float storage.
 * @author eeverman
 *
 */
@Immutable
public class UncertaintyDataImm implements UncertaintyData {

	private static final long serialVersionUID = 1L;


	/**
	 * A 2D array in which the first column is MEAN (bias) and the 2nd is
	 * the standard error.  The rows match the reaches of the model.
	 *
	 * The columns are the first dimension, thus read it as data[column][row].
	 * More specifically: [two columns as mean, standard error][reach]
	 */
	final float[][] _data;

	/**
	 * Constructs an immutable set of UncertaintyData.
	 *
	 * The passed data is not copied, thus this is cooperative immutability.
	 * The passed data is a 2D array in which the first column is MEAN (bias)
	 * and the 2nd is the standard error.  The rows match the reaches of the model.
	 *
	 * The columns are the first dimension, thus read it as data[column][row].
	 *
	 * @param data as data[two columns as mean, standard error][reach].
	 */
	public UncertaintyDataImm(float[][] data) {
		_data = data;
	}


	/**
	 * @see gov.usgswim.sparrow.UncertaintyData#getMean(int)
	 */
	@Override
	public double getMean(int rowIndex) {
		return _data[0][rowIndex];
	}

	/**
	 * @see gov.usgswim.sparrow.UncertaintyData#getStandardError(int)
	 */
	@Override
	public double getStandardError(int rowIndex) {
		return _data[1][rowIndex];
	}

	/**
	 * @see gov.usgswim.sparrow.UncertaintyData#calcCoeffOfVariation(int)
	 */
	@Override
	public double calcCoeffOfVariation(int rowIndex) {
		double m = _data[0][rowIndex];
		double se = _data[1][rowIndex];

		if (m > 0) {
			double cov = se/m;

			if (! Double.isInfinite(cov)) {
				return cov;
			}
			return (cov > 0)? Double.MAX_VALUE: Double.MAX_VALUE * (-1d);

		}
		return 0d;

	}

	/**
	 * @see gov.usgswim.ImmutableBuilder#toImmutable()
	 */
	@Override
	public UncertaintyData toImmutable() throws IllegalStateException {
		return this;
	}

	/**
	 * @see gov.usgswim.sparrow.UncertaintyData#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (_data != null) {
			return _data[0].length;
		}
		return 0;

	}

}
