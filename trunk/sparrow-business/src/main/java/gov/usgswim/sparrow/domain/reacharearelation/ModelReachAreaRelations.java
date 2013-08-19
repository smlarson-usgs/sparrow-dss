package gov.usgswim.sparrow.domain.reacharearelation;

import java.io.Serializable;
import java.util.List;

/**
 * A list of ReachAreaRelations arranged by reach row.
 * Using the standard ordering for a model's reaches (hydrologic sequence, then
 * reach id sorted), the getRelationsForReachRow will return the area relations
 * for that reach.
 * 
 * @author eeverman
 */
public interface ModelReachAreaRelations extends Serializable {
	
	/**
	 * Returns the ReachAreaRelations for the specified reach.
	 * 
	 * The reach is specified by its ROW NUMBER, NOT its reach id (to save space).
	 * 
	 * @param reachRow The reach's row, per standard ordering of hydro sequest then reach id sorting.
	 * @return 
	 */
	ReachAreaRelations getRelationsForReachRow(int reachRow);
	
	/**
	 * The number of reach rows, which must match the number of reach rows in the
	 * associated model.
	 * 
	 * @return 
	 */
	int getRowCount();
	
}
