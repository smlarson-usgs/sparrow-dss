package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadPredefinedSessions;
import gov.usgswim.sparrow.parser.PredefinedSession;

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
	public List<PredefinedSession> createEntry(Object ignored) throws Exception {
		LoadPredefinedSessions action = new LoadPredefinedSessions();
		List<PredefinedSession> sessionList = action.run();

		sessionList = action.run();
		
		return sessionList;
	}

}
