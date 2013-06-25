package gov.usgs.webservices.framework.formatter;

/**
 * This class wraps html prefixes and suffixes around the output of the
 * PercentValueFormatter
 * @author cschroed, eeverman
 */
public class HTMLRelativePercentCellFormatter implements ValueFormatter{
	private PercentValueFormatter percentFormatter = new PercentValueFormatter();
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
	public HTMLRelativePercentCellFormatter(ValueFormatter baseFormatter,
			String inColumnRelPercentBoxClassName, boolean isFraction) {

		this.baseFormatter = baseFormatter;
		this.inColumnRelPercentBoxClassName =
				((inColumnRelPercentBoxClassName != null)?
				inColumnRelPercentBoxClassName:DEFAULT_REL_PERCENT_IN_COL_CLASS_NAME);
		this.isFraction = isFraction;
	}

	public HTMLRelativePercentCellFormatter(ValueFormatter baseFormatter, boolean isFraction) {
		this(baseFormatter, null, isFraction);
	}

	public HTMLRelativePercentCellFormatter(boolean isFraction) {
		this(new SimpleValueFormatter(IFormatter.OutputType.XHTML), null, isFraction);
	}



	@Override
	public String format(String value) {

		String cleanVal = "";
		double dblVal = 0;
		int percVal = 0;

		try {
			cleanVal = baseFormatter.format(
				percentFormatter.format(value)
				);
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
