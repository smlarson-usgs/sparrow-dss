package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import static gov.usgswim.sparrow.action.Action.log;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import java.util.HashMap;

/**
 * Creates a dbf file containing an ID column and a data column.
 * 
 * The columns are written in order as follows:
 * HashMap (ID,Value) where the ID is the key 
 * <ol>
 * <li>[ID Column of named specified] : Integer number of 9* digits.
 * <li>VALUE : Decimal number of 14 digits total, four of which are decimal places.
 * </ol>
 * 
 * * Note that 10 digits is the NHD definition of the COMID:
 * http://nhd.usgs.gov/nhd_faq.html#q119
 * this is currently set to 9 digits so that we match the NHD shapefile, which only
 * has 9 digits (thus, is an integer).
 * 
 * Null and NaN values are OK to write, but NaN will be read back as Null.
 * Null ID are never allowed.
 * 
 * @author smlarson
 *
 */
public class GetModelOutputValues extends Action<HashMap> {

	/** Ensures a minimum of 6 digits of precision for all numbers. */
	private static final int REQUIRED_SIGNIFICANT_DIGITS = 6;
	
	/** The value column should have at least one decimal place to force the Double data type */
	private static final int MIN_REQUIRED_DECIMAL_PLACES = 1;
	
	//private final String idColumnName;
	private final ColumnIndex columnIndex;
	private final ColumnData dataColumn;
	//private final File outputFile;
	private final ReachRowValueMap rowsToInclude;
	
	//self init
	/** Give the digit places available, the max value representable in the file */
	private double maxValueInDbf = 0;
	private double minValueInDbf = 0;
	
	/**
	 * All parameters are required to be non-null.  In addition, the idColumn and
	 * dataColumns must have a matching number of rows.
	 * 
	 * @param columnIndex ColumnData instance containing IDs for each row.
	 *	Must be convertible to Integers.
	 * @param dataColumn ColumnData instance containing values for each row.
	 *	Values must be convertible to Double values.
	 * @param rowsToInclude Optional.  If specified, the row numbers in this map
	 *	are used to determine what rows to include.
	 */
	public GetModelOutputValues(ColumnIndex columnIndex, ColumnData dataColumn, ReachRowValueMap rowsToInclude) {
		this.columnIndex = columnIndex;
		this.dataColumn = dataColumn;
	//	this.outputFile = outputFile;
	//	this.idColumnName = idColumnName;
		this.rowsToInclude = rowsToInclude;
	}

	@Override
	protected void validate() {
		if (columnIndex == null) {
			addValidationError("The id column cannot be null");
		} else if (dataColumn == null) {
			addValidationError("The data column cannot be null");
		} else if (! (columnIndex.getMaxRowNumber() == (dataColumn.getRowCount() - 1))) {
			addValidationError("The column index and data column must have the same number of rows");
		}
		
//		if (outputFile == null) {
//			addValidationError("The outputFile cannot be null");
//		} else if (! outputFile.isFile()) {
//			addValidationError("The outputFile must be a file, not a directory");
//		} else if (! outputFile.canWrite()) {
//			addValidationError("The outputFile must be writable");
//		}
	}

	
	@Override
	public HashMap doAction() throws Exception {
                
                HashMap outputMap = new HashMap();
                
	//	DbaseFileHeader header = new DbaseFileHeader();
	//	header.addColumn(idColumnName, 'N', 9, 0);
	//	
		//Determine the number of digits & decimal places
		int leftDigits = getRequiredDigitsLeftOfTheDecimal(
						dataColumn.getMaxDouble(), dataColumn.getMinDouble(), REQUIRED_SIGNIFICANT_DIGITS);
		int rightDigits = getRequiredDigitsRightOfTheDecimal(
						leftDigits, REQUIRED_SIGNIFICANT_DIGITS, MIN_REQUIRED_DECIMAL_PLACES);
		
		
	//	header.addColumn("VALUE", 'N', leftDigits + rightDigits, rightDigits);
		
		
		//Build the max value possible w/ this number of digits
		maxValueInDbf = getMaxValueforDigits(leftDigits, rightDigits);
		minValueInDbf = (-1d) * maxValueInDbf;

		
	//	if (rowsToInclude == null) {
	//		header.setNumRecords(dataColumn.getRowCount());
	//	} else {
	//		header.setNumRecords(rowsToInclude.size());
	//	}
		

	//	FileChannel foc = null;
	//	FileOutputStream fos = null;
	//	DbaseFileWriter dbfWriter = null;
//		try {
//			fos = new FileOutputStream(outputFile, false);
//			foc = fos.getChannel();
//			dbfWriter = new DbaseFileWriter(header, foc, Charset.forName("UTF-8"), null);
		
		
			//Keep reusing the same array for each row
			Object[] oneRow = new Object[2]; // SPDSSII-28 #TODO# write out the dbf file to the postgres model_output table

			for (int row = 0; row < dataColumn.getRowCount(); row++) {
				
				if (rowsToInclude == null || rowsToInclude.hasRowNumber(row)) {
					
					Long id = columnIndex.getIdForRow(row);

					if (id.longValue() > Integer.MAX_VALUE) {
						throw new Exception("IDs larger than 9 digits are not currently supported b/c our NHD shapefile only has 9 digits.");
					}

					Double val = dataColumn.getDouble(row);

					if (val != null) {
						//Null values are OK, they just fail the next check

						if (val.doubleValue() > maxValueInDbf) {
							val = maxValueInDbf;
							log.warn("A + infinity value was writtin to the dbf file to be joined"
											+ " to a shapefile for mapping, however, the max representable"
											+ " value is " + maxValueInDbf + ", which was used instead.");
						} else if (val.doubleValue() < minValueInDbf) {
							val = minValueInDbf;
							log.warn("A - infinity value was writtin to the dbf file to be joined"
											+ " to a shapefile for mapping, however, the min representable"
											+ " value is " + minValueInDbf + ", which was used instead.");
						}
					}

					//Null and NaN value are OK here, but NaN values are read back as null
					oneRow[0] = (int)id.intValue(); 
					oneRow[1] = val;

					//dbfWriter.write(oneRow);
                                        outputMap.put((int)id.intValue(), val);//this is an int to match the shapefile. See note above.
                                        //SPDSSII-28 load the hashmap with the values for this dbf file (river network, modelnbr, generatated hash for the name of the dbf file)
                                        
				} else {
					//Just leave this value out of the dbf file.
					//We are mapping a delivery data series and this reach is not
					//upstream of the target(s)
				}
			}
			
			//log.debug("Wrote " + dataColumn.getRowCount() + " rows to dbf file, " + outputFile.getAbsolutePath());
                        log.debug("Wrote " + dataColumn.getRowCount() + " rows to hash map. ");
			//return outputFile; 
                        return outputMap;
//		} finally {
//			
//			try {
//				if (dbfWriter != null) {
//					dbfWriter.close();
//				}
//				if (foc != null) {
//					foc.close();
//				}
//				if (fos != null) {
//					fos.close();
//				}
//			} catch (IOException iOException) {
//				//Ignore - failure to close only
//			}
//		}
	}

