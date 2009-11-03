package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTable.Immutable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.sparrow.UncertaintyData;


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
			int validColumnIndex, boolean useNullForNonValues, double nullValue) {
		super(base);
		this.uncertaintyData = uncertaintyData.toImmutable();
		this.validColumnIndex = validColumnIndex;
		this.useNullForNonValues= useNullForNonValues; 
		this.nullValue = nullValue;
	}

	/**
	 * Actual type is a mixture, but when the comparison is done, resolution is Double.
	 */
	public Class<?> getDataType(int col) {
		return Double.class;
	}

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
		} else {
			if (useNullForNonValues) {
				return null;
			} else {
				return nullValue;
			}
		}
	}

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
		} else {
			if (useNullForNonValues) {
				return null;
			} else {
				return (float) nullValue;
			}
		}
	}

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
		} else {
			if (useNullForNonValues) {
				return null;
			} else {
				return (int) nullValue;
			}
		}
	}

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
		} else {
			if (useNullForNonValues) {
				return null;
			} else {
				return (long) nullValue;
			}
		}
	}
	
	public Double getMaxDouble(int col) {
		return FindHelper.bruteForceFindMaxDouble(this, col);
	}

	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(this);
	}

	public Double getMinDouble(int col) {
		return FindHelper.bruteForceFindMinDouble(this, col);
	}

	public Double getMinDouble() {
		return FindHelper.bruteForceFindMinDouble(this);
	}

	public String getString(int row, int col) {
		return Double.toString(getDouble(row, col));
	}

	public String getUnits(int col) {
		return "Unitless";
	}

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

}
