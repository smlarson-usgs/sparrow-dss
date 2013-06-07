package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.ReachRowValueMapBuilder;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.domain.reacharearelation.AreaRelation;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.request.ReachAreaFractionMapRequest;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

/**
 * Calculates a column of regional areas contributing to a set of downstream
 * target reaches.
 *
 * One row per region.  If request for state level, there would be one row per
 * state represented in a model.
 *
 * The returned regional area is the area contributing to the all of the target
 * reaches.  Region area not upstream of the targets is not counted.  Area upstream
 * of a diversion is counted based on the FRAC at the diversion.
 *
 * There is no index for the column.  Rows correspond to the region areas.
 *
 * @author eeverman
 * @see gov.usgswim.sparrow.action.CalcAreaFractionMap
 * @see gov.usgswim.sparrow.action.CalcFractionedWatershedArea
 *
 */
public class CalcContributingFractionedAreaForRegions extends Action<ColumnData> {
	//caller must specify aggLevel
	protected AggregationLevel aggLevel;

	//The caller must specify one of these:
	protected Collection<Long> terminalReachIds;
	protected transient TerminalReaches terminalReaches;

	//The action loads these values if terminalReaches is not supplied
	protected ModelReachAreaRelations areaRelations;
	protected ReachRowValueMap watershedAreaFractionMap;
	protected DataTable reachCatchmentAreas;
	protected DataTable areaDetail;

	/**
	 * This constructor does NOT require the TerminalReaches instance be in the cache.
	 * @param terminalReachesId The ID of a set of TerminalReaches in the Cache.
	 * @param aggLevel The level of aggregation.
	 */
	public CalcContributingFractionedAreaForRegions(TerminalReaches terminalReaches, AggregationLevel aggLevel) {
		this.terminalReaches = terminalReaches;
		this.aggLevel = aggLevel;
	}

	public CalcContributingFractionedAreaForRegions(Collection<Long> terminalReachIds,
			AggregationLevel aggLevel,
			ModelReachAreaRelations areaRelations,
			ReachRowValueMap watershedAreaFractionMap,
			DataTable reachCatchmentAreas,
			DataTable areaDetail) {

		this.terminalReachIds = terminalReachIds;
		this.aggLevel = aggLevel;
		this.areaRelations = areaRelations;
		this.watershedAreaFractionMap = watershedAreaFractionMap;
		this.reachCatchmentAreas = reachCatchmentAreas;
		this.areaDetail = areaDetail;

	}



	@Override
	protected void validate() {
		if (terminalReaches != null){
			if (terminalReaches.isEmpty()) {
				this.addValidationError("The terminalReaches parameter is empty - there must be at least one terminal reach.");
			}
			if (aggLevel == null) {
				this.addValidationError("The aggregate level reaches parameter cannot be null");
			}
			if (! (aggLevel.isHuc() || aggLevel.isPolitical())) {
				this.addValidationError("The aggregation level (the level at which to load the area relations for) must be either a HUC or a political region.");
			}
		}
		else if(this.terminalReachIds != null){
			if (terminalReachIds.isEmpty()) {
				this.addValidationError("The terminalReacheIds parameter is empty - there must be at least one terminal reach.");
			}
			if (aggLevel == null) {
				this.addValidationError("The aggregate level reaches parameter cannot be null");
			}
			if (! (aggLevel.isHuc() || aggLevel.isPolitical())) {
				this.addValidationError("The aggregation level (the level at which to load the area relations for) must be either a HUC or a political region.");
			}
			if(null == areaRelations){
				this.addValidationError("Area relations must be non-null");
			}
			if(null == watershedAreaFractionMap || watershedAreaFractionMap.isEmpty()){
				this.addValidationError("Watershed Area Fraction Map must be non-null, non-empty");
			}
			if(null == reachCatchmentAreas){
				this.addValidationError("reachCatchmentAreas must be non-null");
			}
			if(null == areaDetail){
				this.addValidationError("areaDetail must be non-null");
			}
		}
		else{
			this.addValidationError("Either the terminalReaches parameter or the terminalReachIds parameter must not be null.");
		}
	}

