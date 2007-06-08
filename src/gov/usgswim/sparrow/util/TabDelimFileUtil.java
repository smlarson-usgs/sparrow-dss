package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.Double2D;

import gov.usgswim.sparrow.Int2D;

import gov.usgswim.sparrow.PredictionDataSet;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.OutputStream;

import java.io.OutputStreamWriter;

import java.io.PrintWriter;

import java.sql.Connection;

import java.util.ArrayList;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

public class TabDelimFileUtil {
	public final static String[] ANCIL_HEADINGS = new String[] {
		 "LOCAL_ID", "STD_ID", "LOCAL_SAME"
	};
	
	public final static String[] TOPO_HEADINGS = new String[] {
		 "FNODE", "TNODE", "IFTRAN", "HYDSEQ"
	};
	
	private TabDelimFileUtil() { /* no instances */}
	
	
	/**
	 * Loads all PredictionDataSet data from text files based on either a
	 * classpath package or a filesystem directory.
	 * 
	 * All iterations and the ancil data is included.
	 * 
	 * @param rootPackage
	 * @param rootDir
	 * @param modelId
	 * @param enhNetworkId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static PredictionDataSet loadFullPredictDataSet(
				String rootPackage, String rootDir, long modelId, long enhNetworkId)
				throws FileNotFoundException, IOException {
				
				
		return loadPredictDataSet(rootPackage, rootDir, modelId, enhNetworkId,
				false, true);
	}
	
	/**
	 * Loads a minimal PredictionDataSet from text files based on either a
	 * classpath package or a filesystem directory.
	 * 
	 * Only the first iteration is included.  Ancil data IS included.
	 * 
	 * @param rootPackage
	 * @param rootDir
	 * @param modelId
	 * @param enhNetworkId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static PredictionDataSet loadMinimalPredictDataSet(
				String rootPackage, String rootDir, long modelId, long enhNetworkId)
				throws FileNotFoundException, IOException {
				
				
		return loadPredictDataSet(rootPackage, rootDir, modelId, enhNetworkId,
				true, true);
	}

	/**
	 * Loads a PredictionDataSet from text files based on either a classpath package
	 * or a filesystem directory.
	 * 
	 * A lastIncludedIteration value can be passed so that only a portion of the
	 * iterations are visible.  This value is the last iteration included, so passing
	 * a value of zero will include only the zero iteration.  Passing -1 will
	 * include all iterations.  The iteration passed must exist.
	 * 
	 * @param rootPackage
	 * @param rootDir
	 * @param modelId
	 * @param enhNetworkId
	 * @param onlyZeroIteration
	 * @param includeAncilData
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static PredictionDataSet loadPredictDataSet(
				String rootPackage, String rootDir, long modelId, long enhNetworkId,
				boolean onlyZeroIteration, boolean includeAncilData)
				throws FileNotFoundException, IOException {
		
		PredictionDataSet pd = new PredictionDataSet();
		
		if (rootPackage != null) {
			if (! rootPackage.endsWith("/")) rootPackage = rootPackage + "/";

			if (includeAncilData) {
				pd.setAncil( TabDelimFileUtil.readAsDouble(TabDelimFileUtil.class.getResourceAsStream(rootDir + "ancil.txt"), true, ANCIL_HEADINGS) );
			}
			pd.setCoef( TabDelimFileUtil.readAsDouble(TabDelimFileUtil.class.getResourceAsStream(rootDir + "coef.txt"), true) );
			pd.setSrc( TabDelimFileUtil.readAsDouble(TabDelimFileUtil.class.getResourceAsStream(rootDir + "src.txt"), true) );
			pd.setTopo( TabDelimFileUtil.readAsDouble(TabDelimFileUtil.class.getResourceAsStream(rootDir + "topo.txt"), true, TOPO_HEADINGS) );
			
		} else if (rootDir != null){
			File root = new File(rootDir);
			
			if (includeAncilData) {
				pd.setAncil( TabDelimFileUtil.readAsInteger(new File(root, "ancil.txt"), true, ANCIL_HEADINGS) );
			}
			pd.setCoef( TabDelimFileUtil.readAsDouble(new File(root, "coef.txt"), true) );
			pd.setSrc( TabDelimFileUtil.readAsDouble(new File(root, "src.txt"), true) );
			pd.setTopo( TabDelimFileUtil.readAsDouble(new File(root, "topo.txt"), true, TOPO_HEADINGS) );
			
			root = null;
		} else {
			throw new IllegalArgumentException("Must specify a rootPackage or rootDir.");
		}
		
		if (onlyZeroIteration) {
			//Find the last row containing this iteration
			int lastRow = pd.getCoef().orderedSearchLast(0, 0);
			
			if (lastRow > -1) {
				pd.setCoef( new Data2DView(pd.getCoef(), 0, lastRow + 1, 0, pd.getCoef().getColCount()) );
			} else {
				throw new IllegalStateException("Could not find the zero iteration in the data");
			}
			
			
		}
		
		
		ModelBuilder mb = new ModelBuilder(modelId);
		mb.setEnhNetworkId(enhNetworkId);
		Model model = mb.getImmutable();
		
		pd.setModel( model );
		
		return pd;
	}
	
	public static Double2D readAsDouble(File file, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
	  FileInputStream fis = new FileInputStream(file);
		return readAsDouble(fis, hasHeadings, null);		
	}
	
	public static Double2D readAsDouble(File file, boolean hasHeadings, String[] mappedHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
	  FileInputStream fis = new FileInputStream(file);
		return readAsDouble(fis, hasHeadings, mappedHeadings);		
	}
	
	public static Double2D readAsDouble(InputStream source, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
		
		return readAsDouble(source, hasHeadings, null);
	}
			
			
	public static Double2D readAsDouble(InputStream source, boolean hasHeadings, String[] mappedHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		
	  InputStreamReader isr = new InputStreamReader(source);
	  BufferedReader br = new BufferedReader(isr);
		
		
		try {
		
		  ArrayList list = new ArrayList(500);
			String[] headings = null;
			int[] remappedColumns = null;	//indexes to map the columns to
		  String s = null;					//Single line from file
		  int colCount = 0; //Number of columns (minus one) - must match in each row
			int mappedColumnCount = 0;	//Number of columns in the output
			
			if (hasHeadings) headings = readHeadings(br);
			
			if (mappedHeadings == null) {
				//assign later
			} else {
				remappedColumns = mapByColumnHeadings(headings, (String[]) mappedHeadings);
				mappedColumnCount = mappedHeadings.length;
			}
			
			while ((s=br.readLine())!=null){
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
				data[i] = (double[]) list.get(i);
			}
			
			Double2D data2D = new Double2D(data, (mappedHeadings==null)?headings:mappedHeadings);
			
			return data2D;
			
		} finally {
		  try {
		    br.close();
		  } catch (IOException e) {
		    //At this point we ignore.
		  }
			br = null;
		}
	}
	
	
	public static Int2D readAsInteger(File file, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		FileInputStream fis = new FileInputStream(file);
		return readAsInteger(fis, hasHeadings, null);
		
	}
	
	public static Int2D readAsInteger(File file, boolean hasHeadings, String[] mappedHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		FileInputStream fis = new FileInputStream(file);
		return readAsInteger(fis, hasHeadings, mappedHeadings);
		
	}
	
	public static Int2D readAsInteger(InputStream source, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		return readAsInteger(source, hasHeadings, null);
	}
	
	public static Int2D readAsInteger(InputStream source, boolean hasHeadings, String[] mappedHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		
		InputStreamReader isr = new InputStreamReader(source);
		BufferedReader br = new BufferedReader(isr);
		
		
		try {
		
		  ArrayList list = new ArrayList(500);
		  String[] headings = null;
			int[] remappedColumns = null;	//indexes to map the columns to
		  String s = null;          //Single line from file
		  int colCount = 0; //Number of columns in the input data - must match in each row
			int mappedColumnCount = 0;	//Number of columns in the output
		  
		  if (hasHeadings) headings = readHeadings(br);
			
			if (mappedHeadings == null) {
				//assign later
			} else {
				remappedColumns = mapByColumnHeadings(headings, (String[]) mappedHeadings);
				mappedColumnCount = mappedHeadings.length;
			}
			
			while ((s=br.readLine())!=null){
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
					
					list.add(row);
				} else {
					//ignore empty lines
				}
			}
			
			//copy the array list to an int[][] array
			int[][] data = new int[list.size()][];
			for (int i = 0; i < data.length; i++)  {
				data[i] = (int[]) list.get(i);
			}
			
		  Int2D data2D = new Int2D(data, (mappedHeadings==null)?headings:mappedHeadings);
		  
		  return data2D;
			
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				//At this point we ignore.
			}
			br = null;
		}
	}
	
	public static void write(Data2D data, File destination)
			throws IOException  {
	  FileOutputStream os = new FileOutputStream(destination, false);
		write(data, os);
		os.flush();
		os.close();
	}
			
			
			
	public static void write(Data2D data, OutputStream destination)
			throws IOException  {
			
		PrintWriter pw = new PrintWriter(destination);
		//BufferedWriter bw = new BufferedWriter(osw);
		
		
		StringBuffer line = new StringBuffer();
		int colCount = data.getColCount();
		int rowCount = data.getRowCount();
		
		//headings
		for (int i = 0; i < colCount; i++)  {
			line.append(data.getHeading(i, true) + "\t");
		}
		
		//Trim extra tab from end of line and write
		line.setLength(line.length() - 1);
		pw.println(line.toString());
		line.setLength(0);

		for (int r=0; r<rowCount; r++) {
			for (int c=0; c<colCount; c++) {
				line.append(data.getValueAt(r, c).toString() + "\t");
			}
			//Trim extra tab from end of line and write
			line.setLength(line.length() - 1);
			pw.println(line.toString());
			line.setLength(0);
		}
		
		pw.flush();
			
	}
	
	private static String[] readHeadings(BufferedReader br) throws IOException {
			
		
		String s;         //Single line from file
		
		if ((s=br.readLine())!=null) {
			String src[] = s.split("\t");
			
			if ( src.length > 0 && !(src.length == 1 && ("".equals(src[0]))) ) {
	
				return src;
				
			} else {
			
				return null;
				
			}
		} else {
		
			return null;
			
		}
			
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
	
	/**
	 * Returns an array of column indexes mapping the file headings to the 
	 * required headings.
	 * 
	 * The use case would be where only certain columns should be read in from the
	 * text file and the files can be named.  This method will take the list
	 * of columns that you want to import (mappedHeadings) and locates them in
	 * the headings from the file (fileHeadings).
	 * 
	 * A return array of column indexes is returned.  The first value in the array
	 * is the column index of the first mappedHeading, and so on.  The return
	 * array will be the same length as mappedHeadings or an error will be thrown
	 * because a heading could not be found.
	 * 
	 * Comparisons are case insensitive.
	 * 
	 * In the case of duplicate column names in the fileHeadings, only the first
	 * index is returned.
	 * 
	 * @param fileHeadings
	 * @param mappedHeadings
	 * @return
	 */
	 /*
	private static int[] mapColumnHeadings(String[] fileHeadings, String[] mappedHeadings) {
		int[] hIndex = new int[mappedHeadings.length];
		
		for(int i=0; i<mappedHeadings.length; i++) {
			for(int j=0; j<fileHeadings.length; j++) {
				boolean found = false;
				if (mappedHeadings[i].equalsIgnoreCase(fileHeadings[j])) {
					hIndex[i] = j;
					found = true;
					break;
				}
				
				if (!found) {
					throw new IllegalStateException("The mapped heading '" + mappedHeadings[i] + "' was not found in the source file.");
				}
			}
		}
		
		return hIndex;
	}
	*/
	
}
