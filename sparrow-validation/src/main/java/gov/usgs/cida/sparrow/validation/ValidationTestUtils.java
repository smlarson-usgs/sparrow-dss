package gov.usgs.cida.sparrow.validation;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardLongColumnData;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.util.TabDelimFileUtil;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.text.StrTokenizer;

/**
 *
 * @author eeverman
 */
public class ValidationTestUtils {

	public final static String ID_COL_KEY = "id_col";	//Table property of the key column
	
	
	public static DataTable loadModelTextFile(Long modelId, String textModelDirectory) throws Exception {
		File dir = new File(textModelDirectory);
		File file = new File(dir, modelId.toString() + ".txt");
		
		
		InputStream is = new FileInputStream(file);
		DataTable dt = null;
		
		try {
			dt = readAsDouble(is, true);
		} catch (Exception e) {
			throw new Exception("Error reading: " + file.getAbsolutePath(), e);
		}
		
		return dt;
	}
	
	public static Integer findIdColumn(String[] headings) {
		int idCol = -1;
		
		
		idCol = ArrayUtils.indexOf(headings, "local_id");
		if (idCol > -1) {
			return idCol;
		}
		

		idCol = ArrayUtils.indexOf(headings, "mrb_id");
		if (idCol > -1) {
			return idCol;
		}
		

		idCol = ArrayUtils.indexOf(headings, "waterid");
		if (idCol > -1) {
			return idCol;
		}
		
		idCol = ArrayUtils.indexOf(headings, "reach");
		if (idCol > -1) {
			return idCol;
		}
		
		return -1;
	}
	
	
	public static DataTable readAsDouble(InputStream source, boolean hasHeadings)
		throws Exception  {

		int indexCol;
		InputStreamReader isr = new InputStreamReader(source);
		BufferedReader br = new BufferedReader(isr);

		try {

			int[] remappedColumns = null;	//indexes to map the columns to
			int mappedColumnCount = 0;	//Number of columns in the output

			String[] headings = (hasHeadings)? TabDelimFileUtil.readHeadings(br): null;
			indexCol = findIdColumn(headings);

			if (indexCol < 0) {
				throw new Exception("Could not find an ID Column");
			}
			
			List<double[]> rows = readDataBodyAsDouble(br, remappedColumns, mappedColumnCount);

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

				for (int j = 0; j<numColumns; j++) {
					builder.setValue(Double.valueOf(row[j + offset]), i, j);
				}
			}
			
			//builder.buildIndex(indexCol);
			builder.setProperty(ID_COL_KEY, Integer.toString(indexCol));
			
			//return builder;
			ColumnData[] cols = new ColumnData[builder.getColumns().length];
			
			System.arraycopy(builder.getColumns(), 0, cols, 0, builder.getColumns().length);
			
			ColumnData oldCol = cols[indexCol];
			StandardLongColumnData newCol = new StandardLongColumnData(oldCol, true, 0L);
			cols[indexCol] = newCol;

			SimpleDataTable table = new SimpleDataTable(cols, builder.getName(), builder.getDescription(), builder.getProperties());
			
			return table;

		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				//At this point we ignore.
			}
			br = null;
		}
	}
	

	public static List<double[]> readDataBodyAsDouble(BufferedReader br, int[] remappedColumns, int mappedColumnCount) throws NumberFormatException, IOException {
		List<double[]> rows = new ArrayList<double[]>(500);
		int colCount = 0; //Number of columns (minus one) - must match in each row
		int curRow = 0;	//Current row number - 0 based;
		String s = null;					//Single line from file
		
		while ((s=br.readLine())!=null) {
			StrTokenizer strTk = new StrTokenizer(s);
			strTk.setIgnoreEmptyTokens(false);
			strTk.setDelimiterChar('\t');
			String src[] = strTk.getTokenArray();
			//String src[] = s.split("\t");

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
						for (int i = 0; i < src.length; i++)  {
							System.out.println(i + ": " + src[i]);
						}

						throw new IllegalStateException("Parse Error: Row " + curRow + " has " + src.length + " columns, previous columns had " + colCount + " columns.");
					}
				}

				double[] row = new double[mappedColumnCount];

				for (int i = 0; i < src.length; i++)  {
					if (remappedColumns[i] != -1) {
						if (src[i].length() > 0) {
							
							//Simple hack to ignore text columns in the predict.txt file
							
							try {
								row[remappedColumns[i]] = Double.parseDouble( src[i] );
							} catch (Exception e) {
								row[remappedColumns[i]] = Double.NaN;
							}
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
	

	public static boolean isNonEmptyLine(String[] src) {
		return src.length > 0 && !(src.length == 1 && ("".equals(src[0])));
	}
	
	
}
