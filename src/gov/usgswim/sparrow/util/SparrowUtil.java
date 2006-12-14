package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.Int2D;

import java.io.PrintStream;

public class SparrowUtil {
	private SparrowUtil() { /* no instances */ }



	public static void print2DArray(Int2D data, String caption) {
	  print2DArray(data, caption, System.out);
	}
	
	public static void print2DArray(Double2D data, String caption) {
	  print2DArray(data, caption, System.out);
	}

	public static void print2DArray(Int2D data, String caption, PrintStream stream) {
	  int[][] values = data.getData();
		stream.println(caption);
		
		if (data.hasHeadings()) {
			for (int i = 0; i < data.getColCount(); i++)  {
			  stream.print(data.getHeading(i) + "\t");
			}
		}
		
		for (int i = 0; i < values.length; i++)  {
		  for (int j = 0; i < values[i].length; i++)  {
				stream.print("" + values[i][j] + "\t");
			}
		  stream.println("");
		}
	}
	
	public static void print2DArray(Double2D data, String caption, PrintStream stream) {
	  double[][] values = data.getData();
		stream.println(caption);
		
	  if (data.hasHeadings()) {
	    for (int i = 0; i < data.getColCount(); i++)  {
	      stream.print(data.getHeading(i) + "\t");
	    }
	  }
		
		for (int i = 0; i < values.length; i++)  {
			for (int j = 0; j < values[i].length; j++)  {
				stream.print("" + values[i][j] + "\t");
			}
			stream.println("");
		}
	}
}
