package gov.usgs.cida.datatable;

/**
 * Empty index implementation that treats row IDs and row numbers as the same.
 * 
 * @author eeverman
 */
public class NoIdsColumnIndex implements ColumnIndex {
	
	private int maxRowNumber;
	
	public NoIdsColumnIndex(int maxRowNumber) {
		this.maxRowNumber = maxRowNumber;
	}
	
	@Override
	public int getRowForId(Long id) {
		return id.intValue();
	}
	
	@Override
	public int getRowForId(long id) {
		return (int) id;
	}

	@Override
	public Long getIdForRow(int row) {
		return new Long(row);
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
	public int getMaxRowNumber() {
		return maxRowNumber;
	}

	@Override
	public MutableColumnIndex toMutable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFullyPopulated() {
		return true;
	}



}
