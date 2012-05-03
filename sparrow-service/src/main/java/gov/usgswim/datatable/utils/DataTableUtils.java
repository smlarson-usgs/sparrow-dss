package gov.usgswim.datatable.utils;

import static java.sql.Types.BIGINT;
import static java.sql.Types.CHAR;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.REAL;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.VARCHAR;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTableUtils {
	public static final Map<Integer, Class<?>> sqlTypeToJavaType = new HashMap<Integer, Class<?>>();
	static {	// Recommended mappings from http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html
		//		sqlTypeToJavaType.put(BIT, Boolean.class);
		//		sqlTypeToJavaType.put(TINYINT, Byte.class);
		//		sqlTypeToJavaType.put(SMALLINT, Short.class); these don't work yet
		sqlTypeToJavaType.put(INTEGER, Integer.class);
		sqlTypeToJavaType.put(BIGINT, Long.class);
		sqlTypeToJavaType.put(FLOAT, Double.class); // this seems wrong but is correct
		sqlTypeToJavaType.put(REAL, Float.class);
		sqlTypeToJavaType.put(DOUBLE, Double.class);
		sqlTypeToJavaType.put(NUMERIC, Double.class); // recommended is BigDecimal
		sqlTypeToJavaType.put(DECIMAL, Double.class); // recommended is BigDecimal
		sqlTypeToJavaType.put(CHAR, String.class);
		sqlTypeToJavaType.put(VARCHAR, String.class);
		sqlTypeToJavaType.put(LONGVARCHAR, String.class);
		sqlTypeToJavaType.put(DATE, String.class); // recommended is java.sql.Date, subclassing java.util.Date
		sqlTypeToJavaType.put(TIME, String.class); // recommended is java.sql.Time, subclassing java.util.Date
		sqlTypeToJavaType.put(TIMESTAMP, String.class); // recommmended is java.sql.TimeStamp, subclassing java.util.Date
	}

	/**
	 * static variable to control whether exceptions are thrown
	 */
	public static volatile boolean FAIL_SILENTLY = false;

	
	// ------------------------
	//
	// ------------------------
	/**
	 * Copies the data of a DataTable with name, description, and units. Properties are omitted
	 * @param dt
	 * @return
	 */
	public static DataTableWritable cloneBaseStructure(DataTable dt) {
		int cols = dt.getColumnCount();
		DataTableWritable result = new SimpleDataTableWritable();
		for (int col=0; col<cols; col++) {
			String name = dt.getName(col);
			String units = dt.getUnits(col);
			Class<?> type = null;
			try {
				// Trying to get a datatype from an empty column which hasn't
				// had its type explicitly specified will throw an error.
				// In that case, we make a best guess as a generic number type
				type = dt.getDataType(col);
			} catch (Exception e) {
				if (!FAIL_SILENTLY) {
					System.out.println("WARNING: Could not clone type of column " + name);
					e.printStackTrace();
				}
			}

			ColumnDataWritable column = null;
			if (type == String.class) {
				column = new StandardStringColumnDataWritable(name, units);
			} else if ( type == null) {
				column = new StandardNumberColumnDataWritable<Number>(name, units);
			} else { // a specific number class has been assigned
				StandardNumberColumnDataWritable<Number> numColumn = new StandardNumberColumnDataWritable<Number>(name, units);
				numColumn.setType(type);
				column = numColumn;
			}
			column.setDescription(dt.getDescription(col));
			result.addColumn(column);
		}
		return result;
	}

	// TODO public static copyProperties(DataTable source, DataTableWritable target);

	/**
	 * Returns empty list if the structures match, or a List<String> of error
	 * messages where corresponding columns do not match. Properties other than
	 * name, units, and description are ignored in the comparison
	 *
	 * @param dt1
	 * @param dt2
	 * @return empty list if match
	 */
	public static List<String> compareColumnStructure(DataTable dt1, DataTable dt2) {

		List<String> diagnosis = new ArrayList<String>();

		int minColumns = dt1.getColumnCount();
		if (dt2.getColumnCount() < minColumns) {
			minColumns = dt2.getColumnCount();
			diagnosis.add("The first table has more columns than the second");
		} else if (dt2.getColumnCount() > minColumns) {
			diagnosis.add("The second table has more columns than the first");
		}
		for (int i=0; i<minColumns; i++) {
			if (dt1.getName(i) != dt2.getName(i)) {
				diagnosis.add("column " + i + " names do not match: " + dt1.getName() + " != " + dt2.getName());
			}
			if (dt1.getUnits(i) != dt2.getUnits(i)) {
				diagnosis.add("column " + i + " units do not match: " + dt1.getUnits(i) + " != " + dt2.getUnits(i));
			}
			if (dt1.getDescription(i) != dt2.getDescription(i)) {
				diagnosis.add("column " + i + " descriptions do not match: " + dt1.getDescription(i) + " != " + dt2.getDescription(i));
			}
			{	// Compare column types
				Class<?> dt1Type = null, dt2Type = null;
				try {
					dt1Type = dt1.getDataType(i);
				} catch (Exception e) {/* ignore type not yet defined exception*/ }

				try {
					dt2Type = dt2.getDataType(i);
				} catch (Exception e) { /* ignore type not yet defined exception*/ }

				if (dt1Type != dt2Type) {
					diagnosis.add("column " + i + " types do not match: " + dt1Type + " != " + dt2Type);
				}
			}
		}
		return diagnosis;
	}


	// -----------------------------
	// Header data retrieval methods
	// -----------------------------
	public static String[] getHeadings(DataTable dt) {
		String[] result = null;
		if (dt!= null && dt.getColumnCount() > 0) {
			result = new String[dt.getColumnCount()];
			for (int i=0; i<result.length; i++) {
				result[i] = dt.getName(i);
			}
		}
		return result;
	}

	// --------------------------
	// Row data retrieval methods
	// --------------------------
	public static int[] getIntRow(DataTable source, int row){
		int[] result = new int[source.getColumnCount()];
		for (int j=0; j<result.length; j++) {
			result[j] = source.getInt(row, j);
		}
		return result;
	}

	public static double[] getDoubleRow(DataTable source, int row){
		double[] result = new double[source.getColumnCount()];
		for (int j=0; j<result.length; j++) {
			result[j] = source.getDouble(row, j);
		}
		return result;
	}

	// -----------------------------
	// Column data retrieval methods
	// -----------------------------
	public static int[] getIntColumn(DataTable data, int col) {
		int size = data.getRowCount();
		int[] result = new int[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getInt(row, col);
		}
		return result;
	}

	public static double[] getDoubleColumn(DataTable source, int col){
		double[] result = new double[source.getRowCount()];
		for (int i=0; i<result.length; i++) {
			result[i] = source.getDouble(i, col);
		}
		return result;
	}

	/**
	 * @param data
	 * @param col
	 * @return
	 */
	public static Long[] getWrappedLongColumn(DataTable data, int col) {
		int size = data.getRowCount();
		Long[] result = new Long[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getLong(row, col);
		}
		return result;
	}

	/**
	 * @param data
	 * @param col
	 * @return
	 */
	public static long[] getLongColumn(DataTable data, int col) {
		int size = data.getRowCount();
		long[] result = new long[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getLong(row, col);
		}
		return result;
	}
	/**
	 * @param source
	 * @return null if source is null or does not have IDs
	 */
	public static Long[] getWrappedRowIds(DataTable source) {
		Long[] result = null;
		if (source != null && source.hasRowIds()) {
			result = new Long[source.getRowCount()];
			for (int row = 0; row < source.getRowCount(); row++) {
				result[row] = source.getIdForRow(row);
			}
		}
		return result;
	}

	/**
	 * @param source
	 * @return null if source is null or does not have IDs
	 */
	public static long[] getRowIds(DataTable source) {
		long[] result = null;
		if (source != null && source.hasRowIds()) {
			result = new long[source.getRowCount()];
			for (int row = 0; row < source.getRowCount(); row++) {
				result[row] = source.getIdForRow(row);
			}
		}
		return result;
	}

	public static String[] getStringColumn(DataTable data, int col) {
		int size = data.getRowCount();
		String[] result = new String[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getString(row, col);
		}
		return result;
	}

	/**
	 * inferTypes() analyzes the metadata for the ResultSet to guess at the
	 * datatypes. Nulls are returned in case of an unrecognized SQL type
	 *
	 * @param rset
	 * @return
	 * @throws SQLException
	 */
	public static Class<?>[] inferTypes(ResultSet rset) throws SQLException {
		return inferTypes(rset.getMetaData());
	}

	/**
	 * inferTypes() analyzes the metadata for the resultset to guess at the
	 * datatypes. Nulls are returned in case of an unrecognized SQL type
	 *
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public static Class<?>[] inferTypes(ResultSetMetaData metaData) throws SQLException {
		Class<?>[] inferredTypes = new Class[metaData.getColumnCount()];
		for (int i=0; i<inferredTypes.length; i++){
			int sqlType = metaData.getColumnType(i+1);

			Class<?> nominalType = sqlTypeToJavaType.get(sqlType);
			if ((sqlType == NUMERIC) || (sqlType == FLOAT) || (sqlType == REAL) || (sqlType == DOUBLE) ||(sqlType == DECIMAL) ) {
				// This works for Oracle, but I'm not sure about other database metadata
				// Numeric types with 0 fractional digits are integers
				int precis = metaData.getPrecision(i+1);
				int scale = metaData.getScale(i+1);

				if (scale == 0) {
					if (precis > 9) {
						nominalType = Long.class;
					} else {
						nominalType = Integer.class;
					}
				}
			}
			inferredTypes[i] = nominalType;
		}
		return inferredTypes;
	}

	// ----------
	// ID setters
	// ----------
	public static DataTableWritable setIds(DataTableWritable data, int[] ids) {
		int size = Math.min(data.getRowCount(), ids.length);
		for (int row = 0; row < size; row++) {
			data.setRowId(ids[row], row);
		}
		return data;
	}

	public static DataTableWritable setIds(DataTableWritable data, long[] ids) {
		int size = Math.min(data.getRowCount(), ids.length);
		for (int row = 0; row < size; row++) {
			data.setRowId(ids[row], row);
		}
		return data;
	}

	public static DataTableWritable setIds(DataTableWritable data, Long[] ids) {
		int size = Math.min(data.getRowCount(), ids.length);
		for (int row = 0; row < size; row++) {
			data.setRowId(ids[row], row);
		}
		return data;
	}




	// --------------------
	// Data loading methods
	// --------------------
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

	public static DataTableWritable fill(DataTableWritable dt, String resourceName, boolean isFirstColID, String delimiter, boolean isFirstLineHeader)
	{
		InputStream stream = null;
		try {
			
			//There seem to be two ways to get a resource, each of which works
			//in different situations... will try both
			stream = DataTableWritable.class.getResourceAsStream(resourceName);
			if (stream == null) {
				stream =
					Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			return fill(dt, in, isFirstColID, delimiter, isFirstLineHeader);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return null;
	}

	public static DataTableWritable fill(DataTableWritable dt, File dataFile, boolean isFirstColID, String delimiter, boolean isFirstLineHeader)
	{
		try {
			return fill(dt, new BufferedReader( new FileReader(dataFile)), isFirstColID, delimiter, isFirstLineHeader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static DataTableWritable fill(DataTableWritable dt, BufferedReader dataReader, boolean isFirstColID, String delimiter, boolean isFirstLineHeader)
	{
		try
		{
			if (isFirstLineHeader) dataReader.readLine(); // skip the header line
			int row = 0;
			int offset = isFirstColID ? 1 : 0;
			String line = null;
			while( (line = dataReader.readLine()) != null)
			{
				//Its important that empty tokens be preserved
				String[] input = StringUtils.splitPreserveAllTokens(line, '\t');
				
				for (int i=0; i<input.length; i++) {
					input[i] = StringUtils.trimToNull(input[i]);
				}
				
				for(int srcColIndex = 0; srcColIndex < input.length; srcColIndex++)
				{

					if (isFirstColID && srcColIndex == 0) {
						dt.setRowId(Long.valueOf(input[0]), row);
						continue;	//skip the rest
					}
					
					int outColIndex = srcColIndex - offset;
					Class<?> type = dt.getDataType(outColIndex);
					String val = input[srcColIndex];
					
					if (type == Integer.class) {
						if (val != null) {
							dt.setValue(Integer.valueOf(val), row, outColIndex);
						} else {
							dt.setValue(new Integer(0), row, outColIndex);
						}
					} else if(type == Long.class) {
						if (val != null) {
							dt.setValue(Long.valueOf(val), row, outColIndex);
						} else {
							dt.setValue(new Long(0), row, outColIndex);
						}
					} else if(type == Float.class) {
						if (val != null) {
							dt.setValue(Float.valueOf(val), row, outColIndex);
						} else {
							dt.setValue(new Float(0), row, outColIndex);
						}
					} else if(type == Double.class) {
						if (val != null) {
							dt.setValue(Double.valueOf(val), row, outColIndex);
						} else {
							dt.setValue(new Double(0), row, outColIndex);
						}
					} else {
						dt.setValue(val, row, outColIndex);
					}
									
				}

				row++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dt;
	}
	
	/**
	 * Returns the total for a column.
	 * If the values are all null or there are no rows, zero is returned.
	 * NaN values are ignored.  An infinite value will result in an infinite result.
	 * 
	 * @param tab
	 * @param col
	 * @return 
	 */
	public static double getColumnTotal(DataTable tab, int col) {
		if (col >= tab.getColumnCount() || col < 0) {
			throw new IllegalArgumentException("The specified column is beyond the defined columns.");
		}
		
		int rows = tab.getRowCount();
		double total = 0D;
		
		for (int r=0; r<rows; r++) {
			Double v = tab.getDouble(r, col);
			if (v != null && ! v.isNaN()) {
				total+= v;
			}
		}
		
		return total;
	}

}
