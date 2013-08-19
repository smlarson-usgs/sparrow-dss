package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.utils.XMLUtils;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author eeverman
 */
public class HTMLTableCellFormatter extends SimpleValueFormatter {
	public static final String PREFIX = "<td>";
	public static final String SUFFIX = "</td>";

	public HTMLTableCellFormatter(IFormatter.OutputType outputType) {
		super(outputType);
	}

	@Override
	public String format(String value) {
		Double numValue = null;
		boolean isNumericValue = true;
		try{
			numValue = Double.parseDouble(value);
		}
		catch(NumberFormatException e){
			isNumericValue = false;
		}
		if(isNumericValue){
			DecimalFormat numFormatter = new DecimalFormat();
			numFormatter.setGroupingSize(3);
			numFormatter.setGroupingUsed(true);
			numFormatter.setMaximumFractionDigits(5);
			value = numFormatter.format(Double.parseDouble(value));
		}
		return PREFIX + super.format(value) + SUFFIX;
	}
}
