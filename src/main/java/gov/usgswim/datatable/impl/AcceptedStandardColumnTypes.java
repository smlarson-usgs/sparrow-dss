/**
 * 
 */
package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnDataWritable;

public enum AcceptedStandardColumnTypes {
	INTEGER(Integer.class), 
	LONG(Long.class), 
	FLOAT(Float.class), 
	DOUBLE(Double.class), 
	STRING(String.class);
	
	private Class<?> clazz;

	private AcceptedStandardColumnTypes(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public ColumnDataWritable makeNewColumn() {
		switch (this) {
			case INTEGER:
				return new StandardNumberColumnDataWritable<Integer>().setType(Integer.class);
			case LONG:
				return new StandardNumberColumnDataWritable<Long>().setType(Long.class);
			case FLOAT:
				return new StandardNumberColumnDataWritable<Float>().setType(Float.class);
			case DOUBLE:
				return new StandardNumberColumnDataWritable<Double>().setType(Double.class);
			case STRING:
				return new StandardStringColumnDataWritable(null, null);
			default:
				return null;
		}
	}
	
	public static AcceptedStandardColumnTypes getColumnType(Class<?> aClass) {
		for (AcceptedStandardColumnTypes colType: values()) {
			if (colType.clazz == aClass) {
				return colType;
			}
		}
		return null;
	}
}