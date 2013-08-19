package gov.usgs.cida.datatable.view;

import gov.usgs.cida.datatable.ColumnAttribs;
import gov.usgs.cida.datatable.ColumnAttribsImm;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.impl.FindHelper;
import gov.usgs.cida.datatable.utils.DataTableUtils;

/**
 * A nominally immutable view of a ColumnData with an added row with the sum
 * total of all values in the base column.  This column will report a rowCount
 * of one more than the base column.
 * 
 * The view assumes that the base implementation is immutable and calculates
 * the total at construction time.  The total is never null - if no totalable
 * values are found or there are no rows, the total will be zero.
 * 
 * If the underlying column is immutable, this instance will be immutable.
 * If the underlying column implements the ColumnDataWritable interface, calling
 * toImmutable on this instance will create a new immutable implementation.  If
 * the base column does not implement ColumnDataWritable, calling toImmutable
 * will simply return this same instance.
 * 
 * Note that to add this column to a table, all columns in the table will need
 * to have the same number of rows.  Typical use would be wrap all columns in
 * this view so that each column will report the same number of rows.
 * 
 * Note that findXXX methods will search on the base table, not the view.
 * This allows these methods to search on the 'real' values and ignore the
 * generated total value.
 * 
 * @author eeverman
 */
public class TotalingColumnDataView extends AbstractColumnDataView {

	private final double total;
	
	public TotalingColumnDataView(ColumnData baseCol, ColumnAttribs colAttribs) {
		super(baseCol, baseCol, colAttribs);
		total = DataTableUtils.getColumnTotal(baseCol);
	}
		
	@Override
	public Double getDouble(int row) {
		if (row == this.baseCol.getRowCount()) {
			return total;
		} else {
			return baseCol.getDouble(row);
		}
	}
	
	@Override
	public Object getValue(int row) {
		if (row == this.baseCol.getRowCount()) {
			return new Double(total);
		} else {
			return baseCol.getValue(row);
		}
	}

	@Override
	public boolean isValid() {
		return baseCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		if (baseCol instanceof ColumnDataWritable) {
			ColumnData cdImm = baseCol.toImmutable();
			return new TotalingColumnDataView(cdImm, colAttribs);
		} else {
			return this;
		}
	}

	@Override
	public int[] findAll(Object value) {
		return FindHelper.findAllWithoutIndex(value, baseCol);
	}

	@Override
	public int findFirst(Object value) {
		return FindHelper.bruteForceFindFirst(baseCol, value);
	}

	@Override
	public int findLast(Object value) {
		return FindHelper.bruteForceFindLast(baseCol, value);
	}

	@Override
	public Double getMaxDouble() {
		return FindHelper.findMaxDouble(baseCol);
	}

	@Override
	public Integer getMaxInt() {
		Double maxD = getMaxDouble();
		if (maxD != null) {
			return maxD.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Double getMinDouble() {
		return FindHelper.findMinDouble(baseCol);
	}

	@Override
	public Integer getMinInt() {
		Double minD = getMinDouble();
		if (minD != null) {
			return minD.intValue();
		} else {
			return null;
		}
	}

	@Override
	public Integer getRowCount() {
		return baseCol.getRowCount() + 1;
	}

}
