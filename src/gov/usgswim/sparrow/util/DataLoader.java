package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DBuilder;
import gov.usgswim.sparrow.Data2DWritable;
import gov.usgswim.sparrow.Double2DImm;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.Int2DImm;

import gov.usgswim.sparrow.PredictDataBuilder;

import gov.usgswim.sparrow.domain.ModelBuilder;

import gov.usgswim.sparrow.domain.SourceBuilder;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.Statement;

import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Reads in data from the db to Data2D instances.
 * 
 * The methods in this class pull the SQL query strings from the associated
 * DataLoader.properties file.  The values in this file are read fresh for
 * each invocation, so updates will be visible.  Tests show that it takes
 * somewhere between .25ms to .5ms to dynamically load a string from the
 * properties file in this way.
 */
public class DataLoader {
	protected static Logger log = Logger.getLogger(LoadTestRunner.class); //logging for this class
	
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
		
		dataSet.setSrcIds( loadSourceIds(conn, modelId));
		dataSet.setSys( loadSystemInfo(conn, modelId) );
		dataSet.setTopo( loadTopo(conn, modelId) );
		dataSet.setCoef( loadSourceReachCoef(conn, modelId, 0, dataSet.getSrcIds()) );
		dataSet.setDecay( loadDecay(conn, modelId, 0) );
		dataSet.setSrc( loadSourceValues(conn, modelId, dataSet.getSrcIds()) );
		
		return dataSet.getImmutable();
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
		
		dataSet.setSrcIds( loadSourceIds(conn, modelId));
		dataSet.setSys( loadSystemInfo(conn, modelId) );
		dataSet.setTopo( loadTopo(conn, modelId) );
		dataSet.setCoef( loadSourceReachCoef(conn, modelId, dataSet.getSrcIds()) );
		dataSet.setDecay( loadDecay(conn, modelId, 0) );
		dataSet.setSrc( loadSourceValues(conn, modelId, dataSet.getSrcIds()) );
		
