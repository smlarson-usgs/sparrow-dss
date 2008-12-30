package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.SourceBuilder;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
/**
 * Reads in data from the db to DataTable instances.
 * 
 * The methods in this class pull the SQL query strings from the associated
 * DataLoader.properties file.  The values in this file are read fresh for
 * each invocation, so updates will be visible.  Tests show that it takes
 * somewhere between .25ms to .5ms to dynamically load a string from the
 * properties file in this way.
 */
public class DataLoader {


	protected static Logger log = Logger.getLogger(LoadTestRunner.class); //logging for this class
	public static int DO_NOT_INDEX = -1;
	public static final int SOURCE_ID_COL = 0;

	public DataLoader() {
	}

	/**
	 * Loads only the data required to run a prediction.
	 * 
	 * 
	 * @param conn
	 * @param modelId
	 * @return
	 * @throws SQLException
	 */
	//TODO This should be renamed 'loadMinimalModelDataSet'
	public static PredictData loadMinimalPredictDataSet(Connection conn, int modelId)
	throws SQLException,
	IOException {
		PredictDataBuilder dataSet = new PredictDataBuilder();
		try {

			dataSet.setSrcMetadata( loadSrcMetadata(conn, modelId));
			dataSet.setSys( loadSystemInfo(conn, modelId) );
			dataSet.setTopo( loadTopo(conn, modelId) );
			dataSet.setCoef( loadSourceReachCoef(conn, modelId, 0, dataSet.getSrcMetadata()) );
			dataSet.setDecay( loadDecay(conn, modelId, 0) );
			dataSet.setSrc( loadSourceValues(conn, modelId, dataSet.getSrcMetadata()) );

		} catch (Exception e) {
			log.error(DataLoader.class.getSimpleName() + ".loadMinimalPredictDataSet() failed with error:", e);
		} finally {
			SharedApplication.closeConnection(conn, null);
		}
		return dataSet.toImmutable();
	}

	/**
	 * Loads the complete dataset for a model (including bootstrap data).
	 * 
	 * @param conn
	 * @param modelId
	 * @return
	 * @throws SQLException
	 * 
	 * TODO:  This should load the model as well....
	 */
	public static PredictData loadFullModelDataSet(Connection conn, int modelId)
	throws SQLException,
	IOException {

		PredictDataBuilder dataSet = new PredictDataBuilder();

		dataSet.setSrcMetadata( loadSrcMetadata(conn, modelId));
		dataSet.setSys( loadSystemInfo(conn, modelId) );
		dataSet.setTopo( loadTopo(conn, modelId) );
		dataSet.setCoef( loadSourceReachCoef(conn, modelId, dataSet.getSrcMetadata()) );
		dataSet.setDecay( loadDecay(conn, modelId, 0) );
		dataSet.setSrc( loadSourceValues(conn, modelId, dataSet.getSrcMetadata()) );

		return dataSet.toImmutable();
	}

	/**
	 * Convenience method for returning all public, approved models and their
	 * sources.
	 * 
	 * @param conn The JDBC connection object.
	 * @return All public, approved models and their sources.
	 */
	public static List<ModelBuilder> loadModelMetaData(Connection conn)
	throws SQLException, IOException {
		return loadModelMetaData(conn, true, true, false, true);
	}

