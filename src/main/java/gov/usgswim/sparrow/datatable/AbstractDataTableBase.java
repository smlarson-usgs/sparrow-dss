package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable;

import java.util.Set;

public abstract class AbstractDataTableBase implements DataTable {

	protected final DataTable base;

	public AbstractDataTableBase(DataTable base) {
		super();

		this.base = base;
	}

	public int[] findAll(int col, Object value) {
		return base.findAll(col, value);
	}

	public int findFirst(int col, Object value) {
		return base.findFirst(col, value);
	}

	public int findLast(int col, Object value) {
		return base.findLast(col, value);
	}

	public Integer getColumnByName(String name) {
		return base.getColumnByName(name);
	}

	public int getColumnCount() {
		return base.getColumnCount();
	}

	public String getDescription() {
		return base.getDescription();
	}

	public String getDescription(int col) {
		return base.getDescription(col);
	}

	public Long getIdForRow(int row) {
		return base.getIdForRow(row);
	}

	public Integer getMaxInt(int col) {
		return getMaxDouble(col).intValue();
	}

	public Integer getMaxInt() {
		return getMaxDouble().intValue();
	}

	public Integer getMinInt(int col) {
		return getMinDouble(col).intValue();
	}

	public Integer getMinInt() {
		return getMinDouble().intValue();
	}

	public String getName() {
		return base.getName();
	}

	public String getName(int col) {
		return base.getName(col);
	}

	public String getProperty(String name) {
		return base.getProperty(name);
	}

	public String getProperty(int col, String name) {
		return base.getProperty(col, name);
	}

	public Set<String> getPropertyNames() {
		return base.getPropertyNames();
	}

	public Set<String> getPropertyNames(int col) {
		return base.getPropertyNames(col);
	}

	public int getRowCount() {
		return base.getRowCount();
	}

	public int getRowForId(Long id) {
		return base.getRowForId(id);
	}

	public boolean hasRowIds() {
		return base.hasRowIds();
	}

	public boolean isIndexed(int col) {
		return base.isIndexed(col);
	}

	public boolean isValid() {
		return base.isValid();
	}

}