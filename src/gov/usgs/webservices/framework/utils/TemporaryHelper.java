package gov.usgs.webservices.framework.utils;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.AcceptedStandardColumnTypes;
import gov.usgswim.datatable.impl.DataTableUtils;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.loader.Analyzer;
import gov.usgswim.sparrow.loader.DataFileDescriptor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public abstract class TemporaryHelper {

	//	static<T extends DataTableWritable> T fillInt(T dt, Data2D data) {
	//		return BuilderHelper.fill(dt, data.getIntData(), null);
	//	}

	//	static<T extends DataTableWritable> T fillDouble(T dt, Data2D data) {
	//		return BuilderHelper.fill(dt, data.getDoubleData(), null);
	//	}

	public static DataTableWritable setIds(DataTableWritable data, int[] ids) {
		//		 TODO [IK] Move to DataTableUtils
		int size = Math.min(data.getRowCount(), ids.length);
		for (int row = 0; row < size; row++) {
			data.setRowId(ids[row], row);
		}
		return data;
	}

	public static DataTableWritable setIds(DataTableWritable data, long[] ids) {
		//	 TODO [IK] Move to DataTableUtils
		int size = Math.min(data.getRowCount(), ids.length);
		for (int row = 0; row < size; row++) {
			data.setRowId(ids[row], row);
		}
		return data;
	}

	public static DataTableWritable setIds(DataTableWritable data, Long[] ids) {
		// TODO [IK] Move to DataTableUtils
		int size = Math.min(data.getRowCount(), ids.length);
		for (int row = 0; row < size; row++) {
			data.setRowId(ids[row], row);
		}
		return data;
	}


	public static long[] getRowIds(DataTable dt) {
		// TODO [ik] reconcile with DataTableUtils
		if (dt != null && dt.hasRowIds()) {
			long[] result = new long[dt.getRowCount()];
			for (int i=0; i<result.length; i++) {
				result[i] = dt.getIdForRow(i);
			}
			return result;
		}
		System.err.println("getRowIds called on a null or a DataTable without Ids. Must populate ids first?");
		return null;
	}

	public static long[] getLongData(DataTable dt, int col) {
		long[] result = new long[dt.getRowCount()];
		for (int i=0; i<result.length; i++) {
			result[i] = dt.getLong(i, col);
		}
		return result;
	}

	/**
	 * Loads a single column from the resultSet source to the Double2D destination table.
	 * For consistency, the from and to columns are ZERO INDEXED in both cases.
	 * 
	 * @param source Resultset to load the data from.  The resultset is assumed to be before the first row.
	 * @param dest The destination Double2D table
	 * @param fromCol The column (zero indexed) in the resultset to load from
	 * @param toCol The column (zero indexed) in the Double2D table to load to
	 * @throws SQLException
	 */
	public static void loadDoubleColumn(ResultSet source, DataTableWritable dest, int fromCol, int toCol) throws SQLException {

		fromCol++;		//covert to ONE base index
		int currentRow = 0;

		while (source.next()){

			Double d = source.getDouble(fromCol);
			dest.setValue(d, currentRow,  toCol);
			currentRow++;

		}

	}

	public static void sampleDataTable(DataTable dt) {
		//		System.out.println("=======");
		//		for (int row=0; row< 3; row++) {
		//			System.out.println();
		//			for (int col=0; col<8; col++) {
		//				System.out.print(dt.getDouble(row, col) + " | ");
		//			}
		//		}
		//		System.out.println("=======");
	}

	public static void printDataTable(DataTable dt, String caption) throws IOException {
		printDataTable(dt, caption, System.out);
	}

	public static void printDataTable(DataTable dt, String caption, PrintStream stream) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(stream));
		printDataTable(dt, caption, writer);
	}

	public static void printDataTable(DataTable dt, String caption,
			Writer writer) throws IOException {
		if (dt != null) {
			// print headings
			String[] headings = DataTableUtils.getHeadings(dt);
			if (caption != null) {
				writer.write(caption);
				writer.write("\n");
			}
			{	// print headings
				for (String heading: headings) {
					writer.write(heading);
					writer.write("\t");
				}
				writer.write("\n");
			}

			// Print data
			for (int i=0; i < dt.getRowCount(); i++) {
				for (int j=0; j<dt.getColumnCount(); j++) {
					Object value = dt.getValue(i, j);
					writer.write((value == null)? "": value.toString());
					writer.write("\t");
				}
				writer.write("\n");
			}
		}
		writer.flush();
	}

	public static String[] getStringColumn(DataTable data, int col) {
		//		 TODO [IK] move to DataTableUtils
		int size = data.getRowCount();
		String[] result = new String[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getString(row, col);
		}
		return result;
	}

	public static Long[] getLongColumn(DataTable data, int col) {
		// TODO [IK] move to DataTableUtils
		int size = data.getRowCount();
		Long[] result = new Long[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getLong(row, col);
		}
		return result;
	}

	private static DataTableWritable toDataTable(File dataFile, Class[] specifiedColumnTypes,
			boolean isFirstColumnID) throws IOException {
		DataFileDescriptor fileDescription = Analyzer.analyzeFile(dataFile);
		int lengthAdjustment = (isFirstColumnID)? 1: 0;
		if (specifiedColumnTypes == null) specifiedColumnTypes = new Class[fileDescription.getColumnCount() - lengthAdjustment];

		// ------------------------------------------------
		// CONFIGURE THE DATATABLE ACCORDING TO columnTypes
		// ------------------------------------------------
		// Ignore the first column of fileDescription if it is an ID
		Class[] inferredColumnTypes = new Class[fileDescription.getColumnCount() - lengthAdjustment];
		for (int i=lengthAdjustment; i<fileDescription.getColumnCount(); i++) {
			inferredColumnTypes[i-lengthAdjustment] = fileDescription.dataTypes[i].clazz;
		}

		DataTableWritable result = new SimpleDataTableWritable();
		int offset = lengthAdjustment;
		// Create all the columns.
		for (int i=0; i<specifiedColumnTypes.length; i++) {
			ColumnDataWritable column = null;
			// use the specified column type if available. Otherwise, use the inferred type.
			Class columnClass = (specifiedColumnTypes[i] == null)? inferredColumnTypes[i]: specifiedColumnTypes[i];
			AcceptedStandardColumnTypes colType = AcceptedStandardColumnTypes.getColumnType(columnClass);
			colType = (colType == null)? AcceptedStandardColumnTypes.STRING: colType; // default is STRING type


			column = colType.makeNewColumn();
			String columnName = "field_" + i;
			if (fileDescription.hasColumnHeaders()) {
				columnName = fileDescription.getHeaders()[i + offset];
			}

			column.setName(columnName);
			result.addColumn(column);
		}
		// assert(columns is now completely filled with no nulls);
		return fill(result, dataFile, isFirstColumnID, fileDescription.delimiter, fileDescription.hasColumnHeaders());
	}

	public static DataTableWritable fill(DataTableWritable dt, File data, boolean isFirstColID, String delimiter, boolean isFirstLineHeader)
	{
		try
		{    	
			BufferedReader reader = new BufferedReader( new FileReader(data));
			if (isFirstLineHeader) reader.readLine(); // skip the header line
			int row = 0;
			int offset = isFirstColID ? 1 : 0;
			String line = null;
			if (isFirstLineHeader) reader.readLine(); // advance past the header line
			while( ( line = reader.readLine() ) != null) 
			{
				String[] input = line.split(delimiter);
				for(int j = 0; j < line.length(); j++)
				{
					Class type = dt.getDataType(j);
					if(type == Integer.class)
						dt.setValue(Integer.valueOf(input[j + offset]), row, j);
					else
						if(type == Long.class)
							dt.setValue(Long.valueOf(input[j + offset]), row, j);
						else
							if(type == Float.class)
								dt.setValue(Float.valueOf(input[j + offset]), row, j);
							else
								if(type == Double.class)
									dt.setValue(Double.valueOf(input[j + offset]), row, j);
								else
									dt.setValue(input[j + offset], row, j);
				}

				if(isFirstColID)
					dt.setRowId(Long.valueOf(input[0]), row);
				row++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dt;
	}

	public static DataTableWritable toDataTable(File dataFile) throws SQLException, IOException {
		return toDataTable(dataFile, null, false);
	}

	public static float[] extractSortedValues(DataTable data, int columnIndex) {
		int totalRows = data.getRowCount();
		float[] values = new float[totalRows];	
		//Export all values in the specified column to values[] so they can be sorted
		for (int r=0; r<totalRows; r++) {
			values[r] = data.getFloat(r, columnIndex);
		}
	
		Arrays.sort(values);
		return values;
	}

}
