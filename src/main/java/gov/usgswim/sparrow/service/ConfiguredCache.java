/**
 * 
 */
package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.cachefactory.AdjustedSourceFactory;
import gov.usgswim.sparrow.cachefactory.AggregateIdLookupKludgeFactory;
import gov.usgswim.sparrow.cachefactory.AnalysisResultFactory;
import gov.usgswim.sparrow.cachefactory.BinningFactory;
import gov.usgswim.sparrow.cachefactory.CatchmentAreaFactory;
import gov.usgswim.sparrow.cachefactory.ComparisonResultFactory;
import gov.usgswim.sparrow.cachefactory.DeliveryFractionFactory;
import gov.usgswim.sparrow.cachefactory.LoadReachAttributesFactory;
import gov.usgswim.sparrow.cachefactory.ModelMetadataFactory;
import gov.usgswim.sparrow.cachefactory.NSDataSetFactory;
import gov.usgswim.sparrow.cachefactory.PredictDataFactory;
import gov.usgswim.sparrow.cachefactory.PredictResultFactory;
import gov.usgswim.sparrow.cachefactory.ReachByIDFactory;
import gov.usgswim.sparrow.cachefactory.ReachByPointFactory;
import gov.usgswim.sparrow.cachefactory.ReachesByCriteriaFactory;
import gov.usgswim.sparrow.cachefactory.UncertaintyDataFactory;
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
	
	CatchmentAreas(false, new CatchmentAreaFactory())
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
}