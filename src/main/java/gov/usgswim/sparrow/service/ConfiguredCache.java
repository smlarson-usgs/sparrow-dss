/**
 * 
 */
package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.cachefactory.*;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public enum ConfiguredCache{
	// distributed caches
	PredictContext(true),
	AdjustmentGroups(true),
	Analyses(true),
	TerminalReaches(true),
	AreaOfInterest(true),
	PredefinedSessions(true, new PredefinedSessionFactory()),
	
	// non-distributed caches
	PredictData(false, new PredictDataFactory()),
	DeliveryFraction(false, new DeliveryFractionFactory()),
	NSDataSet(false, new NSDataSetFactory()),
	ComparisonResult(false, new ComparisonResultFactory()),
	AnalysisResult(false, new AnalysisResultFactory()),
	
	StandardErrorEstimateData(false, new UncertaintyDataFactory()),
	AdjustedSource(false, new AdjustedSourceFactory()),
	PredictResult(false, new PredictResultFactory()),
	IdentifyReachByPoint(false, new ReachByPointFactory()),
	IdentifyReachByID(false, new ReachByIDFactory()),
	
	LoadReachAttributes(false, new LoadReachAttributesFactory()),
	ReachesByCriteria(false, new ReachesByCriteriaFactory()),
	DataBinning(false, new BinningFactory()),
	AggregateIdLookup(false, new AggregateIdLookupKludgeFactory()),
	LoadModelMetadata(false, new ModelMetadataFactory()),
	
	CatchmentAreas(false, new CatchmentAreaFactory()),
	StreamFlow(false, new StreamFlowFactory())
	;
	
	public final boolean isDistributed;
	public final CacheEntryFactory factory;

	private ConfiguredCache(boolean isDistributed, CacheEntryFactory... factory) {
		this.isDistributed = isDistributed;
		if (factory != null && factory.length > 0) {
			this.factory = factory[0];
		} else {
			this.factory = null;
		}
	}
	
	public Object get(Object key, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		Element e  = (quiet)? c.getQuiet(key): c.get(key);
		return (e != null)? e.getObjectValue(): null;
	}
	
	/**
	 * Removes an item from the cache using the cache key.
	 * 
	 * This method should notify all peer caches to remove it as well.
	 * @param key
	 * @return True if removed, false if it was not in the cache.
	 */
	public boolean remove(Object key) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		return c.remove(key);
	}
}