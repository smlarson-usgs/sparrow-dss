package gov.usgswim.sparrow.loader;

import java.util.HashMap;
import java.util.Map;

public class DataFileDescriptor {
	public enum DataType {
		StringType("String", false, false), 
		IntType("Integer", false, true), 
		ByteType("Byte", false, true), 
		ShortType("Short", false, true), 
		LongType("Long", false, true), 
		FloatType("Float", true, false), 
		DoubleType("Double", true, false);
	
		public boolean isFloat;
		public boolean isInt;
		public String display;

		private DataType(String display, boolean isFloat, boolean isInt) {
			this.display = display;
			this.isFloat = isFloat;
			this.isInt = isInt;
		}
		
		/**
		 * Returns standard datatype for this kind of data
		 * @return
		 */
		public DataType upType() {
			switch(this) {
				case StringType: return StringType;
				case LongType: return LongType;
				case DoubleType: return DoubleType;
				case FloatType: return FloatType;
				case IntType:
				case ShortType:
				case ByteType:
					return IntType;
				default:
					return StringType;
			}
		}
		
	};
	// ===============
	// INSTANCE FIELDS
	// ===============
	public String fileName;
	byte dataStart;
	int columnCount;
	public String delimiter;
	public int lines;
	private boolean hasColumnHeaders;
	private String[] columnHeaders;
	DataType[] dataTypes;
	String[][] columnMetadata;
	protected Map<String, Integer> colNameIndexHash;
	
	// ===========
	// CONSTRUCTOR
	// ===========
	public DataFileDescriptor(String name) {
		this.fileName = name;
	}
	
	// =======
	// METHODS
	// =======
	public void setHeaders(String [] headers) {
		this.columnHeaders = headers;
		hasColumnHeaders = (headers != null) && (headers.length > 0);
	}
	
	public String[] getHeaders() {
		return columnHeaders;
	}
	
	public boolean hasColumnHeaders() {
		return hasColumnHeaders;
	}
	
	public String toString() {
		return toStringBuilder().toString();
	}
	
	public StringBuilder toStringBuilder() {
		int dataLineCount = (hasColumnHeaders)? lines - 1: lines;
		StringBuilder result = new StringBuilder("FILENAME: " + fileName + " " + dataLineCount + " lines of data\n");
		if (hasColumnHeaders) {
			result.append("\tHEADERS[" + columnHeaders.length + "]: ");
			for (String header: columnHeaders) {
				result.append(header).append(",");
			}
			result.append("\n");
		} else {
			result.append("\tNO HEADERS\n");
		}
		
		if (dataTypes != null && dataTypes.length > 0) {
			result.append("\tTYPES[" + dataTypes.length + "]: ");
			for (DataType type: dataTypes) {
				result.append(type.display).append(",");
			}
			result.append("\n");
		} else {
			result.append("\tNO TYPES\n");
		}
		
		return result;
	}
	
	/**
	 * Returns the index of the column, -1 if not found.
	 * 
	 * @param colName
	 * @return
	 */
	public int indexOf(String colName) {
		if (columnHeaders == null) return -1; // can't ask for indices if there are no headers
		if (colNameIndexHash == null) {
			// initialize
			colNameIndexHash = new HashMap<String, Integer>();
			for (int i=0; i<columnHeaders.length; i++) {
				colNameIndexHash.put(columnHeaders[i], i);
			}
		}
		Integer result = colNameIndexHash.get(colName);
		return (result == null)? -1: result;
	}
	
	/**
	 * Returns true if all the columns exist
	 * 
	 * @param colName
	 * @return
	 */
	public boolean hasColumns(String... colName) {
		for (String name: colName) {
			if (indexOf(name) < 0) return false;
		}
		return true;
	}
	
	
	

}
