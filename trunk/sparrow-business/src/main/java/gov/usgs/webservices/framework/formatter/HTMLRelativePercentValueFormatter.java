package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;

/**
 * A formatter that creates a chunk of html that can be easily styled to show
 * a box partially filled.
 * This formatter assumes an HTML, XHTML or similar output format.
 * 
 * Values that are not convertable to numbers are as zero.
 * 
 * @author eeverman
 */
public class HTMLRelativePercentValueFormatter implements ValueFormatter {

	public static final String DEFAULT_REL_PERCENT_IN_COL_CLASS_NAME = "rel-percent-in-col";
	
	private final ValueFormatter baseFormatter;
	private final String inColumnRelPercentBoxClassName;
	private final boolean isFraction;
	
	/**
	 * 
	 * @param inColumnRelPercentBoxClassName The html class name used for the div tag
	 *that creates the relative percentage box for rel percentages within a column.
	 * If null, 'rel-percent-in-col' is used.
	 * @param isFraction If true, the value is considered a fraction of 1.  If false, a percentage.
	 */
	public HTMLRelativePercentValueFormatter(ValueFormatter baseFormatter, 
			String inColumnRelPercentBoxClassName, boolean isFraction) {
		
		this.baseFormatter = baseFormatter;
		this.inColumnRelPercentBoxClassName =
				((inColumnRelPercentBoxClassName != null)?
				inColumnRelPercentBoxClassName:DEFAULT_REL_PERCENT_IN_COL_CLASS_NAME);
		this.isFraction = isFraction;
	}
	
	public HTMLRelativePercentValueFormatter(ValueFormatter baseFormatter, boolean isFraction) {
		this(baseFormatter, null, isFraction);
	}
	
	public HTMLRelativePercentValueFormatter(boolean isFraction) {
		this(new SimpleValueFormatter(OutputType.XHTML), null, isFraction);
	}
	
	
	
	@Override
	public String format(String value) {
		
		String cleanVal = "";
		double dblVal = 0;
		int percVal = 0;
		
		try {
			cleanVal = baseFormatter.format(value);
			dblVal = Double.parseDouble(value);
			
			if (isFraction) {
				percVal = (int)(dblVal * 100);
			} else {
				percVal = (int)dblVal;
			}
		} catch (Exception e) {
			//Ignore
		}
		
		
		
		String html =
				"<td class=\"" + inColumnRelPercentBoxClassName + "\"><div style=\"width:" + percVal + "%;\">"
				+ "<span>" + cleanVal + "</span>"
				+ "</div></td>";
		
		
		return html;
	}
}
