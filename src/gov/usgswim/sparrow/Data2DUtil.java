package gov.usgswim.sparrow;

import org.apache.commons.lang.ArrayUtils;

public class Data2DUtil {
	private Data2DUtil() {
		//no instances
	}
	
	public static int[][] copyToIntData(int[][] data) {
		if (data != null && data.length > 0 && data[0] != null && data[0].length > 0) {
			int rc = data.length;
			int cc = data[0].length;
				
			int[][] out = new int[rc][];
			
			for (int r = 0; r < rc; r++)  {
				int[] row = new int[cc];
				System.arraycopy(data[r], 0, row, 0, cc);
				out[r] = row;
			}
			
			return out;

		} else {
			return Data2D.EMPTY_INT_2D_DATA;
		}
	}
	
	public static int[][] copyToIntData(double[][] data) {
		if (data != null && data.length > 0 && data[0] != null && data[0].length > 0) {
			int rc = data.length;
			int cc = data[0].length;
				
			int[][] out = new int[rc][];
			
			for (int r = 0; r < rc; r++)  {
				int[] row = new int[cc];
				for (int c = 0; c < cc; c++)  {
					row[c] = (int) data[r][c];
				}
				
				out[r] = row;
			}
			
			return out;

		} else {
			return Data2D.EMPTY_INT_2D_DATA;
		}
	}
	
	public static double[][] copyToDoubleData(int[][] data) {
		if (data != null && data.length > 0 && data[0] != null && data[0].length > 0) {
			int rc = data.length;
			int cc = data[0].length;
				
			double[][] out = new double[rc][];
			
			for (int r = 0; r < rc; r++)  {
				double[] row = new double[cc];
				for (int c = 0; c < cc; c++)  {
					row[c] = data[r][c];
				}
				out[r] = row;
			}
			
			return out;

		} else {
			return Data2D.EMPTY_DOUBLE_2D_DATA;
		}
	}
	
	public static double[][] copyToDoubleData(double[][] data) {
		if (data != null && data.length > 0 && data[0] != null && data[0].length > 0) {
			int rc = data.length;
			int cc = data[0].length;
				
			double[][] out = new double[rc][];
			
			for (int r = 0; r < rc; r++)  {
				double[] row = new double[cc];
				System.arraycopy(data[r], 0, row, 0, cc);
				out[r] = row;
			}
			
			return out;

		} else {
			return Data2D.EMPTY_DOUBLE_2D_DATA;
		}
	}
	
	
	public static String[] copyStrings(String[] src) {
		if (src != null && src.length > 0) {
			String[] out = new String[src.length];
			System.arraycopy(src, 0, out, 0, src.length);
			return out;
		} else {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
	}
	

}
