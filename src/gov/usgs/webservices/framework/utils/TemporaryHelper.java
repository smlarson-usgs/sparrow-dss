package gov.usgs.webservices.framework.utils;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.DataTableUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	public static void printDataTable(DataTable dt) {
		if (dt != null) {
			// print headings
			String[] headings = DataTableUtils.getHeadings(dt);
			System.out.println();
			for (String heading: headings) {
				System.out.print(heading);
				System.out.print("\t");
			}
			System.out.println();
			// Print data
			for (int i=0; i < dt.getRowCount(); i++) {
				for (int j=0; j<dt.getColumnCount(); j++) {
					Object value = dt.getValue(i, j);
					System.out.print((value == null)? "": value.toString());
					System.out.print("\t");
				}
				System.out.println();
			}
		}
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
}
