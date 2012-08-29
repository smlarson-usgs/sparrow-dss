package gov.usgswim.datatable.view;

import gov.usgswim.datatable.*;
import gov.usgswim.datatable.impl.FindHelper;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A view of a table that provides a relative percentage row and/or column in
 * addition to the rows and columns of the base table.
 * 
 * The added row and/or column is always the last row or column in the table.
 * Relative percentage is based on the
 * 
 * Find and min/max functions will ignore the added relative percentage additions.
 * 
 * @author eeverman
 */
public class RelativePercentageView extends AbstractDataTableView implements DataTable.Immutable {

	/**
	 * The index of the row containing values for which a relative percentage
	 * should be calculated for each column.  The view will create an additional
	 * row at the end of the table containing the relative percentages of each
	 * value in this row.
	 * 
	 * May be null to indicate that no relative percentage row should be added.
	 */
	Integer relPercentBaseRow;
	
	/**
	 * The index of the row added to contain the relative percent.
	 * 
	 * Will always be one row beyond the last row of the base table, or null to
	 * indicate that no relative percentage row should be added.
	 */
	Integer relPercentRow;
	
	/**
	 * Total value (sum) of all values in the relPercentRow, excluding the
	 * relPercentHeaderColumn (if specified) and the possible added column for
	 * the relative percentage column.
	 */
	double relPercentRowTotal;
	
	/** Name of the relPercentRow. */
	String relPercentRowName;
	
	/**
	 * Column to place the relPercentRowName in (since rows don't have a name).
	 * If null, no row label is added.  If specified, the first column used for
	 * totalling (and for which there will be a relative percentage) will be
	 * the first column after this column.
	 */
	Integer relPercentHeaderColumn;
	
	/**
	 * The first column used for relative percent.
	 * If relPercentHeaderColumn is specified, it will be the first column after
	 * that column.  Note that no value is provided for columns which are not
	 * numeric types.
	 */
	int relPercentRowFirstCol;
	
	/**
	 * The index of the column containing values for which a relative percentage
	 * should be calculated for each row.  The view will create an additional
	 * column at the end of the table containing the relative percentages of each
	 * value in this column.
	 * 
	 * May be null to indicate that no relative percentage column should be added.
	 */
	Integer relPercentBaseCol;
	
	/**
	 * The index of the column added to contain the relative percent.
	 * 
	 * Will always be one column beyond the last column of the base table, or null to
	 * indicate that no relative percentage column should be added.
	 */
	Integer relPercentCol;
	
		/**
	 * Total value (sum) of all values in the relPercentCol, excluding the
	 * possible added row for the relative percentage row.
	 */
	double relPercentColTotal;
	
	/** Name of the relPercentCol. */
//	String relPercentColName;
	
	/** Description of the relPercentCol. */
//	String relPercentColDesc;
	
	/**
	 * If true, 100% is represented as 1.  If false, 100% is represented as 100.
	 */
	boolean useFraction;
	
	/** Attributes for the relative percentage column */
	ColumnAttribs relPercentColAttribs;
	
	
	/**
	 * Full constructor
	 * @param baseTableOrView
	 * @param relPercentBaseRow
	 * @param relPercentRowName
	 * @param relPercentHeaderColumn
	 * @param relPercentBaseCol
	 * @param relPercentColName
	 * @param relPercentColDesc
	 * @param useFraction 
	 */
	public RelativePercentageView(DataTable baseTableOrView,
					Integer relPercentBaseRow, String relPercentRowName, Integer relPercentHeaderColumn,
					Integer relPercentBaseCol, 	String relPercentColName, String relPercentColDesc,
					boolean useFraction) {
		
		super(baseTableOrView);
		
		//
		//Some simple validation
		if (relPercentBaseRow == null && relPercentBaseCol == null) {
			throw new IllegalArgumentException(
							"Either the relPercentBaseRow of relPercentBaseCol parameter (or both) must be specified.");
		}
		if (relPercentBaseRow != null && (relPercentBaseRow < 0 || relPercentBaseRow >= base.getRowCount())) {
			throw new IllegalArgumentException("The relPercentBaseRow, " + relPercentBaseRow + ", is invalid");
		}
		if (relPercentBaseCol != null && (relPercentBaseCol < 0 || relPercentBaseCol >= base.getColumnCount())) {
			throw new IllegalArgumentException("The relPercentBaseCol, " + relPercentBaseCol + ", is invalid");
		}
		
		
		//
		//Simple assignment
		this.relPercentBaseRow = relPercentBaseRow;
		this.relPercentBaseCol = relPercentBaseCol;
		this.useFraction = useFraction;
		
		//
		//Configure relative row percentage
		if (relPercentBaseRow != null) {
			relPercentRow = base.getRowCount();
			this.relPercentHeaderColumn = relPercentHeaderColumn;
			
			//First column to create totals and a relative percentage from
			if (relPercentHeaderColumn != null) {
				relPercentRowFirstCol = relPercentHeaderColumn + 1;
			} else {
				relPercentRowFirstCol = 0;
			}
			
			
			//Create name and description
			if (relPercentRowName == null) {
				if (useFraction) {
					this.relPercentRowName = RelationType.rel_fraction.getFullName();
				} else {
					this.relPercentRowName = RelationType.rel_percent.getFullName();
				}
			} else {
				this.relPercentRowName = relPercentRowName;
			}
			
			initRowTotal();
			
		}
		
		if (relPercentBaseCol != null) {
			relPercentCol = base.getColumnCount();
			ColumnAttribsBuilder colAttribsBuilder = new ColumnAttribsBuilder();
			
			if (useFraction) {
				colAttribsBuilder.setProperty(RelationType.XML_ATTRIB_NAME, RelationType.rel_fraction.name());
			} else {
				colAttribsBuilder.setProperty(RelationType.XML_ATTRIB_NAME, RelationType.rel_percent.name());
			}
			
			//Create name and description
			if (relPercentColName == null) {
				if (useFraction) {
					colAttribsBuilder.setName(RelationType.rel_fraction.getFullName());
				} else {
					colAttribsBuilder.setName(RelationType.rel_percent.getFullName());
				}
			} else {
				colAttribsBuilder.setName(relPercentColName);
			}
			
			if (relPercentColDesc == null) {
				if (useFraction) {
					colAttribsBuilder.setDescription(
							"Relative fraction of each row to the total of all rows for '" +
							base.getName(relPercentBaseCol) + "'");
				} else {
					colAttribsBuilder.setDescription(
							"Relative percentage of each row to the total of all rows for '" + 
							base.getName(relPercentBaseCol) + "'");
				}
			} else {
				colAttribsBuilder.setDescription(relPercentColDesc);
			}
			
			relPercentColAttribs = colAttribsBuilder.toImmutable();
			
			initColTotal();
		}
		
	}
	
