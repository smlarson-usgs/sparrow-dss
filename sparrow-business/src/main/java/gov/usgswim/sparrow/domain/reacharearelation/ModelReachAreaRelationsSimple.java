package gov.usgswim.sparrow.domain.reacharearelation;

/**
 *
 * @author eeverman
 */
public class ModelReachAreaRelationsSimple implements ModelReachAreaRelations {

	private final ReachAreaRelations[] relations;
	
	public ModelReachAreaRelationsSimple(ReachAreaRelations[] relations) {
		this.relations = relations;
	}
	
	@Override
	public ReachAreaRelations getRelationsForReachRow(int reachRow) {
		return relations[reachRow];
	}

	@Override
	public int getRowCount() {
		return relations.length;
	}
	
}
