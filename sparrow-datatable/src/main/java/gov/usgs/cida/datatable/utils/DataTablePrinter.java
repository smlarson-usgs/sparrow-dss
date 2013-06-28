package gov.usgs.cida.datatable.utils;

import gov.usgs.cida.datatable.DataTable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * Convenience methods for outputting a DataTable. You can specify number of
 * rows, number of columns, even a collection of row numbers or row ids. If you
 * don't specify a writer for output, by default things are output to System.out
 * 
 * @author ilinkuo
 * 
 */
public class DataTablePrinter {

	/**
	 * Convenience method for printing a DataTable with a caption
	 * @param dt
	 * @param caption
	 * @throws IOException
	 */
	public static void printDataTable(DataTable dt, String caption) throws IOException {
		printDataTable(dt, caption, System.out);
	}

	public static void printDataTable(DataTable dt, String caption, PrintStream stream) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(stream));
		printDataTable(dt, caption, writer);
	}

	public static void printDataTable(DataTable dt, String caption, Writer writer)
	throws IOException {
		if (dt != null) {
	
			if (caption != null) {
				writer.write(caption);
				writer.write("\n");
			}
			printDataTableHeaders(dt, writer);
	
			// Print data
			int maxj = dt.getColumnCount() - 1;
			for (int i=0; i < dt.getRowCount(); i++) {
				for (int j=0; j<=maxj; j++) {
					Object value = dt.getValue(i, j);
					writer.write((value == null)? "": value.toString());
					if (j < maxj) { writer.write("\t");}
				}
				writer.write("\n");
			}
		}
		
		writer.flush();
	}

	/**
	 * Help method for
	 * {@link #printDataTableSample(DataTable, int, int, Writer)}. Not really
	 * meant to be called on its own
	 *
	 * @param dt
	 * @param writer
	 */
	public static void printDataTableHeaders(DataTable dt, Writer writer) {
		String[] headings = DataTableUtils.getHeadings(dt);
		try {
			for (String heading: headings) {
				writer.write(heading == null ? "" : heading);
				writer.write("\t");
			}
			writer.write("\n");
		} catch (IOException e) {
			if (!DataTableUtils.FAIL_SILENTLY) {
				System.err.println("Unable to print DataTable headers");
				e.printStackTrace();
			}
		}
	}

	public static void printDataTableSample(DataTable dt, Collection<Integer> sampleRows) {
		printDataTableSample(dt, sampleRows, 8);
	}

	public static void printDataTableSample(DataTable dt, Collection<Integer> sampleRows, int sampleColumns) {
		Writer writer = new OutputStreamWriter(System.out);
		try {
			printDataTableSample(dt, sampleRows, sampleColumns, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printDataTableSample(DataTable dt, Collection<Integer> sampleRows, int sampleColumns, Writer writer)
	throws IOException {
		writer.append("=======\n");
		printDataTableHeaders(dt, writer);
		int columnsToPrint = Math.min(sampleColumns, dt.getColumnCount());
		boolean hasRowIds = dt.hasRowIds();
	
		for (int row : sampleRows) {
			Long id = (hasRowIds)? dt.getIdForRow(row): null;
			printRow(dt, id, row, writer, columnsToPrint);
		}
		writer.append("\n=======\n");
	}

	// ====================
	// Output/print methods
	// ====================
	/**
	 * Prints a sample of the first 8 columns of the DataTable to System.out.
	 * Calls {@link #printDataTableSample(DataTable, int, int, Writer)}
	 * @param dt DataTable
	 * @param sampleSize
	 */
	public static void printDataTableSample(DataTable dt, int sampleSize) {
		printDataTableSample(dt, sampleSize, 8);
	}

	/**
	 * Prints a sample of the DataTable to System.out, with specified number of columns and rows.
	 * Calls {@link #printDataTableSample(DataTable, int, int, Writer)}
	 * @param dt
	 * @param sampleSize
	 * @param sampleColumns
	 */
	public static void printDataTableSample(DataTable dt, int sampleSize, int sampleColumns) {
		Writer writer = new OutputStreamWriter(System.out);
		try {
			printDataTableSample(dt, sampleSize, sampleColumns, writer);
			writer.flush(); // it is the responsibility of the writer provider to flush
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Base method for printing a sample of a DataTable
	 *
	 * @param dt DataTable
	 * @param sampleSize
	 * @param sampleColumns
	 * @param writer
	 * @throws IOException
	 */
	public static void printDataTableSample(DataTable dt, int sampleSize, int sampleColumns, Writer writer)
		throws IOException {
		writer.append("=======\n");
		printDataTableHeaders(dt, writer);
		int rowsToPrint = Math.min(sampleSize, dt.getRowCount());
		int columnsToPrint = Math.min(sampleColumns, dt.getColumnCount());
		boolean hasRowIds = dt.hasRowIds();
		
		for (int row=0; row < rowsToPrint; row++) {
			Long id =(hasRowIds)? dt.getIdForRow(row): null;
			printRow(dt, id, row, writer, columnsToPrint);
		}
		writer.append("\n=======\n");
	}

	public static void printDataTableSampleWithIds(DataTable dt, Collection<Long> sampleIDs) {
		printDataTableSampleWithIds(dt, sampleIDs, 8);
	}

	public static void printDataTableSampleWithIds(DataTable dt, Collection<Long> sampleIDs, int sampleColumns) {
		Writer writer = new OutputStreamWriter(System.out);
		try {
			printDataTableSampleWithIds(dt, sampleIDs, sampleColumns, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printDataTableSampleWithIds(DataTable dt, Collection<Long> sampleIDs, int sampleColumns, Writer writer)
	throws IOException {
		writer.append("=======\n");
		printDataTableHeaders(dt, writer);
		int columnsToPrint = Math.min(sampleColumns, dt.getColumnCount());
		
		if (dt.hasRowIds()) {
			for (Long id : sampleIDs) {
				printRow(dt, id, dt.getRowForId(id), writer, columnsToPrint);
			}
		}
	
		writer.append("\n=======\n");
	}

	public static void printRow(DataTable dt, Long id, int row, Writer writer,
			int columnsToPrint) throws IOException {
		writer.append("\n");
		if (id != null ) writer.append("(" + id + ") ");
		for (int col=0; col < columnsToPrint; col++) {
			writer.append(dt.getString(row, col) + " | ");
		}
	}

	/**
	 * Returns a sample of the DataTable in a StringBuffer
	 *
	 * @param dt DataTable
	 * @param sampleSize number of rows
	 * @param sampleColumns number of columns
	 * @return StringBuffer sample
	 */
	public static StringBuffer sampleDataTable(DataTable dt, int sampleSize, int sampleColumns) {
		StringWriter writer = new StringWriter();
		try {
			printDataTableSample(dt, sampleSize, sampleColumns, writer);
			// writer.flush(); DON'T FLUSH THE STRINGWRITER otherwise we can't return anything
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.getBuffer();
	}

}
