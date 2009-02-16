package gov.usgswim.sparrow;

import gov.usgswim.datatable.DataTable;

public class PredictResultStructure {

	public int sourceCount;
	public int totalIncrementalColOffset;
	public int grandTotalColOffset;
	public int reachCount;
	public int rchValColCount;
	public static PredictResultStructure analyzePredictResultStructure(int maxReachRow,
			DataTable sourceValues) {
		
		PredictResultStructure prs = new PredictResultStructure();
		prs.reachCount = maxReachRow + 1; // # of reaches is equal to the max row index + 1
		prs.sourceCount = sourceValues.getColumnCount(); // # of sources is equal to the number of 'columns' in an
															// arbitrary row (row zero)
		
		prs.totalIncrementalColOffset = 2*prs.sourceCount;
		prs.grandTotalColOffset = prs.totalIncrementalColOffset + 1;
		/*
		 * The number of predicted values per reach (k = number of sources, i =
		 * reach #) [i, 0 ... (k-1)] incremental added at reach, per source k
		 * (NOT decayed, just showing what comes in) [i, k ... (2k-1)] total at
		 * reach (w/ up stream contrib), per source k (decayed) [i, (2k)] total
		 * incremental contribution at reach (NOT decayed) [i, (2k + 1)] grand
		 * total at reach (incremental + from node). Comparable to measured.
		 * (decayed)
		 */
		prs.rchValColCount = (prs.sourceCount * 2) + 2;
		return prs;
	}

}