	/**
	 * Returns the number of digits to the left of the decimal needed to represent
	 * the range of numbers described by the min and max values.
	 * 
	 * Null or NaN values are treated as zero.  If both the min and max values are
	 * infinite, the number of required digits is (arbitrarily) returned as
	 * 3x the number of requiredSignificantDigits. 
	 * 
	 * The minimum return value is 1, since for our dbf library, it always reserves
	 * at least one integer digit.
	 * 
	 * @param minValue
	 * @param maxValue
	 * @param requiredSignificantDigits
	 * @return 
	 */
	public static int getRequiredDigitsLeftOfTheDecimal(Double minValue, Double maxValue, int requiredSignificantDigits) {
		if (minValue == null || Double.isNaN(minValue)) minValue = 0d;
		if (maxValue == null || Double.isNaN(maxValue)) maxValue = 0d;

		minValue = Math.abs(minValue);
		maxValue = Math.abs(maxValue);

		double absMaxValue = Math.max(minValue, maxValue);

		if (absMaxValue < 1) {
			return 1;
		} else if (Double.isInfinite(absMaxValue)) {
			return requiredSignificantDigits * 3;
		}	else {
						double log10OfMax = Math.log10(absMaxValue);
			return (int)log10OfMax + 1;
		}

	}
	
	/**
	 * Calculates the number of decimal places (right of the decimal) required to
	 * give the desired number of significant digits, given the number of digits
	 * to the left of the decimal needed to hold the larger numbers.
	 * 
	 * In the cases that either no decimal places are available (all available
	 * sigfigs are already used to the left of the decimal) or all values are zero,
	 * the minRequiredDecimalPlaces is returned.
	 * 
	 * @param requiredDigitsLeftOfTheDecimal
	 * @param requiredSignificantDigits
	 * @param minRequiredDecimalPlaces If not decimal places are req'ed based on sigfigs, set another minimum.
	 * @return 
	 */
	public static int getRequiredDigitsRightOfTheDecimal(
					int requiredDigitsLeftOfTheDecimal, int requiredSignificantDigits, int minRequiredDecimalPlaces) {
		
		if (requiredDigitsLeftOfTheDecimal == 0 && requiredSignificantDigits == 0) {
			return minRequiredDecimalPlaces;
		} else {
			int decimalPlaces = requiredSignificantDigits - requiredDigitsLeftOfTheDecimal;
			if (decimalPlaces < 0) decimalPlaces = 0;
			return Math.max(decimalPlaces, minRequiredDecimalPlaces);
		}
	}
	
	/**
	 * Given a number of integer digits (left of decimal) and decimal digits
	 * (right of decimal), the returned number is the max number that can be
	 * represented.
	 * 
	 * If the digits are both zero, zero is returned.
	 * 
	 * Example return value:  9999.99
	 * 
	 * @param integerDigits
	 * @param decimalDigits
	 * @return 
	 */
	public static double getMaxValueforDigits(int integerDigits, int decimalDigits) {
		
		if (integerDigits < 1 && decimalDigits < 1) {
			return 0d;
		}
		
		String maxValueStr = "";
		for (int i=0; i < integerDigits; i++) {
			maxValueStr+="9";
		}
		maxValueStr+=".";
		for (int i=0; i < decimalDigits; i++) {
			maxValueStr+="9";
		}
		
		return Double.parseDouble(maxValueStr);
	}

}