	public RelativePercentageView(DataTable baseTableOrView,
				Integer relPercentBaseRow, Integer relPercentHeaderColumn,
				Integer relPercentBaseCol,
				boolean useFraction) {

		this(baseTableOrView,
				relPercentBaseRow, null, relPercentHeaderColumn,
				relPercentBaseCol, 	null, null,
				useFraction);
	}
	
	/**
	 * Find the total of all values in the relPercentBaseRow and store to relPercentRowTotal.
	 */
	protected void initRowTotal() {
		double tot = 0;
		
		for (int c = relPercentRowFirstCol; c < base.getColumnCount(); c++) {
			Double v = base.getDouble(relPercentBaseRow, c);
			if (v != null) tot += v;
		}
		
		relPercentRowTotal = tot;
	}
	
	/**
	 * Find the total of all values in the relPercentBaseCol and store to relPercentColTotal.
	 */
	protected void initColTotal() {
		double tot = 0;
		
		for (int r = 0; r < base.getRowCount(); r++) {
			Double v = base.getDouble(r, relPercentBaseCol);
			if (v != null) tot += v;
		}
		
		relPercentColTotal = tot;
	}
	
	/**
	 * The total for the relPercentBaseRow, not including the relativePercentage
	 * column, if specified.
	 * 
	 * @return 
	 */
	public Double getBaseRowTotal() {
		if (relPercentBaseRow != null) {
			return relPercentRowTotal;
		} else {
			return null;
		}
	}
	
		/**
	 * The total for the relPercentBaseCol, not including the relativePercentage
	 * row, if specified.
	 * 
	 * @return 
	 */
	public Double getBaseColTotal() {
		if (relPercentBaseCol != null) {
			return relPercentColTotal;
		} else {
			return null;
		}
	}
	
