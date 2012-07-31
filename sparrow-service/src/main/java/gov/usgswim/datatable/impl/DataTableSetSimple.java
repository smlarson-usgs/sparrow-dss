package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableSet;
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
		return getTables()[tableIndex];
	}
	
	@Override
	public int getTableCount() {
		return tables.length;
	}
	

	@Override
	public DataTableSet.Immutable toImmutable() {
		return this;
	}


}
