package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;

/**
 * Creates a dbf file containing an ID column and a data column.
 * 
 * The columns are written in order as follows:
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
 * @author eeverman
 *
 */
public class WriteDbfFile extends Action<File> {

	private final String idColumnName;
	private final ColumnIndex columnIndex;
	private final ColumnData dataColumn;
	private final File outputFile;
	private final ReachRowValueMap rowsToInclude;
	
	/**
	 * All parameters are required to be non-null.  In addition, the idColumn and
	 * dataColumns must have a matching number of rows.
	 * 
	 * @param columnIndex ColumnData instance containing IDs for each row.
	 *	Must be convertible to Integers.
	 * @param dataColumn ColumnData instance containing values for each row.
	 *	Values must be convertible to Double values.
	 * @param outputFile A valid file reference to write to.
	 *	If it exists, it will be overwritten.
	 * @param idColumnName The name to use for the ID column.
	 * @param rowsToInclude Optional.  If specified, the row numbers in this map
	 *	are used to determine what rows to include.
	 */
	public WriteDbfFile(ColumnIndex columnIndex, ColumnData dataColumn, File outputFile, String idColumnName, ReachRowValueMap rowsToInclude) {
		this.columnIndex = columnIndex;
		this.dataColumn = dataColumn;
		this.outputFile = outputFile;
		this.idColumnName = idColumnName;
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
		
		if (outputFile == null) {
			addValidationError("The outputFile cannot be null");
		} else if (! outputFile.isFile()) {
			addValidationError("The outputFile must be a file, not a directory");
		} else if (! outputFile.canWrite()) {
			addValidationError("The outputFile must be writable");
		}
	}

	
	@Override
	public File doAction() throws Exception {

		DbaseFileHeader header = new DbaseFileHeader();
		header.addColumn(idColumnName, 'N', 9, 0);
		header.addColumn("VALUE", 'N', 14, 4);
		
		if (rowsToInclude == null) {
			header.setNumRecords(dataColumn.getRowCount());
		} else {
			header.setNumRecords(rowsToInclude.size());
		}
		

		FileChannel foc = null;
		FileOutputStream fos = null;
		DbaseFileWriter dbfWriter = null;
		try {
			fos = new FileOutputStream(outputFile, false);
			foc = fos.getChannel();
			dbfWriter = new DbaseFileWriter(header, foc, Charset.forName("UTF-8"), null);
		
		
			//Keep reusing the same array for each row
			Object[] oneRow = new Object[2];

			for (int row = 0; row < dataColumn.getRowCount(); row++) {
				
				if (rowsToInclude == null || rowsToInclude.hasRowNumber(row)) {
					
					Long id = columnIndex.getIdForRow(row);

					if (id.longValue() > Integer.MAX_VALUE) {
						throw new Exception("IDs larger than 9 digits are not currently supported b/c our NHD shapefile only has 9 digits.");
					}

					Double val = dataColumn.getDouble(row);

					if (val != null) {
						//Null values are OK, they just fail the next check

						if (val.doubleValue() > 9999999999.9999D) {
							throw new Exception("Values larger than 10 places left of the decimal + 4 right of the decimal not currently supported.");
						}
					}

					//Null and NaN value are OK here, but NaN values are read back as null
					oneRow[0] = id;
					oneRow[1] = val;

					dbfWriter.write(oneRow);
				} else {
					//Just leave this value out of the dbf file.
					//We are mapping a delivery data series and this reach is not
					//upstream of the target(s)
				}
			}
			
			log.debug("Wrote " + dataColumn.getRowCount() + " rows to dbf file, " + outputFile.getAbsolutePath());

			return outputFile;
		} finally {
			
			try {
				if (dbfWriter != null) {
					dbfWriter.close();
				}
				if (foc != null) {
					foc.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException iOException) {
				//Ignore - failure to close only
			}
		}
	}



}
