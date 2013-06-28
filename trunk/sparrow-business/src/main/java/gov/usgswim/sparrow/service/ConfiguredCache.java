package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.cachefactory.*;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.monitor.CacheInvocation;

import java.util.Collections;
import java.util.List;
import net.sf.ehcache.CacheException;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Enum for all caches that also adds the factories during init.
 * 
 * @author eeverman
 */
public enum ConfiguredCache {
	// Caches that would be distributed, if we were doing distributed caching
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
	LoadModelReachIdentificationAttributes(false, new LoadModelReachIdentificationAttributesFactory()),
	ReachesByCriteria(false, new ReachesByCriteriaFactory()),
	DataBinning(false, new BinningFactory()),
	AggregateIdLookup(false, new AggregateIdLookupKludgeFactory()),
	LoadModelMetadata(false, new ModelMetadataFactory()),
	
	CatchmentAreas(false, new UnitAreaFactory()),
	HUC8Table(false, new HUC8TableFactory()),
	HUC(false, new HUCFactory()),
	ReachWatershed(false, new ReachWatershedFactory()),
	StreamFlow(false, new StreamFlowFactory()),
	
	//FindReachSupportService
	EDACodeColumn(false, new EDACodeColumnFactory()),
	EDANameColumn(false, new EDANameColumnFactory()),
	
	//Delivery Based Reports
	TotalDeliveredLoadSummaryReport(false, new BuildTotalDeliveredLoadSummaryReportFactory()),
	TotalDeliveredLoadByUpstreamRegionReport(false, new BuildTotalDeliveredLoadByUpstreamRegionReportFactory()),
	StatesForModel(false, new StatesForModelFactory()),
	ModelReachAreaRelations(false, new ModelReachAreaRelationsFactory()),
	HucsForModel(false, new LoadHucsForModelFactory()),
	EdasForModel(false, new LoadEdasForModelFactory()),
	
	
	//Watersheds
	PredefinedWatershedsForModel(false, new LoadPredefinedWatershedsForModelFactory()),
	PredefinedWatershedReachesForModel(false, new LoadPredefinedWatershedReachesForModelFactory()),
	
	//Fractional Area Calculations
	ReachAreaFractionMap(false, new ReachAreaFractionMapFactory()),
	FractionedWatershedArea(false, new FractionedWatershedAreaFactory()),
	FractionedWatershedAreaTable(false, new FractionedWatershedAreaTableFactory()),
	
	ReachFullId(false, new ReachFullIdFactory())
	;
	
	public final boolean isDistributed;
	public final CacheEntryFactory factory;
	
	/**
	 * True by default to indicate the cache is in use.  Can be set to false
	 * to allow no cache to be used and/or disable the cache if configured.
	 */
	public final boolean isCached;	

	private ConfiguredCache(boolean isDistributed, CacheEntryFactory... factory) {
		
		isCached = true;		//standard case
		
		this.isDistributed = isDistributed;
		if (factory != null && factory.length > 0) {
			this.factory = factory[0];
		} else {
			this.factory = null;
		}
	}
	
	private ConfiguredCache(boolean isDistributed, boolean isCached, CacheEntryFactory... factory) {
		
		//The cache is not used
		this.isCached = isCached;
		
		this.isDistributed = isDistributed;
		if (factory != null && factory.length > 0) {
			this.factory = factory[0];
		} else {
			this.factory = null;
		}
	}
	
	public Ehcache getCacheImplementation() {
		Ehcache cache = SparrowCacheManager.getInstance().getEhcache(this.name());
		return cache;
	}
	
