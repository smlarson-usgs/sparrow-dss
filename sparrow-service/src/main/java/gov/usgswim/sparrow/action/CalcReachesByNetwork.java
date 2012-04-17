package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaRelationType;
import gov.usgswim.sparrow.domain.CriteriaType;
import gov.usgswim.sparrow.domain.DeliveryFractionMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Calculates a list of reaches upstream of a give reach, which includes the
 * given reach.
 * 
 * The returned list of reach IDs is sorted in ascending order.
 * 
 * @author eeverman
 *
 */
public class CalcReachesByNetwork extends Action<long[]> {
	
	protected Criteria criteria;
	
	public CalcReachesByNetwork() {
		super();
	}
	

	
	@Override
	public long[] doAction() throws Exception {
		
		//Need a TerminalReach to calculate the delivery map, which we can then
		//convert to just an array list.
		TerminalReaches tr = null;

		//PredictData is needed b/c it contains the mapping from row number
		//to reach id.  The DelFracMap is keyed by row number, not reach ID.
		PredictData pd = SharedApplication.getInstance().getPredictData(criteria.getModelID());
		
		//The return value
		long[] rowIds = null;
		
		
		//Action validation checking
		if (! CriteriaType.REACH.equals( criteria.getCriteriaType() )) {
			String msg = "This action is only valid for the CriteriaType '"
				+ CriteriaType.REACH + "', found '" + criteria.getCriteriaType() + "'";
			
			setPostMessage(msg);
			log.error("UNEXPECTED STATE: " + msg);
			return null;
		}
		
		if (! CriteriaRelationType.UPSTREAM.equals( criteria.getRelation() )) {
			String msg = "Only  '" + CriteriaRelationType.UPSTREAM
				+ "' is supported.  Found '" + criteria.getRelation() + "'";
			
			setPostMessage(msg);
			log.error("UNEXPECTED STATE: " + msg);
			return null;
		}
		
		
		try {
			String valStr = criteria.getValue();
			Long reachId = Long.parseLong(valStr);
			List<Long> targetList = new ArrayList<Long>(1);
			targetList.add(reachId);
			
			tr = new TerminalReaches(criteria.getModelID(), targetList);
		} catch (NumberFormatException e) {
			this.setPostMessage("The reach ID: '" + criteria.getValue() + "' is not a valid reach id");
			return null;
		}
		
		//The only part of this data we care about is the keys, which are row numbers
		DeliveryFractionMap delFracMap =
				SharedApplication.getInstance().getDeliveryFractionMap(tr);
		Iterator<Integer> reachRows = delFracMap.keySet().iterator();
		
		rowIds = new long[delFracMap.size()];
		int rowIdx = 0;
		
		while (reachRows.hasNext()) {
			Integer rowNum = reachRows.next();
			rowIds[rowIdx] = pd.getIdForRow(rowNum);
			rowIdx++;
		}
		
		Arrays.sort(rowIds);
		
		return rowIds;
	}
	
	
	public Criteria getCriteria() {
		return criteria;
	}


	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}
}
