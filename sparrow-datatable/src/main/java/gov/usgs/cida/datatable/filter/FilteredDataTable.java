package gov.usgs.cida.datatable.filter;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
import gov.usgs.cida.datatable.impl.FindHelper;
import gov.usgs.cida.datatable.impl.DataTableImmutableWrapper;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBiMap;
import gov.usgs.cida.datatable.*;

/**
 * Represents a filtered view of a source data table, by filtering entire rows
 * and/or columns out of the table.  The underlying data remains unchanged, but
 * row and column indices are mapped to give the {@code FilteredDataTable} the
 * appearance of having those rows and/or columns removed.
 *
 * <p />Filtering is achieved by implementing a predicate class that extends
 * {@code RowFilter} or {@code ColumnFilter} to filter rows or columns,
 * respectively.
 *
 * @see gov.usgswim.datatable.filter.RowFilter
 * @see gov.usgswim.datatable.filter.ColumnFilter
 */
public class FilteredDataTable implements DataTableWritable {

	private static final long serialVersionUID = 1L;

	/** The datatable to be filtered. */
    protected DataTable source;

    /** The filter to apply to the datatable's rows.  May be {@code null}. */
    protected RowFilter rowFilter;

    /** The filter to apply to the datatable's columns.  May be {@code null}. */
    protected ColumnFilter colFilter;

    /** A mapping from the filtered table's rows to the source table's rows. */
    protected Map<Integer,Integer> rowMap;

    /** A mapping from the filtered table's columns to the source table's columns. */
    protected Map<Integer,Integer> colMap;

    /**
     * Constructs a new {@code FilteredDataTable} that filters by row.
     *
     * @param source The datatable to be filtered.
     * @param filter The filter to apply to the datatable's rows.
     */
    public FilteredDataTable(DataTable source, RowFilter filter) {
        this(source, filter, null);
    }

    /**
     * Constructs a new {@code FilteredDataTable} that filters by column.
     *
     * @param source The datatable to be filtered.
     * @param filter The filter to apply to the datatable's columns.
     */
    public FilteredDataTable(DataTable source, ColumnFilter filter) {
        this(source, null, filter);
    }

    /**
     * Constructs a new {@code FilteredDataTable} that may filter by both row
     * and column.
     *
     * @param source The datatable to be filtered.
     * @param rowFilter The filter to apply to the datatable's rows.
     * @param colFilter The filter to apply to the datatable's columns.
     */
    public FilteredDataTable(DataTable source, RowFilter rowFilter, ColumnFilter colFilter) {
        this.source = source;
        this.rowFilter = rowFilter;
        this.colFilter = colFilter;
        if (this.rowFilter != null) {
            this.rowMap = buildRowMap();
        }
        if (this.colFilter != null) {
            this.colMap = buildColumnMap();
        }
    }

    /**
     * Creates a new {@code Map} containing the translation from the view's row
     * number (key) to the source's row number (value).
     *
     * Implementation note: Internally, this method uses a bi-directional map to
     * allow translation from view to source, as well as from source to view.
     *
     * @see mapR(int)
     * @see mapRowReverse(int)
     * @return A {@code Map} containing the translation from the view's row to
     *         the source's row.
     */
    protected Map<Integer,Integer> buildRowMap() {
        int newRowNum = 0;
        Map<Integer,Integer> mappedRows = HashBiMap.create();
        for (int rowNum = 0; rowNum < source.getRowCount(); rowNum++) {
            if (rowFilter.accept(source, rowNum)) {
                mappedRows.put(newRowNum++, rowNum);
            }
        }
        return mappedRows;
    }

    /**
     * Creates a new {@code Map} containing the translation from the view's
     * column number (key) to the source's column number (value).
     *
     * Implementation note: Internally, this method uses a bi-directional map to
     * allow translation from view to source, as well as from source to view.
     *
     * @see mapC(int)
     * @see mapColReverse(int)
     * @return A {@code Map} containing the translation from the view's column
     *         to the source's column.
     */
    protected Map<Integer,Integer> buildColumnMap() {
        int newColNum = 0;
        Map<Integer,Integer> mappedColumns = HashBiMap.create();
        for (int colNum = 0; colNum < source.getColumnCount(); colNum++) {
            if (colFilter.accept(source, colNum)) {
                mappedColumns.put(newColNum++, colNum);
            }
        }
        return mappedColumns;
    }

    public DataTable.Immutable toImmutable() {
        DataTable immutableCore = new FilteredDataTable(source.toImmutable(), rowFilter, colFilter);
        DataTable.Immutable result = new DataTableImmutableWrapper(immutableCore);

        // invalid self
        source = null;
        return result;
    }


    public boolean isValid() {
        return source.isValid();
    }
    
	@Override
	public boolean isValid(int columnIndex) {
		return source.isValid(columnIndex);
	}

