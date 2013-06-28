
package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.action.Action.getDataSeriesProperty;
import gov.usgswim.sparrow.domain.DataSeriesType;

/**
 *
 * @author cschroed
 */
public enum AreaType {
	TOTAL_CONTRIBUTING(getDataSeriesProperty(DataSeriesType.total_contributing_area, false), getDataSeriesProperty(DataSeriesType.total_contributing_area, true)),
	TOTAL_UPSTREAM(getDataSeriesProperty(DataSeriesType.total_upstream_area, false), getDataSeriesProperty(DataSeriesType.total_upstream_area, true)),
	INCREMENTAL(getDataSeriesProperty(DataSeriesType.incremental_area, false), getDataSeriesProperty(DataSeriesType.incremental_area, true));

	private final String name;
	private final String description;
	AreaType(String name, String description){
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
}
