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
		
		idIndex = buildEmptyHashMap(rowCount);
		
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
		
		idIndex = buildEmptyHashMap(rowCount);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = idList[r];
			idColumn[r] = id;
			idIndex.put(id, r);
		}
	}
	
	/**
	 * Builds a new immutable instance from the passed instance.
	 * 
	 * The new instance is detached from the passed instance.
	 * 
	 * @param idList Array of IDs in the order the rows are in the table.
	 */
	public HashMapColumnIndex(ColumnIndex original) throws IllegalArgumentException {
		
		if (! original.isFullyPopulated()) {
			throw new IllegalArgumentException(
					"The original ColumnIndex must be fully populated " +
					"in order to construct this immutable implementation from it.");
		}
		
		int rowCount = original.getMaxRowNumber() + 1;
		idColumn = new long[rowCount];
		
		idIndex = buildEmptyHashMap(rowCount);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = original.getIdForRow(r);
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
		
		idIndex = buildEmptyHashMap(rowCount);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = idList.get(r);
			
			if (id == null) {
				throw new IllegalArgumentException(
						"The idList must not contain nulls " +
						"to construct this type of immutable ColumnIndex from it.");
			}
			
			idColumn[r] = id;
			idIndex.put(id, r);
		}
	}
	
	/**
	 * Construct an 'ideal' sized hash map for the given number of values.
	 * 
	 * @param valueCount The number of values
	 * @return
	 */
	protected HashMap<Long, Integer> buildEmptyHashMap(int valueCount) {
		int hashSize = (valueCount / 2) * 2;	//even number equal to or one less that value count
		hashSize += 3;	//ensure an odd number
		
		//load Factor of 1 is a good compromise for size / speed.
		return new HashMap<Long, Integer>(hashSize, 1.1f);
	}
	
	@Override
	public int getRowForId(Long id) {
		Integer row = idIndex.get(id);
		if (row != null) return row;
		return -1;
	}
	
	@Override
	public int getRowForId(long id) {
		Integer row = idIndex.get(id);
		if (row != null) return row;
		return -1;
	}

	@Override
	public Long getIdForRow(int row) {
		try {
			return idColumn[row];
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public ColumnIndex toImmutable() {
		return this;
	}
	
	@Override
	public MutableColumnIndex toMutable() {
		return new MutableHashMapColumnIndex(this);
	}

	@Override
	public boolean hasIds() {
		return true;
	}
	
	@Override
	public int getMaxRowNumber() {
		return idColumn.length - 1;
	}
	
	@Override
	public boolean isFullyPopulated() {
		return true;
	}

}
