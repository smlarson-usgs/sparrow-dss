package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTableWritable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BuilderHelper {
	public static final int DO_NOT_INDEX = -1; // -1 is an invalid column index, so it will be ignored

	// ===================
	// Fill Helper Methods
	// ===================
	/**
	 * Fill the given empty DataTableWritable with integer data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings
	 * @return
	 */
	public static<T extends DataTableWritable> T fill(T dt, int[][] data, String[] headings){
		return fill(dt, data, headings, DO_NOT_INDEX);
	}

	/**
	 * Fill the given empty DataTableWritable with integer data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings (assumed to be null or of same width as the data)
	 * @param indexCol an invalid index <0 will be ignored
	 * @return
	 */
	public static<T extends DataTableWritable> T fill(T dt, int[][] data, String[] headings, int indexCol){
		assert(headings == null || headings.length == data[0].length);
		int width = data[0].length;

		// set the DataTable headings
		for (int j=0; j<width; j++) {
			if (j != indexCol) {  // skip the heading for the index column
				String heading = (headings == null)? null: headings[j];
				dt.addColumn(new StandardNumberColumnDataWritable<Integer>(heading, null));
			}

		}
		// fill in the data
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<width; j++) {
				int offset = (indexCol >= 0 && j>indexCol)? -1: 0;
				if (j == indexCol) {
					dt.setRowId(data[i][j], i);
				} else {
					dt.setValue(data[i][j], i, j + offset);
				}
			}
		}
		return dt;
	}

	/**
	 * Fill the given empty DataTableWritable with double data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings
	 * @return
	 */
	public static<T extends DataTableWritable> T fill(T dt, double[][] data, String[] headings){
		return fill(dt, data, headings, DO_NOT_INDEX);
	}

	/**
	 * Fill the given empty DataTableWritable with double data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings
	 * @return
	 */
	public static<T extends DataTableWritable> T fillTranspose(T dt, double[][] data, String[] headings){
		return fillTranspose(dt, data, headings, DO_NOT_INDEX);
	}

	/**
	 * Fill the given empty DataTableWritable with double data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings (assumed to be null or of same width as data)
	 * @param indexCol an invalid index <0 will be ignored
	 * @return
	 */
	public static<T extends DataTableWritable> T fill(T dt, double[][] data, String[] headings, int indexCol){
		int width = data[0].length;
		// fill headings
		for (int j=0; j<width; j++) {
			if (j != indexCol) {
				String heading = (headings == null)? null: headings[j];
				dt.addColumn(new StandardNumberColumnDataWritable<Double>(heading, null));
			}
		}
		// fill data
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<width; j++) {
				int offset = (indexCol >= 0 && j>indexCol)? -1: 0;
				if (j == indexCol) {
					Double id = data[i][j];
					dt.setRowId(id.longValue(), i);
				} else {
					dt.setValue(data[i][j], i, j + offset);
				}
			}
		}
		return dt;
	}

	/**
	 * Fill the given empty DataTableWritable with transposed double data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings (assumed to be null or of same width as data)
	 * @param indexCol an invalid index <0 will be ignored
	 * @return
	 */
	public static<T extends DataTableWritable> T fillTranspose(T dt, double[][] data, String[] headings, int indexCol){
		int width = data.length;
		// fill headings
		for (int j=0; j<width; j++) {
			if (j != indexCol) {
				String heading = (headings == null)? null: headings[j];
				dt.addColumn(new StandardNumberColumnDataWritable<Double>(heading, null));
			}
		}
		// fill data
		int totalRows = data[0].length;
		for (int i=0; i<totalRows; i++) {
			for (int j=0; j<width; j++) {
				int offset = (indexCol >= 0 && j>indexCol)? -1: 0;
				if (j == indexCol) {
					Double id = data[j][i];
					dt.setRowId(id.longValue(), i);
				} else {
					dt.setValue(data[j][i], i, j + offset);
				}
			}
		}
		return dt;
	}

	/**
	 * Fill the given empty DataTableWritable with integer data and appropriate headings
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param headings (assumed to be null or of same width as the data)
	 * @param indexCol an invalid index <0 will be ignored
	 * @return
	 */
	public static<T extends DataTableWritable> T fill(T dt, float[][] data, String[] headings){
		assert(headings == null || headings.length == data[0].length);
		int width = data[0].length;
		// No index column handling necessary because index column must be integer
		// set the DataTable headings
		for (int j=0; j<width; j++) {
			String heading = (headings == null)? null: headings[j];
			dt.addColumn(new StandardNumberColumnDataWritable<Float>(heading, null));
		}
		// fill in the data
		for (int i=0; i<data.length; i++) {
			for (int j=0; j<width; j++) {
				dt.setValue(data[i][j], i, j);
			}
		}
		return dt;
	}

	/**
	 * Fill the empty but correctly configured DataTableWritable with the ResultSet
	 *
	 * @param <T>
	 * @param dt
	 * @param data
	 * @param isFirstColID
	 * @return
	 */
	public static<T extends DataTableWritable> T fill(T dt, ResultSet data, boolean isFirstColID) {
		try {
			int row = 0;
			int offset = (isFirstColID)? 2: 1; // ResultSet column index begins with 1, not 0.
			while (data.next()) {
				// read the row and fill
				for (int j = 0; j < dt.getColumnCount(); j++) {
					// set the cell values for the row
					Class<?> type = dt.getDataType(j);
					if (type == Integer.class) {
						dt.setValue(data.getInt(j + offset), row, j);
					} else if (type == Long.class) {
						dt.setValue(data.getLong(j + offset), row, j);
					} else if (type == Float.class) {
						dt.setValue(data.getFloat(j + offset), row, j);
					} else if (type == Double.class) {
						dt.setValue(data.getDouble(j + offset), row, j);
					} else { // String class by default
						dt.setValue(data.getString(j + offset), row, j);
					}
				}

				// set the rowID, if desired
				if (isFirstColID) {
					dt.setRowId(data.getLong(1), row);
				}
				row++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dt;
	}

	/**
	 * Fill the given possibly non-empty List with a defaultValue
	 *
	 * @param <T>
	 * @param values
	 * @param row
	 * @param defaultValue
	 */
	public static<T> void fillIfNecessary(List<T> values, int row, T defaultValue) {
		if (row >= values.size()) {
			for (int i = values.size(); i <= row; i++) {
				values.add(defaultValue);
			}
		}
	}

	// =========================
	// Conversion Helper Methods
	// =========================
	/**
	 * Convert all the source Map's List<Integer> values to int[] values, saving storage.
	 * @param <T> the type of the Map's key
	 * @param source
	 * @return
	 */
	public static<T> Map<T, int[]> convertIndex(Map<T, List<Integer>> source) {
		Map<T, int[]> result = new HashMap<T, int[]>();
		for (T key: source.keySet()) {
			List<Integer> intList = source.get(key);
			if (intList != null && intList.size() != 0) {
				result.put(key, BuilderHelper.toIntArray(intList));
			}
		}
		return result;
	}

	/**
	 * Builds an map for a set of long values.  The map provide fast access to
	 * find the index of a given value in the set of values.
	 *
	 * Example:  values = [34, 634, 92]
	 * The map would contain a key 634L which would return 1, which is the index
	 * of 634 in the array.
	 *
	 * If the values array does not contain unique values, only the second entry
	 * of the value will be in the index, which in most cases will cause unexpected
	 * results.
	 *
	 * @param values A list of unique Long values (must be unique)
	 * @return
	 */
	public static Map<Long, Integer> buildIndex(long[] values) {
		Map<Long, Integer> map = new HashMap<Long, Integer>(values.length, 1.1f);

		int length = values.length;

		for (int i = 0; i<length; i++) {
			map.put(values[i], i);
		}


		return map;
	}

	/**
	 * Returns a detached array of plain ints given an List<Integer> input. This
	 * saves memory storage if the original is discarded.
	 *
	 * @param source
	 * @return null if original List is null or empty.
	 */
	public static int[] toIntArray(List<Integer> source) {
		int[] result = null;
		if (source != null && source.size() != 0) {
			result = new int[source.size()];
			for (int i=0; i<source.size(); i++) {
				result[i] = source.get(i).intValue();
			}
		}
		return result;
	}

	/**
	 * Returns a detached array of plain ints given an List<Long> input. This
	 * saves memory storage if the original is discarded.
	 *
	 * @param source
	 * @return null if original List is null or empty.
	 */
	public static long[] toLongArray(List<Long> source) {
		long[] result = null;
		if (source != null && source.size() != 0) {
			result = new long[source.size()];
			for (int i=0; i<source.size(); i++) {
				result[i] = source.get(i).longValue();
			}
		}
		return result;
	}

	public static Number convertNumber(Class<?> type, Number value) {
		if (type == Double.class) {
			return Double.valueOf(value.doubleValue());
		} else if (type == Integer.class) {
			return Integer.valueOf(value.intValue());
		} else if (type == Long.class) {
			return Long.valueOf(value.longValue());
		} else if (type == Float.class) {
			return Float.valueOf(value.floatValue());
		}
		assert(false): "Unreachable. Only four basic types allowed";
		return null;
	}

	public static ColumnDataWritable createColWriteable(String name, Class<?> type, String units) {
		if (Integer.class == type) {
			StandardNumberColumnDataWritable<Integer> col = new StandardNumberColumnDataWritable<Integer>(name, units);
			col.setType(type);
			return col;
		} else if (Float.class == type) {
			StandardNumberColumnDataWritable<Float> col = new StandardNumberColumnDataWritable<Float>(name, units);
			col.setType(type);
			return col;
		} else if (Double.class == type) {
			StandardNumberColumnDataWritable<Double> col = new StandardNumberColumnDataWritable<Double>(name, units);
			col.setType(type);
			return col;
		} else if (Long.class == type) {
			StandardNumberColumnDataWritable<Long> col = new StandardNumberColumnDataWritable<Long>(name, units);
			col.setType(type);
			return col;
		} else if (Number.class == type) {
			StandardNumberColumnDataWritable<Number> col = new StandardNumberColumnDataWritable<Number>(name, units);
			// col.setType(type); don't set the type
			return col;
		} else if (String.class == type) {
			return new StandardStringColumnDataWritable(name, units);
		} else { // default is String
			return new StandardStringColumnDataWritable(name, units);
		}
	}

}
