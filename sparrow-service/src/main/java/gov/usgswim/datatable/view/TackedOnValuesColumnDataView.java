package gov.usgswim.datatable.view;

import gov.usgswim.datatable.ColumnAttribs;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;

/**
 * A nominally immutable view of a ColumnData with rows added rows.  This column
 * will report a rowCount of the number of rows in the base column plus the
 * number of tacked on values.
 * 
 * Note that this view looks at how many rows are in the base column in the
 * constructor, so it will not update correctly if rows are added to the
 * base column.  Also, do not keep a reference to the tacked on values: they are
 * not copied.
 * 
 * If the underlying column is immutable, this instance will be immutable.
 * If the underlying column implements the ColumnDataWritable interface, calling
 * toImmutable on this instance will create a new immutable implementation.  If
 * the base column does not implement ColumnDataWritable, calling toImmutable
 * will simply return this same instance.
 * 
 * Note that to add this column to a table, all columns in the table will need
 * to have the same number of rows.  Typical use would be wrap all columns in
 * this view (or the TotallingColumnDataView) so that each column will report
 * the same number of rows.
 * 
 * Note that findXXX methods will search all rows in the column, including those
 * values that are tacked on.
 * 
 * @author eeverman
 */
public class TackedOnValuesColumnDataView extends AbstractColumnDataView {

	private final Object[] tackOns;
	private final int baseRowCount;
	private final int totalRowCount;
	
	
	public TackedOnValuesColumnDataView(ColumnData baseCol, ColumnAttribs colAttribs, Object valueToTackOn) {
		this(baseCol, colAttribs, new Object[] {valueToTackOn});
	}
		
		
	public TackedOnValuesColumnDataView(ColumnData baseCol, ColumnAttribs colAttribs, Object[] valuesToTackOn) {
		super(baseCol, baseCol, colAttribs);
		
		if (valuesToTackOn == null)
			throw new IllegalArgumentException("The valuesToTackOn cannot be null");
				
		tackOns = valuesToTackOn;
		baseRowCount = baseCol.getRowCount();
		totalRowCount = baseRowCount + tackOns.length;
	}
		
	@Override
	public Double getDouble(int row) {
		
		Object v = getValue(row);
		
		if (v instanceof Number) {
			Number n = (Number)v;
			return n.doubleValue();
		} else {
			return null;
		}
	}
	
	@Override
	public Object getValue(int row) {
		
		if (row < baseRowCount) {
			
			return baseCol.getDouble(row);	//just get the base value
			
		} else if (row < totalRowCount) {
			
			return tackOns[row - baseRowCount];	//grab a tacked on value
			
		} else {
			throw new IllegalArgumentException("The specified row is beyond the number of rows in the column: " + row);
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
			return new TackedOnValuesColumnDataView(cdImm, colAttribs, tackOns);
		} else {
			return this;
		}
	}

	@Override
	public Integer getRowCount() {
		return totalRowCount;
	}

}
