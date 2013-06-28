package gov.usgswim.sparrow.domain.reacharearelation;

/**
 *
 * @author eeverman
 */
public class AreaRelationImpl implements AreaRelation {

	private final long areaId;
	private final double fraction;
					
					
	public AreaRelationImpl(long areaId, double fraction) {
		this.areaId = areaId;
		this.fraction = fraction;
	}
	

	@Override
	public long getAreaId() {
		return areaId;
	}

	@Override
	public double getFraction() {
		return fraction;
	}
	
}