	/**
	 * Returns all models that meet the specified criteria.  Note that the
	 * {@code isApproved}, {@code isPublic}, and {@code isArchived} criteria are
	 * ANDed together when retrieving models.  For example, specifying
	 * {@code isApproved} = {@code true},
	 * {@code isPublic} = {@code true},
	 * {@code isArchived} = {@code false}
	 * will return models that are approved and public, but not archived.
	 * 
	 * @param conn The JDBC connection object.
	 * @param isApproved Whether or not to return approved models.
	 * @param isPublic Whether or not to return public models.
	 * @param isArchived Whether or not to return archived models.
	 * @param getSources Whether or not to attach the model's sources.
	 * @return All models that meet the specified criteria.
	 */
	public static List<ModelBuilder> loadModelMetaData(Connection conn,
			boolean isApproved, boolean isPublic, boolean isArchived,
			boolean getSources) throws SQLException, IOException {

		List<ModelBuilder> models = new ArrayList<ModelBuilder>(23);

		// Build filtering parameters and retrieve the queries from properties
		Object[] params = {
				"IsApproved", (isApproved ? "T" : "F"),
				"IsPublic", (isPublic ? "T" : "F"),
				"IsArchived", (isArchived ? "T" : "F")
		};
		String selectModels = getQuery("SelectModelsByAccess", params);


		Statement stmt = null;
		ResultSet rset = null;

		try {
			stmt = conn.createStatement();
			stmt.setFetchSize(100);

			try {
				rset = stmt.executeQuery(selectModels);

				while (rset.next()) {
					ModelBuilder m = new ModelBuilder();
					m.setId(rset.getLong("SPARROW_MODEL_ID"));
					m.setApproved(StringUtils.equalsIgnoreCase("T", rset.getString("IS_APPROVED")));
					m.setPublic(StringUtils.equalsIgnoreCase("T", rset.getString("IS_PUBLIC")));
					m.setArchived(StringUtils.equalsIgnoreCase("T", rset.getString("IS_ARCHIVED")));
					m.setName(rset.getString("NAME"));
					m.setDescription(rset.getString("DESCRIPTION"));
					m.setDateAdded(rset.getDate("DATE_ADDED"));
					m.setContactId(rset.getLong("CONTACT_ID"));
					m.setEnhNetworkId(rset.getLong("ENH_NETWORK_ID"));
					m.setUrl(rset.getString("URL"));
					m.setNorthBound(rset.getDouble("BOUND_NORTH"));
					m.setEastBound(rset.getDouble("BOUND_EAST"));
					m.setSouthBound(rset.getDouble("BOUND_SOUTH"));
					m.setWestBound(rset.getDouble("BOUND_WEST"));
					models.add(m);
				}
			} finally {
				rset.close();
			}

			if (getSources) {
				String inModelsWhereClause = " ";
				if (!models.isEmpty()) {
					List<Long> modelIds = new ArrayList<Long>();
					for (ModelBuilder model: models) {
						modelIds.add(model.getId());
					}
					inModelsWhereClause = " WHERE SPARROW_MODEL_ID in (" + StringUtils.join(modelIds.toArray(), ", ") + ") ";

				}
				String selectSources = getQuery("SelectAllSources", "InModels", inModelsWhereClause);

				try {
					rset = stmt.executeQuery(selectSources);

					while (rset.next()) {
						SourceBuilder s = new SourceBuilder();
						s.setId(rset.getLong("SOURCE_ID"));
						s.setName(rset.getString("NAME"));
						s.setDescription(rset.getString("DESCRIPTION"));
						s.setSortOrder(rset.getInt("SORT_ORDER"));
						s.setModelId(rset.getLong("SPARROW_MODEL_ID"));
						s.setIdentifier(rset.getInt("IDENTIFIER"));
						s.setDisplayName(rset.getString("DISPLAY_NAME"));
						s.setConstituent(rset.getString("CONSTITUENT"));
						s.setUnits(rset.getString("UNITS"));

						//The models and sources are sorted by model_id, so scroll forward
						//thru the models until we find the correct one.
						int modelIndex = 0;
						while ((modelIndex < models.size() &&
								models.get(modelIndex).getId() != s.getModelId()) /* don't scroll past last model*/) {
							modelIndex++;
						}

						if (modelIndex < models.size()) {
							models.get(modelIndex).addSource(s);
						} else {
							log.warn("Found sources not matched to a model.  Likely caused by record insertion during the queries.");
						}
					}
				} catch(Exception e) {
					e.printStackTrace(System.err);

				} finally {
					rset.close();
				}
			}
		} finally {
			stmt.close();
		}

		return models;
	}


