package gov.usgswim.sparrow;

import gov.usgswim.NotThreadSafe;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

/**
 * A view of a Data2D that allows coefficients to be sent for each column, which
 * modifies the values returned from the get() methods.
 * 
 * The underlying data is not editable and is not affected.
 */
@NotThreadSafe
public class Data2DColumnCoefView extends Data2DView implements Data2D {

	double[] coef;
	Double maxValue;
	
	public Data2DColumnCoefView(Data2D data) {
		this(data, null, -1);
		coef = new double[getColCount()];
		Arrays.fill(coef, 1d);
	}
	
	public Data2DColumnCoefView(Data2D data, double[] coef) {
		this(data, coef, -1);
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
			Arrays.fill(coef, 1d);
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
