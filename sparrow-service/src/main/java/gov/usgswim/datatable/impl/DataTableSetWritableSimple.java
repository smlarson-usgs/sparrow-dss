package gov.usgswim.datatable.impl;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableSet;
import gov.usgswim.datatable.DataTableSetWritable;
import gov.usgswim.datatable.impl.DataTableSetSimple;
import java.util.*;

/**
 * This implementation does not care if the DataTable implementations are
 * writable or not.  Only the structure of the DataTableSet is ensured to be
 * modifiable.
 * 
 * @author eeverman
 */
public class DataTableSetWritableSimple 
		extends AbstractDataTableSet<DataTable>
		implements DataTableSetWritable<DataTable> {

	protected List<DataTable> tables = new ArrayList<DataTable>();
	
	
	public DataTableSetWritableSimple() {
		super(null, null);
	}
	
	public DataTableSetWritableSimple(Collection<DataTable> tables, String name, String description) {
		super(name, description);
		if (tables != null) tables.addAll(tables);
	}
	
	public DataTableSetWritableSimple(String name, String description) {
		super(name, description);
	}
	
	@Override
	public DataTableSetWritable addTable(DataTable table) {
		tables.add(table);
		return this;
	}

	@Override
	public DataTable removeTable(int index) throws IndexOutOfBoundsException {
		return tables.remove(index);
	}

	@Override
	public DataTable setTable(DataTable table, int tableIndex) {
		DataTable replaced = null;
		if (tableIndex < tables.size()) {
			replaced = tables.get(tableIndex);
			tables.set(tableIndex, table);
		} else if (tableIndex == tables.size()) {
			tables.add(table);
		} else {
			throw new IndexOutOfBoundsException(
					"The specified table index would leave a gap in the columns");
		}
		
		return replaced;
	}


	@Override
	public void setDescription(String desc) {
		description = desc;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public DataTable getTable(int tableIndex) {
		return tables.get(tableIndex);
	}
	
	@Override
	public DataTable[] getTables() {
		return tables.toArray(new DataTable[tables.size()]);
	}
	
	@Override
	public int getTableCount() {
		return tables.size();
	}


	@Override
	public DataTableSet.Immutable toImmutable() {
		DataTable.Immutable[] newTables = new DataTable.Immutable[tables.size()];
		for (int i = 0; i < newTables.length; i++) {
			newTables[i] = tables.get(i).toImmutable();
		}
		
		return new DataTableSetSimple(newTables, name, description);
	}


}
