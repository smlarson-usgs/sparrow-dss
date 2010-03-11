package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


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
			ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		
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