	@Override
	public Double getDouble(int row, int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			if (relPercentRow != null && relPercentRow.equals(row)) {
				return null;	//No value for the rel percentage of the rel percentage row
			} else {
				Double fractionTop = super.getDouble(row, relPercentBaseCol);
				if (fractionTop != null) {
					Double frac = fractionTop / relPercentColTotal;
					if (useFraction) {
						return frac;
					} else {
						return frac * 100;
					}
				} else {
					return null;
				}
			}
		} else if (relPercentRow != null && relPercentRow.equals(row)) {

			Double fractionTop = super.getDouble(relPercentBaseRow, col);
			if (fractionTop != null) {
				Double frac = fractionTop / relPercentRowTotal;
				if (useFraction) {
					return frac;
				} else {
					return frac * 100;
				}
			} else {
				return null;
			}

		} else {
			return super.getDouble(row, col);
		}
	}
	
	@Override
	public Object getValue(int row, int col) {
		if (
						(relPercentRow != null && relPercentRow.equals(row)) ||
						(relPercentCol != null && relPercentCol.equals(col))) {
			return getDouble(row, col);
		} else {
			return super.getValue(row, col);
		}
	}


	@Override
	public Integer getColumnByName(String name) {
		if (relPercentColAttribs != null && relPercentColAttribs.getName("").equals(name)) {
			return this.relPercentCol;
		} else {
			return super.getColumnByName(name);
		}
	}

	@Override
	public int getColumnCount() {
		if (relPercentCol != null) {
			return super.getColumnCount() + 1;
		} else {
			return super.getColumnCount();
		}
	}

	@Override
	public Class<?> getDataType(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return Double.class;
		} else {
			return super.getDataType(col);
		}
	}

	@Override
	public String getDescription(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return relPercentColAttribs.getDescription();
		} else {
			return super.getDescription(col);
		}
	}

	@Override
	public String getName(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return relPercentColAttribs.getName();
		} else {
			return super.getName(col);
		}
	}

	@Override
	public Map<String, String> getProperties(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return relPercentColAttribs.getProperties(null);
		} else {
			return super.getProperties(col);
		}
	}

	@Override
	public String getProperty(int col, String name) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return relPercentColAttribs.getProperty(name);
		} else {
			return super.getProperty(col, name);
		}
	}

	@Override
	public Set<String> getPropertyNames(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return relPercentColAttribs.getPropertyNames(null);
		} else {
			return super.getPropertyNames(col);
		}
	}

	@Override
	public int getRowCount() {
		if (relPercentRow != null) {
			return super.getRowCount() + 1;
		} else {
			return super.getRowCount();
		}
	}

	@Override
	public String getUnits(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			if (useFraction) {
				return "fraction";
			} else {
				return "percent";
			}
		} else {
			return super.getUnits(col);
		}
	}

	@Override
	public boolean isValid(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return true;
		} else {
			return super.isValid(col);
		}
	}

	@Override
	public Immutable toImmutable() {
		if (base instanceof DataTable.Immutable) {
			return this;
		} else {
			return new RelativePercentageView(base.toImmutable(),
					relPercentBaseRow, relPercentRowName, relPercentHeaderColumn,
					relPercentBaseCol, 	relPercentColAttribs.getName(), relPercentColAttribs.getDescription(),
					useFraction);
		}
	}
	
	@Override
	public ColumnIndex getIndex() {
		if (super.base instanceof DataTable.Immutable) {
			return ((DataTable.Immutable) super.base).getIndex();
		} else {
			return new HashMapColumnIndex(super.base);
		}
	}
	
	// ==========================
	// FindXXX() Methods
	// ==========================
	/**
	 * Override so that when searching a normal (not a relative percentage) column,
	 * only the base table is searched.
	 * 
	 * This intentionally excludes the possibly added relative percentage row.  If
	 * the relative percentage column is specified, it is searched as expected.
	 */
	@Deprecated
	@Override
	public int[] findAll(int col, Object value) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return FindHelper.bruteForceFindAll(this, col, value);
		} else {
			return base.findAll(col, value);
		}
	}

	/**
	 * Override so that when searching a normal (not a relative percentage) column,
	 * only the base table is searched.
	 * 
	 * This intentionally excludes the possibly added relative percentage row.  If
	 * the relative percentage column is specified, it is searched as expected.
	 */
	@Override
	@Deprecated
	public int findFirst(int col, Object value) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return FindHelper.bruteForceFindFirst(this, col, value);
		} else {
			return base.findFirst(col, value);
		}
	}

	/**
	 * Override so that when searching a normal (not a relative percentage) column,
	 * only the base table is searched.
	 * 
	 * This intentionally excludes the possibly added relative percentage row.  If
	 * the relative percentage column is specified, it is searched as expected.
	 */
	@Override
	@Deprecated
	public int findLast(int col, Object value) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return FindHelper.bruteForceFindLast(this, col, value);
		} else {
			return base.findLast(col, value);
		}
	}

	// ========================
	// Max-Min Methods
	// ========================
	/**
	 * Override so that when searching a normal (not a relative percentage) column,
	 * only the base table is searched.
	 * 
	 * This intentionally excludes the possibly added relative percentage row.  If
	 * the relative percentage column is specified, it is searched as expected.
	 */
	@Override
	public Double getMaxDouble(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return FindHelper.bruteForceFindMaxDouble(this, col);
		} else {
			return base.getMaxDouble(col);
		}
	}

	/**
	 * Override to search only the base table, intentionally excluding added
	 * relative relative percentage values.
	 */
	@Override
	public Double getMaxDouble() {
		return FindHelper.bruteForceFindMaxDouble(base);
	}
	
	/**
	 * Override so that when searching a normal (not a relative percentage) column,
	 * only the base table is searched.
	 * 
	 * This intentionally excludes the possibly added relative percentage row.  If
	 * the relative percentage column is specified, it is searched as expected.
	 */
	@Override
	public Double getMinDouble(int col) {
		if (relPercentCol != null && relPercentCol.equals(col)) {
			return FindHelper.bruteForceFindMinDouble(this, col);
		} else {
			return base.getMinDouble(col);
		}
	}

	/**
	 * Override to search only the base table, intentionally excluding added
	 * relative relative percentage values.
	 */
	@Override
	public Double getMinDouble() {
		return base.getMinDouble();
	}
	
}
