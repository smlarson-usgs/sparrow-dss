package gov.usgswim.sparrow.domain.reacharearelation;

/**
 *
 * @author eeverman
 */
public class ModelReachAreaRelationsBuilder implements ModelReachAreaRelations {

	private ReachAreaRelations[] relations;
	
	
	//relations; = new ArrayList<ReachAreaRelations>;
	
	public ModelReachAreaRelationsBuilder(int rowCount) {
		relations = new ReachAreaRelations[rowCount];
	}
	
	public void set(int reachRow, ReachAreaRelations relation) {
		relations[reachRow] = relation;
	}
	
	
	@Override
	public ReachAreaRelations getRelationsForReachRow(int reachRow) {
		return relations[reachRow];
	}
	
	@Override
	public int getRowCount() {
		return relations.length;
	}
	
	public ModelReachAreaRelations toImmutable() {
		ModelReachAreaRelationsSimple imm = new ModelReachAreaRelationsSimple(relations);
		relations = null;
		return imm;
	}
	
}