	/**
	 * Turns a query that returns two columns into a Map<Integer, Integer>.
	 * The first column is used as the key, the second column is used as the value.
	 * @param conn
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	private static Map<Integer, Integer> buildIntegerMap(Connection conn, String query) throws SQLException {
		DataTableWritable data = readAsInteger(conn, query, 1000);
		int rows = data.getRowCount();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>((int)(rows * 1.2), 1f);

		for (int r = 0; r < rows; r++)  {
			map.put(
					new Integer(data.getInt(r, 0)),
					new Integer(data.getInt(r, 1))
			);
		}

		return map;
	}


	/**
	 * Returns an ordered DataTable of all REACHes in the MODEL
	 * <h4>Data Columns, sorted by HYDSEQ.  One row per reach (i = reach index)</h4>
	 * <p>Row IDs duplicate the Reach Ids in column zero.</p>
	 * <ol>
	 * <li>[i][0] IDENTIFIER - The model specific ID for this reach
	 * <li>[i][1] REACH_ID - The system id for the reach (db unique id)
	 * </ol>
	 *
	 * Sort by HYDSEQ then IDENTIFIER, since in some cases HYDSEQ is not unique.
	 * [IK] the use of IDENTIFIER has no significance except to guarantee some 
	 * deterministic ordering of the results. Any other attribute would do.
	 *
	 * @param conn A JDBC Connection to run the query on
	 * @param modelId The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTable loadSystemInfo(Connection conn, long modelId) throws SQLException,
	IOException {
		String query = getQuery("SelectSystemData", modelId);

		DataTableWritable data = readAsInteger(conn, query, 2000, DataLoader.DO_NOT_INDEX);
		int[] ids = DataTableUtils.getIntColumn(data, 0);
		return DataTableUtils.setIds(data, ids);

	}

	/**
	 * Returns a DataTable of all topo data for for a single model.
	 * <h4>Data Columns (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>FNODE - The from node
	 * <li>TNODE - The to node
	 * <li>IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>HYDSEQ - Hydrologic sequence order
	 * </ol>
	 * 
	 * TODO:  Is HYDSEQ needed here since it duplicates system info?
	 * [IK] It seems the Topo and SystemInfo are not both needed
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadTopo(Connection conn, long modelId) throws SQLException,
	IOException {
		String query = getQuery("SelectTopoData", modelId);


		return readAsInteger(conn, query, 1000);
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

		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {

			String query =
				getQuery("SelectReachCoef", new Object[] {
						"ModelId", modelId, "Iteration", iteration, "SourceId", sources.getInt(srcIndex, SOURCE_ID_COL)
				});

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {

				rs = st.executeQuery(query);
				sourceReachCoef.addColumn(new StandardNumberColumnDataWritable<Double>());
				loadColumn(rs, sourceReachCoef, 0, srcIndex);

			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}

		}

		return sourceReachCoef;

	}

	/**
	 * Returns a DataTable of all source/reach coef's for for all iterationa of a model.
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
				getQuery("SelectAllReachCoef", new Object[] {
						"ModelId", modelId, "SourceId", sources.getInt(srcIndex, SOURCE_ID_COL)
				});

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {

				rs = st.executeQuery(query);
				sourceReachCoef.addColumn(new StandardNumberColumnDataWritable<Double>());
				loadColumn(rs, sourceReachCoef, 0, srcIndex);

			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
		}

		return sourceReachCoef;

	}


	/**
	 * Returns a DataTable of all decay data for for a single model.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ then IDENTIFIER</h4>
	 * <p><i>Note:  These are actually delivery terms - that is 1/decay</i></p>
	 * 
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[0] == the instream decay at reach i.<br>
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * <li>[1] == the upstream decay at reach i.<br>
	 *   This decay is applied to the load coming from the upstream node.
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static DataTableWritable loadDecay(Connection conn, long modelId, int iteration) throws SQLException,
	IOException {

		String query = getQuery("SelectDecayCoef", new Object[] {
				"ModelId", modelId, "Iteration", iteration
		});

		return readAsDouble(conn, query, 2000);

	}


	/**
	 * Returns a DataTable of all source values for a single model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
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
		String selectNames = getQuery("SelectSourceNames", modelId);

		Statement headSt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		headSt.setFetchSize(20);
		ResultSet headRs = null;

		String[] headings = new String[sourceCount];
		try {
			headRs = headSt.executeQuery(selectNames);
			for (int i = 0; i < sourceCount; i++)  {
				headRs.next();
				headings[i] = headRs.getString(1);
			}
		} finally {
			if (headRs != null) {
				headRs.close();
				headRs = null;
			}
		}

		DataTableWritable sourceValue = new SimpleDataTableWritable();


		for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {
			String constituent = sources.getString(srcIndex, sources.getColumnByName("CONSTITUENT"));
			String units = sources.getString(srcIndex, sources.getColumnByName("UNITS"));
			String precision = sources.getString(srcIndex, sources.getColumnByName("PRECISION"));

			String query =
				getQuery("SelectSourceValues", new Object[] {
						"ModelId", modelId, "SourceId", sources.getInt(srcIndex, SOURCE_ID_COL)
				});

			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;

			try {
				StandardNumberColumnDataWritable<Double> column = new StandardNumberColumnDataWritable<Double>(headings[srcIndex], units);
				column.setProperty("constituent", constituent);
				column.setProperty("precision", precision);
				sourceValue.addColumn(column);

				rs = st.executeQuery(query);
				loadColumn(rs, sourceValue, 0, srcIndex);

			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}

		}

		return sourceValue;

	}

	/**
	 * Returns a single column DataTable of all source IDs for a single model.
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <ol>
	 * <li>IDENTIFIER - The Model specific ID for the source (usually numbered starting w/ 1)
	 * <li>SOURCE_ID - The DB ID for the source
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return	An DataTable object contains the list of source_id's in a single column
	 * @throws SQLException
	 */
	//	public static DataTableWritable loadSourceIds(Connection conn, long modelId) throws SQLException,
	//	IOException {
	//
	//		String query = getQuery("SelectSourceIds", modelId);
	//
	//		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	//		st.setFetchSize(1000);
	//
	//		ResultSet rs = null;
	//
	//		try {
	//
	//			rs = st.executeQuery(query);
	//			return readAsInteger(rs, DO_NOT_INDEX);
	//
	//		} finally {
	//			if (rs != null) {
	//				rs.close();
	//				rs = null;
	//			}
	//		}
	//	}


