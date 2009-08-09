/**
 *
 */
package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DLUtils{

	/**
	 * Turns a query that returns two columns into a Map<Integer, Integer>.
	 * The first column is used as the key, the second column is used as the value.
	 * @param conn
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	private static Map<Integer, Integer> buildIntegerMap(Connection conn, String query) throws SQLException {
		DataTableWritable data = readAsInteger(conn, query, 1000);
		int rows = data.getRowCount();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>((int)(rows * 1.2), 1f);

		for (int r = 0; r < rows; r++)  {
			map.put(
					new Integer(data.getInt(r, 0)),
					new Integer(data.getInt(r, 1))
			);
		}

		return map;
	}

	/**
	 * Loads a single column from the resultSet source to the destination DataTable.
	 * For consistency, the from and to columns are ZERO INDEXED in both cases.
	 *
	 * @param source Resultset to load the data from.  The resultset is assumed to be before the first row.
	 * @param dest The destination DataTable
	 * @param fromCol The column (zero indexed) in the resultset to load from
	 * @param toCol The column (zero indexed) in the DataTable to load to
	 * @throws SQLException
	 */
	public static void loadColumn(ResultSet source, DataTableWritable dest, int fromCol, int toCol) throws SQLException {

		fromCol++;		//covert to ONE base index
		int currentRow = 0;

		while (source.next()){
			Double d = source.getDouble(fromCol);
			dest.setValue(d, currentRow,  toCol);
			currentRow++;
		}

	}

	/**
	 * Creates an unindexed DataTable from the passed query.
	 *
	 * All values in the source must be convertable to an integer.
	 *
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(Connection conn, String query, int fetchSize) throws SQLException {
		return readAsInteger(conn, query, fetchSize, DataLoader.DO_NOT_INDEX);
	}

	/**
	 * Creates a DataTable from the passed resultset.
	 *
	 * All values in the source must be convertable to a double.
	 *
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsDouble(ResultSet source) throws SQLException {

		ArrayList<double[]> list = new ArrayList<double[]>(500);
		String[] headings = null;

		headings = readHeadings(source.getMetaData());
		int colCount = headings.length; //Number of columns

		while (source.next()){


			double[] row = new double[colCount];

			for (int i=1; i<=colCount; i++) {
				row[i - 1] = source.getDouble(i);
			}

			list.add(row);

		}


		//copy the array list to a double[][] array
		double[][] data = new double[list.size()][];
		for (int i = 0; i < data.length; i++)  {
			data[i] = list.get(i);
		}

		DataTableWritable result = new SimpleDataTableWritable(data, headings);

		return result;
	}

	/**
	 * Creates a DataTable from the passed query.
	 *
	 * All values in the source must be convertable to a double.
	 *
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsDouble(Connection conn, String query, int fetchSize) throws SQLException {
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;

		try {

			rs = st.executeQuery(query);
			return readAsDouble(rs);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}

	/**
	 * Creates an DataTable from the passed query with an optional index.
	 *
	 * All values in the source must be convertable to an integer.
	 *
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @param indexCol A valid column index or -1 to indicate no index
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(Connection conn, String query, int fetchSize, int indexCol) throws SQLException {
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;

		try {

			rs = st.executeQuery(query);
			return readAsInteger(rs, indexCol);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}

	/**
	 * Creates an unindexed DataTable from the passed resultset.
	 *
	 * All values in the source must be convertable to an integer.
	 *
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(ResultSet source) throws SQLException {
		return readAsInteger(source, DataLoader.DO_NOT_INDEX);
	}

	/**
	 * Creates a DataTable from the passed resultset with an optional index.
	 *
	 * All values in the source must be convertable to an integer.
	 *
	 * @param source
	 * @param indexCol A valid column index or -1 to indicate no index
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(ResultSet source, int indexCol) throws SQLException {

		ArrayList<int[]> list = new ArrayList<int[]>(500);
		String[] headings = null;

		headings = readHeadings(source.getMetaData());
		int colCount = headings.length; //Number of columns

		while (source.next()){
			int[] row = new int[colCount];

			for (int i=1; i<=colCount; i++) {
				row[i - 1] = source.getInt(i);
			}
			list.add(row);
		}


		//copy the array list to a int[][] array
		int[][] data = new int[list.size()][];
		for (int i = 0; i < data.length; i++)  {
			data[i] = list.get(i);
		}

		DataTableWritable result = new SimpleDataTableWritable(data, headings, indexCol);
		//		if (indexCol > DO_NOT_INDEX) {
		//			result.buildIndex(indexCol);
		//		}

		return result;
	}

	public static DataTableWritable readAsInteger(BufferedReader source, int indexCol, String delimiter) throws IOException {
		// Assume that it has headings
		String line = source.readLine();
		String[] headings = line.split(delimiter);
		if (indexCol >=0) {
		//	Arrays
		}
		//DataTableWritable result = new SimpleDataTableWritable(headings, indexCol);


		return null;
	}

	public static String[] readHeadings(ResultSetMetaData meta) throws SQLException {
		int count = meta.getColumnCount();
		String[] headings = new String[count];

		for (int i=1; i<=count; i++) {
			headings[i - 1] = meta.getColumnName(i);
		}

		return headings;

	}

}