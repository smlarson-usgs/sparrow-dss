package gov.usgs.cida.sparrow.calculation.calculators.totalContributingAreaCalculator;

import lombok.Data;

/**
 * A Pair to ensure association between a reachId and the reach's calculated area.
 * @author cschroed
 */
@Data public class IdAreaPair {
	private final Long reachId;
	private final Double area;
}