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
		stream.println(caption);
		
		if (data.hasHeadings()) {
			for (int i = 0; i < data.getColCount(); i++)  {
			  stream.print(data.getHeading(i) + "\t");
			}
		}
		
		stream.println("");
		
		for (int r = 0; r < data.getRowCount(); r++)  {
			for (int c = 0; c < data.getColCount(); c++)  {
				stream.print("" + data.getInt(r, c) + "\t");
			}
			stream.println("");
		}
	}
	
	public static void print2DArray(Double2D data, String caption, PrintStream stream) {
		stream.println(caption);
		
	  if (data.hasHeadings()) {
	    for (int i = 0; i < data.getColCount(); i++)  {
	      stream.print(data.getHeading(i) + "\t");
	    }
	  }
		
		stream.println("");
		
		for (int r = 0; r < data.getRowCount(); r++)  {
			for (int c = 0; c < data.getColCount(); c++)  {
				stream.print("" + data.getDouble(r, c) + "\t");
			}
			stream.println("");
		}
	}
}
