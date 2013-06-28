package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.domain.AggregationLevel;

import gov.usgswim.sparrow.domain.reacharearelation.*;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Loads the state area relations for all reaches in a model.
 * 
 * @See ModelReachAreaRelations for details on data structure.
 *  
 * @todo:  This now needs to accept HUC levels for the queries.
 * 
 * @author eeverman
 */
public class LoadModelReachAreaRelations extends Action<ModelReachAreaRelations> {
	
	
	private static final String STATE_QUERY_NAME = "stateQuery";
	private static final String HUC_QUERY_NAME = "hucQuery";
	private static final String EDA_QUERY_NAME = "edaQuery";
	
	private static final int REACH_ID_COL = 0;
	private static final int AREA_ID_COL = 1;
	private static final int FRACTION_COL = 2;
	
	protected Long modelId;
	protected AggregationLevel aggLevel;
	
	
	//Action loaded data
	protected TopoData topoData;
	protected DataTable reachAreaRelationData;
	
	
	
	public LoadModelReachAreaRelations(ModelAggregationRequest request) throws Exception {
		super();
		
		modelId = request.getModelID();
		aggLevel = request.getAggLevel();
	}
	
	public LoadModelReachAreaRelations(Long modelId, AggregationLevel aggLevel) throws Exception {
		super();
		
		this.modelId = modelId;
		this.aggLevel = aggLevel;
	}
	
	/**
	 * Debug calculation-only constructor.
	 * 
	 * Passes in all the data required for the calculation, so db access is not required.
	 * @param topoData
	 * @param reachAreaRelationData
	 * @throws Exception 
	 */
	public LoadModelReachAreaRelations(TopoData topoData, DataTable reachAreaRelationData) throws Exception {
		super();
		
		this.topoData = topoData;
		this.reachAreaRelationData = reachAreaRelationData;
	}
	
	@Override
	protected void validate() {
		if (topoData == null && modelId == null) {
			this.addValidationError("The topoData and modelID parameters cannot both be null");
		}
		
		if (reachAreaRelationData == null && aggLevel == null) {
			this.addValidationError("The aggLevel and reachAreaRelationData parameters cannot both be null");
		}
		
		if (aggLevel != null && ! (aggLevel.isHuc() || aggLevel.isPolitical() || aggLevel.isEda())) {
			this.addValidationError("The aggregation level (the level at which to load the area relations for) must be either HUC, EDA or a political region.");
		}
	}
	
	/**
	 * Clear designation of init values
	 */
	protected void initRequiredFields() throws Exception {
		if (topoData == null && modelId != null) {
			topoData = SharedApplication.getInstance().getPredictData(modelId).getTopo();
		}
		
		if (reachAreaRelationData == null) {
			reachAreaRelationData = loadReachAreaRelationsTable(modelId, aggLevel);
		}
	}


	@Override
	public ModelReachAreaRelations doAction() throws Exception {
		initRequiredFields();
		
		return convertTableToRelations(reachAreaRelationData, topoData);
	}
	
	protected DataTable loadReachAreaRelationsTable(Long modelId, AggregationLevel aggLevel) throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", modelId);
		DataTableWritable loadedTable = null;
		
		if (aggLevel.isHuc()) {
			params.put("Huclevel", aggLevel.getHucLevel().getLevel());
			
			ResultSet rset = getROPSFromPropertiesFile(HUC_QUERY_NAME, getClass(), params).executeQuery();
			addResultSetForAutoClose(rset);
			loadedTable = DataTableConverter.toDataTable(rset);
			int valuesRowCount = loadedTable.getRowCount();
			
			//Replace the huc codes ("0034") with the has code of the same 
			StandardNumberColumnDataWritable hucIds = new StandardNumberColumnDataWritable();
			for (int r = 0; r < valuesRowCount; r++) {
				String huc = loadedTable.getString(r, 1);
				Integer id = huc.hashCode();
				hucIds.setValue(id, r);
			}
			
			loadedTable.setColumn(hucIds, AREA_ID_COL);
			
		} else if (aggLevel.equals(AggregationLevel.STATE)) {
			
			ResultSet rset = getROPSFromPropertiesFile(STATE_QUERY_NAME, getClass(), params).executeQuery();
			addResultSetForAutoClose(rset);
			loadedTable = DataTableConverter.toDataTable(rset);
			
		} else {
			//validation ensures we don't end up here
			throw new Exception("Unexpected AggregationLevel '" + aggLevel.getName() + "'");
		}
		
		return loadedTable;
	}
	
	public ModelReachAreaRelations convertTableToRelations(DataTable reachAreaRelationData, DataTable topo) {
		
		int modelRowCount = topo.getRowCount();

		
		ModelReachAreaRelationsBuilder builder = new ModelReachAreaRelationsBuilder(modelRowCount);
		
		//DataTable Structure / Sample
		//____________________________________
		//	REACH_ID	|	AREA_ID	|	FRACTION
		//____________________________________
		//		1				|		1			|		1.0
		//		2				|		2			|		.5
		//		2 (!)		|		8			|		.5
		//		3				|		1			|		1.0
		//____________________________________
		
		int relationDataRowCount = reachAreaRelationData.getRowCount();
		int rowInRelationData = 0;
		
		//Iterate over rows in the model, which can have fewer rows than the relationData
		for (int rowInModelAndResult = 0; rowInModelAndResult < modelRowCount; rowInModelAndResult++) {
			Long reachIdInModelAndResult = topo.getIdForRow(rowInModelAndResult);
			Long reachIdInRelationData = reachAreaRelationData.getLong(rowInRelationData, REACH_ID_COL);
			
			if (reachIdInModelAndResult.equals(reachIdInRelationData)) {
				
				//List of all areas that this reach is related to (states it is in or huc it is in)
				List<AreaRelation> relations = new ArrayList<AreaRelation>(1);
				
				//Loop rows in the relationData as long as they are the same reach ID as the reachIdInModelAndResult
				while (reachIdInModelAndResult.equals(reachIdInRelationData) && rowInRelationData < relationDataRowCount) {
					
					//Create and add the relation for the current relation row
					long areaId = reachAreaRelationData.getLong(rowInRelationData, AREA_ID_COL);
					double fraction = reachAreaRelationData.getDouble(rowInRelationData, FRACTION_COL);
					AreaRelationImpl relation = new AreaRelationImpl(areaId, fraction);
					relations.add(relation);
					
					//increment and update current relation values
					rowInRelationData++;
					
					if (rowInRelationData < relationDataRowCount) {
						reachIdInRelationData = reachAreaRelationData.getLong(rowInRelationData, REACH_ID_COL);
					} else {
						//will fall out of the loop at top
					}
				}
				
				ReachAreaRelationsSimple reachRelations = new ReachAreaRelationsSimple(reachIdInModelAndResult, relations);
				builder.set(rowInModelAndResult, reachRelations);
				
				
			} else {
				//No relations found
				ReachAreaRelationsEmpty empty = new ReachAreaRelationsEmpty(reachIdInModelAndResult);
				builder.set(rowInModelAndResult, empty);
			}
			
		}
		
		return builder.toImmutable();
	}




}
