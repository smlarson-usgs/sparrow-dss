package gov.usgswim.sparrow;

import gov.usgs.cida.datatable.*;
import gov.usgs.cida.datatable.view.AbstractDataTableView;
import org.apache.commons.lang.ArrayUtils;

/**
 * This class is mostly created for the purpose of testing so that a topo
 * data object can be created from a view or existing table.
 * 
 * It could replace TopoDataImm - I'm not sure if there is a performance
 * penalty for the composition or not.
 * 
 * @author eeverman
 */
public class TopoDataComposit extends AbstractDataTableView implements TopoData {

	private static final long serialVersionUID = 1L;
	
	
	public TopoDataComposit(DataTable base) {
		super(base.toImmutable());
	}
	
	//
	//Basic get operations
	@Override
	public int getFromNode(int row) {
		return getInt(row, PredictData.TOPO_FNODE_COL);
	}

	@Override
	public int getToNode(int row) {
		return getInt(row, PredictData.TOPO_TNODE_COL);
	}

	@Override
	public boolean isIfTran(int row) {
		return getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
	}

	@Override
	public int getHydSeq(int row) {
		return getInt(row, PredictData.TOPO_HYDSEQ_COL);
	}

	@Override
	public boolean isShoreReach(int row) {
		return getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
	}

	@Override
	public double getFrac(int row) {
		return getDouble(row, PredictData.TOPO_FRAC_COL);
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
	public boolean isIdAllowedDownstreamReaches(long reachId) {
		return isAllowedDownstreamReaches(getRowForId(reachId));
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
		if (! isAllowedUpstreamReaches(row)) return ArrayUtils.EMPTY_INT_ARRAY;
		
		int fnode = getFromNode(row);

		//The index requires that an Integer be used.
		int[] upstream = findAll(PredictData.TOPO_TNODE_COL, new Integer((int)fnode));
		
		upstream = removeReachesNotAllowedToHaveDownstreamReaches(upstream);

		return upstream;
	}
	

	@Override
	public long[] findAllowedUpstreamReachIds(long reachId) {
		int[] found = findAllowedUpstreamReaches(getRowForId(reachId));
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
				return 1d;

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
	 * @return 
	 */
	protected int[] removeReachesNotAllowedToHaveDownstreamReaches(int[] rows) {

		for (int rr = 0; rr < rows.length; rr++) {
			
			if (! isAllowedDownstreamReaches(rows[rr])) {
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

	@Override
	public Immutable toImmutable() {
		return this;
	}

	@Override
	public ColumnIndex getIndex() {
		return base.getIndex();
	}
	


}
