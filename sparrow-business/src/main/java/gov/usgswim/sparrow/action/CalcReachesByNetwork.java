package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaRelationType;
import gov.usgswim.sparrow.domain.CriteriaType;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
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
	
	//User set state
	protected Criteria criteria;
	
	
	//Action loaded state
	PredictData modelPredictData;
	
	
	public CalcReachesByNetwork(Criteria criteria) {
		this.criteria = criteria;
	}

	@Override
	protected void validate() {
		//Action validation checking
		if (! CriteriaType.REACH.equals( criteria.getCriteriaType() )) {
			String msg = "This action is only valid for the CriteriaType '"
				+ CriteriaType.REACH + "', found '" + criteria.getCriteriaType() + "'";
			
			this.addValidationError(msg);
		}
		
		if (! CriteriaRelationType.UPSTREAM.equals( criteria.getRelation() )) {
			String msg = "Only  '" + CriteriaRelationType.UPSTREAM
				+ "' is supported.  Found '" + criteria.getRelation() + "'";
			
			this.addValidationError(msg);
		}
	}
	
	
	@Override
	protected void initFields() throws Exception {
		//PredictData is needed b/c it contains the mapping from row number
		//to reach id.  The DelFracMap is keyed by row number, not reach ID.
		modelPredictData = SharedApplication.getInstance().getPredictData(criteria.getModelID());
	}
	
	

	
	@Override
	public long[] doAction() throws Exception {
		
		//Need a TerminalReach to calculate the delivery map, which we can then
		//convert to just an array list.
		TerminalReaches tr = null;
		
		//The return value
		long[] rowIds = null;
		
		
		//Create a TerminalReach from the passed reach ID
		String reachId = criteria.getValue();
		List<String> targetList = new ArrayList<String>(1);
		targetList.add(reachId);
		tr = new TerminalReaches(criteria.getModelID(), targetList);

		
		//The only part of this data we care about is the keys, which are row numbers
		ReachRowValueMap delFracMap =
				SharedApplication.getInstance().getDeliveryFractionMap(tr);
		Iterator<Integer> reachRows = delFracMap.keySet().iterator();
		
		rowIds = new long[delFracMap.size()];
		int rowIdx = 0;
		
		while (reachRows.hasNext()) {
			Integer rowNum = reachRows.next();
			rowIds[rowIdx] = modelPredictData.getIdForRow(rowNum);
			rowIdx++;
		}
		
		Arrays.sort(rowIds);
		
		return rowIds;
	}

}
