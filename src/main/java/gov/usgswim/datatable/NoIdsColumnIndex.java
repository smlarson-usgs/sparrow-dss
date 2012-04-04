package gov.usgswim.datatable;

/**
 * Empty index implementation that treats row IDs and row numbers as the same.
 * 
 * @author eeverman
 */
public class NoIdsColumnIndex implements ColumnIndex {
	
	public NoIdsColumnIndex() {
	}
	
	@Override
	public int getRowForId(Long id) {
		return id.intValue();
	}

	@Override
	public Long getIdForRow(int row) {
		return new Long(row);
	}

	@Override
	public ColumnIndex getDetachedClone() {
		// This instance is immutable, so return it.
		return this;
	}

	@Override
	public ColumnIndex toImmutable() {
		return this;
	}

	@Override
	public boolean hasIds() {
		return false;
	}

	@Override
	public boolean isValidForRowNumber(int rowNumber) {
		return (rowNumber > -1);
	}

}
