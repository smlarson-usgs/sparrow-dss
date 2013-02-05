package gov.usgswim.sparrow;

import gov.usgs.cida.datatable.*;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author eeverman
 */
public class TopoDataImm extends SimpleDataTable implements TopoData {

	private static final long serialVersionUID = 1L;
	
	// ===========
	// Constructors
	// ===========

	/**
	 * Constructor.
	 * 
	 * @param columns 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(List<ColumnData> columns) {
		super(columns);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param columns 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(ColumnData[] columns) {
		super(columns);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param refTable
	 * @param columns
	 * @param properties
	 * @param idColumn 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(DataTable refTable, ColumnData[] columns, Map<String, String> properties, List<Long> idColumn) {
		super(refTable, columns, properties, idColumn);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param refTable
	 * @param columns
	 * @param properties
	 * @param idColumn 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(DataTable refTable, ColumnData[] columns, Map<String, String> properties, ColumnData idColumn) {
		super(refTable, columns, properties, idColumn);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param columns
	 * @param name
	 * @param description
	 * @param properties
	 * @param rowIds 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(ColumnData[] columns, String name, String description, Map<String, String> properties, long[] rowIds) {
		super(columns, name, description, properties, rowIds);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param columns
	 * @param name
	 * @param description
	 * @param properties 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(ColumnData[] columns, String name, String description, Map<String, String> properties) {
		super(columns, name, description, properties);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param columns
	 * @param name
	 * @param description
	 * @param properties
	 * @param columnIndex 
	 * 
	 * @See gov.usgs.cida.datatable.impl.SimpleDataTable
	 */
	public TopoDataImm(ColumnData[] columns, String name, String description, Map<String, String> properties, ColumnIndex columnIndex) {
		super(columns, name, description, properties, columnIndex);
	}
	
	
	//
	//Basic get operations
	@Override
	public int getFromNode(int row) {
		return this.getInt(row, PredictData.TOPO_FNODE_COL);
	}

	@Override
	public int getToNode(int row) {
		return this.getInt(row, PredictData.TOPO_TNODE_COL);
	}

	@Override
	public boolean isIfTran(int row) {
		return this.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
	}

	@Override
	public int getHydSeq(int row) {
		return this.getInt(row, PredictData.TOPO_HYDSEQ_COL);
	}

	@Override
	public boolean isShoreReach(int row) {
		return this.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
	}

	@Override
	public double getFrac(int row) {
		return this.getDouble(row, PredictData.TOPO_FRAC_COL);
	}
	
	//
	//Topo logic operations

	@Override
	public boolean isAllowedUpstreamReaches(int row) {
		return ! isShoreReach(row);
	}

	@Override
	public boolean isIdAllowedUpstreamReaches(long reachId) {
		return isAllowedUpstreamReaches(getRowForId(reachId));
	}

	@Override
	public boolean isAllowedDownstreamReaches(int row) {
 		return ((! isShoreReach(row)) && isIfTran(row));
	}
	
	@Override
	public boolean isAllowedDownstreamReaches(int row, boolean ignoreIfTran) {
		if (ignoreIfTran) {
			return (! isShoreReach(row));
		} else {
			return ((! isShoreReach(row)) && isIfTran(row));
		}
	}

	@Override
	public boolean isIdAllowedDownstreamReaches(long reachId) {
		return isAllowedDownstreamReaches(getRowForId(reachId));
	}

	@Override
	public boolean isIdAllowedDownstreamReaches(long reachId, boolean ignoreIfTran) {
		return isAllowedDownstreamReaches(getRowForId(reachId), ignoreIfTran);
	}
		
	@Override
	public boolean isPartOfDiversion(int row) {
		if (isShoreReach(row)) return false;
		
		
		int[] rows = findAnyReachLeavingSameNode(row);
		int cnt = 0;	//count of non-shore reaches leaving this node
		
		for (int rr : rows) {
			if (isAllowedUpstreamReaches(rr)) cnt++;
		}
		
		return (cnt > 1);
	}

	@Override
	public boolean isIdPartOfDiversion(long reachId) {
		return isPartOfDiversion(getRowForId(reachId));
	}

	@Override
	public int[] findAllowedUpstreamReaches(int row) {
		return findAllowedUpstreamReaches(row, false);
	}
	
	@Override
	public int[] findAllowedUpstreamReaches(int row, boolean ignoreIfTran) {
		if (! isAllowedUpstreamReaches(row)) return ArrayUtils.EMPTY_INT_ARRAY;
		
		int fnode = getFromNode(row);

		//The index requires that an Integer be used.
		int[] upstream = findAll(PredictData.TOPO_TNODE_COL, new Integer((int)fnode));
		
		upstream = removeReachesNotAllowedToHaveDownstreamReaches(upstream, ignoreIfTran);

		return upstream;
	}

