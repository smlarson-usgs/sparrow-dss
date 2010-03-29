package gov.usgswim.sparrow.action;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.util.DLUtils;

public class LoadModelPredictData extends Action<PredictData>{

	private boolean bootstrap;
	private Long modelId;
	
	public static final int SOURCE_ID_COL = 0;
	
	/**
	 * Creates a Action to load the entire model
	 * @param modelId the ID of the Sparrow Model to load
	 * @param bootstrap	<b>true</b>		to load only the data required to run a prediction.
	 * 					<b>false</b>	to load the complete dataset for a model (including bootstrap data). 
	 */
	public LoadModelPredictData(Long modelId, boolean bootstrap) {
		this.bootstrap = bootstrap;
		this.modelId = modelId;
	}
	
	@Override
	protected PredictData doAction() throws Exception {
		PredictDataBuilder dataSet = new PredictDataBuilder();
		Connection con = this.getConnection();
		
		dataSet.setSrcMetadata( loadSourceMetadata(con, modelId));
		dataSet.setTopo( loadTopo(con, modelId) );
		if (!this.bootstrap) {
			dataSet.setCoef( loadSourceReachCoef(con, modelId, 0, dataSet.getSrcMetadata()) );
		} else {
			dataSet.setCoef( loadSourceReachCoef(con, modelId, dataSet.getSrcMetadata()) );
		}
		dataSet.setDelivery( loadDelivery(con, modelId, 0) );
		// TODO fix: this actually is going to fail for multiple iterations
		dataSet.setSrc( loadSourceValues(con, modelId, dataSet.getSrcMetadata()) );
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
	 * TODO:  Finish this load method and use instead of loadSourceIds.  Update
	 * PredictData to have set/getSrcMetaData instead of srcIds.
	 *
	 * @param conn
	 * @param modelId
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static DataTableWritable loadSourceMetadata(Connection conn, long modelId)
	throws SQLException, IOException {

		String query = getTextWithParamSubstitution("SelectSourceData", LoadModelPredictData.class, "ModelId", "" + modelId);

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(30);

		ResultSet rs = null;
		DataTableWritable result = null;
		try {
			rs = st.executeQuery(query);
			result = DataTableConverter.toDataTable(rs, true);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
		if (result == null) {
			log.error("UNABLE to loadsource metadata from " + LoadModelPredictData.class.getSimpleName() + "loadSourceMetadata()");
		} else if (log.isDebugEnabled()) {
			log.debug("Printing sample of source metadata ...");
			log.debug(DataTablePrinter.sampleDataTable(result, 10, 10));
		}
		return result;
	}
	
	/**
	 * 	 * Returns an ordered DataTable of all REACHes in the MODEL
	 * <h4>Data Columns, sorted by HYDSEQ.  One row per reach (i = reach index)</h4>
	 * <p>Row IDs duplicate the Reach Ids in column zero.</p>
	 * <ol>
	 * <li>id: IDENTIFIER - The model specific ID for this reach
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * </ol>
	 *
	 * Sort by HYDSEQ then IDENTIFIER, since in some cases HYDSEQ is not unique.
	 * [IK] the use of IDENTIFIER has no significance except to guarantee some
	 * deterministic ordering of the results. Any other attribute would do.
	 *
	 * Returns a DataTable of all topo data for for a single model.
	 * <h4>Data Columns (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>[i][0]MODEL_REACH - The db id for this reach
	 * <li>[i][1]FNODE - The from node
	 * <li>[i][2]TNODE - The to node
	 * <li>[i][3]IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>[i][4]HYDSEQ - Hydrologic sequence order
	 * </ol>
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadTopo(Connection conn, long modelId) throws SQLException,
	IOException {
		String query = getTextWithParamSubstitution("SelectTopoData", LoadModelPredictData.class, "ModelId", "" + modelId);

		DataTableWritable result = DLUtils.readAsInteger(conn, query, 1000, 0);

		/** TNODE is used heavily during delivery calcs to find reaches, so index */
		result.buildIndex(PredictData.TNODE_COL);

		if (log.isDebugEnabled()) {
			log.debug("Printing sample of topo ...");
			log.debug(DataTablePrinter.sampleDataTable(result, 10, 10));
		}

		assert(result.hasRowIds()): "topo should have IDENTIFIER as row ids";
		//assert(result.isIndexed(PredictData.TNODE_COL)): "topo tnodes should be indexed";
		return result;
	}
	
	/**
	 * Returns a DataTable of all source/reach coef's for for a single iteration of a model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>[Source Name 1] - The coef's for the first source in one column
	 * <li>[Source Name 2...] - The coef's for the 2nd...
	 * <li>...
	 * </ol>
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @param sources	An DataTable list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadSourceReachCoef(Connection conn, long modelId, int iteration, DataTable sources) throws SQLException,
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
					"ModelId", modelId, "Iteration", iteration, "SourceId", sources.getInt(0, SOURCE_ID_COL));
		loadIndexValues(conn, sourceReachCoef, rowIdQuery, "Identifier");

		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {

			String query =
				getTextWithParamSubstitution("SelectReachCoef", LoadModelPredictData.class,
						"ModelId", modelId, "Iteration", iteration, "SourceId", sources.getInt(srcIndex, SOURCE_ID_COL)
				);

			//The query has two columns and we only want the Value column
			query = "Select Value from ( " + query + " )";

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {

				rs = st.executeQuery(query);
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
	 * Returns a DataTable of all source/reach coef's for for all iterations of a model.
	 * <h4>Data Columns with one row per reach (sorted by ITERATION then HYDSEQ)</h4>
	 * <ol>
	 * <li>[Source Name 1] - The coef's for the first source in one column
	 * <li>[Source Name 2...] - The coef's for the 2nd...
	 * <li>...
	 * </ol>
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param sources	An DataTable list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadSourceReachCoef(Connection conn, long modelId, DataTable sources) throws SQLException,
	IOException {

		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}

		int sourceCount = sources.getRowCount();

		DataTableWritable sourceReachCoef = new SimpleDataTableWritable();
		//new double[reachCount * itCount][sourceCount]);
		// TODO [eric] The query needs to be revised to not retrieve all the
		// iterations at once otherwise it is likely to cause an out-of-memory
		// exception. In previous experience, 62382 reaches * 51 iterations * 5
		// sources * 8 bytes per double primitive ~128M!

		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {

			String query =
				getTextWithParamSubstitution("SelectAllReachCoef", LoadModelPredictData.class,
						"ModelId", modelId, "SourceId", sources.getInt(srcIndex, SOURCE_ID_COL)
				);

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {

				rs = st.executeQuery(query);
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
	 * <h4>Data Columns, sorted by HYDSEQ then IDENTIFIER</h4>
	 *
	 * <p>One row per reach (i = reach index)</p>
	 * For data definitions, please see:
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
	 * <li>[Source Name 1] - The values for the first source in one column
	 * <li>[Source Name 2...] - The values for the 2nd...
	 * <li>...
	 * </ol>
	 *
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param sources	An DataTable list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadSourceValues(Connection conn, long modelId, DataTable sources) throws SQLException,
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
					"ModelId", modelId, "SourceId", sources.getInt(0, SOURCE_ID_COL)
			);
		loadIndexValues(conn, sourceValues, rowIdQuery, "Identifier");

		for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {
			String constituent = sources.getString(srcIndex, sources.getColumnByName("CONSTITUENT"));
			String units = sources.getString(srcIndex, sources.getColumnByName("UNITS"));
			String precision = sources.getString(srcIndex, sources.getColumnByName("PRECISION"));

			String query = getTextWithParamSubstitution("SelectSourceValues", LoadModelPredictData.class,
						"ModelId", modelId, "SourceId", sources.getInt(srcIndex, SOURCE_ID_COL)
				);

			//The query has two columns and we only want the Value column
			query = "Select Value from ( " + query + " )";

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {
				StandardNumberColumnDataWritable<Double> column = new StandardNumberColumnDataWritable<Double>(headings[srcIndex], units);
				column.setProperty("constituent", constituent);
				column.setProperty("precision", precision);
				sourceValues.addColumn(column);

				rs = st.executeQuery(query);
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
	
	protected static void loadIndexValues(Connection conn, DataTableWritable table,
			String baseQuery, String indexColumnName) throws SQLException {
		//Grab the query for the first source, but only taking the ID vals

		String query = "Select " + indexColumnName + " from ( " + baseQuery + " )";

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(2000);
		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
			DLUtils.loadIndex(rs, table, 0);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}

	public boolean isLoadBootstrap() {
		return this.bootstrap;
	}

	public void setLoadBootstrap(boolean dataOnly) {
		this.bootstrap = dataOnly;
	}

	public Long getModelId() {
		return this.modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

}
