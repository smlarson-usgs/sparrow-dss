package gov.usgswim.datatable.view;

import gov.usgswim.datatable.ColumnAttribs;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;

/**
 * A simple column view that simply allows the column name, description and
 * other properties to be redefined without affecting the underlying column.
 * 
 * @author eeverman
 */
public class RenameColumnDataView extends AbstractColumnDataView {
	
	public RenameColumnDataView(ColumnData baseCol, ColumnAttribs colAttribs) {
		super(baseCol, baseCol, colAttribs);
	}
		
	@Override
	public Double getDouble(int row) {
		return baseCol.getDouble(row);
	}
	
	@Override
	public Object getValue(int row) {
		return baseCol.getValue(row);
	}

	@Override
	public boolean isValid() {
		return baseCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		if (baseCol instanceof ColumnDataWritable) {
			ColumnData cdImm = baseCol.toImmutable();
			return new RenameColumnDataView(cdImm, colAttribs);
		} else {
			return this;
		}
	}

	@Override
	public Integer getRowCount() {
		return baseCol.getRowCount();
	}

}
