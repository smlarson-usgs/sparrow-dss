package gov.usgswim.sparrow.action;

import java.util.ArrayList;
import java.util.List;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaRelationType;
import gov.usgswim.sparrow.domain.CriteriaType;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.domain.ReachElement;
import gov.usgswim.sparrow.domain.ReachGroup;

/**
 * Action will take a reachId and an AdjustmentGroups object and report any
 * groups/logical sets from the AdjustmentGroups object that the reachId exists in.
 * 
 * TODO as a hack, right now the action uses criteria to specify these detected groups.
 * Consider a dedicated object for this.
 */
public class GetReachGroupsContainingReach extends Action<List<Criteria>> {
	Long reachId;
	AdjustmentGroups groups;
	
    public GetReachGroupsContainingReach(Long reachId, AdjustmentGroups groups) throws Exception {
    	this.reachId = reachId;
    	this.groups = groups;
    }
    
	public List<Criteria> doAction() throws Exception {
		ArrayList<Criteria> groupsFound = new ArrayList<Criteria>();
		
		if(this.groups.getDefaultGroup() != null) {
			boolean reachFound = false;
			for(ReachElement r : this.groups.getDefaultGroup().getExplicitReaches()){
				if(r.getId().equals(this.reachId)) {
					reachFound = true;
				}
			}
			if(reachFound)
				groupsFound.add(new Criteria(groups.getModelID(), CriteriaType.UNKNOWN, CriteriaRelationType.UNKNOWN, "default"));
		}
		
		if(this.groups.getIndividualGroup() != null) {
			boolean reachFound = false;
			for(ReachElement r : this.groups.getIndividualGroup().getExplicitReaches()){
				if(r.getId().equals(this.reachId)) {
					reachFound = true;
				}
			}
			if(reachFound) {
				groupsFound.add(new Criteria(groups.getModelID(), CriteriaType.UNKNOWN, CriteriaRelationType.UNKNOWN, "individual"));
			}
		}
		
		for(ReachGroup g : this.groups.getReachGroups()){
			boolean reachFound = false;
			for(ReachElement r : g.getExplicitReaches()){
				if(r.getId().equals(this.reachId)) {
					reachFound = true;
				}
			}
			if(reachFound) {
				groupsFound.add(new Criteria(groups.getModelID(), CriteriaType.UNKNOWN, CriteriaRelationType.UNKNOWN, g.getName()));
			}
			
			List<LogicalSet> ls = g.getLogicalSets();
			for(int i = 0; i < ls.size(); i++){
				long[] reachIds = g.getLogicalReachIDs(i);
				if(reachIds != null) {
					for(int j = 0; j < reachIds.length; j++) {
						if(this.reachId == reachIds[j]){ 
							Criteria old = ls.get(i).getCriteria().get(0);
							groupsFound.add(new Criteria(old.getModelID(), old.getCriteriaType(), old.getRelation(), g.getName()));
						}
					}
				}
			}
		}
		
		return groupsFound;
	}
}