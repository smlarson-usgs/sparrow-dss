package gov.usgswim.sparrow.action;

import java.util.ArrayList;
import java.util.List;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ConflictingReachGroup;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.domain.ReachGroup;
import gov.usgswim.sparrow.service.SharedApplication;

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
		
		if(groups.getIndividualGroup() != null) {
			if(groups.getIndividualGroup().contains(this.reachId)) {
				groupsFound.add(new ConflictingReachGroup("Individual", "Individual", "Explicitly added"));
			}
		}
		
		//check explicit reaches separate from the logical sets so that the user
		//can more easily recognize where the conflict is happening.
		for(ReachGroup g : groups.getReachGroups()){
			if (g.getExplicitReachIds().contains(this.reachId)) {
				groupsFound.add(new ConflictingReachGroup("Individual", g.getName(), "Explicitly added"));
			}
			
			for(LogicalSet ls : g.getLogicalSets()){
				Criteria criteria = ls.getCriteria().get(0);
				long[] reachIds = loadReaches(criteria);
				
				if(reachIds != null) {
					for(long oneReachId : reachIds) {
						if(this.reachId.equals(oneReachId)){ 
							groupsFound.add(new ConflictingReachGroup(criteria.getCriteriaType().toString(), g.getName(), criteria.getValue()));
						}
					}
				}
			}
		}
		
		return groupsFound;
	}
	
	private long[] loadReaches(Criteria criteria) throws Exception {
		return SharedApplication.getInstance().getReachesByCriteria(criteria);
	}	
}