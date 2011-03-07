package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Criteria;

/**
 * Finds a list of reaches based on criteria for use in an adjustment set.
 * 
 * This action simply delegates to other other actions, which specialize in
 * different criteria types.
 * 
 * @author eeverman
 *
 */
public class ReachesByCriteria extends Action<long[]> {
	
	protected Criteria criteria;
	
	
	@Override
	public long[] doAction() throws Exception {
		
		// Branching code based on criteria type
		if (criteria.getCriteriaType().isHucCriteria()) {
			LoadReachesInHuc action = new LoadReachesInHuc();
			action.setCriteria(criteria);
			long[] results = action.run();
			return results;
		} else if (criteria.getCriteriaType().isNetworkCriteria()) {
			CalcReachesByNetwork action = new CalcReachesByNetwork();
			action.setCriteria(criteria);
			long[] results = action.run();
			return results;
		} else {
			String msgString = "Unexpected CriteriaType '" + criteria.getCriteriaType() + "'";
			this.setPostMessage(msgString);
			return null;
		}
	}
	
	
	public Criteria getCriteria() {
		return criteria;
	}


	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}
}
