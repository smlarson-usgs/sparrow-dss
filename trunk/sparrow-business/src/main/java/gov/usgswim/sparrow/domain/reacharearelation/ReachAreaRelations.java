package gov.usgswim.sparrow.domain.reacharearelation;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author eeverman
 */
public interface ReachAreaRelations extends Serializable {
	
	/**
	 * Return the reach ID for this relation.
	 * @return 
	 */
	long getReachId();
	
	/**
	 * Return an immutable List of all area relationships for this reach, or an
	 * empty List if there are no relationships.
	 * 
	 * @return This method never returns null.
	 */
	List<AreaRelation> getRelations();
	
}
