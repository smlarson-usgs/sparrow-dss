package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TabDelimFileUtil {


	private TabDelimFileUtil() { /* no instances */}


	// =================
	// DATATABLE READ METHODS
	// =================
	public static DataTable readAsDouble(File file, boolean hasHeadings, String[] mappedHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {
		if (indexCol > -1) {
			return read(file, hasHeadings, mappedHeadings).buildIndex(indexCol);
		}
		return read(file, hasHeadings, mappedHeadings);
	}
	public static DataTableWritable readAsDouble(BufferedReader source, boolean hasHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {
		if (indexCol > -1) {
			return readAsDouble(source, hasHeadings, null).buildIndex(indexCol);
		}
		return readAsDouble(source, hasHeadings, null);
	}

	public static DataTableWritable readAsDouble(BufferedReader reader, boolean hasHeadings, String[] mappedHeadings)
	throws FileNotFoundException, IOException, NumberFormatException  {

		try {

			int[] remappedColumns = null;	//indexes to map the columns to
			int mappedColumnCount = 0;	//Number of columns in the output

			String[] headings = (hasHeadings)? TabDelimFileUtil.readHeadings(reader): null;


			if (mappedHeadings != null) {
				remappedColumns = TabDelimFileUtil.mapByColumnHeadings(headings, mappedHeadings);
				mappedColumnCount = mappedHeadings.length;
			}

			List<double[]> rows = TabDelimFileUtil.readDataBodyAsDouble(reader, remappedColumns, mappedColumnCount);

			// If there is exactly one extra column, then it's the ID column and it's first.
			boolean hasIDColumnFirst =(mappedColumnCount == 0 && headings != null && rows.get(0).length == headings.length + 1);

			//copy the array list to a double[][] array

			DataTableWritable builder = new SimpleDataTableWritable();
			// Configure the columns
			int numColumns = (headings != null)? headings.length: rows.get(0).length;
			numColumns = (mappedColumnCount > 0)? mappedColumnCount: numColumns;
			for (int i=0; i< numColumns; i++) {
				// no units in this test
				String heading = (headings != null)? headings[i]: null;
				ColumnDataWritable column = new StandardNumberColumnDataWritable<Double>(heading, null);
				builder.addColumn(column);
			}

			// Add the data
			boolean debug = false;
			for (int i=0; i<rows.size(); i++) {
				double[] row = rows.get(i);
				int offset = 0;
				if (hasIDColumnFirst) {
					builder.setRowId(Double.valueOf(row[0]).longValue(), i);
					offset = 1;
				}
				for (int j = 0; j<numColumns; j++) {
					if (debug) System.out.println(i + " - " + j);
					builder.setValue(Double.valueOf(row[j + offset]), i, j);
				}
			}
			return builder;

		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				//At this point we ignore.
			}
			reader = null;
		}
	}
	
	public static DataTable readAsDouble(File file, boolean hasHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {
		if (indexCol > -1) {
			return read(file, hasHeadings, null).buildIndex(indexCol);
		}
		return read(file, hasHeadings, null);
	}

	public static DataTableWritable read(File file, boolean hasHeadings, String[] mappedHeadings)
	throws FileNotFoundException, IOException, NumberFormatException  {
		
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		return readAsDouble(br, hasHeadings, mappedHeadings);
	}

	public static DataTable readAsInteger(File file, boolean hasHeadings, String[] mappedHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {
		if (indexCol > -1) {
			return read(file, hasHeadings, mappedHeadings).buildIndex(indexCol);
		}
		return read(file, hasHeadings, mappedHeadings);

	}
	public static DataTable readAsDouble(BufferedReader source, boolean hasHeadings, String[] mappedHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {
		if (indexCol > -1) {
			return readAsDouble(source, hasHeadings, mappedHeadings).buildIndex(indexCol);
		}
		return readAsDouble(source, hasHeadings, mappedHeadings);
	}

	// =================
	// DATATABLE METHODS (end)
	// =================

	public static DataTableWritable readAsInteger(File file, boolean hasHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {

		return read(file, hasHeadings);

	}

	public static DataTableWritable readAsInteger(BufferedReader source, boolean hasHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {
		if (indexCol > -1) {
			return TabDelimFileUtil.readAsInt(source, hasHeadings, null).buildIndex(indexCol);
		}
		return TabDelimFileUtil.readAsInt(source, hasHeadings, null);
	}

	public static DataTable readAsInteger(BufferedReader source, boolean hasHeadings, String[] mappedHeadings, int indexCol)
	throws FileNotFoundException, IOException, NumberFormatException  {

		return read(source, hasHeadings, mappedHeadings);
	}

	public static DataTableWritable read(File file, boolean hasHeadings)
		throws FileNotFoundException, IOException, NumberFormatException  {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		return read(br, hasHeadings, null);
	}

	public static DataTableWritable read(BufferedReader source, boolean hasHeadings)
	throws FileNotFoundException, IOException, NumberFormatException  {
		return readAsDouble(source, hasHeadings, null);
	}

	public static DataTableWritable read(BufferedReader source, boolean hasHeadings, String[] mappedHeadings)
	throws FileNotFoundException, IOException, NumberFormatException  {


		try {

			ArrayList<double[]> list = new ArrayList<double[]>(500);
			String[] headings = null;
			int[] remappedColumns = null;	//indexes to map the columns to
			String s = null;					//Single line from file
			int colCount = 0; //Number of columns (minus one) - must match in each row
			int mappedColumnCount = 0;	//Number of columns in the output

			if (hasHeadings) headings = TabDelimFileUtil.readHeadings(source);

			if (mappedHeadings == null) {
				//assign later
			} else {
				remappedColumns = TabDelimFileUtil.mapByColumnHeadings(headings, mappedHeadings);
				mappedColumnCount = mappedHeadings.length;
			}

			while ((s=source.readLine())!=null){
				String src[] = s.split("\t");

				if ( src.length > 0 && !(src.length == 1 && ("".equals(src[0]))) ) {

					if (colCount == 0) {
						colCount = src.length;
						if (remappedColumns == null) {
							//assign mappings now
							remappedColumns = new int[colCount];
							for (int i = 0; i < colCount; i++)  {
								remappedColumns[i] = i;
							}

							mappedColumnCount = colCount;
						}
					} else {
						if (src.length != colCount) {
							throw new IllegalStateException("Each row in the file must have the same number of delimiters");
						}
					}

					double[] row = new double[mappedColumnCount];

					for (int i = 0; i < src.length; i++)  {
						if (remappedColumns[i] != -1) {
							if (src[i].length() > 0) {
								row[remappedColumns[i]] = Double.parseDouble( src[i] );
							} else {
								row[remappedColumns[i]] = 0;
							}
						}
					}

					list.add(row);
				} else {
					//ignore empty lines
				}
			}

			//copy the array list to a double[][] array
			double[][] data = new double[list.size()][];
			for (int i = 0; i < data.length; i++)  {
				data[i] = list.get(i);
			}

			SimpleDataTableWritable dataWritable = new SimpleDataTableWritable(data, (mappedHeadings==null)?headings:mappedHeadings);

			return dataWritable;

		} finally {
			try {
				source.close();
			} catch (IOException e) {
				//At this point we ignore.
			}
			source = null;
		}
	}

	public static void write(DataTable data, File destination)
	throws IOException  {
		FileOutputStream os = new FileOutputStream(destination, false);
		write(data, os);
		os.flush();
		os.close();
	}



	public static void write(DataTable data, OutputStream destination)
	throws IOException  {

		PrintWriter pw = new PrintWriter(destination);
		//BufferedWriter bw = new BufferedWriter(osw);


		StringBuffer line = new StringBuffer();
		int colCount = data.getColumnCount();
		int rowCount = data.getRowCount();

		//headings
		for (int i = 0; i < colCount; i++)  {
			line.append(data.getName(i) + "\t");
		}

		//Trim extra tab from end of line and write
		line.setLength(line.length() - 1);
		pw.println(line.toString());
		line.setLength(0);

		for (int r=0; r<rowCount; r++) {
			for (int c=0; c<colCount; c++) {
				line.append(data.getValue(r, c).toString() + "\t");
			}
			//Trim extra tab from end of line and write
			line.setLength(line.length() - 1);
			pw.println(line.toString());
			line.setLength(0);
		}

		pw.flush();

	}


	static List<int[]> readDataBodyAsInt(BufferedReader br, int[] remappedColumns, int mappedColumnCount) throws NumberFormatException, IOException {
		List<int[]> rows = new ArrayList<int[]>(500);
		int colCount = 0; //Number of columns (minus one) - must match in each row
		String s = null;					//Single line from file
		while ((s=br.readLine())!=null){
			String src[] = s.split("\t");

			if ( TabDelimFileUtil.isNonEmptyLine(src) ) {

				if (colCount == 0) {
					colCount = src.length; // initialize column count
					if (remappedColumns == null) {
						//assign default mapping now
						remappedColumns = new int[colCount];
						for (int i = 0; i < colCount; i++)  {
							remappedColumns[i] = i;
						}

						mappedColumnCount = colCount;
					}
				} else {
					if (src.length != colCount) {
						throw new IllegalStateException("Each row in the file must have the same number of delimiters");
					}
				}

				int[] row = new int[mappedColumnCount];

				for (int i = 0; i < src.length; i++)  {
					if (remappedColumns[i] != -1) {
						if (src[i].length() > 0) {
							row[remappedColumns[i]] = Integer.parseInt( src[i] );
						} else {
							row[remappedColumns[i]] = 0;
						}
					}
				}

				rows.add(row);
			} else {
				//ignore empty lines
			}
		}
		return rows;
	}


	public static boolean isNonEmptyLine(String[] src) {
		return src.length > 0 && !(src.length == 1 && ("".equals(src[0])));
	}


	public static String[] readHeadings(BufferedReader br) throws IOException {
		String s;         //Single line from file
		if ((s=br.readLine())!=null) {
			String src[] = s.split("\t");
			return ( isNonEmptyLine(src) )? src: null;
		}
		return null;
	}


	/**
	 * Returns an array of column indexes mapping the file headings to the
	 * required headings.
	 *
	 * The use case would be where only certain columns should be read in from the
	 * text file and the files can be named.  This method will take the list
	 * of columns that you want to import (mappedHeadings) and locates them in
	 * the headings from the file (fileHeadings).
	 *
	 * The returned int array maps the column indexes in the file to the output.
	 * Columns that are not needed in the output contain a mapping value of -1.
	 *
	 * For instance, a return array may look like this:
	 * {-1, 2, -1, 0, 1}
	 *
	 * Which would mean that columns 0 and 2 are not needed in the output, and
	 * columns 1, 3, and 4 are mapped to output columns 2, 0, and 1 respectively.
	 *
	 * The return array will be the same length as fileHeadings.
	 *
	 * Comparisons are case insensitive and a IllegalStateException is thrown if
	 * a heading in mappedHeadings is not found in fileHeadings.
	 *
	 * In the case of duplicate column names in the fileHeadings, only the first
	 * index is returned.
	 *
	 * @param fileHeadings
	 * @param mappedHeadings
	 * @return
	 */
	public static int[] mapByColumnHeadings(String[] fileHeadings, String[] mappedHeadings) {
		int[] hIndex = new int[fileHeadings.length];
		Arrays.fill(hIndex, -1);

		for(int i=0; i<mappedHeadings.length; i++) {
			boolean found = false;

			for(int j=0; j<fileHeadings.length; j++) {
				if (mappedHeadings[i].equalsIgnoreCase(fileHeadings[j])) {
					hIndex[j] = i;
					found = true;
					break;
				}
			}

			if (!found) {
				throw new IllegalArgumentException("The mapped heading '" + mappedHeadings[i] + "' was not found in the source file.");
			}
		}

		return hIndex;
	}


	static List<double[]> readDataBodyAsDouble(BufferedReader br, int[] remappedColumns, int mappedColumnCount) throws NumberFormatException, IOException {
		List<double[]> rows = new ArrayList<double[]>(500);
		int colCount = 0; //Number of columns (minus one) - must match in each row
		int curRow = 0;	//Current row number - 0 based;
		String s = null;					//Single line from file
		
		while ((s=br.readLine())!=null) {
			String src[] = s.split("\t");

			if ( isNonEmptyLine(src) ) {

				if (colCount == 0) {
					colCount = src.length; // initialize column count
					if (remappedColumns == null) {
						//assign default mapping now
						remappedColumns = new int[colCount];
						for (int i = 0; i < colCount; i++)  {
							remappedColumns[i] = i;
						}

						mappedColumnCount = colCount;
					}
				} else {
					if (src.length != colCount) {
						throw new IllegalStateException("Parse Error: Row " +
								curRow + 1 + " has " + src.length +
								" columns, previous columns had " + colCount + " columns.");
					}
				}

				double[] row = new double[mappedColumnCount];

				for (int i = 0; i < src.length; i++)  {
					if (remappedColumns[i] != -1) {
						if (src[i].length() > 0) {
							row[remappedColumns[i]] = Double.parseDouble( src[i] );
						} else {
							row[remappedColumns[i]] = 0;
						}
					}
				}

				rows.add(row);
			} else {
				//ignore empty lines
			}
			
			curRow++;
		}
		return rows;
	}


	public static DataTableWritable readAsInt(BufferedReader source, boolean hasHeadings, String[] mappedHeadings)
	throws FileNotFoundException, IOException, NumberFormatException  {

		try {

			int[] remappedColumns = null;	//indexes to map the columns to
			int mappedColumnCount = 0;	//Number of columns in the output

			String[] headings = (hasHeadings)? readHeadings(source): null;


			if (mappedHeadings != null) {
				remappedColumns = mapByColumnHeadings(headings, mappedHeadings);
				mappedColumnCount = mappedHeadings.length;
			}

			List<int[]> rows = readDataBodyAsInt(source, remappedColumns, mappedColumnCount);

			// If there is exactly one extra column, then it's the ID column and it's first.
			boolean hasIDColumnFirst =(headings != null && rows.get(0).length == headings.length + 1);

			//copy the array list to a double[][] array

			DataTableWritable builder = new SimpleDataTableWritable();
			// Configure the columns
			int numColumns = (headings != null)? headings.length: rows.get(0).length;
			for (int i=0; i< numColumns; i++) {
				// no units in this test
				String heading = (headings != null)? headings[i]: null;
				ColumnDataWritable column = new StandardNumberColumnDataWritable<Double>(heading, null);
				builder.addColumn(column);
			}

			// Add the data
			for (int i=0; i<rows.size(); i++) {
				int[] row = rows.get(i);
				int offset = 0;
				if (hasIDColumnFirst) {
					builder.setRowId(row[0], i);
					offset = 1;
				}
				for (int j = 0; j<numColumns; j++) {
					builder.setValue(Integer.valueOf(row[j + offset]), i, j);
				}
			}
			return builder;

		} finally {
			try {
				source.close();
			} catch (IOException e) {
				//At this point we ignore.
			}
			source = null;
		}
	}




}
