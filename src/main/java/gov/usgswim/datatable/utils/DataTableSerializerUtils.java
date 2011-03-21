package gov.usgswim.datatable.utils;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes DataTables to String, files and streams.
 * 
 * TODO:  Nulls are supported for string columns and column metadata, but is not
 * fully supported for numeric columns.  This class is basically doing the right
 * thing, but the Numeric columns do not do null checking so would likely result
 * in NullPointers if used.
 * 
 * TODO: Arbitrary table and column properties are not serialized.
 * 
 * @author eeverman
 */
public class DataTableSerializerUtils {
	
	/** String used to encode a null value */
	public static final String NULL_STRING = "[null]";
	
	private static Map<String, Class<?>> acceptableColumnTypes = new HashMap<String, Class<?>>();
	static {
		acceptableColumnTypes.put("java.lang.String", String.class);
		acceptableColumnTypes.put("java.lang.Integer", Integer.class);
		acceptableColumnTypes.put("java.lang.Long", Long.class);
		acceptableColumnTypes.put("java.lang.Float", Float.class);
		acceptableColumnTypes.put("java.lang.Double", Double.class);
	}

	/*
	 * The SERIALIZATION FORMAT:
	 * Line 0: global table properties, followed by column headers and its properties
	 * Line 1+: data, tab-delimited
	 *
	 * The purpose is to maintain maximum graceful fail compatibility with simple
	 * printDatatable() output, and simple tab-delimited file output for excel.
	 */

	/**
	 * Serializes a table to the provided StreamBuilder. This method is not
	 * recommended for large DataTables as the StringBuilder can consume much
	 * memory. For large tables, use one other streaming versions of
	 * serializeToText() instead.
	 *
	 * @param dt
	 * @param sb
	 * @return
	 */
	public static StringBuilder serializeToText(DataTable dt, StringBuilder sb) {
		sb.append(serializeGlobalMetadata(dt));
		sb.append(serializeColumnMetadata(dt));

		sb.append("\n");
		sb.append(serializeDataRows(dt, null, null));
		return sb;
	}

	public static void serializeToText(DataTable dt, OutputStream stream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(stream);
		serializeToText(dt, writer);
	}

	public static void serializeToText(DataTable dt, Writer writer) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(serializeGlobalMetadata(dt));
		sb.append(serializeColumnMetadata(dt));

		sb.append("\n");
		writer.write(sb.toString());

