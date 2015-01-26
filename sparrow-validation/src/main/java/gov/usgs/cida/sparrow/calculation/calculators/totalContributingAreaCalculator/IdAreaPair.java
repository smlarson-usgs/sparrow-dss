package gov.usgs.cida.sparrow.calculation.calculators.totalContributingAreaCalculator;

/**
 * A Pair to ensure association between a reachId and the reach's calculated area.
 * @author cschroed
 */
public class IdAreaPair {
	private final Long reachId;
	private final Double area;

	public IdAreaPair(Long reachId, Double area) {
		this.reachId = reachId;
		this.area = area;
	}
	
	public Long getReachId() {
		return reachId;
	}

	public Double getArea() {
		return area;
	}
	
}