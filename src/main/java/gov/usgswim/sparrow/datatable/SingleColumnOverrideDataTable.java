package gov.usgswim.sparrow.datatable;

import java.util.Map;
import java.util.Set;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.FindHelper;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;

/**
 * Presents a view of an underlying table, but overlays a single column on top.
 * 
 * The override column replaces all the values that would otherwise be returned
 * for that column and must have the same number of rows as the underlying table.
 * Note that since this column replaces the underlying data, the column metadata
 * of the override column is used in place of the underlying source column.
 * 
 * @author eeverman
 *
 */
public class SingleColumnOverrideDataTable extends AbstractDataTableBase implements DataTable.Immutable {
	
	private static final long serialVersionUID = 1L;

	/** A column of coefficients that are multiplied by values in the specified column */
	private ColumnData overrideCol;	
	
	/** The index of the column (zero based) that the coefCol should be applied to */
	private int colIndexNum;
	
	/** Whether to divide by the coefficient column instead of multiply */
	private boolean invertCoef = false;
	
	/** Specifies the attributes for the specified column, allowing overwrites */
	private ColumnAttribs colAttribs;

	/**
	 * Create a new instance.
	 * 
	 * Values are returned from sourceData as-is, except for the column specified
	 * by overrideColumnIndex.  Values retrieved from that column are returned
	 * from the overrideColumn instead.
	 * 
	 * The attributes for the specified column (colAttribs) can be left null,
	 * in which each the attribs of the underlying sourceData column will be used.
	 * If a colAttribs instance is passed, any null (by not empty) values will
	 * be pulled from the sourceData column.
	 * 
	 * @param sourceData
	 * @param overrideColumn
	 * @param overrideColumnIndex The column index in the base table to be overridden.
	 * @param colAttribs Attributes for the specified column
	 */
	public SingleColumnOverrideDataTable(DataTable sourceData, ColumnData overrideColumn, int overrideColumnIndex, ColumnAttribs colAttribs) {
		super(sourceData);
		overrideCol = overrideColumn;
		colIndexNum = overrideColumnIndex;
		
		if (overrideCol.getRowCount() != sourceData.getRowCount()) {
			throw new IllegalArgumentException(
					"The number of rows in the sourceData table " +
					"and the overrideColumn must be the same.");
		} else if (overrideColumnIndex < 0 || overrideColumnIndex >= sourceData.getColumnCount()) {
			throw new IllegalArgumentException(
					"The overrideColumnIndex cannot be less than zero or beyond " +
					"the last column of the sourceData.");
		}
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	protected Double getOverrideValue(int row) {
		Double lookup = overrideCol.getDouble(row);
		return lookup;
	}
	
	public DataTable.Immutable toImmutable() {
		return new SingleColumnOverrideDataTable(base, overrideCol, colIndexNum, colAttribs);
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
		if (col == colIndexNum) {
			return (Number) overrideCol.getDouble(row);
		} else {
			return base.getValue(row, col);
		}
	}
	
	@Override
	public Double getDouble(int row, int col) {
		if (col == colIndexNum) {
			return overrideCol.getDouble(row);
		} else {
			return base.getDouble(row, col);
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
			return colAttribs.getDescription(overrideCol.getDescription());
		} else {
			return super.getDescription(col);
		}
	}

	@Override
	public String getName(int col) {
		if (col == colIndexNum) {
			return colAttribs.getName(overrideCol.getName());
		} else {
			return super.getName(col);
		}
	}

	@Override
	public String getProperty(int col, String name) {
		if (col == colIndexNum) {
			return colAttribs.getProperty(name, overrideCol.getProperty(name));
		} else {
			return super.getProperty(col, name);
		}
	}

	@Override
	public String getUnits(int col) {
		if (col == colIndexNum) {
			return colAttribs.getUnits(overrideCol.getUnits());
		} else {
			return super.getUnits(col);
		}
	}
	
}
