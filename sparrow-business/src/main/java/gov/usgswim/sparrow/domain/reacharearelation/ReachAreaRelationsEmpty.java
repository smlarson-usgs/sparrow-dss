package gov.usgswim.sparrow.domain.reacharearelation;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author eeverman
 */
public class ReachAreaRelationsEmpty implements ReachAreaRelations {

	final long reachId;
	final List<AreaRelation> relations;
	
	public ReachAreaRelationsEmpty(long reachId) {
		this.reachId = reachId;
		this.relations = Collections.emptyList();
	}
	
	
	@Override
	public long getReachId() {
		return reachId;
	}

	@Override
	public List<AreaRelation> getRelations() {
		return relations;
	}
	
}
