package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadReachByPoint;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

import org.apache.log4j.Logger;



/**
 * This factory finds a Reach based on a ModelPoint.
 *
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 *
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 *
 * @author eeverman
 *
 */
public class ReachByPointFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachByPointFactory.class); //logging for this class

	@Override
	public ReachInfo createEntry(Object request) throws Exception {

		ModelPoint req = (ModelPoint) request;

		return new LoadReachByPoint(req).run();

	}

}