	/**
	 * Fetch an object by its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key
	 * @return
	 */
	public Object get(Object key) {
		return get(key, false);
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
		Ehcache cache = SparrowCacheManager.getInstance().getEhcache(this.name());
		
		//monitoring
		CacheInvocation invocation = new CacheInvocation(this, key, key.toString());
		invocation.start();
		
		try {
			if (cache != null && isCached) {
				
				//Element may be expired, so this is not 100% for sure a hit
				//Note:  We cannot use cache.get() to determine if cached b/c the factory will create it.
				invocation.setCacheHit(cache.isKeyInCache(key));
				
				Element e  = (quiet)? cache.getQuiet(key): cache.get(key);
				
				if (e != null) {
					Object value = e.getObjectValue();
					invocation.setNonNullResponse(value != null);
					return value;
				} else {
					invocation.setNonNullResponse(false);
					return null;
				}
				
			} else if (factory != null && ! isCached) {
				//No cache defined, but we do have a factory, so invoke directly
				
				invocation.setCacheHit(false);
				try {
					Object o = factory.createEntry(key);
					invocation.setNonNullResponse(o != null);
					return o;
				} catch (Exception e) {
					e.printStackTrace();
					invocation.setError(e);
					invocation.setNonNullResponse(false);
					return null;	//We don't have a way to report this at this level
				}
			} else {
				//There is no factory and no cache... return null I guess
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			invocation.setError(t);
			invocation.setNonNullResponse(false);
			
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				return null;	//We don't have a way to report this at this level
			}
		} finally {
			invocation.finish();
		}
	}
	
	/**
	 * Fetch an object by its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key
	 * @return
	 */
	public Object get(Long key) {
		return get(key, false);
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
		Ehcache cache = SparrowCacheManager.getInstance().getEhcache(this.name());
		
		//monitoring
		CacheInvocation invocation = new CacheInvocation(this, key, key.toString());
		invocation.start();
		
		try {
			if (cache != null && isCached) {

				//Element may be expired, so this is not 100% for sure a hit
				//Note:  We cannot use cache.get() to determine if cached b/c the factory will create it.
				invocation.setCacheHit(cache.isKeyInCache(key));

				Element e  = (quiet)? cache.getQuiet(key): cache.get(key);

				if (e != null) {
					Object value = e.getObjectValue();
					invocation.setNonNullResponse(value != null);
					return value;
				} else {
					invocation.setNonNullResponse(false);
					return null;
				}

			} else if (factory != null && ! isCached) {
				//No cache defined, but we do have a factory, so invoke directly
				
				invocation.setCacheHit(false);
				try {
					Object o = factory.createEntry(key);
					invocation.setNonNullResponse(o != null);
					return o;
				} catch (Exception e) {
					e.printStackTrace();
					invocation.setError(e);
					invocation.setNonNullResponse(false);
					return null;	//We don't have a way to report this at this level
				}
			} else {
				//There is no factory and no cache... return null I guess
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			invocation.setError(t);
			invocation.setNonNullResponse(false);
			
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				return null;	//We don't have a way to report this at this level
			}
		} finally {
			invocation.finish();
		}
	}
	
	/**
	 * Fetch an object by its key.
	 * 
	 * No type checking is done, so callers must ensure they are using the correct
	 * class types.
	 * @param key An Integer key is converted to a long.
	 * @return
	 */
	public Object get(Integer key) {
		return get(new Long(key), false);
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
	public List getKeysWithExpiryCheck() throws IllegalStateException, CacheException {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		
		if (c != null) {
			return c.getKeysWithExpiryCheck();
		} else {
			return Collections.EMPTY_LIST;
		}
	}
	
	public List getKeys() throws IllegalStateException, CacheException {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		
		if (c != null) {
			return c.getKeys();
		} else {
			return Collections.EMPTY_LIST;
		}
	}
	
	public Element getElementQuiet(Object key) throws IllegalStateException, CacheException {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		
		if (c != null) {
			return c.getQuiet(key);
		} else {
			return null;
		}
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
	
	public void put(Object key, Object value) {
		Ehcache c = SparrowCacheManager.getInstance().getEhcache(this.name());
		Element e  = new Element(key, value);
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