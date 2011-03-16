package gov.usgswim.sparrow.datatable;

import gov.usgswim.Immutable;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.SimpleDataTable;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An immutable implementation of PredictResult. This is simply an Immutable
 * DataTable with convenience getXXX methods appropriate to the content
 *
 * This will likely be the only implementation unless the PredictRunner code is
 * modified to use a builder instead of arrays.
 * 
 * One guarantee that this class makes is that the Total column for a given series
 * always follows the individual source columns.  For instance, all the per source
 * incremental columns are immediately followed by the total incremental column.
 * 
 *
 * @author eeverman
 */
@Immutable
public class PredictResultImm extends SimpleDataTable implements PredictResult {

	private static final long serialVersionUID = 1L;

	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Incremental value for that source.
	 */
	private final Map<Long, Integer> srcIdIncMap;
	
	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Decayed Incremental value for that source.
	 */
	private final Map<Long, Integer> srcIdDecayedIncMap;

	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Total value for that source.
	 */
	private final Map<Long, Integer> srcIdTotalMap;

	/**
	 * Index of the total Incremental column.
	 */
	private final int totalIncCol;
	
	/**
	 * Index of the total Decayed Incremental column.
	 */
	private final int totalDecayedIncCol;

	/**
	 * Index of the total Total column.
	 */
	private final int totalTotalCol;

	/**
	 * The number of sources.
	 */
	private final int sourceCount;


	public PredictResultImm(ColumnData[] columns, long[] rowIds, Map<String, String> properties,
				Map<Long, Integer> srcIdIncMap,
				Map<Long,Integer> srcIdDecayedIncMap,
				Map<Long, Integer> srcIdTotalMap,
				int totalIncCol, int totalDecayedIncCol, int totalTotalCol) {

		super(columns, "Prediction Data", "Prediction Result Data", properties, rowIds);

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdIncMap.size() * 2 + 1);
			map.putAll(srcIdIncMap);
			this.srcIdIncMap = map;
		}
		
		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdDecayedIncMap.size() * 2 + 1);
			map.putAll(srcIdDecayedIncMap);
			this.srcIdDecayedIncMap = map;
		}

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdTotalMap.size() * 2 + 1);
			map.putAll(srcIdTotalMap);
			this.srcIdTotalMap = map;
		}

		this.totalIncCol = totalIncCol;
		this.totalDecayedIncCol = totalDecayedIncCol;
		this.totalTotalCol = totalTotalCol;
		this.sourceCount = srcIdIncMap.size();
		
		//Check that total columns follow the individual source columns
		if (totalIncCol != (findMinValue(srcIdIncMap) + sourceCount)) {
			throw new IllegalArgumentException("The incremental total column must immediately follow the per-source columns");
		}
		
		if (totalDecayedIncCol != (findMinValue(srcIdDecayedIncMap) + sourceCount)) {
			throw new IllegalArgumentException("The decayed incremental total column must immediately follow the per-source columns");
		}
		
		if (totalTotalCol != (findMinValue(srcIdTotalMap) + sourceCount)) {
			throw new IllegalArgumentException("The total load column must immediately follow the per-source columns");
		}
	}

	@Override
    public int getSourceCount() {
        return sourceCount;
    }
	
	@Override
	public int getFirstIncrementalColForSrc() {
		return findMinValue(srcIdIncMap);
	}
	
	@Override
	public int getFirstDecayedIncrementalColForSrc() {
		return findMinValue(srcIdDecayedIncMap);
	}
	
	@Override
	public int getFirstTotalColForSrc() {
		return findMinValue(srcIdTotalMap);
	}
	
	private static int findMinValue(Map<Long, Integer> map) {
		Iterator<Entry<Long, Integer>> vals = map.entrySet().iterator();
		int min = Integer.MAX_VALUE;
		while (vals.hasNext()) {
			Entry<Long, Integer> entry = vals.next();
			if (entry.getValue() < min) {
				min = entry.getValue();
			}
		}
		return min;
	}

    @Override
    public Double getIncremental(int row) {
        return getDouble(row, totalIncCol);
    }
    
	@Override
	public Double getDecayedIncremental(int row) {
		return getDouble(row, totalDecayedIncCol);
	}

	@Override
    public int getIncrementalCol() {
        return totalIncCol;
    }
    
	@Override
	public int getDecayedIncrementalCol() {
		return totalDecayedIncCol;
	}

	@Override
    public Double getTotal(int row) {
        return getDouble(row, totalTotalCol);
    }

	@Override
    public int getTotalCol() {
        return totalTotalCol;
    }

	@Override
    public int getIncrementalColForSrc(Long srcId) {
        return srcIdIncMap.get(srcId);
    }
	
	@Override
	public int getDecayedIncrementalColForSrc(Long srcId) {
		return srcIdDecayedIncMap.get(srcId);
	}

	@Override
    public Double getIncrementalForSrc(int row, Long srcId) {
        return getDouble(row, srcIdIncMap.get(srcId));
    }
	
	@Override
	public Double getDecayedIncrementalForSrc(int row, Long srcId) {
		return getDouble(row, srcIdDecayedIncMap.get(srcId));
	}

	@Override
    public int getTotalColForSrc(Long srcId) {
        return srcIdTotalMap.get(srcId);
    }

	@Override
    public Double getTotalForSrc(int row, Long srcId) {
        return getDouble(row, srcIdTotalMap.get(srcId));
    }



}
