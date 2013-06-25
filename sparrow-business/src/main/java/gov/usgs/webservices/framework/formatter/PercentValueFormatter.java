package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;

/**
 * A formatter that converts a number to a percent.
 * Values that are not convertable to numbers and
 * values less than 1 are displayed as "<%1".
 *
 *
 * @author eeverman, cschroed
 */
public class PercentValueFormatter implements ValueFormatter {
	public static final String lessThanOne = "<1%";
	@Override
	public String format(String value) {
		Double percent;
		try{
			percent = Double.parseDouble(value);
		}
		catch (NumberFormatException e){
			percent = 0D;
		}
		if(percent < 1D){
			return lessThanOne;
		}
		else{
			return value;
		}
	}


}
