package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.ColumnFromTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.StdErrorEstTable;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.WeightRunner;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;


/**
 *  Loads a table containing reach attributes.

 * @author eeverman
 *
 */
public class LoadReachAttributes extends Action<DataTable> {
	
	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "attributesSQL";
	
	protected long modelId;
	protected long reachId;
	
	
	
	public LoadReachAttributes(long modelId, long reachId) {
		super();
		this.modelId = modelId;
		this.reachId = reachId;
	}



	public LoadReachAttributes() {
		super();
	}



	@Override
	protected DataTable doAction() throws Exception {
		String sql = this.getText(QUERY_NAME);
		Connection conn = getConnection();	//Auto closed (resultsets and statements are not)
		PreparedStatement st =
			conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
		
		st.setLong(1, reachId);
		st.setLong(2, modelId);
		
		
		ResultSet rset = null;
		DataTableWritable attributes = null;
		try {
			rset = st.executeQuery();
			attributes = DataTableConverter.toDataTable(rset);
		} finally {
			close(st);
			close(rset);
			//Connection is auto-closed
		}
		return attributes;
		
	}



	public long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}



	public long getReachId() {
		return reachId;
	}



	public void setReachId(long reachId) {
		this.reachId = reachId;
	}


}
