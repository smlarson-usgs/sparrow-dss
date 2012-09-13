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
		for(ReachGroup g : this.groups.getReachGroups()){
			//check explicit reaches
			for(ReachElement r : g.getExplicitReaches()){
				boolean reachFound = false;
				for(int i = 0; i < newSetReaches.length; i++) {
					if(r.getId().equals(newSetReaches[i])) {
						reachFound = true;
					}
				}
				if(reachFound) {
					groupsFound.add(new ConflictingReachGroup("individual", g.getName(), String.valueOf(r.getId())));
				}
			}
			
			//check each logical sets reaches for overlap
			List<LogicalSet> ls = g.getLogicalSets();
			for(int i = 0; i < ls.size(); i++){
				if(isHuc(ls.get(i).getCriteria().get(0).getCriteriaType()) && isHuc(newType)) { //HUC vs HUC case
					if(doHucNumbersOverlap(ls.get(i))){
						Criteria old = ls.get(i).getCriteria().get(0);
						groupsFound.add(new ConflictingReachGroup(old.getCriteriaType().toString(), g.getName(), old.getValue()));
					}
				} else { //All other cases, should cover HUC vs Upstream and Upstream vs Upstream
					long[] reachIds = g.getLogicalReachIDs(i);
					groupsFound.addAll(checkAllReachesInLogicalSet(newSetReaches, reachIds, ls.get(i), g.getName()));
				}
			}
		}
		
		//Check individual and default groups
		if(newSetReaches!=null){
			for(int i = 0; i <newSetReaches.length; i++) { //For every reach in the new set
				if(this.groups.getDefaultGroup() != null) { //check the default group for overlap
					for(ReachElement r : this.groups.getDefaultGroup().getExplicitReaches()){
						boolean reachFound = false;
						
						if(r.getId().equals(newSetReaches[i])) {
							reachFound = true;
						}
						if(reachFound) {
							groupsFound.add(new ConflictingReachGroup("default", "default", String.valueOf(r.getId())));
							break;
						}
					}
				}
			
				if(this.groups.getIndividualGroup() != null) { //check the individual group for overlap
					for(ReachElement r : this.groups.getIndividualGroup().getExplicitReaches()){
						boolean reachFound = false;
						if(r.getId().equals(newSetReaches[i])) {
							reachFound = true;
						}
						if(reachFound) {
							groupsFound.add(new ConflictingReachGroup("individual", "individual", String.valueOf(r.getId())));
							break;
						}
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
	
	private long[] loadReaches(Criteria criteria) {
		ReachesByCriteria getNewReaches = new ReachesByCriteria();
		getNewReaches.setCriteria(criteria);
		
		//Check individual and default groups
		long[] newSetReaches = null;
		try {
			newSetReaches = getNewReaches.run();
		} catch(Exception e) {
			//do nothing
		}
		
		return newSetReaches;
	}
}