package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnAttribs;
import gov.usgswim.datatable.ColumnAttribsImm;
import gov.usgswim.datatable.ColumnData;


/**
 * Calculate the percentage of one column with respect to another.
 * 
 * Note that this is different that percentage change.
 * 
 * The top part of the percentage fraction is called the 'compareCol' in the
 * code, the 'baseCol' is the bottom of the fraction.  The actual calculation is:
 * (compareCol / baseCol) * 100
 * 
 * There are some odd cases (zeros) that are dealt with in the getDouble()
 * method.
 * 
 * Units are always 'Percentage'.
 * 
 * See AbstractColumnDataView for info on how properties are handled.
 * 
 * @author eeverman
 *
 */
public class PercentageColumnData extends AbstractColumnDataView {

	private static final long serialVersionUID = 1L;
	/** The numerator column of the percentage.  */
	protected ColumnData compareCol;
	
	public PercentageColumnData(ColumnData baseCol, ColumnData compareCol, ColumnData propertiesCol, ColumnAttribs colAttribs) {

		super(baseCol, propertiesCol, colAttribs);
		
		this.compareCol = compareCol;
		
		if (compareCol == null) {
			throw new IllegalArgumentException("The compareCol cannot be null.");
		}
		
		//Use the baseCol for properties if not spec'ed
		if (propertiesCol == null) {
			propertiesCol = baseCol;
		}
		
		if (colAttribs != null) {
			this.colAttribs = colAttribs.toImmutable();
		} else {
			this.colAttribs = new ColumnAttribsImm();
		}
	}
	
	@Override
	public String getUnits() {
		return "Percentage";
	}
	
	/**
	 * The basic calculation is: (compareCol / baseCol) * 100
	 * However, the following zero and null assumptions are made:
	 * 
	 * <ul>
	 * <li>A null value is never returned from this method.
	 * <li>If both values are null, zero is returned.
	 * <li>Any single null value will be considered a zero.
	 * <li>Any positive comp value over a zero base is considered a 100% increase.
	 * <li>Any negative comp value over a zero base is considered a 100% decrease.
	 * </ul>
	 */
	@Override
	public Double getDouble(int row) {
		
		//TODO:  Increase or decreases from zero are considered +/-100%.
		//Is that correct?  JIRA issue filed: http://privusgs4.er.usgs.gov//browse/SPDSS-313
	
		Double b = baseCol.getDouble(row);
		Double c = compareCol.getDouble(row);
		
		if (b == null && c == null) {
			return 0d;
		}
		
		if (b == null) b = 0d;
		if (c == null) c = 0d;
		
		if (b != 0d) {
			return 100d * c / b;
		} else if (c > 0) {
			return 100d;
		} else if (c < 0) {
			return -100d;
		} else {
			return 0d;	// 0 over 0
		}
		
	}
	
	@Override
	public boolean isValid() {
		return compareCol.isValid() && baseCol.isValid();
	}

	@Override
	public ColumnData toImmutable() {
		return this;
	}

}