		return dataSet.getImmutable();
	}
	
	public static List<ModelBuilder> loadModelMetaData(Connection conn) throws SQLException,
																																						 IOException {
		List<ModelBuilder> models = new ArrayList<ModelBuilder>(23);
		
		String selectModels = getQuery("SelectAllModels");
		String selectSources = getQuery("SelectAllSources");
		
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
			
			try {
				rset = stmt.executeQuery(selectSources);
				int modelIndex = 0;
				
				while (rset.next()) {
					SourceBuilder s = new SourceBuilder();
					s.setId(rset.getLong("SOURCE_ID"));
					s.setName(rset.getString("NAME"));
					s.setDescription(rset.getString("DESCRIPTION"));
					s.setSortOrder(rset.getInt("SORT_ORDER"));
					s.setModelId(rset.getLong("SPARROW_MODEL_ID"));
					s.setIdentifier(rset.getInt("IDENTIFIER"));
					s.setDisplayName(rset.getString("DISPLAY_NAME"));
					
					//The models and sources are sorted by model_id, so scroll forward
					//thru the models until we find the correct one.
					while (
								(models.get(modelIndex).getId() != s.getModelId()) &&
								(modelIndex < models.size()) /* don't scoll past last model*/ )  {
						modelIndex++;
					}
					
					if (modelIndex < models.size()) {
						models.get(modelIndex).addSource(s);
					} else {
						log.warn("Found sources not matched to a model.  Likely caused by record insertion during the queries.");
					}
				}
				
			} finally {
				rset.close();
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
		Data2D data = readAsInteger(conn, query, 1000);
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
	 * Returns a Int2D table of all System info
	 * <h4>Data Columns, sorted by HYDSEQ.  One row per reach (i = reach index)</h4>
	 * <p>Row IDs duplicate the Reach Ids in column zero.</p>
	 * <ol>
	 * <li>[i][0] REACH_ID - The system id for the reach (db unique id)
	 * <li>[i][1] HYDSEQ - The model specific hydrological sequence number
	 * </ol>
	 *
	 * TODO:  WE sort by HYDSEQ HERE - THAT IS REALLY BAD, SINCE THERE ARE DUPLICATES.
	 *
	 * @param conn A JDBC Connection to run the query on
	 * @param modelId The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Int2DImm loadSystemInfo(Connection conn, long modelId) throws SQLException,
																														IOException {
		String query = getQuery("SelectSystemData", modelId);
	
		Int2DImm data = readAsInteger(conn, query, 2000, -1);
		int[] ids = data.getIntColumn(0);
		return new Int2DImm(data.getIntData(), data.getHeadings(), 0, ids);
		
	}
	
	/**
	 * Returns a Int2D table of all topo data for for a single model.
	 * <h4>Data Columns (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>FNODE - The from node
	 * <li>TNODE - The to node
	 * <li>IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Int2DImm loadTopo(Connection conn, long modelId) throws SQLException,
																											IOException {
		String query = getQuery("SelectTopoData", modelId);
			
	
		return readAsInteger(conn, query, 1000);
	}
	
	/**
	 * Returns a Double2D table of all source/reach coef's for for a single iteration of a model.
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
	 * @param sources	An Int2D list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Data2D loadSourceReachCoef(Connection conn, long modelId, int iteration, Data2D sources) throws SQLException,
																																								 IOException {
	
		if (iteration < 0) {
			throw new IllegalArgumentException("The iteration cannot be less then zero");
		}
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	

		String reachCountQuery = getQuery("SelectReachCount", modelId);
		
		Data2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Data2DBuilder sourceReachCoef = new Data2DBuilder(new double[reachCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			
			String query =
				getQuery("SelectReachCoef", new Object[] {
					"ModelId", modelId, "Iteration", iteration, "SourceId", sources.getInt(srcIndex, 1)
				});
			
			
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;
			
			try {
			
				rs = st.executeQuery(query);
				loadColumn(rs, sourceReachCoef, 0, srcIndex);
				
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
			
		}
		
		return sourceReachCoef.buildDoubleImmutable(-1);

	}
	
	/**
	 * Returns a Double2D table of all source/reach coef's for for all iterationa of a model.
	 * <h4>Data Columns with one row per reach (sorted by ITERATION then HYDSEQ)</h4>
	 * <ol>
	 * <li>[Source Name 1] - The coef's for the first source in one column
	 * <li>[Source Name 2...] - The coef's for the 2nd...
	 * <li>...
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param sources	An Int2D list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Data2D loadSourceReachCoef(Connection conn, long modelId, Data2D sources) throws SQLException,
																																	IOException {
	
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	
		String reachCountQuery = getQuery("SelectReachCount", modelId);

		String itCountQuery = getQuery("SelectIterationCount", modelId);
			
		Data2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		Data2D itCountData = readAsInteger(conn, itCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int itCount = itCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Data2DBuilder sourceReachCoef = new Data2DBuilder(new double[reachCount * itCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				getQuery("SelectAllReachCoef", new Object[] {
					"ModelId", modelId, "SourceId", sources.getInt(srcIndex, 0)
				});

			
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;
			
			try {
			
				rs = st.executeQuery(query);
				loadColumn(rs, sourceReachCoef, 0, srcIndex);
				
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
			
		}
		
		return sourceReachCoef.buildDoubleImmutable(-1);

	}

	
	/**
	 * Returns a Double2D table of all decay data for for a single model.
	 * 
	 * <h4>Data Columns, sorted by HYDSEQ</h4>
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream decay at reach i.<br>
	 *   This decay is assumed to be at mid-reach and already computed as such.
	 *   That is, it would normally be the sqr root of the instream decay, and
	 *   it is assumed that this value already has the square root taken.
	 * <li>src[i][1] == the upstream decay at reach i.<br>
	 *   This decay is applied to the load coming from the upstream node.
	 * <li>Additional columns ignored
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param iteration The iteration for which coef's should be returned.  Zero is the nominal value - all others are for bootstrapping.
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Double2DImm loadDecay(Connection conn, long modelId, int iteration) throws SQLException,
																														IOException {

		String query = getQuery("SelectDecayCoef", new Object[] {
				"ModelId", modelId, "Iteration", iteration
		});

		return readAsDouble(conn, query, 2000);
		
	}
	
	
	/**
	 * Returns a Double2D table of all source values for for a single model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>[Source Name 1] - The values for the first source in one column
	 * <li>[Source Name 2...] - The values for the 2nd...
	 * <li>...
	 * </ol>
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @param sources	An Int2D list of the sources for the model, in one column (see loadSource)
	 * @return Fetched data - see Data Columns above.
	 * @throws SQLException
	 */
	public static Data2D loadSourceValues(Connection conn, long modelId, Data2D sources) throws SQLException,
																															 IOException {
	
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
		
		String reachCountQuery = getQuery("SelectReachCount", modelId);

		Data2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		//Load column headings using the source display names
		String selectNames = getQuery("SelectSourceNames", modelId);

		String[] headings = new String[sourceCount];
		
		Statement headSt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		headSt.setFetchSize(20);
		ResultSet headRs = null;
		
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
		
		
		Data2DBuilder sourceValue = new Data2DBuilder(new double[reachCount][sourceCount], headings);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				getQuery("SelectSourceValues", new Object[] {
					"ModelId", modelId, "SourceId", sources.getInt(srcIndex, 1)
				});
				
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(2000);
			ResultSet rs = null;
			
			try {
			
				rs = st.executeQuery(query);
				loadColumn(rs, sourceValue, 0, srcIndex);
				
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
			
		}

		return sourceValue.buildDoubleImmutable(-1);

	}
	
	/**
	 * Returns a single column Int2D table of all source IDs for a single model.
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <ol>
	 * <li>SOURCE_ID - The DB ID for the source
	 * </ol>
	 * 
	 * The returned data is indexed on the SOURCE_ID column.
	 * 
	 * @param conn	A JDBC Connection to run the query on
	 * @param modelId	The ID of the Sparrow model
	 * @return	An Int2D object contains the list of source_id's in a single column
	 * @throws SQLException
	 */
	public static Int2DImm loadSourceIds(Connection conn, long modelId) throws SQLException,
																														IOException {
		
		String query = getQuery("SelectSourceIds", modelId);
	
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(1000);

		ResultSet rs = null;
		
		try {
		
			rs = st.executeQuery(query);
			return readAsInteger(rs, 0);
			
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
	
	/**
	 * Loads a single column from the resultSet source to the Double2D destination table.
	 * For consistency, the from and to columns are ZERO INDEXED in both cases.
	 * 
	 * @param source Resultset to load the data from.  The resultset is assumed to be before the first row.
	 * @param dest The destination Double2D table
	 * @param fromCol The column (zero indexed) in the resultset to load from
	 * @param toCol The column (zero indexed) in the Double2D table to load to
	 * @throws SQLException
	 */
	public static void loadColumn(ResultSet source, Data2DWritable dest, int fromCol, int toCol) throws SQLException {
		
		fromCol++;		//covert to ONE base index
		int currentRow = 0;
		
		while (source.next()){
		
			double d = source.getDouble(fromCol);
			dest.setValueAt(new Double(d), currentRow,  toCol);
			currentRow++;
			
		}

	}
	
	
	/**
	 * Creates an unindexed Int2DImm table from the passed query.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static Int2DImm readAsInteger(Connection conn, String query, int fetchSize) throws SQLException {
		return readAsInteger(conn, query, fetchSize, -1);
	}
	
	/**
	 * Creates an Int2DImm table from the passed query with an optional index.
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
	public static Int2DImm readAsInteger(Connection conn, String query, int fetchSize, int indexCol) throws SQLException {
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
	 * Creates an unindexed Int2DImm table from the passed resultset.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static Int2DImm readAsInteger(ResultSet source) throws SQLException {
		return readAsInteger(source, -1);
	}
	
	/**
	 * Creates an Int2DImm table from the passed resultset with an optional index.
	 * 
	 * All values in the source must be convertable to an integer.
	 * 
	 * @param source
	 * @param indexCol A valid column index or -1 to indicate no index
	 * @return
	 * @throws SQLException
	 */
	public static Int2DImm readAsInteger(ResultSet source, int indexCol) throws SQLException {
			
		ArrayList list = new ArrayList(500);
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
		
		Int2DImm data2D = new Int2DImm(data, headings, indexCol, null);
		
		return data2D;
	}
	
	
	/**
	 * Creates a Double2DImm table from the passed query.
	 * 
	 * All values in the source must be convertable to a double.
	 * 
	 * @param conn	A connection to use for the query
	 * @param query The query to run
	 * @param fetchSize Number of rows returned per fetch - use large values (1000+ for queries w/ few columns)
	 * @return
	 * @throws SQLException
	 */
	public static Double2DImm readAsDouble(Connection conn, String query, int fetchSize) throws SQLException {
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
	 * Creates a Double2DImm table from the passed resultset.
	 * 
	 * All values in the source must be convertable to a double.
	 * 
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	public static Double2DImm readAsDouble(ResultSet source) throws SQLException {
			
		ArrayList list = new ArrayList(500);
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
		
		Double2DImm data2D = new Double2DImm(data, headings);
		
		return data2D;
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
	public static String getQuery(String name, Object[] params) throws IOException {
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