    // ===============
    // Utility Methods
    // ===============
    /**
     * Utility method to handle the translation of columns from view index to
     * source index.
     *
     * @param col
     * @return
     */
    protected Integer mapC(int col) {
        if (col >= source.getColumnCount()) {
            throw new IndexOutOfBoundsException("0-based index of " + col + " is >= # of columns " + source.getColumnCount());
        } else if (colMap == null) {
            return col;
        } else if (col >= colMap.size()) {
            throw new IndexOutOfBoundsException("0-based index of " + col + " is >= # of columns " + colMap.size());
        } else {
            return colMap.get(col);
        }
    }

    /**
     * Utility method to handle the translation of columns from source index to
     * view index.
     *
     * @param col
     * @return
     */
    protected Integer mapColReverse(int col) {
        if (colMap == null) {
            return col;
        } else if (col >= source.getColumnCount()) {
            throw new IndexOutOfBoundsException("0-based index of " + col + " is >= # of columns " + source.getColumnCount());
        } else {
            HashBiMap<Integer,Integer> map = (HashBiMap<Integer,Integer>)colMap;
            return map.inverse().get(col);
        }
    }

    /**
     * Utility method to handle the translation of rows from view index to
     * source index.
     *
     * @param row
     * @return
     */
    protected Integer mapR(int row) {
        if (row >= source.getRowCount()) {
            throw new IndexOutOfBoundsException("0-based index of " + row + " is >= # of rows " + source.getRowCount());
        } else if (rowMap == null) {
            return row;
        } else if (row >= rowMap.size()) {
            throw new IndexOutOfBoundsException("0-based index of " + row + " is >= # of rows " + rowMap.size());
        } else {
            return rowMap.get(row);
        }
    }

    /**
     * Utility method to handle the translation of rows from source index to
     * view index.
     *
     * @param row
     * @return
     */
    protected Integer mapRowReverse(int row) {
        if (rowMap == null) {
            return row;
        } else if (row >= source.getRowCount()) {
            throw new IndexOutOfBoundsException("0-based index of " + row + " is >= # of rows " + source.getRowCount());
        } else {
            HashBiMap<Integer,Integer> map = (HashBiMap<Integer,Integer>)rowMap;
            return map.inverse().get(row);
        }
    }

    // ====================
    // INDEX & FIND Methods (may be inaccurate if rows are filtered
    // ====================
    /**
     * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
     * @deprecated Use at your own risk. The result may be inaccurate if not all rows are available in the view
     */
    @Deprecated
	public int[] findAll(int col, Object value) {
//      // Do we need to worry about incomplete rows? YES!
//      // if rows censored, then must adjust all returned values by startRow and check
//      // TODO brute force implementation later.
//      boolean isRowsUncensored = startRow == 0 && rowCount == source.getRowCount();
//      return (col < colCount && isRowsUncensored)? source.findAll(mapC(col), value): new int[0];
        return FindHelper.bruteForceFindAll(this, col, value);
    }

    /**
     * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
     * @deprecated Use at your own risk. The result may be inaccurate if not all rows are available in the view
     */
    @Deprecated
	public int findFirst(int col, Object value) {
//      // Do we need to worry about incomplete rows? YES
//      // TODO check result out of upper range of rows
//      return (col < colCount)? source.findFirst(mapC(col), value) - startRow: -1;
        return FindHelper.bruteForceFindFirst(this, col, value);
    }

    /**
     * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
     * @deprecated Use at your own risk. The result may be inaccurate if not all rows are available in the view
     */
    @Deprecated
	public int findLast(int col, Object value) {
//      // Do we need to worry about incomplete rows? YES
//      // TODO check result out of upper range of rows
//      return (col < colCount)? source.findLast(mapC(col), value) - startRow: -1;
        return FindHelper.bruteForceFindLast(this, col, value);
    }

    // ========================
    // Adjusted Max-Min Methods
    // ========================
    // TODO [IK] change the implementation of these later
    public Double getMaxDouble(int col) {
        return FindHelper.bruteForceFindMaxDouble(this, col);
    }

    public Double getMaxDouble() {
        return FindHelper.bruteForceFindMaxDouble(this);
    }

    public Integer getMaxInt(int col) {
        return getMaxDouble(col).intValue();
    }

    public Integer getMaxInt() {
        return getMaxDouble().intValue();
    }

    public Double getMinDouble(int col) {
        return FindHelper.bruteForceFindMinDouble(this, col);
    }

    public Double getMinDouble() {
        return FindHelper.bruteForceFindMinDouble(this);
    }

    public Integer getMinInt(int col) {
        return getMinDouble(col).intValue();
    }

    public Integer getMinInt() {
        return getMinDouble().intValue();
    }

	@Override
	public ColumnData getColumn(int colIndex) {
		
		if (colIndex < 0 || colIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Requested column index does not exist.");
		}
		
		ColumnDataFromTable col = new ColumnDataFromTable(this, colIndex);
		return col;
	}
	
