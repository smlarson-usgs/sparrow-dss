package gov.usgswim.sparrow.action;

import java.util.ArrayList;
import java.util.List;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ConflictingReachGroup;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaType;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.domain.ReachGroup;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * Action will take a LogicalSet object and an AdjustmentGroups object and report any
 * groups/logical sets from the AdjustmentGroups object that the reaches in the given 
 * LogicalSet exist in.
 */
public class GetReachGroupsOverlappingLogicalSet extends Action<List<ConflictingReachGroup>> {
	LogicalSet newSet;
	AdjustmentGroups groups;
	
    public GetReachGroupsOverlappingLogicalSet(LogicalSet newSet, AdjustmentGroups groups) throws Exception {
    	this.newSet = newSet;
    	this.groups = groups;
    }
    
	public List<ConflictingReachGroup> doAction() throws Exception {
		ArrayList<ConflictingReachGroup> groupsFound = new ArrayList<ConflictingReachGroup>();
		
		long[] newSetReaches = loadReaches(this.newSet.getCriteria().get(0));
		
		CriteriaType newType = this.newSet.getCriteria().get(0).getCriteriaType();
		
		//Check the Individual Group
		if (containsAny(groups.getIndividualGroup().getExplicitReachIds(), newSetReaches)) {
			groupsFound.add(new ConflictingReachGroup("Individual", "Individual", "Explicitly added"));
		}
		
		//Check all the individual reach groups
		for(ReachGroup existingGroup : this.groups.getReachGroups()) {
			if (containsAny(existingGroup.getExplicitReachIds(), newSetReaches)) {
				groupsFound.add(new ConflictingReachGroup("Individual", existingGroup.getName(), "Explicitly added"));
			}
			
			//check each logical sets reaches for overlap
			List<LogicalSet> existingLogicalSet = existingGroup.getLogicalSets();
			for(LogicalSet existingSet : existingGroup.getLogicalSets()) {
				Criteria existingCriteria = existingSet.getCriteria().get(0);
				
				if(isHuc(existingCriteria.getCriteriaType()) && isHuc(newType)) { //HUC vs HUC case
					if(doHucNumbersOverlap(existingSet)){
						groupsFound.add(new ConflictingReachGroup(existingCriteria.getCriteriaType().toString(), existingGroup.getName(), existingCriteria.getValue()));
					}
				} else { //All other cases, should cover HUC vs Upstream and Upstream vs Upstream
					long[] reachIds = loadReaches(existingCriteria);
					if (containsAny(reachIds, newSetReaches)) {
						groupsFound.add(new ConflictingReachGroup(existingCriteria.getCriteriaType().toString(), existingGroup.getName(), existingCriteria.getValue()));
					}
				}
			}
		}
		
		return groupsFound;
	}
	
	private boolean isHuc(CriteriaType type) {
		switch(type) {
			case HUC2:
			case HUC4:
			case HUC6:
			case HUC8: return true;
			default: return false;
		}
	}
	
	private boolean doHucNumbersOverlap(LogicalSet ls) throws Exception {
		if(this.newSet.getCriteria().get(0).getValue().indexOf(ls.getCriteria().get(0).getValue()) >= 0 ||
				ls.getCriteria().get(0).getValue().indexOf(this.newSet.getCriteria().get(0).getValue()) >= 0	
		)
			return true;
		
		return false;
	}
		
	private long[] loadReaches(Criteria criteria) throws Exception {
		return SharedApplication.getInstance().getReachesByCriteria(criteria);
	}
	
	/**
	 * Returns true if any of the values in the list or array are the same value.
	 * @param list
	 * @param array
	 * @return 
	 */
	private boolean containsAny(List<Long> list, long[] array) {
		for (long v : array) {
			if (list.contains(new Long(v))) return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if any of the values in the two arrays are the same.
	 * 
	 * @param array1
	 * @param array2
	 * @return 
	 */
	private boolean containsAny(long[] array1, long[] array2) {
		for (long v1 : array1) {
			for (long v2 : array2) {
				if (v1 == v2) return true;
			}
		}
		return false;
	}
}