package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTable.Immutable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.domain.DataSeriesType;

import java.io.IOException;


/**
 * Returns an error estimate based on a base set of prediction results and an
 * UncertaintyData instance.
 *
 * Find, ID, metadata, getMax/Min, return values from the base data.
 *
 * Some of the constraints dealing with zero and null values are outlined in the
 * JIRA task (#) from Greg Schwarz email.
 *
 * Note that the UncertaintyData is only valid for a specific column in the
 * underlying dataset.  To prevent errors, the index of this column is set in
 * the constructor and validated on every call.
 *
 * @author eeverman
 */
public class StdErrorEstTable extends AbstractDataTableBase implements Immutable {

	private final UncertaintyData uncertaintyData;
	private int validColumnIndex;
	private boolean useNullForNonValues;
	private double nullValue;
	private DataSeriesType dataSeriesType;

	/**
	 * Constructs a new Immutable StdErrorEstCoefTable instance.
	 *
	 * @param base The prediction result data
	 * @param uncertaintyData The uncertainty data.
	 * @param validColumnIndex The *only* valid column to ask for.
	 * @param useNullForNonValues If true, return a null for uncalculable values
	 * @param nullValue If useNullForNonValues if false, return this double value.
	 */
	public StdErrorEstTable(DataTable base, UncertaintyData uncertaintyData,
			int validColumnIndex, boolean useNullForNonValues, double nullValue, DataSeriesType dataSeriesType) {
		super(base);
		this.uncertaintyData = uncertaintyData.toImmutable();
		this.validColumnIndex = validColumnIndex;
		this.useNullForNonValues= useNullForNonValues;
		this.nullValue = nullValue;
		this.dataSeriesType = dataSeriesType;
		
		//TODO:  The name and description of this table are not right.
		
	}

	/**
	 * Actual type is a mixture, but when the comparison is done, resolution is Double.
	 */
	@Override
	public Class<?> getDataType(int col) {
		return Double.class;
	}

	@Override
	public Double getDouble(int row, int col) {

		if (col != validColumnIndex) {
			throw new IllegalArgumentException("Only column " +
					validColumnIndex + " is valid for this StdErrorEstCoefTable.");
		}

		Double b = base.getDouble(row, col);
		Double c = uncertaintyData.calcCoeffOfVariation(row);

		if (b != null && c != null && c != 0d) {
			//Note:  Its OK for the base value to be zero.  In theory, we are
			//are very confident within the model that if we completely turn off
			//inputs, there should be no flux, thus no error.
			return b * c;
		}

		if (useNullForNonValues) {return null;}
		return nullValue;


	}

	@Override
	public Float getFloat(int row, int col) {

		if (col != validColumnIndex) {
			throw new IllegalArgumentException("Only column " +
					validColumnIndex + " is valid for this StdErrorEstCoefTable.");
		}

		Double b = base.getDouble(row, col);
		Double c = uncertaintyData.calcCoeffOfVariation(row);

		if (b != null && c != null && b != 0d && c != 0d) {
			//See zero value note in getDouble
			return (float)(b * c);
		}
		if (useNullForNonValues) return null;

		return (float) nullValue;

	}

	@Override
	public Integer getInt(int row, int col) {

		if (col != validColumnIndex) {
			throw new IllegalArgumentException("Only column " +
					validColumnIndex + " is valid for this StdErrorEstCoefTable.");
		}

		Double b = base.getDouble(row, col);
		Double c = uncertaintyData.calcCoeffOfVariation(row);

		if (b != null && c != null && b != 0d && c != 0d) {
			//See zero value note in getDouble
			return (int)(b * c);
		}

		if (useNullForNonValues) return null;
		return (int) nullValue;
	}

	@Override
	public Long getLong(int row, int col) {

		if (col != validColumnIndex) {
			throw new IllegalArgumentException("Only column " +
					validColumnIndex + " is valid for this StdErrorEstCoefTable.");
		}

		Double b = base.getDouble(row, col);
		Double c = uncertaintyData.calcCoeffOfVariation(row);

		if (b != null && c != null && b != 0d && c != 0d) {
			//See zero value note in getDouble
			return (long)(b * c);
		}

		if (useNullForNonValues) return null;
		return (long) nullValue;

	}

	@Override
	public Double getMaxDouble(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col);
	}

	@Override
	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	@Override
	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	@Override
	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	@Override
	public String getString(int row, int col) {
		return Double.toString(getDouble(row, col));
	}

	/**
	 * The units are the same as the base data - its a variance, not a percentage.
	 */
	@Override
	public String getUnits(int col) {
		return base.getUnits(validColumnIndex);
	}
	
	@Override
	public String getDescription(int col) {
		try {
			return Action.getDataSeriesProperty(dataSeriesType, true);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getName(int col) {
		try {
			return Action.getDataSeriesProperty(dataSeriesType, false);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public Object getValue(int row, int col) {
		return getDouble(row, col);
	}

	@Override
	public boolean isValid() {
		return super.isValid();
	}

	public Immutable toImmutable() {
		return this;
	}
	
	/* Unsupported for this class */
	@Override
	public ColumnData getColumn(int colIndex) {
		throw new UnsupportedOperationException(
				"This method is not supported for this type of DataTable view, " +
				"since there is no real ColumnData instance containing the data. ");
	}



}