	/**
	 * Returns metadata about the source types in the model.
	 * 
	 * Typically 5-10 rows per model.
	 *
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <h5>IDENTIFIER - The Row ID (not a column). The Model specific ID for the source (starting w/ 1)</h5>
	 * <ol>
	 * <li>SOURCE_ID - (long) The database unique ID for the source
	 * <li>NAME - (String) The full (long text) name of the source
	 * <li>DISPLAY_NAME - (String) The short name of the source, used for display
	 * <li>DESCRIPTION - (String) A description of the source (could be long)
	 * <li>CONSTITUENT - (String) The name of the Constituent being measured
	 * <li>UNITS - (STring) The units the constituent is measured in
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
	public static DataTableWritable loadSrcMetadata(Connection conn, long modelId)
	throws SQLException, IOException {

		String query = getQuery("SelectSourceData", modelId);

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(30);

		ResultSet rs = null;

		try {
			rs = st.executeQuery(query);
			return DataTableConverter.toDataTable(rs, true);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}


	}


	/**
	 * Loads a single column from the resultSet source to the destination DataTable.
	 * For consistency, the from and to columns are ZERO INDEXED in both cases.
	 * 
	 * @param source Resultset to load the data from.  The resultset is assumed to be before the first row.
	 * @param dest The destination DataTable
	 * @param fromCol The column (zero indexed) in the resultset to load from
	 * @param toCol The column (zero indexed) in the DataTable to load to
	 * @throws SQLException
	 */
	public static void loadColumn(ResultSet source, DataTableWritable dest, int fromCol, int toCol) throws SQLException {

		fromCol++;		//covert to ONE base index
		int currentRow = 0;

		while (source.next()){

			Double d = source.getDouble(fromCol);
			dest.setValue(d, currentRow,  toCol);
			currentRow++;

		}

	}


