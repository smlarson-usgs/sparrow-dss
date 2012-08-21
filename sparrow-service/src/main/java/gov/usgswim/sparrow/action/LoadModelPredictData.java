package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DLUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class LoadModelPredictData extends Action<PredictData> implements ILoadModelPredictData {

	private Long modelId;
	
	/** Summary message for completed action */
	private StringBuffer message = new StringBuffer();
	
	public LoadModelPredictData() {
	}
	
	/**
	 * Creates a Action to load the entire model.
	 * Only the iteration zero set of coef's are loaded for coef data.
	 * @param modelId the ID of the Sparrow Model to load
	 */
	public LoadModelPredictData(Long modelId) {
		this.modelId = modelId;
	}
	
	@Override
	public PredictData doAction() throws Exception {
		PredictDataBuilder dataSet = new PredictDataBuilder();
		Connection con = this.getROConnection();
		
		dataSet.setSrcMetadata( loadSourceMetadata(con, modelId));
		dataSet.setTopo( loadTopo(con, modelId) );
		dataSet.setCoef( loadSourceReachCoef(con, modelId, 0, dataSet.getSrcMetadata()) );
		dataSet.setDelivery( loadDelivery(con, modelId, 0) );
		dataSet.setSrc( loadSourceValues(con, modelId, dataSet.getSrcMetadata()) );
		
		//At this point we no longer need a connection for this action.
		//release it back to the pool so  we don't hold a connection while
		//calling getModelMetadata.
		close(con);
		
		message.append("Loaded Predict data for model " + modelId + NL);
		message.append("  Src Meta Rows: " + dataSet.getSrcMetadata().getRowCount() + NL);
		message.append("  Topo Rows: " + dataSet.getTopo().getRowCount() + NL);
		message.append("  Coef Rows: " + dataSet.getCoef().getRowCount() + NL);
		message.append("  Delivery Rows: " + dataSet.getDelivery().getRowCount() + NL);
		message.append("  Source Rows: " + dataSet.getSrc().getRowCount() + NL);
		
		//Add the model metadata
		ModelRequestCacheKey modelKey = new ModelRequestCacheKey(modelId, false, false, false);
		SparrowModel model = SharedApplication.getInstance().getModelMetadata(modelKey).get(0);
		dataSet.setModel(model);
		
		return dataSet.toImmutable();
	}
	
	/**
	 * Returns metadata about the source types in the model.
	 *
	 * Typically 5-10 rows per model.
	 *
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <h5>IDENTIFIER - The Row ID (not a column). The SparrowModel specific ID for the source (starting w/ 1)</h5>
	 * <ol>
	 * <li>SOURCE_ID - (long) The database unique ID for the source
	 * <li>NAME - (String) The full (long text) name of the source
	 * <li>DISPLAY_NAME - (String) The short name of the source, used for display
	 * <li>DESCRIPTION - (String) A description of the source (could be long)
	 * <li>CONSTITUENT - (String) The name of the Constituent being measured
	 * <li>UNITS - (String) The units the constituent is measured in
	 * <li>PRECISION - (int) The number of decimal places
	 * <li>IS_POINT_SOURCE (boolean) 'T' or 'F' values that can be mapped to boolean.
	 * </ol>
	 *
	 *
	 * @param conn
	 * @param modelId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public DataTableWritable loadSourceMetadata(Connection conn, long modelId)
	throws SQLException, IOException {

		String query = getTextWithParamSubstitution("SelectSourceData", LoadModelPredictData.class, "ModelId", "" + modelId);

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		addStatementForAutoClose(st);
		st.setFetchSize(30);

		ResultSet rs = null;
		DataTableWritable result = null;
		try {
			rs = st.executeQuery(query);
			addResultSetForAutoClose(rs);
			result = DataTableConverter.toDataTable(rs, true);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
		if (result == null) {
			String err = "UNABLE to loadsource metadata from " + LoadModelPredictData.class.getSimpleName() + "loadSourceMetadata()";
			log.error(err);
			throw new IOException(err);
		} else {
			//Update unit to be the user displayable form
			for (int r = 0; r < result.getRowCount(); r++) {
				SparrowUnits unit = SparrowUnits.parse(result.getString(r, PredictData.SOURCE_META_UNIT_COL));
				result.setValue(unit.getUserName(), r, PredictData.SOURCE_META_UNIT_COL);
			}

		}
			
			
		if (log.isDebugEnabled()) {
			log.debug("Printing sample of source metadata ...");
			log.debug(DataTablePrinter.sampleDataTable(result, 10, 10));
		}
		
		return result;
	}
	
	/**
	 * Returns an ordered DataTable of all topological data in the MODEL
	 * <h4>Data Columns</h4>
	 * One row per reach (i = reach index)
	 * <h5>Row ID: IDENTIFIER from DB</h5>
	 * <ul>
	 * <li>[i][0]MODEL_REACH - The db id for this reach (the actual identifier is used as the index)
	 * <li>[i][1]FNODE - The from node
	 * <li>[i][2]TNODE - The to node
	 * <li>[i][3]IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>[i][4]HYDSEQ - Hydrologic sequence order (starting at 1, no gaps)
	 * <li>[i][5]SHORE_REACH - 1 if a shore reach, 0 otherwise.
	 * <li>[i][6]FRAC - Fraction of the upstream load/flow entering this reach.  Non-one at a diversion.
	 * </ul>
	 * 
	 * <h4>Sorting</h4>
	 * Sorted by HYDSEQ then IDENTIFIER, since in some cases HYDSEQ is not unique.
	 * The use of IDENTIFIER has no significance except to guarantee
	 * deterministic ordering of the results.
	 *
	 * For complete data definitions, please see:
	 * @see gov.usgswim.sparrow.PredictData#getTopo()
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public DataTableWritable loadTopo(Connection conn, long modelId) throws Exception,
	IOException {
		
		//Create param map
		HashMap<String, Object> params = new HashMap<String, Object>(1, 1);
		params.put("ModelId", "" + modelId);
		
		//Expected column types
		Class<?>[] colTypes = {Integer.class, Integer.class, Integer.class,
			Integer.class, Integer.class, Integer.class, Double.class};
		
		PreparedStatement statement = getROPSFromPropertiesFile("SelectTopoData", this.getClass(), params);
		addStatementForAutoClose(statement);
		ResultSet rset = statement.executeQuery();
		addResultSetForAutoClose(rset);
		DataTableWritable result = DataTableConverter.toDataTable(rset, colTypes, true);
		
		/** TNODE is used heavily during delivery calcs to find reaches, so index */
		result.buildIndex(PredictData.TOPO_TNODE_COL);

		if (log.isDebugEnabled()) {
			log.debug("Printing sample of topo ...");
			log.debug(DataTablePrinter.sampleDataTable(result, 10, 10));
		}

		assert(result.hasRowIds()): "topo should have IDENTIFIER as row ids";
		//assert(result.isIndexed(PredictData.TNODE_COL)): "topo tnodes should be indexed";
		return result;
	}
	
	/**
	 * Returns a DataTable of all source/reach coef's.
	 * 
	 * <h4>Data Columns</h4>
	 * <p>One row per reach (i = reach index).
	 * Row ID is IDENTIFIER (not the db model_reach_id)</p>
	 * <ol>
	 * <li>[i][Source 1] - The coef for the first source of reach i
	 * <li>[i][Source 2] - The coef's for the 2nd source of reach i
	 * <li>[i][Source 2] - The coef's for the 3rd...
	 * <li>...as many columns as there are sources.
	 * </ol>
	 * For complete data definitions, please see:
	 * @see gov.usgswim.sparrow.PredictData#getDeliverygetCoef()
	 * 
	 * <h4>Sorting</h4>
	 * Sorted by HYDSEQ then IDENTIFIER, since in some cases HYDSEQ is not unique.
	 * The use of IDENTIFIER has no significance except to guarantee
	 * deterministic ordering of the results.
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @param sources	An DataTable list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public DataTableWritable loadSourceReachCoef(Connection conn, long modelId, int iteration, DataTable sources) throws SQLException,
	IOException {

		if (iteration < 0) {
			throw new IllegalArgumentException("The iteration cannot be less then zero");
		}
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}

		int sourceCount = sources.getRowCount();
		DataTableWritable sourceReachCoef = new SimpleDataTableWritable();

		//Assign row IDs directly from the base query
		String rowIdQuery =
			getTextWithParamSubstitution("SelectReachCoef", LoadModelPredictData.class,
					"ModelId", modelId, "Iteration", iteration, "SourceId", sources.getInt(0, PredictData.SOURCE_META_ID_COL));
		loadIndexValues(conn, sourceReachCoef, rowIdQuery, "Identifier");

		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {

			String query =
				getTextWithParamSubstitution("SelectReachCoef", LoadModelPredictData.class,
						"ModelId", modelId, "Iteration", iteration, "SourceId", sources.getInt(srcIndex, PredictData.SOURCE_META_ID_COL)
				);

			//The query has two columns and we only want the Value column
			query = "Select Value from ( " + query + " )";

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			addStatementForAutoClose(st);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {

				rs = st.executeQuery(query);
				addResultSetForAutoClose(rs);
				sourceReachCoef.addColumn(new StandardNumberColumnDataWritable<Double>());
				DLUtils.loadColumn(rs, sourceReachCoef, 0, srcIndex);

			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}

		}

		if (log.isDebugEnabled()) {
			log.debug("Printing sample of sourceReachCoef ...");
			log.debug(DataTablePrinter.sampleDataTable(sourceReachCoef, 10, 10));
		}

		return sourceReachCoef;

	}
	
	
	/**
	 * Returns a DataTable of all delivery data for for a single model.
	 *
	 * <h4>Data Columns</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream delivery at reach i
	 * <li>[i][1] == the upstream delivery at reach i.
	 * </ol>
	 * <h4>Sorting</h4>
	 * Sorted by HYDSEQ then IDENTIFIER, since in some cases HYDSEQ is not unique.
	 * The use of IDENTIFIER has no significance except to guarantee
	 * deterministic ordering of the results.
	 *
	 * For complete data definitions, please see:
	 * @see gov.usgswim.sparrow.PredictData#getDelivery()
	 *
	 * TODO:  All the other 'per reach' data tables load row ids, which allows
	 * a consistency check to be done.  This is not done yet for this b/c
	 * there is no direct loadAsDouble() method that supports loading an index.
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadDelivery(Connection conn, long modelId, int iteration) throws SQLException,
	IOException {

		String query = getTextWithParamSubstitution("SelectDeliveryCoef", LoadModelPredictData.class,
				"ModelId", modelId, "Iteration", iteration
		);

		DataTableWritable decay = DLUtils.readAsDouble(conn, query, 2000);
		if (log.isDebugEnabled()) {
			log.debug("Printing sample of decay ...");
			log.debug(DataTablePrinter.sampleDataTable(decay, 10, 10));
		}
		return decay;
	}
	
	/**
	 * Returns a DataTable of all source values for a single model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>id: IDENTIFIER - The model specific ID for this reach (loaded w/ query for data check)
	 * <li>[Source 1] - The values for the first source in one column
	 * <li>[Source 2...] - The values for the 2nd...
	 * <li>...
	 * </ol>
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param sources	An DataTable list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public DataTableWritable loadSourceValues(Connection conn, long modelId, DataTable sources) throws SQLException,
	IOException {

		int sourceCount = sources.getRowCount();
		if (sourceCount == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}

		// Load column headings using the source display names

		// TODO need to add display name to source_metadata to eliminate the magic number of 2
		Integer display_name_col = sources.getColumnByName("display_name");
		display_name_col = (display_name_col == null)? 2: display_name_col;
		String[] headings = DataTableUtils.getStringColumn(sources, display_name_col);

		DataTableWritable sourceValues = new SimpleDataTableWritable();

		//Assign row IDs directly from the base query
		String rowIdQuery = getTextWithParamSubstitution("SelectSourceValues", LoadModelPredictData.class,
					"ModelId", modelId, "SourceId", sources.getInt(0, PredictData.SOURCE_META_ID_COL)
			);
		loadIndexValues(conn, sourceValues, rowIdQuery, "Identifier");

		for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {
			String constituent = sources.getString(srcIndex, sources.getColumnByName("CONSTITUENT"));
			String units = sources.getString(srcIndex, sources.getColumnByName("UNITS"));
			String precision = sources.getString(srcIndex, sources.getColumnByName("PRECISION"));

			String query = getTextWithParamSubstitution("SelectSourceValues", LoadModelPredictData.class,
						"ModelId", modelId, "SourceId", sources.getInt(srcIndex, PredictData.SOURCE_META_ID_COL)
				);

			//The query has two columns and we only want the Value column
			query = "Select Value from ( " + query + " )";

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			addStatementForAutoClose(st);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {
				StandardNumberColumnDataWritable<Double> column = new StandardNumberColumnDataWritable<Double>(headings[srcIndex], units);
				column.setProperty("constituent", constituent);
				column.setProperty("precision", precision);
				sourceValues.addColumn(column);

				rs = st.executeQuery(query);
				addResultSetForAutoClose(rs);
				DLUtils.loadColumn(rs, sourceValues, 0, srcIndex);
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}

		}
		if (log.isDebugEnabled()) {
			log.debug("Printing sample of sources ...");
			log.debug(DataTablePrinter.sampleDataTable(sourceValues, 10, 10));
		}
		return sourceValues;

	}
	
	protected void loadIndexValues(Connection conn, DataTableWritable table,
			String baseQuery, String indexColumnName) throws SQLException {
		//Grab the query for the first source, but only taking the ID vals

		String query = "Select " + indexColumnName + " from ( " + baseQuery + " )";

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		addStatementForAutoClose(st);
		st.setFetchSize(2000);
		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
			addResultSetForAutoClose(rs);
			DLUtils.loadIndex(rs, table, 0);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.action.ILoadModelPredictData#getModelId()
	 */
	public Long getModelId() {
		return this.modelId;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.action.ILoadModelPredictData#setModelId(java.lang.Long)
	 */
	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	@Override
	protected String getPostMessage() {
		return message.toString();
	}

}
