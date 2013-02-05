package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.TOPO_IFTRAN_COL;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.ReachRowValueMapImm;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.*;

/**
 * Calculates the area fractions for reaches upstream of a single selected reach.
 * 
 * The returned ReachRowValueMap contains the fraction of each upstream
 * incremental area that should be 'counted' when calculating upstream area.
 * As an example, consider the delta at the mouth of the Mississippi where the
 * river splits into several smaller reaches that actually enter the Gulf.  For
 * a given delta reach, a fraction - say .1 - of the upstream flow may enter that
 * reach.  When calculating the total upstream area of the delta reach, that same
 * fraction, .1 in this case, should be applied to the upstream incremental areas
 * since not all the load from those upstream area flow to the reach.
 * 
 * Similarly, if there is another split (called a diversion) farther upstream,
 * the fractions would compound.
 * 
 * For most parts of a model, the fraction will be one for all reaches with respect
 * to a given downstream reach.  However, any place the river splits, the FRAC
 * value from the model topo table will be some fraction less than one and reaches
 * upstream of this point will have their areas fractionally counted towards
 * downstream cumulative areas.
 * 
 * @author eeverman
 *
 */
public class CalcReachAreaFractionMap extends Action<ReachRowValueMap> {

	protected TopoData topoData;
	protected Long targetReachId;
	protected String msg = null;
	
	/** If true, FRAC values that do not total to 1 will not be corrected. Mostly for debugging. */
	protected boolean forceUncorrectedFracValues = false;
	
	/** If true, IfTran is not considered when looking for upstream reaches.  Debug/teseting use. */
	protected boolean forceIgnoreIfTran = false;
	
	//Alt config params
	protected transient ReachID reachId = null;
	
	/**
	 * Direct parameter initialization for testing
	 * @param topoData
	 * @param targetReachId 
	 * @param forceUncorrectedFracValues If true, do not correct FRAC values that do not total to one.  Always false for prod.
	 */
	public CalcReachAreaFractionMap(TopoData topoData, Long targetReachId, boolean forceUncorrectedFracValues, boolean forceIgnoreIfTran) {
		this.topoData = topoData;
		this.targetReachId = targetReachId;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
		this.forceIgnoreIfTran = forceIgnoreIfTran;
	}
	
	/**
	 * Single param cache-key initialization
	 * @param reachId 
	 * @param forceUncorrectedFracValues If true, do not correct FRAC values that do not total to one.  Always false for prod.
	 */
	public CalcReachAreaFractionMap(ReachID reachId, boolean forceUncorrectedFracValues, boolean forceIgnoreIfTran) {
		this.reachId = reachId;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
		this.forceIgnoreIfTran = forceIgnoreIfTran;
	}
	
	@Override
	protected void validate() {
		if (reachId != null) {
			//Everything is OK
		} else if (topoData == null || targetReachId == null) {
			this.addValidationError("The topo data or the reach ID is null.");
		}
	}
		
	@Override
	public void initFields() {
		if (reachId != null) {
			targetReachId = reachId.getReachID();
			topoData = SharedApplication.getInstance().getPredictData(reachId.getModelID()).getTopo();
		}
	}
	
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public ReachRowValueMap doAction() throws Exception {
		//Hash containing rows as keys and DeliveryReaches as values.
		
		int targetReachRowNumber = topoData.getRowForId(targetReachId);
		
		Collection<FractionalReach> areaFractions = calcAreaFractionForAllUpstreamReaches(targetReachRowNumber);
		ReachRowValueMap map = ReachRowValueMapImm.buildFromReachValues(areaFractions);
		return map;
	}

	/**
	 * Calculate the area fractions for the target reach and all its upstream
	 * reaches.
	 * 
	 * @param targetReachRow
	 * @return 
	 */
	protected Collection<FractionalReach> calcAreaFractionForAllUpstreamReaches(int targetReachRow) {
		
		//Queue of upstream reaches found by not yet processed
		PriorityQueue<FractionalReach> que = new PriorityQueue<FractionalReach>();
		
		//All reaches for which the area fraction calculation is completed
		ArrayList<FractionalReach> completedReaches = new ArrayList<FractionalReach>();
		
		//The fractional area of the start reach is 1 by definition.
		FractionalReach startReach = new FractionalReach(
				targetReachRow, 1D, topoData.getHydSeq(targetReachRow)
		);
		
		que.add(startReach);	//The target reach is the first one in the que to calculate
		
		while (! que.isEmpty()) {
			
			FractionalReach current = que.poll();
			completedReaches.add(current);	//At this point, the fraction is set
			
			//Get a list of 'real' upstream reaches.
			int[] realUpstreamReachRows = topoData.findAllowedUpstreamReaches(current.getRow(), forceIgnoreIfTran);
			
			if (realUpstreamReachRows.length > 0) {
				
				boolean currentIsADiversion = topoData.isPartOfDiversion(current.getRow());
				
				//The area fraction, based on the current downstream reach,
				//is applied to all upstream reaches.  Optionally do not correct the frac value.
				double upstreamFrac = forceUncorrectedFracValues ?
						(current.getFraction() * topoData.getFrac(current.getRow())) :
						(current.getFraction() * topoData.getCorrectedFracForRow(current.getRow()));

				for (int upstreamRow : realUpstreamReachRows) {

					if (currentIsADiversion) {
						
						//Current reach is part of a diversion so these upstream reaches
						//may have multiple routes from the target reach. If this upstreamReach
						//is already in the process que, merge the fraction values (add them).
						//Otherwise, add a new reach to the que.
						mergeOrAddToQue(que, upstreamRow, upstreamFrac);

					} else {

						//Assign the area fraction to this reach and put in the que to process
						que.add(new FractionalReach(upstreamRow, upstreamFrac, topoData.getHydSeq(upstreamRow)));

					}

				}	//each upstream row
			}	//if there are upstream rows
		}	//while the que of reaches to process is not empty
		
		return completedReaches;
	}
	
	/**
	 * If a FractionalReach of the same row is already in the Que, merge the fractions
	 * (add the values).  Otherwise, add a new FractionalReach.
	 * 
	 * @param que
	 * @param row Row of the reach we are looking to merge or add
	 * @param fraction Area fraction of the reach
	 */
	protected void mergeOrAddToQue(PriorityQueue<FractionalReach> que, int row, double fraction) {

		for (FractionalReach r : que) {
			if (r.getRow() == row) {
				
				//Found the reach - add the fraction to it and we're done
				r.addFraction(fraction);
				return;
			}
		}
		
		//Couldn't find this reach in the que, so add as new reach
		que.add(new FractionalReach(row, fraction, topoData.getHydSeq(row)));
	}
	
}
