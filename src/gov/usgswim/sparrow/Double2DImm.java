package gov.usgswim.sparrow;

import gov.usgswim.Immutable;

import java.util.HashMap;

import java.util.LinkedList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * A loose wrapper around a 2D double array, which includes optional column headings.
 */
@Immutable
public class Double2DImm extends Data2DImmAbstract {
	private final double[][] _data;
	
	private volatile HashMap<Double, Integer> idIndex;
	
	public Double2DImm(double[][] data) {
		this(data, null, -1);
	}
	
	public Double2DImm(double[][] data, String[] headings) {
		this(data, headings, -1);
	}
	
	public Double2DImm(double[][] data, String[] headings, Integer indexCol) {
		super((data != null)?data.length:0, (data != null && data.length > 0 && data[0] != null)?data[0].length:0, headings, indexCol);
		_data = data;
		
		idIndex = buildIndex();
	}
	
	public boolean isDoubleData() { return true; }
	
	public Data2D getImmutable() {
		return buildDoubleImmutable(getIdColumn());
	}
	
	public Data2D buildIntImmutable(int indexCol) {
		if (getIdColumn() == indexCol) {
			return this;
		} else {
			return new Int2DImm(getIntData(), getHeadings(), indexCol);
		}
	}
	
	public Data2D buildDoubleImmutable(int indexCol) {
		if (getIdColumn() == indexCol) {
			return this;
		} else {
			return new Double2DImm(_data, getHeadings(), indexCol);
		}
	}
	
	public int[][] getIntData() {
		return Data2DUtil.copyToIntData(_data);
	}
	
	public double[][] getDoubleData() {
		return Data2DUtil.copyToDoubleData(_data);
	}
	
	public Number getValue(int row, int col) throws IndexOutOfBoundsException {
		return new Double(_data[row][col]);
	}
	
	public int getInt(int row, int col) throws IndexOutOfBoundsException {
		return (int) _data[row][col];
	}
	

	public double getDouble(int row, int col) throws IndexOutOfBoundsException {
		return _data[row][col];
	}
	
	/**
	 * A very simple search implementation
	 * @param value
	 * @param column
	 * @return
	 */
	public int orderedSearchFirst(double value, int column) {
		for (int r = 0; r < _data.length; r++)  {
			if (_data[r][column] == value) return r;
		}
		return -1;
	}
	
	/**
	 * A very simple search implementation
	 * @param value
	 * @param column
	 * @return
	 */
	public int orderedSearchLast(double value, int column) {
		for (int r = _data.length - 1; r >= 0; r--)  {
			if (_data[r][column] == value) return r;
		}
		return -1;
	}
	

	public int findRowById(Double id) {
	
		if (idIndex != null) {
			Integer i = idIndex.get(id);
			if (i != null) {
				return i;
			} else {
				return -1;
			}
		} else {
			return -1;
		}

	}
	
	private HashMap<Double, Integer> buildIndex() {
		int rCount = getRowCount();
		int idCol = getIdColumn();
		
		if (idCol > -1) {

			HashMap<Double, Integer> map = new HashMap<Double, Integer>(rCount, 1.1f);
			
			for (int r = 0; r < rCount; r++)  {
				map.put(getDouble(r, idCol), r);
			}
			
			return map;
		} else {
			return null;
		}
	}
}
