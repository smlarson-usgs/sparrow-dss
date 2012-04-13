package gov.usgswim.sparrow.datatable;

import java.util.Map;
import java.util.Set;

import gov.usgswim.datatable.ColumnAttribs;
import gov.usgswim.datatable.ColumnAttribsImm;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;
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
	
	private static final long serialVersionUID = 1L;

	/** A column of coefficients that are multiplied by values in the specified column */
	private ColumnData coefCol;	
	
	/** The index of the column (zero based) that the coefCol should be applied to */
	private int colIndexNum;
	
	/** Whether to divide by the coefficient column instead of multiply */
	private boolean invertCoef = false;
	
	/** Specifies the columns for the specified column, allowing overwrites */
	private ColumnAttribs colAttribs;

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
	 * The attributes for the specified column (colAttribs) can be left null,
	 * in which each the attribs of the underlying sourceData column will be used.
	 * If a colAttribs instance is passed, any null (by not empty) values will
	 * be pulled from the sourceData column.
	 * 
	 * @param sourceData
	 * @param coefColumn
	 * @param coefColumnIndex
	 * @param colAttribs Attributes for the specified column
	 */
	public SingleColumnCoefDataTable(DataTable sourceData, ColumnData coefColumn, int coefColumnIndex, ColumnAttribs colAttribs) {
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
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
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
	 * The attributes for the specified column (colAttribs) can be left null,
	 * in which each the attribs of the underlying sourceData column will be used.
	 * If a colAttribs instance is passed, any null (by not empty) values will
	 * be pulled from the sourceData column.
	 * 
	 * @param sourceData
	 * @param coefValues
	 * @param coefColumnIndex
	 * @param colAttribs Attributes for the specified column
	 */
	public SingleColumnCoefDataTable(DataTable sourceData, double[] coefValues, int coefColumnIndex, ColumnAttribs colAttribs) {
		super(sourceData);
		
		StandardDoubleColumnData cc = new StandardDoubleColumnData(
				coefValues, null, null, null, null, false);
		coefCol = cc;
		
		colIndexNum = coefColumnIndex;
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	/**
	 * The attributes for the specified column (colAttribs) can be left null,
	 * in which each the attribs of the underlying sourceData column will be used.
	 * If a colAttribs instance is passed, any null (by not empty) values will
	 * be pulled from the sourceData column.
	 * 
	 * @param sourceData
	 * @param coefValues
	 * @param coefColumnIndex
	 * @param colAttribs Attributes for the specified column
	 * @param invertCoefficient
	 */
	public SingleColumnCoefDataTable(
			DataTable sourceData, ColumnData coefColumn, int coefColumnIndex, ColumnAttribs colAttribs, boolean invertCoefficient) {
		super(sourceData);
		coefCol = coefColumn;
		colIndexNum = coefColumnIndex;
		invertCoef = invertCoefficient;
		
		if (coefCol.getRowCount() != sourceData.getRowCount()) {
			throw new IllegalArgumentException(
					"The number of rows in the sourceData table " +
					"and the coefColumn must be the same.");
		} else if (coefColumnIndex < 0 || coefColumnIndex >= sourceData.getColumnCount()) {
			throw new IllegalArgumentException(
					"The coefColumnIndex cannot be less than zero or beyond " +
					"the last column of the sourceData.");
		}
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	protected Double getCoefficient(int row) {
		Double lookup = coefCol.getDouble(row);
		return lookup;
	}
	
	public DataTable.Immutable toImmutable() {
		return new SingleColumnCoefDataTable(base.toImmutable(), coefCol.toImmutable(), colIndexNum, colAttribs);
	}
	
	// ==========================
	// Adjusted findXXX() Methods
	// ==========================
	/**
	 * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
	 * @Deprecated Use at your own risk. The result may be inaccurate if rows have been adjusted
	 *  because of rounding
	 */
	@Override
	public int[] findAll(int col, Object value) {
		return FindHelper.bruteForceFindAll(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if columns have been adjusted
	 *  because of rounding.
	 */
	@Override
	public int findFirst(int col, Object value) {
		return FindHelper.bruteForceFindFirst(this, col, value);
	}

	/**
	 * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
	 * @deprecated Use at your own risk. The result may be inaccurate if columns have been adjusted
	 *  because of rounding
	 */
	@Override
	public int findLast(int col, Object value) {
		return FindHelper.bruteForceFindLast(this, col, value);
	}

	// ========================
	// Adjusted Max-Min Methods
	// ========================
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
	
	// =================
	// Adjusted getXXX()
	// =================
	@Override
	public Object getValue(int row, int col) {
		Object value = base.getValue(row, col);
		if (col == colIndexNum) {
			if(!invertCoef) {
				return ((Number) value).doubleValue() * coefCol.getDouble(row);
			}
			else {
				Double retval = null;
				try {
					retval = ((Number) value).doubleValue() / coefCol.getDouble(row);
				} catch (NullPointerException e) {
					//Arithmetic exceptions do not happen for doubles
					retval = Double.NaN;
				}
				return retval;
			}
		} else {
			return value;
		}
	}
	
	@Override
	public Double getDouble(int row, int col) {
		Double value = base.getDouble(row, col);
		if (col == colIndexNum) {
			if(!invertCoef) {
				return value * coefCol.getDouble(row);
			}
			else {
				Double retval = null;
				try {
					retval = value / coefCol.getDouble(row);
				} catch (NullPointerException e) {
					//Arithmetic exceptions do not happen for doubles
					retval = Double.NaN;
				}
				return retval;
			}
		} else {
			return value;
		}
	}
	

	@Override
	public boolean isIndexed(int col) {
		if (col == colIndexNum) {
			return false;
		} else {
			return super.isIndexed(col);
		}
	}

	@Override
	public String getDescription(int col) {
		if (col == colIndexNum) {
			return colAttribs.getDescription(super.getDescription(col));
		} else {
			return super.getDescription(col);
		}
	}

	@Override
	public String getName(int col) {
		if (col == colIndexNum) {
			return colAttribs.getName(super.getName(col));
		} else {
			return super.getName(col);
		}
	}

	@Override
	public String getProperty(int col, String name) {
		if (col == colIndexNum) {
			return colAttribs.getProperty(name, super.getProperty(col, name));
		} else {
			return super.getProperty(col, name);
		}
	}

	@Override
	public String getUnits(int col) {
		if (col == colIndexNum) {
			return colAttribs.getUnits(super.getUnits(col));
		} else {
			return super.getUnits(col);
		}
	}
	
	@Override
	public ColumnData getColumn(int colIndex) {
		
		if (colIndex < 0 || colIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Requested column index does not exist.");
		}
		
		ColumnDataFromTable col = new ColumnDataFromTable(this, colIndex);
		return col;
	}
	
}
