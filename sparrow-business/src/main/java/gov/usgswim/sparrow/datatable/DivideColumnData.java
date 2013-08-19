package gov.usgswim.sparrow.datatable;

import gov.usgs.cida.datatable.view.AbstractColumnDataView;
import gov.usgs.cida.datatable.ColumnAttribs;
import gov.usgs.cida.datatable.ColumnAttribsImm;
import gov.usgs.cida.datatable.ColumnData;

/**
 * A column view that returns the numeratorCol value divided by the
 * denominatorCol for each row.
 * 
 * See the getdouble() method for details on how zero is handled.
 * 
 * See AbstractColumnDataView for info on how properties are handled.
 * 
 * @author eeverman
 *
 */
public class DivideColumnData extends AbstractColumnDataView {

	private static final long serialVersionUID = 1L;
	
	private ColumnData numeratorCol;
	
	/**
	 * Uses the denominator as properties column.
	 * 
	 * @param numeratorCol
	 * @param denominatorCol
	 * @param colAttribs
	 */
	public DivideColumnData(ColumnData numeratorCol, ColumnData denominatorCol, ColumnAttribs colAttribs) {

		super(denominatorCol, denominatorCol, colAttribs);
		
		this.numeratorCol = numeratorCol;
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	/**
	 * Uses the propertiesCol as properties column.
	 * 
	 * @param numeratorCol
	 * @param denominatorCol
	 * @param propertiesCol The column to use for properties (can be a reference
	 *  to one of the other columns)
	 * @param colAttribs
	 */
	public DivideColumnData(ColumnData numeratorCol, ColumnData denominatorCol,
			ColumnData propertiesCol, ColumnAttribs colAttribs) {

		super(denominatorCol, propertiesCol, colAttribs);
		
		this.numeratorCol = numeratorCol;
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}

	/**
	 * The basic calculation is: numeratorCol / denominatorCol
	 * However, the following zero and null assumptions are made:
	 * 
	 * <ul>
	 * <li>A null value is never returned from this method.
	 * <li>If both values are null, zero is returned.
	 * <li>Any single null value will be considered a zero.
	 * <li>Zero over zero is considered zero.
	 * <li>Any other value over zero is NaN.
	 * </ul>
	 */
	@Override
	public Double getDouble(int row) {
		
		//TODO:  Increase or decreases from zero are considered +/-100%.
		//Is that correct?  JIRA issue filed: http://privusgs4.er.usgs.gov//browse/SPDSS-313
	
		Double n = numeratorCol.getDouble(row);
		Double d = baseCol.getDouble(row);
		
		
		if (d == null && n == null) {
			return 0d;
		}
		
		if (d == null) d = 0d;
		if (n == null) n = 0d;
		
		if (d != 0d) {
			return n / d;
		} else if (n == 0) {
			return 0d;	// 0 over 0
		} else {
			return Double.NaN;	//non-zero over zero
		}
		
	}
	
	@Override
	public Object getValue(int row) {
		return getDouble(row);
	}

	@Override
	public boolean isValid() {
		return baseCol.isValid() && numeratorCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
