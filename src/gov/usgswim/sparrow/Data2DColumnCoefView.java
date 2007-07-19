package gov.usgswim.sparrow;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

public class Data2DColumnCoefView extends Data2DView implements Data2D {

	double[] coef;
	Double maxValue;
	
	public Data2DColumnCoefView(Data2D data) {
		this(data, null, -1);
		coef = new double[getRowCount()];
	}
	
	public Data2DColumnCoefView(Data2D data, double[] coef) {
		this(data, coef, -1);
		coef = new double[getRowCount()];
	}
	
	public Data2DColumnCoefView(Data2D data, double[] coef, int indexCol) {
	
	  super(data, 0, data.getColCount(), indexCol);
		
		if (coef != null) {
			if (data.getColCount() != coef.length)
				throw new IllegalArgumentException(
					"The coef array must be the same length as the number of data columns."
				);
			this.coef = coef;
		} else {
			coef = new double[data.getColCount()];
			
			for (int i = 0; i < coef.length; i++)  {
				coef[i] = 1d;
			}
			
		}
	}
	
	public void setCoef(int col, double coef) throws IndexOutOfBoundsException {
		this.coef[col] = coef;
		maxValue = null;
	}
	
	public double getCoef(int col) throws IndexOutOfBoundsException {
		return coef[col];
	}


	public Number getValue(int row, int col) throws IndexOutOfBoundsException {
		Number val = super.getValue(row, col);
		return new Double(val.doubleValue() * coef[col]);
	}

	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		return (int)(super.getDouble(row, col) * coef[col]);
	}

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		return super.getDouble(row, col) * coef[col];
	}

}
