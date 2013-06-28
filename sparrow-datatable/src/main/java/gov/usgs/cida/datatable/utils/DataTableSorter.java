package gov.usgs.cida.datatable.utils;

import gov.usgs.cida.datatable.DataTable;

import java.util.Arrays;

public class DataTableSorter {

	public static interface SortFilter{
		public boolean accept(Number value);
	}

	/**
	 * @param data
	 * @param columnIndex
	 * @param keepInfinities
	 * @param filter (null allowed)
	 * @return
	 * NaNs will always get filtered out
	 */
	public static Double[] extractSortedFilteredDoubleValues(DataTable data, int columnIndex, boolean keepInfinities, DataTableSorter.SortFilter filter) {
		int totalRows = data.getRowCount();
		Double[] tempResult = new Double[totalRows];
		int count = 0;
		if (filter == null) {
			// Set filter to be a dummy pass-through filter
			filter = new DataTableSorter.SortFilter() {
				public boolean accept(Number value){return true;}
			};
		}
	
		//Export all values in the specified column to values[] so they can be sorted
		for (int r=0; r<totalRows; r++) {
			double value = data.getDouble(r, columnIndex);
			if (filter.accept(value) && !Double.isNaN(value) &&
					( keepInfinities || !Double.isInfinite(value) )
					) {
				tempResult[count]=value;
				count++;
			}
		}
		Double[] values = Arrays.copyOf(tempResult, count);
		Arrays.sort(values);
		return values;
	}

	/**
	 * @param data
	 * @param columnIndex
	 * @param keepInfinities
	 * @param filter (null allowed)
	 * @return
	 */
	public static Float[] extractSortedFilteredValues(DataTable data, int columnIndex, boolean keepInfinities, DataTableSorter.SortFilter filter) {
		int totalRows = data.getRowCount();
		Float[] tempResult = new Float[totalRows];
		int count = 0;
		if (filter == null) {
			// Set filter to be a dummy pass-through filter
			filter = new DataTableSorter.SortFilter() {
				public boolean accept(Number value){return true;}
			};
		}
	
		//Export all values in the specified column to values[] so they can be sorted
		for (int r=0; r<totalRows; r++) {
			float value = data.getFloat(r, columnIndex);
			if (filter.accept(value) && !Float.isNaN(value) &&
					( keepInfinities || !Float.isInfinite(value) )
					) {
				tempResult[count]=value;
				count++;
			}
		}
		Float[] values = Arrays.copyOf(tempResult, count);
		Arrays.sort(values);
		return values;
	}

}
