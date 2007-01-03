package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Double2D;

import gov.usgswim.sparrow.Int2D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.util.ArrayList;

public class TabDelimFileUtil {
	private TabDelimFileUtil() { /* no instances */}
	
	public static Double2D readAsDouble(File file, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
	  FileInputStream fis = new FileInputStream(file);
		return readAsDouble(fis, hasHeadings);
		
	}
	
	public static Double2D readAsDouble(InputStream source, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		
	  InputStreamReader isr = new InputStreamReader(source);
	  BufferedReader br = new BufferedReader(isr);
		
		
		try {
		
		  ArrayList list = new ArrayList(500);
			String[] headings = null;
		  String s = null;					//Single line from file
		  int colCount = 0; //Number of columns (minus one) - must match in each row
			
			if (hasHeadings) headings = readHeadings(br);
			
			while ((s=br.readLine())!=null){
				String src[] = s.split("\t");
				
				if ( src.length > 0 && !(src.length == 1 && ("".equals(src[0]))) ) {
					
					if (colCount == 0) {
						colCount = src.length;
					} else {
						if (src.length != colCount) {
							throw new NumberFormatException("Each row in the file must have the same number of delimiters");
						}
					}
		
					double[] row = new double[src.length];
					
					for (int i = 0; i < src.length; i++)  {
						row[i] = Double.parseDouble( src[i] );
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
			
			Double2D data2D = new Double2D(data, headings);
			
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
		return readAsInteger(fis, hasHeadings);
		
	}
	
	public static Int2D readAsInteger(InputStream source, boolean hasHeadings)
			throws FileNotFoundException, IOException, NumberFormatException  {
			
		
		InputStreamReader isr = new InputStreamReader(source);
		BufferedReader br = new BufferedReader(isr);
		
		
		try {
		
		  ArrayList list = new ArrayList(500);
		  String[] headings = null;
		  String s = null;          //Single line from file
		  int colCount = 0; //Number of columns (minus one) - must match in each row
		  
		  if (hasHeadings) headings = readHeadings(br);
			
			while ((s=br.readLine())!=null){
				String src[] = s.split("\t");
				
				  if ( src.length > 0 && !(src.length == 1 && ("".equals(src[0]))) ) {
					
					if (colCount == 0) {
						colCount = src.length;
					} else {
						if (src.length != colCount) {
							throw new NumberFormatException("Each row in the file must have the same number of delimiters");
						}
					}
		
					int[] row = new int[src.length];
					
					for (int i = 0; i < src.length; i++)  {
						row[i] = Integer.parseInt( src[i] );
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
			
		  Int2D data2D = new Int2D(data, headings);
		  
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
}
