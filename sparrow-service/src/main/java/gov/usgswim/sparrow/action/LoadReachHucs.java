package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 *  Load HUC Data into a single column table as:
 *  [Index] : The Reach Identifier
 *  [0] : The HUC ID as a string (i.e, 01002235) for this reach.
 *  One row per reach in the specified model.
 *  
 * @todo:  This query uses an odd replacement scheme:  It replaces all the $$ delimited
 * portions at the time it builds the query, rather than as SQL parameters.
 * 
 * @todo:  It looks like this action is not used
 * 
 * @author eeverman
 *
 */
public class LoadReachHucs extends Action<DataTable>{

	/** Wrapper for modelId and Huc Level */
	protected ModelHucsRequest request;

	/** The query used to build the results - used for logging */
	private String query;
	
	/** The returned datatable - used for logging */
	private DataTable result;


	public void setRequest(ModelHucsRequest request) {
		this.request = request;
	}

	@Override
	public DataTable doAction() throws Exception {
		query = getTextWithParamSubstitution(
				"LoadHucs",
				"ModelId", request.getModelID().toString(),
				"HucLevel", request.getHucLevel().getLevel());

		log.debug("Reach-Huc query to be run: " + query);
		
		PreparedStatement ps = getNewROPreparedStatement(query);
		ps.setFetchSize(200);

		ResultSet rs = ps.executeQuery();
		addResultSetForAutoClose(rs);


		DataTableWritable writeable = DataTableConverter.toDataTable(rs, true);
		writeable.setName("HUC Level " + request.getHucLevel() +
				" aggregation data for model " + request.getModelID());
		writeable.setProperty("model_id", request.getModelID().toString());
		writeable.setProperty("huc_level", request.getHucLevel().toString());
		
		result = writeable.toImmutable();

	
		if (result == null) {
			log.error("UNABLE to load HUC data for model " +
					request.getModelID() + ", HUC level " + request.getHucLevel());
		}
		return result;
	}
	
	@Override
	protected String getPostMessage() {
		if (result != null) {
			return "Loaded " + result.getRowCount() + " rows for model " +
			request.getModelID() + ", HUC level " + request.getHucLevel();
		} else {
			return "UNABLE to load HUC data for model " +
			request.getModelID() + ", HUC level " + request.getHucLevel();
		}
	}

}
