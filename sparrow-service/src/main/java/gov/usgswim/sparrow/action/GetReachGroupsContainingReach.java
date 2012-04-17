package gov.usgswim.sparrow.action;

import java.util.ArrayList;
import java.util.List;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ConflictingReachGroup;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.domain.ReachElement;
import gov.usgswim.sparrow.domain.ReachGroup;

/**
 * Action will take a reachId and an AdjustmentGroups object and report any
 * groups/logical sets from the AdjustmentGroups object that the reachId exists in.
 */
public class GetReachGroupsContainingReach extends Action<List<ConflictingReachGroup>> {
	Long reachId;
	AdjustmentGroups groups;
	
    public GetReachGroupsContainingReach(Long reachId, AdjustmentGroups groups) throws Exception {
    	this.reachId = reachId;
    	this.groups = groups;
    }
    
	public List<ConflictingReachGroup> doAction() throws Exception {
		ArrayList<ConflictingReachGroup> groupsFound = new ArrayList<ConflictingReachGroup>();
		
		if(this.groups.getDefaultGroup() != null) {
			for(ReachElement r : this.groups.getDefaultGroup().getExplicitReaches()){
				boolean reachFound = false;
				if(r.getId().equals(this.reachId)) {
					reachFound = true;
				}
				if(reachFound)
					groupsFound.add(new ConflictingReachGroup("default", "default", String.valueOf(r.getId())));
			}
		}
		
		if(this.groups.getIndividualGroup() != null) {
			for(ReachElement r : this.groups.getIndividualGroup().getExplicitReaches()){
				boolean reachFound = false;
				if(r.getId().equals(this.reachId)) {
					reachFound = true;
				}
				if(reachFound) 
					groupsFound.add(new ConflictingReachGroup("individual", "individual", String.valueOf(r.getId())));
			}
		}
		
		for(ReachGroup g : this.groups.getReachGroups()){
			for(ReachElement r : g.getExplicitReaches()){
				boolean reachFound = false;
				if(r.getId().equals(this.reachId)) {
					reachFound = true;
				}
				if(reachFound) {
					groupsFound.add(new ConflictingReachGroup("individual", g.getName(), String.valueOf(r.getId())));
				}
			}
			
			List<LogicalSet> ls = g.getLogicalSets();
			for(int i = 0; i < ls.size(); i++){
				long[] reachIds = g.getLogicalReachIDs(i);
				if(reachIds != null) {
					for(int j = 0; j < reachIds.length; j++) {
						if(this.reachId == reachIds[j]){ 
							Criteria old = ls.get(i).getCriteria().get(0);
							groupsFound.add(new ConflictingReachGroup(old.getCriteriaType().toString(), g.getName(), old.getValue()));
						}
					}
				}
			}
		}
		
		return groupsFound;
	}
}