package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;

import gov.usgswim.sparrow.domain.reacharearelation.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Loads the state area relations for all reaches in a model.
 * 
 * @See ModelReachAreaRelations for details on data structure.
 *  
 * @author eeverman
 */
public class LoadModelReachAreaRelations extends Action<ModelReachAreaRelations> {
	
	
	private static final String QUERY_NAME = "query";
	private static final int REACH_ID_COL = 0;
	private static final int STATE_FIPS_ID_COL = 1;
	private static final int FRACTION_COL = 2;
	
	protected Long modelId;
	protected PredictData predictData;
	
	
	public LoadModelReachAreaRelations(PredictData predictData) {
		super();
		this.predictData = predictData;
		
		this.modelId = predictData.getModel().getId();
	}



	public LoadModelReachAreaRelations() {
		super();
	}



	@Override
	public ModelReachAreaRelations doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", modelId);
		
		ResultSet rset = getROPSFromPropertiesFile(QUERY_NAME, getClass(), params).executeQuery();
		addResultSetForAutoClose(rset);
		
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		int modelRowCount = predictData.getTopo().getRowCount();

		
		ModelReachAreaRelationsBuilder builder = new ModelReachAreaRelationsBuilder(modelRowCount);
		
		//DataTable Structure:
		//reach id (non-unique)
		//state FIP id (non-unique)
		//fraction in the state
		int relationRowCount = values.getRowCount();
		int currentRelationRow = 0;
		for (int row=0; row<modelRowCount; row++) {
			Long currentReachId = predictData.getIdForRow(row);
			Long currentRelationReachId = values.getLong(currentRelationRow, REACH_ID_COL);
			
			if (currentReachId.equals(currentRelationReachId)) {
				List<AreaRelation> relations = new ArrayList<AreaRelation>(1);
				
				while (currentReachId.equals(currentRelationReachId) && currentRelationRow < relationRowCount) {
					
					//Create and add the relation for the current relation row
					long areaId = values.getLong(currentRelationRow, STATE_FIPS_ID_COL);
					double fraction = values.getDouble(currentRelationRow, FRACTION_COL);
					AreaRelationImpl relation = new AreaRelationImpl(areaId, fraction);
					relations.add(relation);
					
					//increment and update current relation values
					currentRelationRow++;
					
					if (currentRelationRow < relationRowCount) {
						currentRelationReachId = values.getLong(currentRelationRow, REACH_ID_COL);
					} else {
						//will fall out of the loop at top
					}
				}
				
				ReachAreaRelationsSimple reachRelations = new ReachAreaRelationsSimple(currentReachId, relations);
				builder.set(row, reachRelations);
				
				
			} else {
				//No relations found
				ReachAreaRelationsEmpty empty = new ReachAreaRelationsEmpty(currentReachId);
				builder.set(row, empty);
			}
			
		}
		
		return builder.toImmutable();
		
		
	}

	@Override
	protected void validate() {
		if (predictData == null) {
			this.addValidationError("The predictData parameter cannot be null");
			return;
		}
				
		if (modelId == null) {
			this.addValidationError("The model predictData set does not seem to have a model id associated with it.");
		}
	}

	
	public void setPredictData(PredictData predictData) {
		this.predictData = predictData;
	}

}
