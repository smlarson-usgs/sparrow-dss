package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgs.webservices.framework.utils.XMLUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author eeverman
 */
public class SimpleValueFormatter implements ValueFormatter {

	/** The OutputType determines the type of escaping to do */
	OutputType outputType;

	public SimpleValueFormatter(OutputType outputType) {
		this.outputType = outputType;
	}

	@Override
	public String format(String value) {
		if (value == null) {
			return "";
		}

		switch (this.outputType) {
			case CSV:
				value = StringEscapeUtils.escapeCsv(value);
				break;
			case XML: // same as excel
			case EXCEL:
				value = XMLUtils.quickTagContentEscape(value);
				break;
			case DATA:
			case TAB:
				//replace tabs, new line, form feed and carriage return w/ spaces
				value = StringUtils.replaceChars(value, "\t\n\f\r", "    ");
				break;
		}
		return value;
	}
}
