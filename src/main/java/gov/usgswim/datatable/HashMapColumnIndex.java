package gov.usgswim.datatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapColumnIndex implements ColumnIndex {

	private long[] idColumn;
	private Map<Long, Integer> idIndex;
	
	public HashMapColumnIndex(DataTable refTable) {
		int rowCount = refTable.getRowCount();
		idColumn = new long[rowCount];
		
		//load Factor of 1 is a good compromise for size / speed.
		idIndex = new HashMap<Long, Integer>(rowCount, 1.1f);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = refTable.getIdForRow(r);
			idColumn[r] = id;
			idIndex.put(id, r);
		}
	}
	
	/**
	 * Builds a new instance from the passed list of IDs.
	 * 
	 * The new instance is detached from the passed id array.
	 * 
	 * @param idList Array of IDs in the order the rows are in the table.
	 */
	public HashMapColumnIndex(long[] idList) {
		int rowCount = idList.length;
		idColumn = new long[rowCount];
		
		//load Factor of 1 is a good compromise for size / speed.
		idIndex = new HashMap<Long, Integer>(rowCount, 1.1f);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = idList[r];
			idColumn[r] = id;
			idIndex.put(id, r);
		}
	}
	
	/**
	 * Builds a new instance from the passed list of IDs.
	 * 
	 * The new instance is detached from the passed id array.
	 * 
	 * @param idList List of IDs in the order the rows are in the table.
	 */
	public HashMapColumnIndex(List<Long> idList) {
		int rowCount = idList.size();
		idColumn = new long[rowCount];
		
		//load Factor of 1 is a good compromise for size / speed.
		idIndex = new HashMap<Long, Integer>(rowCount, 1.1f);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = idList.get(r);
			idColumn[r] = id;
			idIndex.put(id, r);
		}
	}
	
	@Override
	public int getRowForId(Long id) {
		Integer row = idIndex.get(id);
		if (row != null) return row;
		return -1;
	}

	@Override
	public Long getIdForRow(int row) {
		return idColumn[row];
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
		return true;
	}
	
	@Override
	public boolean isValidForRowNumber(int rowNumber) {
		return (rowNumber > -1 && rowNumber < idColumn.length);
	}

}
