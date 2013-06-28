package gov.usgswim.sparrow.domain.reacharearelation;

import java.io.Serializable;

/**
 * The relation between the parent object and an area with a fractional origination.
 * 
 * Nominally this is a reach to a political or hydrological unit, where the fraction
 * is the portion of the catchment area in that area.
 * 
 * @author eeverman
 */
public interface AreaRelation extends Serializable {



	/**
	 * The ID of the area.
	 * @return 
	 */
	long getAreaId();

	/**
	 * The fraction of the parent object contain in the related area.
	 * 
	 * @return A faction between zero and one.
	 */
	double getFraction();
}
