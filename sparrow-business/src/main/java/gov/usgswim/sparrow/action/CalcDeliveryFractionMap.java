package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.TOPO_IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.ReachRowValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Calculates the delivery fractions for reaches upstream of a list of targets.
 * A list of all upstream reaches are returned, some of which may contain
 * a zero value for delivery.
 * 
 * The returned HashMap<Integer, DeliveryReach> is structured as:
 * key (Integer) is the row number of the reach
 * value (DeliveryReach) contains minimal info on the reach, including the calculated
 * delivery fraction.
 * 
 * @author eeverman
 *
 */
public class CalcDeliveryFractionMap extends Action<ReachRowValueMap> {

	protected PredictData predictData;
	protected Set<Long> targetReachIds;
	protected String msg = null;
	
	/**
	 * Sets the predictData used to calc the delivery fraction.
	 * @param predictData
	 */
	public void setPredictData(PredictData predictData) {
		this.predictData = predictData;
	}

	/**
	 * The targets to calc the delivery fraction w/ respect to.
	 * @param targetReachIds
	 */
	public void setTargetReachIds(Set<Long> targetReachIds) {
		this.targetReachIds = targetReachIds;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public ReachRowValueMap doAction() throws Exception {
		//Hash containing rows as keys and DeliveryReaches as values.
		HashMap<Integer, DeliveryReach> deliveries = calcDeliveryHash(predictData, targetReachIds);
		ReachRowValueMap map = ReachRowValueMap.build(deliveries);
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
			PredictData predictData, Set<Long> targetReachIds) throws Exception {
		
		DataTable topo = predictData.getTopo();
		
		Iterator<Long> targetIdIterator = targetReachIds.iterator();
		
		//que that pops the most downstream target first
		PriorityQueue<DeliveryReach> targetProcessingQue = new PriorityQueue<DeliveryReach>();
		
		//uses a row number as the key
		HashMap<Integer, DeliveryReach> mergedDeliveryFractions = new HashMap<Integer, DeliveryReach>();
		
		//Build processing que to ensure that downstream targets are processed first
		while (targetIdIterator.hasNext()) {
			Long id = targetIdIterator.next();
			int row = predictData.getRowForReachID(id);
			DeliveryReach current = new DeliveryReach(
					row, 1d, topo.getInt(row, PredictData.TOPO_HYDSEQ_COL)
			);
			targetProcessingQue.add(current);
		}
		
		while (! targetProcessingQue.isEmpty()) {
			DeliveryReach currentTarget = targetProcessingQue.poll();
			
			if (mergedDeliveryFractions.get(currentTarget.getRow()) == null) {
				List<DeliveryReach> currentCalcs =
					calcDeliveryForSingleTarget(predictData, currentTarget);
				
				//merge results of the reaches upstream of this target w/ previous
				for (DeliveryReach dr : currentCalcs) {
					DeliveryReach existing = mergedDeliveryFractions.get(dr.getRow());
					if (existing == null) {
						mergedDeliveryFractions.put(dr.getRow(), dr);
					} else {
						existing.addDelivery(dr.getDelivery());
					}
				}
			} else {
				log.info("A target reach (row=" + currentTarget.getRow()
					+ ") was excluded b/c it is upstream of another target reach.");
			}
		}
		
		msg = "Delivery Details:  Model size:  " + topo.getRowCount() +
				" rows, " + targetReachIds.size() + " targets.  " +
				"Found " + mergedDeliveryFractions.size() + " upstream reaches.";
		
		return mergedDeliveryFractions;
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
			PredictData predictData, DeliveryReach targetReach) throws Exception {
		
		DataTable topo = predictData.getTopo();
		
		//Queue of upstream reaches as they are found, in rev hydseq order
		Queue<DeliveryReach> upstreamReaches = new PriorityQueue<DeliveryReach>(4);
		
		//All reaches for which we have thus far calculated a delivery fraction
		List<DeliveryReach> deliveryReaches = new ArrayList<DeliveryReach>();
		
		//Add the current reaches' upstream reaches to the queue
		addUpstreamReachesToQueue(upstreamReaches, predictData, targetReach);
		deliveryReaches.add(targetReach);	//add to output - should be delivery of 1
		
		//Continually loop through upstream reaches
		while (! upstreamReaches.isEmpty()) {
			DeliveryReach current = upstreamReaches.poll();
			
			//Multiple downstream reaches are unlikely, but possible.
			//They would have to come from the same target, split, and rejoin.
			for (DeliveryReach downstream : current.getDownstreamReaches()) {
				int downstreamRow = downstream.getRow();
				
				//complete calculated delivery frac of one of the reaches
				//downstream of the current reach.
				//In the case of a split there would be multiple.
				double downstrmCalcedDeliveryFrac = downstream.getDelivery();
				
				//Total delivery (that is, the inverse of total decay) for  the
				//downstream reach.  NOTE that in our data, total delivery
				//is already multiplied by the fraction in the cases of a split.
				double downstrmTotalDelivery = predictData.getDelivery().
					getDouble(downstreamRow, UPSTREAM_DECAY_COL);
				
				//Zero if the reach has no transport to the downstream reach.
				double ifTran = topo.getDouble(current.getRow(), TOPO_IFTRAN_COL);
				
				double addedDelivery =
					downstrmCalcedDeliveryFrac * downstrmTotalDelivery * ifTran;
				
				current.addDelivery(addedDelivery);
			}
			
			//Add the current reaches' upstream reaches to the queue
			addUpstreamReachesToQueue(upstreamReaches, predictData, current);
			
			deliveryReaches.add(current);	//Add current to list of completed
		}
		
		return deliveryReaches;
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
			PredictData predictData, DeliveryReach current) {
		
		DataTable topo = predictData.getTopo();
		boolean isBaseReachAShoreReach = (1 == topo.getInt(current.getRow(), PredictData.TOPO_SHORE_REACH_COL));

		if (isBaseReachAShoreReach) {
			//Don't do any further processing for reaches 'upstream' of a shore reach.
			//A shore reach just goes along the edge of a lake or ocean, so its not really
			//part of the network.
			return;
		} else {
		
			long fnode = topo.getLong(current.getRow(), PredictData.TOPO_FNODE_COL);

			//The index requires that an Integer be used.
			int[] upstream = topo.findAll(PredictData.TOPO_TNODE_COL, new Integer((int)fnode));

			for (int rowNum : upstream) {
				
				//Don't add this reach if it is a shore reach
				boolean isReachAShoreReach = (1 == topo.getInt(rowNum, PredictData.TOPO_SHORE_REACH_COL));
				
				if (! isReachAShoreReach) {

					DeliveryReach toBeAdded = new DeliveryReach(
						rowNum, 0d, topo.getInt(rowNum, PredictData.TOPO_HYDSEQ_COL), current
					);

					if (! upstreamReaches.contains(toBeAdded)) {
						upstreamReaches.add(toBeAdded);
					} else {
						//This reach already exists in our upstream list.
						//Find it & add the current reach as an added downstream reach.
						Iterator<DeliveryReach> upstreamIt = upstreamReaches.iterator();

						while (upstreamIt.hasNext()) {
							DeliveryReach u = upstreamIt.next();

							if (u.equals(toBeAdded)) {	//.equals based only on row
								u.addDownstreamReach(current);
								break;
							}
						}

					}
				}
			}
		}
	}
}
