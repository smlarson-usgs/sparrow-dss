package gov.usgswim.sparrow.action;

import java.util.ArrayList;
import java.util.List;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ConflictingReachGroup;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaType;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.domain.ReachElement;
import gov.usgswim.sparrow.domain.ReachGroup;

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
		
		
		for(ReachGroup existingGroup : this.groups.getReachGroups()) {
			//check explicit reaches
			for(Long existingReachId : existingGroup.getExplicitReachIds()){
				for(long newReachid : newSetReaches) {
					if(existingReachId.equals(newReachid)) {
						groupsFound.add(new ConflictingReachGroup("individual", existingGroup.getName(), "Explicitly added"));
						break;
					}
				}
			}
			
			//check each logical sets reaches for overlap
			List<LogicalSet> existingLogicalSet = existingGroup.getLogicalSets();
			for(int i = 0; i < existingLogicalSet.size(); i++){
				if(isHuc(existingLogicalSet.get(i).getCriteria().get(0).getCriteriaType()) && isHuc(newType)) { //HUC vs HUC case
					if(doHucNumbersOverlap(existingLogicalSet.get(i))){
						Criteria old = existingLogicalSet.get(i).getCriteria().get(0);
						groupsFound.add(new ConflictingReachGroup(old.getCriteriaType().toString(), existingGroup.getName(), old.getValue()));
					}
				} else { //All other cases, should cover HUC vs Upstream and Upstream vs Upstream
					long[] reachIds = existingGroup.getLogicalReachIDs(i);
					groupsFound.addAll(checkAllReachesInLogicalSet(newSetReaches, reachIds, existingLogicalSet.get(i), existingGroup.getName()));
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
	
	private List<ConflictingReachGroup> checkAllReachesInLogicalSet(long[] newReachIds, long[] reachIdsInLogicalSet, LogicalSet ls, String groupName) throws Exception {
		ArrayList<ConflictingReachGroup> groupsFound = new ArrayList<ConflictingReachGroup>();
		if(reachIdsInLogicalSet != null && newReachIds != null) {
			for(int i = 0; i < newReachIds.length; i++){
				for(int j = 0; j < reachIdsInLogicalSet.length; j++) {
					if(newReachIds[i] == reachIdsInLogicalSet[j]){ 
						Criteria old = ls.getCriteria().get(0);
						groupsFound.add(new ConflictingReachGroup(old.getCriteriaType().toString(), groupName, old.getValue()));
						return groupsFound;
					}
				}
			}
		}
		return groupsFound;
	}
	
	private long[] loadReaches(Criteria criteria) throws Exception {
		ReachesByCriteria getNewReaches = new ReachesByCriteria();
		getNewReaches.setCriteria(criteria);
		
		return getNewReaches.run();
	}
}