package gov.usgswim.sparrow.domain.reacharearelation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eeverman
 */
public class ReachAreaRelationsSimple implements ReachAreaRelations {

	final long reachId;
	final List<AreaRelation> relations;
	
	public ReachAreaRelationsSimple(long reachId, List<AreaRelation> relations) {
		this.reachId = reachId;
		this.relations = Collections.unmodifiableList(relations);
	}
	
	public ReachAreaRelationsSimple(long reachId, AreaRelation relation) {
		this.reachId = reachId;
		
		ArrayList<AreaRelation> tmp  = new ArrayList<AreaRelation>(1);
		tmp.add(relation);
		
		this.relations = Collections.unmodifiableList(tmp);
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
