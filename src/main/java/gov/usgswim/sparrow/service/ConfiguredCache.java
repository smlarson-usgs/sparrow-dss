package gov.usgswim.sparrow.service;

import java.util.List;

import gov.usgswim.sparrow.cachefactory.*;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Enum for all caches that also adds the factories during init.
 * 
 * @author eeverman
 */
public enum ConfiguredCache{
	// distributed caches
	PredictContext(false),
	AdjustmentGroups(false),
	Analyses(false),
	TerminalReaches(false),
	AreaOfInterest(false),
	PredefinedSessions(false, new PredefinedSessionFactory()),
	
	// non-distributed caches
	PredictData(false, new PredictDataFactory()),
	DeliveryFractionHash(false, new DeliveryFractionHashFactory()),
	DeliveryFraction(false, new DeliveryFractionColumnDataFactory()),
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
	
	CatchmentAreas(false, new UnitAreaFactory()),
	HUCData(false, new HUCDataFactory()),
	HUC(false, new HUCFactory()),
	ReachWatershed(false, new ReachWatershedFactory()),
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
	
	/**
	 * Fetch an object by its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key
	 * @param quiet
	 * @return
	 */
	public Object get(Object key, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		Element e  = (quiet)? c.getQuiet(key): c.get(key);
		return (e != null)? e.getObjectValue(): null;
	}
	
	/**
	 * Fetch an object by its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key
	 * @param quiet
	 * @return
	 */
	public Object get(Long key, boolean quiet) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		Element e  = (quiet)? c.getQuiet(key): c.get(key);
		return (e != null)? e.getObjectValue(): null;
	}
	
	/**
	 * Fetch an object by its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key An Integer key is converted to a long.
	 * @param quiet
	 * @return
	 */
	public Object get(Integer key, boolean quiet) {
		return get(new Long(key), quiet);
	}
	
	/**
	 * Returns all non-expired keys in the cache.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getKeysWithExpiryCheck() {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		return c.getKeysWithExpiryCheck();
	}
	
	/**
	 * Put an object and its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key
	 * @param quiet
	 * @return
	 */
	public void put(Long key, Object value) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		Element e  = new Element(key, value);
		c.put(e);
	}
	
	/**
	 * Put an object and its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key A key that is converted to a Long
	 * @param quiet
	 * @return
	 */
	public void put(Integer key, Object value) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		Element e  = new Element(new Long(key), value);
		c.put(e);
	}
	
	/**
	 * Forces the last access time and other stats to be updated on an entry.
	 * 
	 * This is done by first attempting to get the item.  If its not found, it
	 * is put into the cache.
	 * 
	 * @param key
	 * @param value
	 */
	public void touch(Long key, Object value) {
		Object o = get(key, false);
		if (o == null) {
			put(key, value);
		}
	}
	
	/**
	 * Forces the last access time and other stats to be updated on an entry.
	 * 
	 * This is done by first attempting to get the item.  If its not found, it
	 * is put into the cache.
	 * 
	 * @param key The key is converted to a Long.
	 * @param value
	 */
	public void touch(Integer key, Object value) {
		Object o = get(new Long(key), false);
		if (o == null) {
			put(new Long(key), value);
		}
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