	/**
	 * Creates an unindexed DataTable from the passed query.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(Connection conn, String query, int fetchSize) throws SQLException {
		return readAsInteger(conn, query, fetchSize, DO_NOT_INDEX);
	}

	/**
	 * Creates an DataTable from the passed query with an optional index.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @param indexCol A valid column index or -1 to indicate no index
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(Connection conn, String query, int fetchSize, int indexCol) throws SQLException {
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;

		try {

			rs = st.executeQuery(query);
			return readAsInteger(rs, indexCol);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}

	/**
	 * Creates an unindexed DataTable from the passed resultset.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(ResultSet source) throws SQLException {
		return readAsInteger(source, DO_NOT_INDEX);
	}

	/**
	 * Creates a DataTable from the passed resultset with an optional index.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param source
	 * @param indexCol A valid column index or -1 to indicate no index
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsInteger(ResultSet source, int indexCol) throws SQLException {

		ArrayList<int[]> list = new ArrayList<int[]>(500);
		String[] headings = null;		

		headings = readHeadings(source.getMetaData());
		int colCount = headings.length; //Number of columns

		while (source.next()){


			int[] row = new int[colCount];

			for (int i=1; i<=colCount; i++) {
				row[i - 1] = source.getInt(i);
			}

			list.add(row);

		}


		//copy the array list to a int[][] array
		int[][] data = new int[list.size()][];
		for (int i = 0; i < data.length; i++)  {
			data[i] = (int[]) list.get(i);
		}

		DataTableWritable result = new SimpleDataTableWritable(data, headings);
		if (indexCol > DO_NOT_INDEX) {
			result.buildIndex(indexCol);
		}

		return result;
	}


	/**
	 * Creates a DataTable from the passed query.
	 * 
	 * All values in the source must be convertable to a double.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsDouble(Connection conn, String query, int fetchSize) throws SQLException {
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(fetchSize);

		ResultSet rs = null;

		try {

			rs = st.executeQuery(query);
			return readAsDouble(rs);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}

	/**
	 * Creates a DataTable from the passed resultset.
	 * 
	 * All values in the source must be convertable to a double.
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable readAsDouble(ResultSet source) throws SQLException {

		ArrayList<double[]> list = new ArrayList<double[]>(500);
		String[] headings = null;

		headings = readHeadings(source.getMetaData());
		int colCount = headings.length; //Number of columns

		while (source.next()){


			double[] row = new double[colCount];

			for (int i=1; i<=colCount; i++) {
				row[i - 1] = source.getDouble(i);
			}

			list.add(row);

		}


		//copy the array list to a double[][] array
		double[][] data = new double[list.size()][];
		for (int i = 0; i < data.length; i++)  {
			data[i] = (double[]) list.get(i);
		}

		DataTableWritable result = new SimpleDataTableWritable(data, headings);

		return result;
	}

	private static String[] readHeadings(ResultSetMetaData meta) throws SQLException {
		int count = meta.getColumnCount();
		String[] headings = new String[count];

		for (int i=1; i<=count; i++) {
			headings[i - 1] = meta.getColumnName(i);
		}

		return headings;

	}


	/**
	 * Loads the named query and inserts the named values passed in params.
	 * 
	 * params are passed in serial pairs as {"name1", "value1", "name2", "value2"}.
	 * toString is called on each item, so it is OK to pass in autobox numerics.
	 * See the DataLoader.properties file for the names of the parameters available
	 * for the requested query.
	 * 
	 * @param name	Name of the query in the properties file
	 * @param params	An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 */
	public static String getQuery(String name, Object... params) throws IOException {
		String query = getQuery(name);

		for (int i=0; i<params.length; i+=2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i+1].toString();

			query = StringUtils.replace(query, n, v);
		}

		return query;
	}

	/**
	 * Loads the named query and inserts the model ID
	 * 
	 * This is a simplified version of getQuery(String name, String[] params) for
	 * the common case where the only parameter is the modelID.
	 * 
	 * @param name	Name of the query in the properties file
	 * @param modelId The ID of the model.
	 * @return
	 * @throws IOException
	 */
	public static String getQuery(String name, long modelId) throws IOException {
		String baseQuery = getQuery(name);

		return StringUtils.replace(baseQuery, "$ModelId$", Long.toString(modelId));
	}

	public static String getQuery(String name) throws IOException {
		Properties props = new Properties();

		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("gov/usgswim/sparrow/util/DataLoader.properties"));

		return props.getProperty(name);
	}
}

