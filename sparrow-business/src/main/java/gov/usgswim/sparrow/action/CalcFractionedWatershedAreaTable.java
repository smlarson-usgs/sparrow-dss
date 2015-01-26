package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.HashMap;
import java.util.List;

/**
 * Calculates a column of fractioned reach watershed areas, one entry per
 * reach in the passed terminalReaches.
 * 
 * There is no index for the column.  Rows correspond to the model row order.
 * 
 * @author eeverman
 * @see gov.usgswim.sparrow.action.CalcAreaFractionMap
 * @see gov.usgswim.sparrow.action.CalcFractionedWatershedArea
 *
 */
public class CalcFractionedWatershedAreaTable extends Action<ColumnData> {

	//ID of a TerminalReaches object, expected to be pulled from the cache.
	protected Integer terminalReachesId;
	
	/** If true, ignore the fraction and just add up the area.  Mostly for debugging. */
	protected boolean forceNonFractionedResult = false;
	
	
	//normally action loaded values
	protected transient DataTable topoData;
	protected transient TerminalReaches terminalReaches;
	
	protected String msg = null;
	
	
	/**
	 * The main method, allows for terminal reaches to be used as the cache key.
	 * @param terminalReachesId The ID of a set of TerminalReaches in the Cache. 
	 */
	public CalcFractionedWatershedAreaTable(Integer terminalReachesId) {
		this.terminalReachesId = terminalReachesId;
	}
	
	public CalcFractionedWatershedAreaTable(TerminalReaches terminalReaches, boolean forceNonFractionedResult) {
		this.terminalReaches = terminalReaches;
		this.forceNonFractionedResult = forceNonFractionedResult;
	}

	public CalcFractionedWatershedAreaTable(TerminalReaches terminalReaches, boolean forceNonFractionedResult, DataTable topoData) {
		this.terminalReaches = terminalReaches;
		this.forceNonFractionedResult = forceNonFractionedResult;
		this.topoData = topoData;
	}
	
	@Override
	protected void validate() {
		if (terminalReachesId == null && terminalReaches == null) {
			this.addValidationError("Either the terminalReachesId or the terminalReaches parameter must not be null.");
		} else if (terminalReaches != null && terminalReaches.isEmpty()) {
			this.addValidationError("The terminalReaches parameter is empty - there must be at least one terminal reach.");
		} else {
			//everything is OK
		}
	}
	
	@Override
	public void initFields() throws Exception {
		
		if (terminalReaches == null && terminalReachesId != null) {
			terminalReaches = SharedApplication.getInstance().getTerminalReaches(terminalReachesId);
		}
		
		//Terminal reaches OK?
		if (terminalReachesId == null) {
			throw new Exception("Either the terminalReachesId or the terminalReaches parameter must not be null.");
		} else if (terminalReaches != null && terminalReaches.isEmpty()) {
			throw new Exception("The terminalReaches parameter is empty - there must be at least one terminal reach.");
		}
		
		if (topoData == null) {
			topoData = SharedApplication.getInstance().getPredictData(terminalReaches.getModelID()).getTopo();
		}
		
	}
	
	
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public ColumnData doAction() throws Exception {
		
		HashMap<Integer, Double> areaMap = buildMap();
		ColumnData col = buildColumn(areaMap);
		return col;
	}
	
	public ColumnData buildColumn(HashMap<Integer, Double> areaMap) {
		
		HashMap<String, String> props = new HashMap<String, String>(1, 1);
		props.put(TableProperties.CONSTITUENT.toString(), "land area");
		
		String description = "The area of land that drains all the upstream streams"
				+ " and rainfall to the stream reach.  "
				+ "This area is fractioned: Area above a diversion (where the river splits)"
				+ " is multiplied the fraction of flow that flows to a particular reach.";
		
		SparseDoubleColumnData col = new SparseDoubleColumnData(areaMap,
				"Watershed Area, Fractioned", SparrowUnits.SQR_KM.toString(),
				description, props, null, topoData.getRowCount(), null);
		
		return col;
	}
	
	public HashMap<Integer, Double> buildMap() throws Exception {
		double mapSize = (double)terminalReaches.size() * (1D / .75D);
		int mapSizeInt = (int) mapSize;
		mapSizeInt = mapSizeInt / 2 * 2 + 1;	//force an odd number
		
		HashMap<Integer, Double> areaMap = new HashMap<Integer, Double>(mapSizeInt, .8f);
		
		Long modelId = terminalReaches.getModelID();
		List<Long> reachIds = SharedApplication.getInstance().getReachFullIdAsLong(
				modelId, terminalReaches.getReachIdsAsList());
		
		for (Long reachId : reachIds) {
			ReachID rid = new ReachID(modelId, reachId);
			
			//TODO:  These should be configurable
			CalcFractionedWatershedArea action = new CalcFractionedWatershedArea(
					new FractionedWatershedAreaRequest(rid, false, false, forceNonFractionedResult));
			Double area = action.run();
			areaMap.put(topoData.getRowForId(reachId), area);
		}
		
		return areaMap;
	}
	
	@Override
	public Long getModelId() {
		if (terminalReaches != null) {
			return terminalReaches.getModelID();
		} else {
			return null;
		}
	}
	
}
