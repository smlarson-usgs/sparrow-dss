package gov.usgs.cida.datatable.impl;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableSet;
import java.util.*;

/**
 *
 * @author eeverman
 */
public class DataTableSetSimple 
		extends AbstractDataTableSet<DataTable.Immutable>
		implements DataTableSet.Immutable {

	private final DataTable.Immutable[] tables;
	
	public DataTableSetSimple(DataTable.Immutable[] tables, String name, String description) {
		super(name, description);
		this.tables = Arrays.copyOf(tables, tables.length);
	}
	
	public DataTable.Immutable[] getTables() {
		return Arrays.copyOf(tables, tables.length);
	}
	
	@Override
	public DataTable.Immutable getTable(int tableIndex) {
		return tables[tableIndex];
	}
	
	@Override
	public int getTableCount() {
		return tables.length;
	}
	

	@Override
	public DataTableSet.Immutable toImmutable() {
		return this;
	}
	
	@Override
	public ColumnIndex getIndex() {
		if (getTableCount() > 0) {
			return getTable(0).getIndex();
		} else {
			return null;
		}
	}


}
