package gov.usgswim.sparrow.datatable;


/**
 * Compares two PredictResults and returns values as either percentage increase
 * or as absolute values.
 * 
 * find, ID, metadata, getMax/Min, return values from the base data.
 * 
 * getDataType returns Double for all columns b/c the returned data is always a comparison.
 * getUnits returns the units of the base table if absolute, 'percentage' otherwise.
 * 
 * Sparrow specific methods (the PredictResult interface) return column numbers
 * from the base data, but return comparison values for all data access methods.
 * 
 * @author eeverman
 *
 */
public class PredictResultCompare extends DataTableCompare implements PredictResult {
	
	/*
	private final PredictResult base;
	private final PredictResult compare;
	private final boolean absolute;
	*/
	
	/**
	 * Constructs a new comparison instance
	 * @param base	The data to compare to
	 * @param compare	The data to be compared
	 * @param isAbsolute	If true, values are (compare - base).  If false (percentage increase),
	 * the values are (compare - base) / base.
	 */
	public PredictResultCompare(PredictResult base, PredictResult compare, boolean isAbsolute) {
		super(base, compare, isAbsolute);
	}

	public Double getIncremental(int row) {
		return getDouble(row, getIncrementalCol());
	}

	public int getIncrementalCol() {
		return ((PredictResult)base).getIncrementalCol();
	}

	public int getIncrementalColForSrc(Long srcId) {
		return ((PredictResult)base).getIncrementalColForSrc(srcId);
	}

	public Double getIncrementalForSrc(int row, Long srcId) {
		return getDouble(row, ((PredictResult)base).getIncrementalColForSrc(srcId));
	}

	public int getSourceCount() {
		return ((PredictResult)base).getSourceCount();
	}

	public Double getTotal(int row) {
		return getDouble(row, ((PredictResult)base).getTotalCol());
	}

	public int getTotalCol() {
		return ((PredictResult)base).getTotalCol();
	}

	public int getTotalColForSrc(Long srcId) {
		return ((PredictResult)base).getTotalColForSrc(srcId);
	}

	public Double getTotalForSrc(int row, Long srcId) {
		return getDouble(row, ((PredictResult)base).getTotalColForSrc(srcId));
	}

}
