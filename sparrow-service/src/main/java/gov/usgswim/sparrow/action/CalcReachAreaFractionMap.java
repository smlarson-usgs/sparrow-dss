package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.TOPO_IFTRAN_COL;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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

	protected DataTable topoData;
	protected Long targetReachId;
	protected String msg = null;
	
	/** If true, FRAC values that do not total to 1 will not be corrected. Mostly for debugging. */
	protected boolean forceUncorrectedFracValues = false;
	
	//Alt config params
	protected transient ReachID reachId = null;
	
	/**
	 * Direct parameter initialization for testing
	 * @param topoData
	 * @param targetReachId 
	 * @param forceUncorrectedFracValues If true, do not correct FRAC values that do not total to one.  Always false for prod.
	 */
	public CalcReachAreaFractionMap(DataTable topoData, Long targetReachId, boolean forceUncorrectedFracValues) {
		this.topoData = topoData;
		this.targetReachId = targetReachId;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
	}
	
	/**
	 * Single param cache-key initialization
	 * @param reachId 
	 * @param forceUncorrectedFracValues If true, do not correct FRAC values that do not total to one.  Always false for prod.
	 */
	public CalcReachAreaFractionMap(ReachID reachId, boolean forceUncorrectedFracValues) {
		this.reachId = reachId;
		this.forceUncorrectedFracValues = forceUncorrectedFracValues;
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
		HashMap<Integer, DeliveryReach> deliveries = calcDeliveryHash(topoData, targetReachId);
		ReachRowValueMap map = new ReachRowValueMap(deliveries);
		return map;
	}
	
	
	/**
	 * Calculates the delivery fractions for reaches upstream of a specified targets.
	 * A list of all upstream reaches are return, some of which may contain
	 * a zero value for delivery.
	 * 
	 * @param predictData The predict data to search within for upstream reaches
	 * @param targetReachIds A Set of reach IDs that define the targets.
	 * @return
	 * @throws Exception
	 */
	protected HashMap<Integer, DeliveryReach> calcDeliveryHash(
			DataTable topoData, Long targetReachId) throws Exception {
		
		
		//uses a row number as the key
		HashMap<Integer, DeliveryReach> upstreamAreaFractionMap = new HashMap<Integer, DeliveryReach>();
		
		int targetRow = topoData.getRowForId(targetReachId);
		DeliveryReach current = new DeliveryReach(
				targetRow, 1D, topoData.getInt(targetRow, PredictData.TOPO_HYDSEQ_COL)
		);
		

		List<DeliveryReach> upstreamAreaFractions = calcDeliveryForSingleTarget(topoData, current);

		//Hash the results into a HashMap for easy lookups
		for (DeliveryReach dr : upstreamAreaFractions) {
			upstreamAreaFractionMap.put(dr.getRow(), dr);

		}

		
		msg = "Upstream Area Calc Details:  Model size:  " + topoData.getRowCount() +
				" rows, Target ID = " + targetReachId + ".  " +
				"Found " + upstreamAreaFractionMap.size() + " upstream reaches.";
		
		return upstreamAreaFractionMap;
	}
	
	/**
	 * Calculates the delivery fractions for the reaches upstream of a specified target.
	 * A list of all upstream reaches are return, some of which may contain
	 * a zero value for delivery.
	 * 
	 * @param predictData The predict data to search within for upstream reaches
	 * @param targetReach The target reach to calculate delivery to
	 * @return A list of DeliveryReaches which are upstream of the targetReach
	 * @throws Exception
	 */
	protected List<DeliveryReach> calcDeliveryForSingleTarget(
			DataTable topo, DeliveryReach targetReach) throws Exception {
		
		
		//Queue of upstream reaches as they are found, in rev hydseq order
		Queue<DeliveryReach> upstreamReaches = new PriorityQueue<DeliveryReach>(4);
		
		//All reaches for which we have thus far calculated a delivery fraction
		List<DeliveryReach> calcCompletedReaches = new ArrayList<DeliveryReach>();
		
		//Add the current reaches' upstream reaches to the queue
		addUpstreamReachesToQueue(upstreamReaches, topo, targetReach);
		calcCompletedReaches.add(targetReach);	//add to output - should be delivery of 1
		
		//Continually loop through upstream reaches
		while (! upstreamReaches.isEmpty()) {
			DeliveryReach current = upstreamReaches.poll();
			
			//Add the current reaches' upstream reaches to the queue
			addUpstreamReachesToQueue(upstreamReaches, topo, current);
			
			calcCompletedReaches.add(current);	//Add current to list of completed
		}
		
		return calcCompletedReaches;
	}
	
	/**
	 * Adds the reaches immediately upstream to the queue.
	 * Reaches are added with a delivery fraction of zero.  If a duplicate reach
	 * is added (multiple pathways to the same reach), the current reach is
	 * added to its list of downstream reaches.
	 * @param upstreamReaches The queue to add to
	 * @param predictData The predict data to seach within
	 * @param current The current reach for which to find immediate upstream reaches
	 */
	protected void addUpstreamReachesToQueue(Queue<DeliveryReach> upstreamReaches,
			DataTable topo, DeliveryReach current) throws Exception {
		
		boolean isBaseReachAShoreReach = (1 == topo.getInt(current.getRow(), PredictData.TOPO_SHORE_REACH_COL));

		if (isBaseReachAShoreReach) {
			//Don't do any further processing for reaches 'upstream' of a shore reach.
			//A shore reach just goes along the edge of a lake or ocean, so its not really
			//part of the network.
			return;
		} else {
		
			double currentReachFrac = getCorrectedFracForReachRow(current.getRow(), topo);
			double currentReachAccumulatedFrac = current.getDelivery();
			double upstreamFrac = currentReachFrac * currentReachAccumulatedFrac;
			
			long fnode = topo.getLong(current.getRow(), PredictData.TOPO_FNODE_COL);

			//The index requires that an Integer be used.
			int[] upstream = topo.findAll(PredictData.TOPO_TNODE_COL, new Integer((int)fnode));

			for (int rowNum : upstream) {
				
				//Don't add this reach if it is a shore reach
				boolean isReachAShoreReach = (1 == topo.getInt(rowNum, PredictData.TOPO_SHORE_REACH_COL));
				boolean isTran = topo.getDouble(rowNum, TOPO_IFTRAN_COL) > 0;
				
				if (! isReachAShoreReach && isTran) {

					DeliveryReach toBeAdded = new DeliveryReach(
						rowNum, upstreamFrac, topo.getInt(rowNum, PredictData.TOPO_HYDSEQ_COL), current
					);


					upstreamReaches.add(toBeAdded);

				}
			}
		}
	}
	
	protected double getCorrectedFracForReachRow(int row, DataTable topo) throws Exception {
		
		//Bypass switch to use uncorrect values - mostly for debug comparison of models.
		if (forceUncorrectedFracValues) {
			return topo.getDouble(row, PredictData.TOPO_FRAC_COL);
		}
		
		//Find all other reaches that come from this same node
		Integer fnode = topo.getInt(row, PredictData.TOPO_FNODE_COL);
		int[] allReachesAtFromFnode = topo.findAll(PredictData.TOPO_FNODE_COL, fnode);

		if (allReachesAtFromFnode.length == 0) {

			throw new Exception("Could not find any reaches with this fnode '"
					+ fnode + "' for reach row " + row);

		} else if (allReachesAtFromFnode.length == 1) {
			//If only a single reach, the FRAC must be 1.

			return 1d;

		} else {
			//Adjust frac per total

			double fracForReqestedReach = topo.getDouble(row, PredictData.TOPO_FRAC_COL);
			double fracTotal = 0d;

			for (int i = 0; i < allReachesAtFromFnode.length; i++) {
				double thisFrac = topo.getDouble(allReachesAtFromFnode[i], PredictData.TOPO_FRAC_COL);
				fracTotal+= thisFrac;
			}

			if (Math.abs(fracTotal - 1D) > .01) {
				//close enough
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
