package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.NSDataSetBuilder;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.PredictionContext;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import oracle.mapviewer.share.ext.NSDataSet;

/**
 * An EHCache CacheEntryFactory to build a NSDataSet for use by MapViewer.
 * 
 * This class is a thin wrapper over the action CalcAnalysis and is only needed
 * to provide compatibility w/ the EHCache framework.  See the action class
 * for implementation details.
 *
 * Caching, blocking, and de-caching are all handled by the caching system.
 *
 * @author eeverman
 */
public class NSDataSetFactory implements CacheEntryFactory {

	@Override
	public NSDataSet createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext) predictContext;

		NSDataSetBuilder action = new NSDataSetBuilder();
		
		//TODO:  Really should make the type of the factory (PredictionContext)
		//and the action (PredictionContext.DataColumn) match.
		DataColumn data = context.getDataColumn();
		action.setData(data);
		NSDataSet result = action.run();
		return result;
	}

}
