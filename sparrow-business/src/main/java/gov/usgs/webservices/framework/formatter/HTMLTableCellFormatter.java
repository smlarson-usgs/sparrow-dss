package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.utils.XMLUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author eeverman
 */
public class HTMLTableCellFormatter extends SimpleValueFormatter {

	public HTMLTableCellFormatter(IFormatter.OutputType outputType) {
		super(outputType);
	}
	
	@Override
	public String format(String value) {
		return "<td>" + super.format(value) + "</td>";
	}
}