		for (int row=0; row < dt.getRowCount(); row = row + 200) {
			int startRowIndex = row;
			int endRowIndex = Math.min(row + 200, dt.getRowCount() );
			sb = serializeDataRows(dt, startRowIndex, endRowIndex);
			writer.write(sb.toString());
		}
	}

	public static DataTableWritable deserializeFromText(CharSequence source) throws IOException {
		return deserializeFromText(new StringReader(source.toString()));
	}

	public static DataTableWritable deserializeFromText(InputStream source) throws IOException {
		InputStreamReader reader = new InputStreamReader(source);
		return deserializeFromText(reader);
	}

	public static DataTableWritable deserializeFromText(Reader source) throws IOException {
		BufferedReader reader = new BufferedReader(source);
		String line = reader.readLine();

		String[] columnHeadersMetaInfo = line.split("\t");
		//Map<String, String> globalMetaData = parseAsMap(columnHeadersMetaInfo[0],";");

		List<Map<String, String>> metaInfo = new ArrayList<Map<String, String>>();
		for (String info: columnHeadersMetaInfo) {
			metaInfo.add(parseAsMap(info, ";"));
		}

		// construct the table structure
		SimpleDataTableWritable dt = new SimpleDataTableWritable();
		Map<String, String> globalMetaData = metaInfo.get(0);
		dt.setName(globalMetaData.get("name"));
		dt.setDescription(globalMetaData.get("desc"));
		Class<?>[] types = new Class[metaInfo.size()]; // Yeah, so it's one bigger that the real width, so what?

		for (int col = 1; col<metaInfo.size(); col++) {
			Map<String, String> info = metaInfo.get(col);

			String name = (NULL_STRING.equalsIgnoreCase(info.get("name")))?null:info.get("name");
			String description = (NULL_STRING.equalsIgnoreCase(info.get("desc")))?null:info.get("desc");
			String units = (NULL_STRING.equalsIgnoreCase(info.get("units")))?null:info.get("units");
			
			Map<String, Class<?>> debugtypes = acceptableColumnTypes;
			Class<?> type = debugtypes.get(info.get("type"));

			ColumnDataWritable column = null;
			if (type == String.class) {
				column = new StandardStringColumnDataWritable(name, units);
			} else {
				StandardNumberColumnDataWritable<Number> numericColumn = new StandardNumberColumnDataWritable<Number>(name, units);
				numericColumn.setType(type);
				column = numericColumn;
			}
			column.setDescription(description);
			dt.addColumn(column);
			if (type == null) {
				// By default, type is assumed to be a number
				type = Number.class;
			}

			types[col] = type; // record this for efficiency
		}

		// Populate the table data
		boolean hasRowIds = Boolean.parseBoolean(globalMetaData.get("hasRowIds"));
		int row = 0;
		while ( ( line=reader.readLine() ) != null) {
			String[] data = line.split("\t");
			if (hasRowIds) {
				Long id = Long.parseLong(data[0]); // likely to be expensive ... optimize later
				dt.setRowId(id, row);
			}

			for (int col=1; col < data.length; col++) {
				Class<?> type = types[col];
				String s = data[col];
				boolean isNull = NULL_STRING.equals(s);
				if (isNull) s = null;
				
				try {
					if (type == String.class) {
						dt.setValue(s, row, col - 1);
					} else if (type == Double.class) {
						if (! isNull) {
							dt.setValue(Double.parseDouble(s), row, col - 1);
						} else {
							dt.setValue((Double) null, row, col - 1);
						}
					} else if (type == Float.class) {
						if (! isNull) {
							dt.setValue(Float.parseFloat(s), row, col - 1);
						} else {
							dt.setValue((Float) null, row, col - 1);
						}
					} else if (type == Integer.class) {
						if (! isNull) {
							dt.setValue(Integer.parseInt(s), row, col - 1);
						} else {
							dt.setValue((Integer) null, row, col - 1);
						}
					} else if (type == Long.class) {
						if (! isNull) {
							dt.setValue(Long.parseLong(s), row, col - 1);
						} else {
							dt.setValue((Long) null, row, col - 1);
						}
					}
				} catch (Exception e) {
					System.err.println(String.format("error deserializing value %s at row, col (%s,%s)", data[col], row, col-1));
					throw new RuntimeException(e);
				}

			}
			row++;
		}
		return dt;
	}

	private static Map<String, String> parseAsMap(String value, String itemDelimiter) {
		HashMap<String, String> result = new HashMap<String, String>();
		if (value == null || value.length() == 0) return result; // nothing to parse
		assert(value.startsWith("{") && value.endsWith("}") );

		String strippedValue = value.substring(1, value.length() -1); // remove beginning and ending braces
		String[] entries = strippedValue.split(itemDelimiter);
		for (String entry: entries) {
			int pos = entry.indexOf("=");
			String key = entry.substring(0, pos);
			String mapValue = entry.substring(pos + 1);
			if (mapValue.equals("null")) mapValue = null;
			result.put(key, mapValue);
		}

		return result;
	}

	// ===============
	// UTILITY METHODS
	// ===============
	public static final String GLOBAL_METADATA_FORMAT = "{name=%s;desc=%s;hasRowIds=%s}";
	public static final String COLUMN_METADATA_FORMAT = "{name=%s;desc=%s;units=%s;type=%s}";

	public static String serializeGlobalMetadata(DataTable dt) {
		return String.format(GLOBAL_METADATA_FORMAT, dt.getName(), dt.getDescription(), dt.hasRowIds());
		// TODO add table level properties
	}

	public static String serializeColumnMetadata(DataTable dt) {
		StringBuilder result = new StringBuilder();
		int numOfColumns = dt.getColumnCount();
		for (int i=0; i< numOfColumns; i++) {
			result.append("	");
			Class<?> type = null;
			try {
				type = dt.getDataType(i);
			} catch (Exception e) {
				// An exception may be thrown if the datatype is not yet defined implicitly or explicitly
			}

			String typeValue = (type == null)? "null": type.getCanonicalName();
			
			String name = (dt.getName(i) != null)?dt.getName(i):NULL_STRING;
			String desc = (dt.getDescription(i) != null)?dt.getDescription(i):NULL_STRING;
			String unit = (dt.getUnits(i) != null)?dt.getUnits(i):NULL_STRING;
			
			result.append(String.format(COLUMN_METADATA_FORMAT, name, desc, unit, typeValue));
			// TODO add properties
		}
		return result.toString();
	}

	public static StringBuilder makeHeaderLine(DataTable dt) {
		StringBuilder result = new StringBuilder(serializeGlobalMetadata(dt));
		result.append(serializeColumnMetadata(dt));
		return result;
	}


	/**
	 * Serializes data between startRowIndex(inclusive) and endRowIndex (exclusive)
	 *
	 * @param dt
	 * @param startRowIndex
	 * @param endRowIndex
	 * @return
	 */
	public static StringBuilder serializeDataRows(DataTable dt, Integer startRowIndex, Integer endRowIndex) {
		startRowIndex = (startRowIndex == null || startRowIndex < 0)? 0: startRowIndex;
		endRowIndex = (endRowIndex == null)? dt.getRowCount(): Math.min(endRowIndex, dt.getRowCount());
		assert(startRowIndex < endRowIndex): "the starting index should be less than the ending index";

		int numOfCols = dt.getColumnCount();
		boolean hasRowIds = dt.hasRowIds();
		StringBuilder sb = new StringBuilder();
		for (int row=startRowIndex; row < endRowIndex; row++) {
			if (hasRowIds) {
				sb.append(dt.getIdForRow(row));
			}
			for (int col=0; col < numOfCols; col++) {
				sb.append("\t");
				
				String s = dt.getString(row, col);
				if (s != null) {
					sb.append(dt.getString(row, col));
				} else {
					sb.append(NULL_STRING);
				}
				
			}
			sb.append("\n");
		}

		return sb;
	}
}
