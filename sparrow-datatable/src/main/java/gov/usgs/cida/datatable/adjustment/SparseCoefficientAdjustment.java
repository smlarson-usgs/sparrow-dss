package gov.usgs.cida.datatable.adjustment;

import gov.usgs.cida.datatable.*;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
import gov.usgs.cida.datatable.impl.FindHelper;
import gov.usgs.cida.datatable.impl.DataTableImmutableWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * SparseOverrideAdjustment accepts either sourceData or a default values as source, and then
 * allows values to be overridden on a cell by cell basis.
 *
 * @author ilinkuo
 */
public class SparseCoefficientAdjustment implements DataTableWritable {

	private static final long serialVersionUID = 1L;
	
	
		private DataTable source;
		protected Map<String, Number> adjustments = new HashMap<String, Number>();

		// ===========
		// CONSTRUCTOR
		// ===========
		public SparseCoefficientAdjustment(DataTable sourceData) {
			this.source = sourceData;
		}

		// =================
		// Lifecycle Methods
		// =================
		@Override
		public void setValue(String value, int row, int col) throws IndexOutOfBoundsException {
			adjust(row, col, Double.valueOf(value));
		}

		@Override
		public void setValue(Number value, int row, int col) throws IndexOutOfBoundsException {
			adjust(row, col, value);
		}

		@Override
		public void setValue(Object value, int row, int col) throws IndexOutOfBoundsException {
			adjust(row, col, (Number) value);
		}

		public void adjust(int row, int column, Number value) {
			adjustments.put(key(row, column), value);
		}

		private String key(int row, int column) {
			return row + "-" + column;
		}

		@Override
		public DataTable.Immutable toImmutable() {
			SparseCoefficientAdjustment immutableCore = new SparseCoefficientAdjustment(source.toImmutable());
			immutableCore.adjustments.putAll(this.adjustments);
			// invalidate self
			source = null;
			adjustments = null;
			return new DataTableImmutableWrapper(immutableCore);
		}


		// =================
		// Adjusted findXXX()
		// =================
		/**
		 * @see gov.usgswim.datatable.DataTable#findAll(int, java.lang.Object)
		 */
		@Override
		public int[] findAll(int col, Object value) {
			return FindHelper.bruteForceFindAll(this, col, value);
		}

		/**
		 * @see gov.usgswim.datatable.DataTable#findFirst(int, java.lang.Object)
		 */
		@Override
		public int findFirst(int col, Object value) {
			return FindHelper.bruteForceFindFirst(this, col, value);
		}

		/**
		 * @see gov.usgswim.datatable.DataTable#findLast(int, java.lang.Object)
		 */
		@Override
		public int findLast(int col, Object value) {
			return FindHelper.bruteForceFindLast(this, col, value);
		}

		// =================
		// Implementation specific
		// =================

		/**
		 * Returns the coefficient at the specified row and column.
		 *
		 * Returns null if there is no coefficient at this location.
		 */
		public Number getCoef(int row, int col) {
			return adjustments.get(key(row, col));
		}

		// =================
		// Adjusted getXXX()
		// =================
		@Override
		public Double getDouble(int row, int col) {
			Number coef = adjustments.get(key(row, col));
			if (coef == null) {
				return source.getDouble(row, col);
			}
			return coef.doubleValue() * source.getDouble(row, col);
		}

		@Override
		public Float getFloat(int row, int col) {
			return getDouble(row, col).floatValue();
		}

		@Override
		public Integer getInt(int row, int col) {
			return getDouble(row, col).intValue();
		}

		@Override
		public Object getValue(int row, int col) {
			Object value = source.getValue(row, col);
			if (value instanceof Number) {
				return getDouble(row, col);
			}
			return value;
		}

		@Override
		public String getString(int row, int col) {
			Double val = getDouble(row, col);
			if (val != null) {
				return val.toString();
			} else {
				return "[null]";
			}
		}

		@Override
		public Long getIdForRow(int row) {
			return source.getIdForRow(row);
		}

		@Override
		public Long getLong(int row, int col) {
			return getDouble(row, col).longValue();
		}

