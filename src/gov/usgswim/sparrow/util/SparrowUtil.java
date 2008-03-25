package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;

import java.io.PrintStream;

public abstract class SparrowUtil {
	private SparrowUtil() { /* no instances */ }


	public static void printDataTable(DataTable data, String caption) {
		printDataTable(data, caption, System.out);
	}

	public static void printDataTable(DataTable data, String caption, PrintStream stream) {
		stream.println(caption);
		StringBuilder headings = new StringBuilder();
		boolean hasHeadings = false;
		for (int i = 0; i < data.getColumnCount(); i++)  {
			String columnHeading = data.getName(i);
			hasHeadings |= (columnHeading != null && columnHeading.length() > 0);
			headings.append(columnHeading).append("\t");
		}
		if (hasHeadings) {
			stream.print(headings);
		}


		stream.println("");

		for (int r = 0; r < data.getRowCount(); r++)  {
			for (int c = 0; c < data.getColumnCount(); c++)  {
				stream.print(data.getString(r, c) + "\t");
			}
			stream.println("");
		}
	}

}
