package gov.usgs.webservices.framework.utils;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class TemporaryHelper {

//	static<T extends DataTableWritable> T fillInt(T dt, Data2D data) {
//		return BuilderHelper.fill(dt, data.getIntData(), null);
//	}
	
//	static<T extends DataTableWritable> T fillDouble(T dt, Data2D data) {
//		return BuilderHelper.fill(dt, data.getDoubleData(), null);
//	}

	/**
	 * @param data
	 * @param col
	 * @return
	 * @deprecated
	 */
	public static int[] getIntColumn(DataTable data, int col) {
		int size = data.getRowCount();
		int[] result = new int[size];
		for (int row=0; row<size; row++) {
			result[row] = data.getInt(row, col);
		}
		return result;
	}
	
	/**
	 * @param source
	 * @param col
	 * @return
	 * @deprecated
	 */
	public static double[] getDoubleColumn(DataTable source, int col){
		double[] result = new double[source.getRowCount()];
		for (int i=0; i<result.length; i++) {
			result[i] = source.getDouble(i, col);
		}
		return result;
	}
	
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
	
	/**
	 * @param source
	 * @param row
	 * @return
	 * @deprecated
	 */
	public static int[] getIntRow(DataTable source, int row){
		int[] result = new int[source.getColumnCount()];
		for (int j=0; j<result.length; j++) {
			result[j] = source.getInt(row, j);
		}
		return result;
	}
	
	/**
	 * @param source
	 * @param row
	 * @return
	 * @deprecated
	 */
	public static double[] getDoubleRow(DataTable source, int row){
		double[] result = new double[source.getColumnCount()];
		for (int j=0; j<result.length; j++) {
			result[j] = source.getDouble(row, j);
		}
		return result;
	}
	
	public static String[] getHeadings(DataTable source) {
		String[] result = new String[source.getColumnCount()];
		for (int i=0; i < result.length; i++) {
			result[i] = source.getName(i);
		}
		return result;
	}

	public static long[] getRowIds(DataTable dt) {
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
}