	@Override
	public void initFields() throws Exception {


		if (terminalReaches != null) {
			terminalReachIds = SharedApplication.getInstance().getReachFullIdAsLong(
				terminalReaches.getModelID(),
				terminalReaches.getReachIdsAsList()
				);

			Long modelId = terminalReaches.getModelID();

			watershedAreaFractionMap = buildWatershedAreaFractionMap(terminalReaches);

			ModelAggregationRequest modelReachAreaRelelationsRequest =
						new ModelAggregationRequest(modelId, aggLevel);

			areaRelations = SharedApplication.getInstance().getModelReachAreaRelations(modelReachAreaRelelationsRequest);
			UnitAreaRequest unitAreaRequest = new UnitAreaRequest(modelId, AggregationLevel.REACH, false);

			reachCatchmentAreas = SharedApplication.getInstance().getCatchmentAreas(unitAreaRequest);

			if (aggLevel.equals(AggregationLevel.STATE)) {
				areaDetail = SharedApplication.getInstance().getStatesForModel(modelId);
			} else if (aggLevel.isHuc()) {
				areaDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, aggLevel.getHucLevel()));
			} else if (aggLevel.isEda()) {
				//areaDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, aggLevel.getHucLevel()));
			}
		}
	}


	protected ReachRowValueMap buildWatershedAreaFractionMap(TerminalReaches terminalReaches) throws Exception {
		ReachRowValueMapBuilder builder = new ReachRowValueMapBuilder();


		Long modelId = terminalReaches.getModelID();
		List<Long> reachIds = SharedApplication.getInstance().getReachFullIdAsLong(
				modelId, terminalReaches.getReachIdsAsList());

		for (Long id : reachIds) {
			ReachRowValueMap map = SharedApplication.getInstance().getReachAreaFractionMap(
					new ReachAreaFractionMapRequest(new ReachID(modelId, id)));

			builder.mergeByAddition(map);
		}

		return builder;
	}


	@Override
	public ColumnData doAction() throws Exception {
		ColumnData result = populateColumn(reachCatchmentAreas.getColumn(1));
		return result.toImmutable();
	}

	/**
	 *
	 * @param regionResultTable Table to fill
	 * @param sourceColumns Total Delivered Load for all source and the total del load
	 * @param areaRelations reach to state (or other region) relation w/ fraction in each region
	 * @param areaDetail List of all regions (ie areaDetail) that this model touches (all regions for areaRelations)
	 * @throws Exception
	 */
	protected ColumnData populateColumn(ColumnData catchmentAreas ) throws Exception {

		StandardNumberColumnDataWritable resultColumn = new StandardNumberColumnDataWritable("Contributing Area", catchmentAreas.getUnits(), Double.class);
		resultColumn.setValue(0D, areaDetail.getRowCount() - 1);	//ensure we have enough rows

		for (Entry<Integer, Float> reachFrac : watershedAreaFractionMap.entrySet()) {
			int row = reachFrac.getKey();
			float frac = reachFrac.getValue();

			ReachAreaRelations reachRelations = areaRelations.getRelationsForReachRow(row);
			Double catchmentArea = catchmentAreas.getDouble(row);

			//Loop thru each region that this reach has catchment area in
			for (AreaRelation reachRelation : reachRelations.getRelations()) {
				long regionId = reachRelation.getAreaId();
				double reachFractionInRegion = reachRelation.getFraction();
				int regionRow = areaDetail.getRowForId(regionId);

				//Populate the total area column
				double reachAreaInRegionContributing = reachFractionInRegion * frac * catchmentArea;
				Double existingRegionArea = resultColumn.getDouble(regionRow);

				if (existingRegionArea == null) existingRegionArea = 0d;
				resultColumn.setValue(reachAreaInRegionContributing + existingRegionArea, regionRow);

			}
		}

		return resultColumn;

	}

}
