package gov.usgswim.sparrow.datatable;

import java.util.Map;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;

/**
 * A coefficient type DataTable returns data from the base table for all columns
 * except for a specified column, which multiplies its values by the corresponding
 * value in a column of data.
 * 
 * @author eeverman
 *
 */
public class SingleColumnCoefDataTable extends AbstractDataTableBase implements DataTable.Immutable {
	
	/** A column of coefficients that are multiplied by values in the specified column */
	private ColumnData coefCol;	
	
	/** The index of the column (zero based) that the coefCol should be applied to */
	private int colIndexNum;

	/**
	 * Create a new instance.
	 * 
	 * Values are returned from sourceData as-is, except for the column specified
	 * by coefColumnIndex.  Values retrieved from that column are multiplied by
	 * their corresponding value in the coefColumn.
	 * 
	 * For the mathematically inclined:
	 * getValue(row, column) == getValue(row, column) if column != coefColumnIndex.
	 * getValue(row, column) == coefColumn.getDouble(column) * getValue(row, column) if column == coefColumnIndex.
	 * 
	 * @param sourceData
	 * @param coefColumn
	 * @param coefColumnIndex
	 */
	public SingleColumnCoefDataTable(DataTable sourceData, ColumnData coefColumn, int coefColumnIndex) {
		super(sourceData);
		coefCol = coefColumn;
		colIndexNum = coefColumnIndex;
		
		if (coefCol.getRowCount() != sourceData.getRowCount()) {
			throw new IllegalArgumentException(
					"The number of rows in the sourceData table " +
					"and the coefColumn must be the same.");
		} else if (coefColumnIndex < 0 || coefColumnIndex >= sourceData.getColumnCount()) {
			throw new IllegalArgumentException(
					"The coefColumnIndex cannot be less than zero or beyond " +
					"the last column of the sourceData.");
		}
	}
	
	/**
	 * Create a new instance.
	 * 
	 * Values are returned from sourceData as-is, except for the column specified
	 * by coefColumnIndex.  Values retrieved from that column are multiplied by
	 * their corresponding value in the passed coefValues array.
	 * 
	 * For the mathematically inclined:
	 * getValue(row, column) == getValue(row, column) if column != coefColumnIndex.
	 * getValue(row, column) == coefValues[column] * getValue(row, column) if column == coefColumnIndex.
	 * 
	 * 
	 * @param sourceData
	 * @param coefValues
	 * @param coefColumnIndex
	 */
	public SingleColumnCoefDataTable(DataTable sourceData, double[] coefValues, int coefColumnIndex) {
		super(sourceData);
		
		StandardDoubleColumnData cc = new StandardDoubleColumnData(
				coefValues, null, null, null, null, false);
		coefCol = cc;
		
		colIndexNum = coefColumnIndex;
	}
	
	protected Double getCoefficient(int row) {
		Double lookup = coefCol.getDouble(row);
		return lookup;
	}
	
	public DataTable.Immutable toImmutable() {
		return new SingleColumnCoefDataTable(base.toImmutable(), coefCol.toImmutable(), colIndexNum);
	}
	
	// ==========================
	// Adjusted findXXX() Methods
	// ==========================
	/**
	 * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
	 * @Deprecated Use at your own risk. The result may be inaccurate if rows have been adjusted
	 *  because of rounding
	 */
	public int[] findAll(int col, Object value) {
		return FindHelper.bruteForceFindAll(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if columns have been adjusted
	 *  because of rounding.
	 */
	public int findFirst(int col, Object value) {
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if columns have been adjusted
	 *  because of rounding
	 */
	public int findLast(int col, Object value) {
		return FindHelper.bruteForceFindLast(this, col, value);
	}

	// ========================
	// Adjusted Max-Min Methods
	// ========================
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
	
	// =================
	// Adjusted getXXX()
	// =================
	public Object getValue(int row, int col) {
		Object value = base.getValue(row, col);
		if (col == colIndexNum) {
			return ((Number) value).doubleValue() * coefCol.getDouble(row);
		} else {
			return value;
		}
	}
	
	public Double getDouble(int row, int col) {
		Double value = base.getDouble(row, col);
		if (col == colIndexNum) {
			return value * coefCol.getDouble(row);
		} else {
			return value;
		}
	}
	

	public boolean isIndexed(int col) {
		if (col == colIndexNum) {
			return false;
		} else {
			return super.isIndexed(col);
		}
	}
	
}
