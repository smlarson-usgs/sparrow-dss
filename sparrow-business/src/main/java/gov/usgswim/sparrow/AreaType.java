
package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.action.Action.getDataSeriesProperty;
import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;

/**
 *
 * @author cschroed
 */
public enum AreaType {
	TOTAL_CONTRIBUTING(DataSeriesType.total_contributing_area),
	TOTAL_UPSTREAM(DataSeriesType.total_upstream_area),
	INCREMENTAL(DataSeriesType.incremental_area);

	private final String name;	//here for convenience
	private final String description;//here for convenience
	private final DataSeriesType dataSeriesType;//permits accessing other advanced information about the type

	AreaType(DataSeriesType dataSeriesType){
		this.dataSeriesType = dataSeriesType;
		this.name = getDataSeriesProperty(dataSeriesType, false);
		this.description = getDataSeriesProperty(dataSeriesType, true);
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

	/**
	 * @return the dataSeriesType
	 */
	public DataSeriesType getDataSeriesType() {
		return dataSeriesType;
	}

}