	@Override
	public long[] findAllowedUpstreamReachIds(long reachId) {
		int[] found = findAllowedUpstreamReaches(getRowForId(reachId), false);
		return convertRowsToIds(found);
	}
	
	@Override
	public long[] findAllowedUpstreamReachIds(long reachId, boolean ignoreIfTran) {
		int[] found = findAllowedUpstreamReaches(getRowForId(reachId), ignoreIfTran);
		return convertRowsToIds(found);
	}

	@Override
	public int[] findAnyUpstreamReaches(int row) {

		int fnode = getFromNode(row);

		//The index requires that an Integer be used.
		int[] upstream = findAll(PredictData.TOPO_TNODE_COL, new Integer((int)fnode));
		
		return upstream;
	}

	@Override
	public long[] findAnyUpstreamReachIds(long reachId) {
		int[] found = findAnyUpstreamReaches(getRowForId(reachId));
		return convertRowsToIds(found);
	}
	
	@Override
	public int[] findAnyDownstreamReaches(int row) {
		int tnode = getToNode(row);

		//The index requires that an Integer be used.
		int[] downtream = findAll(PredictData.TOPO_FNODE_COL, new Integer((int)tnode));
		
		return downtream;
	}

	@Override
	public double getCorrectedFracForRow(int row) {

		if (isShoreReach(row)) {
			return 0D;
		} else {
			//Excluded shore reaches, so zero rows are returned if a shore reach.
			int[] allReachesAtFromFnode = findFlowReachesLeavingSameNode(row);

			if (allReachesAtFromFnode.length == 1) {

				//If only a single reach (and this reach was not a shore reach),
				//the FRAC of this reach must be one.
				//return 1d;
				
				//We are no longer correcting this value, since it may represent a
				//real outflow to a water utility.
				return getFrac(row);
				
			} else {
				//Adjust frac per total

				double fracForReqestedReach = getFrac(row);
				double fracTotal = 0d;

				for (int i = 0; i < allReachesAtFromFnode.length; i++) {
					double thisFrac = getFrac(allReachesAtFromFnode[i]);
					fracTotal+= thisFrac;
				}

				if (Math.abs(fracTotal - 1D) < .001D) {
					//The total FRAC at this node is within one thousanth of one.
					//OK to use the reach frac value as-is.
					return fracForReqestedReach;
				} else {
					//Adjust the frac based on the total frac
					//Two reaches with fracs of .2 and .2 would have a total of .4, thus
					//an adjusted frac of .2 / .4 == .5
					return fracForReqestedReach / fracTotal;
				}

			}
		}
	}

	@Override
	public double getCorrectedFracForId(long reachId) throws Exception {
		return getCorrectedFracForRow(getRowForId(reachId));
	}
	
	
	
	//
	//Utility methods
	
	@Override
	public long[] convertRowsToIds(int[] rowNumbers) {
		if (rowNumbers.length == 0 || rowNumbers == null) {
			return ArrayUtils.EMPTY_LONG_ARRAY;
		} else {
			long[] ids = new long[rowNumbers.length];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = getIdForRow(rowNumbers[i]);
			}
			
			return ids;
		}
	}
	
	@Override
	public int[] findAnyReachLeavingSameNode(int row) {
		int fnode = getFromNode(row);
		
		return findAll(PredictData.TOPO_FNODE_COL, new Integer((int)fnode));
	}
	
	@Override
	public int[] findFlowReachesLeavingSameNode(int row) {
		int[] found = findAnyReachLeavingSameNode(row);
		return removeShoreReaches(found);
	}
	
	/**
	 * Removes reach rows from an array of reach rows if they
	 * are not allowed to have downstream reaches.
	 * 
	 * @param rows
	 * @param ignoreIfTran If true, ifTran is not considered.
	 * @return 
	 */
	protected int[] removeReachesNotAllowedToHaveDownstreamReaches(int[] rows, boolean ignoreIfTran) {

		for (int rr = 0; rr < rows.length; rr++) {
			
			if (! isAllowedDownstreamReaches(rows[rr], ignoreIfTran)) {
				rows = ArrayUtils.remove(rows, rr);
				rr--;
			}
		}
		
		return rows;
	}
	
	protected int[] removeShoreReaches(int[] rows) {
		for (int rr = 0; rr < rows.length; rr++) {
			
			if (isShoreReach(rows[rr])) {
				rows = ArrayUtils.remove(rows, rr);
				rr--;
			}
		}
		
		return rows;
	}



}
