package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadPredefinedSessions;
import gov.usgswim.sparrow.domain.IPredefinedSession;

import java.util.List;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Wraps the LoadPredefinedSessions Action for cache usage
 * 
 * @author eeverman
 *
 */
public class PredefinedSessionFactory implements CacheEntryFactory {

	@Override
	public List<IPredefinedSession> createEntry(Object modelIdOrUniqueCode) throws Exception {
		
		LoadPredefinedSessions action = null;
		
		if (modelIdOrUniqueCode instanceof Long) {
			action = new LoadPredefinedSessions((Long) modelIdOrUniqueCode);
		} else {
			action = new LoadPredefinedSessions(modelIdOrUniqueCode.toString());
		}
		
		List<IPredefinedSession> sessionList = action.run();

		sessionList = action.run();
		
		return sessionList;
	}

}