		// ========================
		// Adjusted Max-Min Methods
		// ========================
		// TODO [IK] change the implementation of these later
		@Override
		public Double getMaxDouble(int col) {
			return FindHelper.bruteForceFindMaxDouble(this, col);
		}

		@Override
		public Double getMaxDouble() {
			return FindHelper.bruteForceFindMaxDouble(this);
		}

		@Override
		public Integer getMaxInt(int col) {
			return getMaxDouble(col).intValue();
		}

		@Override
		public Integer getMaxInt() {
			return getMaxDouble().intValue();
		}

		@Override
		public Double getMinDouble(int col) {
			return FindHelper.bruteForceFindMinDouble(this, col);
		}

		@Override
		public Double getMinDouble() {
			return FindHelper.bruteForceFindMinDouble(this);
		}

		@Override
		public Integer getMinInt(int col) {
			return getMinDouble(col).intValue();
		}

		@Override
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
		// Delegated methods
		// =================
		@Override
		public Integer getColumnByName(String name) {return source.getColumnByName(name);}

		@Override
		public int getColumnCount() {return source.getColumnCount();}

		@Override
		public Class<?> getDataType(int col) {return source.getDataType(col);}

		@Override
		public String getDescription() {return source.getDescription();}

		@Override
		public String getDescription(int col) {return source.getDescription(col);}

		@Override
		public String getName() {return source.getName();}

		@Override
		public String getName(int col) {return source.getName(col);}

		@Override
		public String getProperty(String name) {return source.getProperty(name);}

		@Override
		public String getProperty(int col, String name) {return source.getProperty(col, name);}

		@Override
		public Set<String> getPropertyNames() {return source.getPropertyNames();}

		@Override
		public Set<String> getPropertyNames(int col) {return source.getPropertyNames(col);}

		@Override
		public Map<String, String> getProperties() { return source.getProperties(); }

		@Override
		public Map<String, String> getProperties(int col) { return source.getProperties(col); }
		
		@Override
		public int getRowCount() {return source.getRowCount();}

		@Override
		public int getRowForId(Long id) { return source.getRowForId(id);}

		@Override
		public String getUnits(int col) { return source.getUnits(col);}

		@Override
		public boolean hasRowIds() { return source.hasRowIds();}

		@Override
		public boolean isIndexed(int col) { return source.isIndexed(col);}

		@Override
		public boolean isValid() { return source.isValid(); }
		
		@Override
		public boolean isValid(int columnIndex) {
			return source.isValid(columnIndex);
		}

		// ===================
		// Unsupported Methods
		// ===================
		@Override
		public DataTableWritable addColumn(ColumnDataWritable column) {
			throw new UnsupportedOperationException("Cannot add columns to a View");
		}
		
		@Override
		public ColumnDataWritable removeColumn(int index)
				throws IndexOutOfBoundsException {
			throw new UnsupportedOperationException("Cannot add or remove columns from a view");
		}
		
		@Override
		public ColumnDataWritable setColumn(ColumnDataWritable column, int columnIndex) {
			
			throw new UnsupportedOperationException(
					"Not really useful to assign columns in a overlayed view." +
					"  Or perhaps it is, but its not implemented.");
		}
		
		@Override
		public ColumnDataWritable[] getColumns() {
			throw new UnsupportedOperationException(
					"Not really useful to work w/ columns in a overlayed view." +
					"  Or perhaps it is, but its not implemented.");
		}
		
		@Override
		public ColumnIndex getIndex() {
			return source.getIndex();
		}

		@Override
		public DataTableWritable buildIndex(int col) {
			throw new UnsupportedOperationException("Implement this if needed");
		}

		@Override
		public void setDescription(String desc) {
			throw new UnsupportedOperationException("Cannot set description on a view");
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException("Cannot set name on a view");
		}

		@Override
		public String setProperty(String key, String value) {
			throw new UnsupportedOperationException("Cannot set properties on a view (but should be able to?)");
		}

		@Override
		public void setRowId(long id, int row) {
			throw new UnsupportedOperationException("Cannot set ids on a view");
		}

	}

