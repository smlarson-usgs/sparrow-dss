package gov.usgs.webservices.framework.formatter;

/**
 *
 * @author eeverman
 */
public class HTMLTableCellFormatter extends SimpleValueFormatter {
	public static final String PREFIX = "<td>";
	public static final String SUFFIX = "</td>";
	
	private ValueFormatter childFormatter;

	/**
	 * Will format based on the specified type, which must be the simple string
	 * version of the basic types Types:
	 * <ul>
	 * <li>Double</li>
	 * <li>Float</li>
	 * <li>Number</li>
	 * <li>Integer</li>
	 * <li>Long</li>
	 * <li>String</li>
	 * </ul>
	 * 
	 * If unrecognized or null, the basic SimpleValueFormatter is used, which
	 * only does output encoding and converts nulls to empty.
	 * 
	 * @param outputType The type of encoding to use for the value
	 * @param type Simple name of a Java Type.
	 */
	public HTMLTableCellFormatter(IFormatter.OutputType outputType, String type) {
		super(outputType);
		
		if (type != null) {
			switch (type) {
				case "Double":
				case "Float":
				case "Number":
					childFormatter = new DoubleValueFormatter(outputType);
					break;
				case "Integer":
				case "Long":
					childFormatter = new IntegerValueFormatter(outputType);
					break;
				default:
					childFormatter = null;	//use superclass

			}
		} else {
			childFormatter = null;	//use superclass
		}
	}
	
	/**
	 * Creates a new formatter using the passed valueFormatter to format the actual
	 * value inside the table cell.  The valueFormatter will handle encoding for
	 * the outputType, so no output type is required here.
	 * @param outputType The type of encoding to use for the value
	 * @param valueFormatter Formatter used to format the actual value.
	 */
	public HTMLTableCellFormatter(ValueFormatter valueFormatter) {
		super(null);
		childFormatter = valueFormatter;
	}

	@Override
	public String format(String value) {

		String formattedVal = null;
		
		if (childFormatter != null) {
			formattedVal = childFormatter.format(value);	//should handle encoding
		} else {
			formattedVal = super.format(value);	//do the encoding only
		}
		return PREFIX + formattedVal + SUFFIX;
	}
}
