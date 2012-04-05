package gov.usgswim.datatable;

import java.util.*;

/**
 * A mutable ColumnIndex that uses a HashMap for indexing.
 * 
 * This instance will allow missing index values and is intended to be used
 * while an index is being built.  Current immutable implementations require
 * that the source mutable version be fully populated (isFullyPopulated())
 * before allows conversion.
 * 
 * @author eeverman
 */
public class MutableHashMapColumnIndex implements MutableColumnIndex {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<Long> idColumn; 
	private Map<Long, Integer> idIndex;
	
	//Number of rows we contain
	//Used to allow the idColumn array to be larger than 
	private int rowCount = 0;	
	
	public MutableHashMapColumnIndex(DataTable refTable) {
		rowCount = refTable.getRowCount();
		idColumn = new ArrayList<Long>(rowCount);
		
		//load Factor of 1 is a good compromise for size / speed.
		idIndex = new HashMap<Long, Integer>(rowCount, 1.1f);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = refTable.getIdForRow(r);
			idColumn.add(id);
			idIndex.put(id, r);
		}
		
	}
	
	/**
	 * Builds a new mutable instance from the passed instance.
	 * 
	 * The new instance is detached from the passed instance.
	 * 
	 * @param idList Array of IDs in the order the rows are in the table.
	 */
	public MutableHashMapColumnIndex(ColumnIndex original) throws IllegalArgumentException {
		
		int rowCount = original.getMaxRowNumber() + 1;
		idColumn = new ArrayList<Long>(rowCount);
		
		idIndex = buildEmptyHashMap(rowCount);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = original.getIdForRow(r);
			idColumn.add(id);
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
	public MutableHashMapColumnIndex(long[] idList) {
		int rowCount = idList.length;
		idColumn = new ArrayList<Long>(rowCount);
		
		//load Factor of 1 is a good compromise for size / speed.
		idIndex = new HashMap<Long, Integer>(rowCount, 1.1f);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = idList[r];
			idColumn.add(id);
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
	public MutableHashMapColumnIndex(List<Long> idList) {
		int rowCount = idList.size();
		idColumn = new ArrayList<Long>(rowCount);
		
		//load Factor of 1 is a good compromise for size / speed.
		idIndex = new HashMap<Long, Integer>(rowCount, 1.1f);
		
		for (int r = 0; r < rowCount; r++) {
			Long id = idList.get(r);
			idColumn.add(id);
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
			return idColumn.get(row);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	@Override
	public void setRowId(int row, Long id) {
		ensureCapacityForRowNumber(row);
		idColumn.set(row, id);
		idIndex.put(id, row);
	}
	
	@Override
	public void setRowId(int row, long id) {
		ensureCapacityForRowNumber(row);
		idColumn.set(row, id);
		idIndex.put(id, row);
	}

	@Override
	public ColumnIndex toImmutable() {
		return new HashMapColumnIndex(this);
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
		return idColumn.size() - 1;
	}

	@Override
	public boolean isFullyPopulated() {
		int rowCount = idColumn.size();
		
		for (int i=0; i<rowCount; i++) {
			if (idColumn.get(i) == null) return false;
		}
		
		return true;
	}
	
	/**
	 * Ensures that we have capacity and values assigned to the idColumn.
	 * 
	 * The backing arrayList will not allow assigning values with gaps.
	 * 
	 * @param rowNumber
	 */
	protected void ensureCapacityForRowNumber(int rowNumber) {
		if (idColumn.size() <= rowNumber) {
			idColumn.ensureCapacity(rowNumber + 1);
			
			//fill new values with null
			for (int r = idColumn.size(); r <= rowNumber; r++) {
				idColumn.add(null);
			}
		}
	}

}