    // =================
    // DataTable Methods
    // =================
    public Integer getColumnByName(String name) {
        return mapColReverse(source.getColumnByName(name));
    }
    public int getColumnCount() {
        if (colMap == null) {
            return source.getColumnCount();
        }
        return colMap.size();
    }
    public Class<?> getDataType(int col) {
        return source.getDataType(mapC(col));
    }
    public String getDescription() {
        return source.getDescription();
    }
    public String getDescription(int col) {
        return source.getDescription(mapC(col));
    }
    public Double getDouble(int row, int col) {
        return source.getDouble(mapR(row), mapC(col));
    }
    public Float getFloat(int row, int col) {
        return source.getFloat(mapR(row), mapC(col));
    }
    public Long getLong(int row, int col) {
        return source.getLong(mapR(row), mapC(col));
    }
    public Long getIdForRow(int row) {
        return source.getIdForRow(mapR(row));
    }
    public Integer getInt(int row, int col) {
        return source.getInt(mapR(row), mapC(col));
    }
    public String getName() {
        return source.getName();
    }
    public String getName(int col) {
        return source.getName(mapC(col));
    }
    public String getProperty(String name) {
        return source.getProperty(name);
    }
    public String getProperty(int col, String name) {
        return source.getProperty(mapC(col), name);
    }
    
	@Override
	public Map<String, String> getProperties() {
		return source.getProperties();
	}

	@Override
	public Map<String, String> getProperties(int col) {
		return source.getProperties(mapC(col));
	}
	
    public Set<String> getPropertyNames() {
        return source.getPropertyNames();
    }
    public Set<String> getPropertyNames(int col) {
        return source.getPropertyNames(mapC(col));
    }
    public int getRowCount() {
    	if (rowMap == null) {
    		return source.getRowCount();
    	}
    	return rowMap.size();
    }
    public int getRowForId(Long id) {
        Integer result = mapRowReverse(source.getRowForId(id));
        return (result == null) ? -1 : result;
    }
    public String getString(int row, int col) {
        return source.getString(mapR(row), mapC(col));
    }
    public String getUnits(int col) {
        return source.getUnits(mapC(col));
    }
    public Object getValue(int row, int col) {
        return source.getValue(mapR(row), mapC(col));
    }
    public boolean hasRowIds() {
        return source.hasRowIds();
    }
    public boolean isIndexed(int col) {
        return source.isIndexed(mapC(col));
    }

    // ======================
    // State-Changing Methods (write through to underlying table, if possible)
    // ======================
    public DataTableWritable addColumn(ColumnDataWritable column) {
    	throw new UnsupportedOperationException("Cannot add or remove columns from a filtered table");
    }
    
	@Override
	public ColumnDataWritable removeColumn(int index)
			throws IndexOutOfBoundsException {
		throw new UnsupportedOperationException("Cannot add or remove columns from a filtered table");
	}
    
    /**
     * This implementation operates on the mapped column indices.
     */
	@Override
	public ColumnDataWritable setColumn(ColumnDataWritable column, int columnIndex) {
		
		throw new UnsupportedOperationException(
				"Not really useful to assign columns in a mapped view." +
				"  Or perhaps it is, but its not implemented.");
	}
	
	@Override
	public ColumnDataWritable[] getColumns() {
		throw new UnsupportedOperationException(
				"Not really useful to work w/ columns in a mapped view." +
				"  Or perhaps it is, but its not implemented.");
	}
	
	@Override
	public ColumnIndex getIndex() {
		throw new UnsupportedOperationException(
				"Not really useful to work w/ an index in a mapped view." +
				"  Or perhaps it is, but its not implemented.");
	}
    
    

    public DataTableWritable buildIndex(int col) {
        DataTable result = null;
        if (source instanceof DataTableWritable) {
            result = ((DataTableWritable) source).buildIndex(mapC(col));
        }
        return (result == null)? null: this;
    }

    public void setDescription(String desc) {
        if (source instanceof DataTableWritable) {
            ((DataTableWritable) source).setDescription(desc);
        }
    }

    public void setName(String name) {
        if (source instanceof DataTableWritable) {
            ((DataTableWritable) source).setName(name);
        }
    }

    public String setProperty(String key, String value) {
        if (source instanceof DataTableWritable) {
            return ((DataTableWritable) source).setProperty(key, value);
        }
        return null;
    }

    public void setRowId(long id, int row) {
        if (source instanceof DataTableWritable) {
            ((DataTableWritable) source).setRowId(id, mapR(row));
        }
    }

    public void setValue(String value, int row, int col) throws IndexOutOfBoundsException {
        if (source instanceof DataTableWritable) {
            ((DataTableWritable) source).setValue(value, mapR(row), mapC(col));
        } else throw new UnsupportedOperationException("setValue() not allowed when base table is not writable");
    }

    public void setValue(Number value, int row, int col) throws IndexOutOfBoundsException {
        if (source instanceof DataTableWritable) {
            ((DataTableWritable) source).setValue(value, mapR(row), mapC(col));
        } else throw new UnsupportedOperationException("setValue() not allowed when base table is not writable");
    }

    public void setValue(Object value, int row, int col) throws IndexOutOfBoundsException {
        if (source instanceof DataTableWritable) {
            ((DataTableWritable) source).setValue(value, mapR(row), mapC(col));
        } else throw new UnsupportedOperationException("setValue() not allowed when base table is not writable");
    }


}
