package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTable;

import java.util.List;
import java.util.Map;

public class DeliveryResult extends SimpleDataTable {
	// ============
	// CONSTRUCTORS
	// ============
	// Use PredictResult
	public DeliveryResult(ColumnData[] columns, Map<String, String> properties, long[] rowIds, 
			double[] nodeTransportFraction, DataTable reachTransportFraction) {
		super(columns, "Delivery Data", "Delivery Result Data", properties, rowIds);
		// TODO Auto-generated constructor stub
	}

	public DeliveryResult(DataTableWritable writable,
			List<ColumnDataWritable> columns, Map<String, String> properties,
			Map<Long, Integer> idIndex, List<Long> idColumn, double[] nodeTransportFraction, DataTable reachTransportFraction) {
		super(writable, columns, properties, idIndex, idColumn);
		// TODO Auto-generated constructor stub
	}
	
	// ================
	// INSTANCE METHODS
	// ================
	@Override
	public int[] findAll(int col, Object value) {
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public int findFirst(int col, Object value) {
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public int findLast(int col, Object value) {
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public Double getDouble(int row, int col) {
		// TODO Auto-generated method stub
		return super.getDouble(row, col);
	}

	@Override
	public Float getFloat(int row, int col) {
		// TODO Auto-generated method stub
		return super.getFloat(row, col);
	}

	@Override
	public Integer getInt(int row, int col) {
		// TODO Auto-generated method stub
		return super.getInt(row, col);
	}

	@Override
	public Long getLong(int row, int col) {
		// TODO Auto-generated method stub
		return super.getLong(row, col);
	}

	@Override
	public Double getMaxDouble() {
		// TODO Auto-generated method stub
		return super.getMaxDouble();
	}

	@Override
	public Double getMaxDouble(int col) {
		// TODO Auto-generated method stub
		return super.getMaxDouble(col);
	}

	@Override
	public Integer getMaxInt() {
		// TODO Auto-generated method stub
		return super.getMaxInt();
	}

	@Override
	public Integer getMaxInt(int col) {
		// TODO Auto-generated method stub
		return super.getMaxInt(col);
	}

	@Override
	public Double getMinDouble() {
		// TODO Auto-generated method stub
		return super.getMinDouble();
	}

	@Override
	public Double getMinDouble(int col) {
		// TODO Auto-generated method stub
		return super.getMinDouble(col);
	}

	@Override
	public Integer getMinInt() {
		// TODO Auto-generated method stub
		return super.getMinInt();
	}

	@Override
	public Integer getMinInt(int col) {
		// TODO Auto-generated method stub
		return super.getMinInt(col);
	}

	@Override
	public Object getValue(int row, int col) {
		// TODO Auto-generated method stub
		return super.getValue(row, col);
	}



}
