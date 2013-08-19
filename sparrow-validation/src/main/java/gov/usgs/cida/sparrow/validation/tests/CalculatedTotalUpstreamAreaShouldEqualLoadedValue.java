package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.Comparator;
import gov.usgswim.sparrow.AreaType;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 *
 * @author cschroed
 */
public class CalculatedTotalUpstreamAreaShouldEqualLoadedValue extends CalculatedAreaShouldEqualLoadedValue {

	public CalculatedTotalUpstreamAreaShouldEqualLoadedValue(
			Comparator comparator,
			Comparator shoreReachComparator,
			boolean failedTestIsOnlyAWarning) {
		super(comparator, shoreReachComparator, failedTestIsOnlyAWarning, AreaType.TOTAL_UPSTREAM);
	}
}