package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.ColumnFromTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.DeliveryRunner;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.StdErrorEstTable;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;
import gov.usgswim.sparrow.service.predict.WeightRunner;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;
import gov.usgswim.sparrow.util.DataLoader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.apache.log4j.Logger;


/**
 *  Load HUC Data into a single column table as:
 *  [Index] : The Reach Identifier
 *  [0] : The HUC ID as a string (i.e, 01002235) for this reach.
 *  One row per reach in the specified model.
 *  
 * @author eeverman
 *
 */
public class LoadReachHucs extends Action<DataTable>{

	/** Wrapper for modelId and Huc Level */
	protected LoadReachHucsRequest request;

	/** The query used to build the results - used for logging */
	private String query;
	
	/** The returned datatable - used for logging */
	private DataTable result;


	public void setRequest(LoadReachHucsRequest request) {
		this.request = request;
	}

	@Override
	protected DataTable doAction() throws Exception {
		query = getTextWithParamSubstitution(
				"LoadHucs",
				"ModelId", request.getModelID().toString(),
				"HucLevel", request.getHucLevel().toString());

		log.debug("Reach-Huc query to be run: " + query);
		
		Connection conn = SharedApplication.getInstance().getConnection();
		ResultSet rs = null;


		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(200);

		rs = st.executeQuery(query);

		try {
			rs = st.executeQuery(query);
			result = DataTableConverter.toDataTable(rs, true);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			conn.close();
		}
		
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
