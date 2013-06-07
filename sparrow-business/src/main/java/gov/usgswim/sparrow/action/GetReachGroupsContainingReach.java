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
		
		if(groups.getIndividualGroup() != null) {
			if(groups.getIndividualGroup().contains(this.reachId)) {
				//We are working w/ system IDs, not client IDs, so don't send the ID
				//to the client.  We are only working w/ one reach, so there we don't
				//need to tell the caller what the client IDs.
				groupsFound.add(new ConflictingReachGroup("individual", "Individual", "Explicitly added"));
			}
		}
		
		//check explicit reaches separate from the logical sets so that the user
		//can more easily recognize where the conflict is happening.
		for(ReachGroup g : groups.getReachGroups()){
			for(Long r : g.getExplicitReachIds()){
				if(r.equals(this.reachId)) {
					groupsFound.add(new ConflictingReachGroup("individual", g.getName(), "Explicitly added"));
					break;
				}
			}
			
			List<LogicalSet> ls = g.getLogicalSets();
			for(int i = 0; i < ls.size(); i++){
				long[] reachIds = g.getLogicalReachIDs(i);
				if(reachIds != null) {
					for(long oneReachId : reachIds) {
						if(this.reachId.equals(oneReachId)){ 
							Criteria criteria = ls.get(i).getCriteria().get(0);
							groupsFound.add(new ConflictingReachGroup(criteria.getCriteriaType().toString(), g.getName(), criteria.getValue()));
						}
					}
				}
			}
		}
		
		return groupsFound;
	}
}