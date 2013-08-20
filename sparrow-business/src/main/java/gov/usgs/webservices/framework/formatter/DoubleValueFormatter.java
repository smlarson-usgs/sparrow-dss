package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import java.text.DecimalFormat;

/**
 * Attempts to format a String value as a Decimal number, then encodes it in the
 * specified output type.
 * 
 * If formatting fails (i.e., its not a number) it is treated as a String.
 * 
 * Instances are not threadsafe.
 * 
 * @author eeverman
 */
public class DoubleValueFormatter extends SimpleValueFormatter {
	DecimalFormat numFormatter;
	
	public DoubleValueFormatter(OutputType outputType) {
		super(outputType);
		
		numFormatter = new DecimalFormat();
		
		//Defaults
		numFormatter.setGroupingSize(3);
		numFormatter.setGroupingUsed(true);
		numFormatter.setMaximumFractionDigits(5);
	}
	
	/**
	 * Constructs an instance w/ decimal format options.
	 * If a format option is null, it will not be set and the default
	 * java.text.DecimalFormat behavior for that option will take place.
	 * 
	 * @param outputType Required output type - allows encoding for CSV, XML, TSV, etc.
	 * @param groupingSize ref DecimalFormat option:  Number of digits in a grouping 123,456, etc.
	 * @param maximumFractionDigits ref DecimalFormat option:  Max digits behind the decimal.
	 */
	public DoubleValueFormatter(OutputType outputType, Integer groupingSize, Integer maximumFractionDigits) {
		super(outputType);
		
		numFormatter = new DecimalFormat();
		
		if (groupingSize != null) {
			numFormatter.setGroupingSize(groupingSize);
			numFormatter.setGroupingUsed(true);
		} else {
			numFormatter.setGroupingUsed(false);
		}
		
		if (maximumFractionDigits != null) {
			numFormatter.setMaximumFractionDigits(maximumFractionDigits);
		}
	}
	
	@Override
	public String format(String rawValue) {
		
		String formattedValue = null;
		
		if (rawValue != null) {
			try{
				Double numValue = Double.parseDouble(rawValue);
				formattedValue = numFormatter.format(numValue);
			} catch(NumberFormatException e){
				formattedValue = rawValue;	//Just allow the original string to be used
			}
		}
		
		formattedValue = super.format(formattedValue);	//encodes for output type
		return formattedValue;
	}
}
