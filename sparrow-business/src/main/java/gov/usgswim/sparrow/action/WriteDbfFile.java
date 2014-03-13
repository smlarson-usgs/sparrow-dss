package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
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
 * @author eeverman
 *
 */
public class WriteDbfFile extends Action<File> {

	private String idColumnName;
	private ColumnData idColumn;
	private ColumnData dataColumn;
	private File outputFile;
	
	/**
	 * All parameters are required to be non-null.  In addition, the idColumn and
	 * dataColumns must have a matching number of rows.
	 * 
	 * @param idColumn ColumnData instance containing IDs for each row.
	 *	Must be convertible to Integers.
	 * @param dataColumn ColumnData instance containing values for each row.
	 *	Values must be convertible to Double values.
	 * @param outputFile A valid file reference to write to.
	 *	If it exists, it will be overwritten.
	 * @param idColumnName The name to use for the ID column.
	 */
	public WriteDbfFile(ColumnData idColumn, ColumnData dataColumn, File outputFile, String idColumnName) {
		this.idColumn = idColumn;
		this.dataColumn = dataColumn;
		this.outputFile = outputFile;
		this.idColumnName = idColumnName;
	}

	@Override
	protected void validate() {
		if (idColumn == null) {
			addValidationError("The id column cannot be null");
		} else if (dataColumn == null) {
			addValidationError("The data column cannot be null");
		} else if (! idColumn.getRowCount().equals(dataColumn.getRowCount())) {
			addValidationError("The id column and data column must have the same number of rows");
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
		header.setNumRecords(idColumn.getRowCount());
		

		FileChannel foc = null;
		FileOutputStream fos = null;
		DbaseFileWriter dbfWriter = null;
		try {
			fos = new FileOutputStream(outputFile, false);
			foc = fos.getChannel();
			dbfWriter = new DbaseFileWriter(header, foc, Charset.forName("UTF-8"), null);
		
		
			//Keep reusing the same array for each row
			Object[] oneRow = new Object[2];

			for (int row = 0; row < idColumn.getRowCount(); row++) {
				
				Object idVal = idColumn.getValue(row);
				Long id = null;
				if (idVal instanceof Number) {
					id = ((Number)idVal).longValue();
					
				} else {
					String s = idVal.toString();
					id = Long.parseLong(s);
				}
				
				if (id.longValue() > Integer.MAX_VALUE) {
					throw new Exception("IDs larger than 9 digits are not currently supported b/c our NHD shapefile only has 9 digits.");
				}
				oneRow[0] = id;
				oneRow[1] = dataColumn.getDouble(row);
				
				dbfWriter.write(oneRow);
			}
			
			log.debug("Wrote " + idColumn.getRowCount() + " rows to dbf file, " + outputFile.getAbsolutePath());

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
