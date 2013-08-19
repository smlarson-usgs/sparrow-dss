package gov.usgswim.sparrow.datatable;

import gov.usgs.cida.datatable.view.AbstractColumnDataView;
import gov.usgs.cida.datatable.ColumnAttribs;
import gov.usgs.cida.datatable.ColumnAttribsImm;
import gov.usgs.cida.datatable.ColumnData;

/**
 * Multiplies two columns together to create a new column view.
 * 
 * The primary column is used as the properties column.
 * See AbstractColumnDataView for info on how properties are handled.
 * 
 * @author eeverman
 *
 */
public class MultiplyColumnData extends AbstractColumnDataView {

	private static final long serialVersionUID = 1L;
	
	/** The coefficient column to use */
	private ColumnData coefCol;
	
	/**
	 * The primary column is used as the properties column.
	 * @param primaryColumn
	 * @param coefColumn
	 * @param colAttribs
	 */
	public MultiplyColumnData(ColumnData primaryColumn, ColumnData coefColumn, ColumnAttribs colAttribs) {

		super(primaryColumn, primaryColumn, colAttribs);
		
		this.coefCol = coefColumn;
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}

	@Override
	public Double getDouble(int row) {
		Double base = baseCol.getDouble(row);
		Double coef = coefCol.getDouble(row);
		
		if (base != null && coef != null) {
			return base * coef;
		} else {
			return null;
		}
	}
	
	@Override
	public Object getValue(int row) {
		return getDouble(row);
	}

	@Override
	public boolean isValid() {
		return baseCol.isValid() && coefCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
