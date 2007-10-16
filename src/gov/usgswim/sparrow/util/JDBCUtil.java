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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class JDBCUtil {
	protected static Logger log = Logger.getLogger(LoadTestRunner.class); //logging for this class
	
	public JDBCUtil() {
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
			throws SQLException {
		
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
			throws SQLException {
		
		PredictDataBuilder dataSet = new PredictDataBuilder();
		
		dataSet.setSrcIds( loadSourceIds(conn, modelId));
		dataSet.setSys( loadSystemInfo(conn, modelId) );
		dataSet.setTopo( loadTopo(conn, modelId) );
		dataSet.setCoef( loadSourceReachCoef(conn, modelId, dataSet.getSrcIds()) );
		dataSet.setDecay( loadDecay(conn, modelId, 0) );
		dataSet.setSrc( loadSourceValues(conn, modelId, dataSet.getSrcIds()) );
		
		return dataSet.getImmutable();
	}
	
	public static List<ModelBuilder> loadModelMetaData(Connection conn) throws SQLException {
		List<ModelBuilder> models = new ArrayList<ModelBuilder>(23);
		
		String selectModels =
			"SELECT " +
			"SPARROW_MODEL_ID,IS_APPROVED,IS_PUBLIC,IS_ARCHIVED,NAME,DESCRIPTION," +
			"DATE_ADDED,CONTACT_ID,ENH_NETWORK_ID,URL," +
			"BOUND_NORTH,BOUND_EAST,BOUND_SOUTH,BOUND_WEST " +
			"FROM SPARROW_MODEL ORDER BY SPARROW_MODEL_ID";
			
		String selectSources =
			"SELECT " +
			"SOURCE_ID,NAME,DESCRIPTION,SORT_ORDER,SPARROW_MODEL_ID,IDENTIFIER,DISPLAY_NAME " +
			"FROM SOURCE ORDER BY SPARROW_MODEL_ID, SORT_ORDER";
		
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
	 * Deletes an entire model from the database.
	 *
	 * If keepModelRecord is true, all model data is deleted, but the model
	 * record in the SPARROW_MODEL table is preserved.  This is intended to
	 * allow model data to be reloaded w/o having to reinsert a model record.
	 *
	 * The strategy is to delete by source, since that provides smaller chunks to
	 * delete.  The final clean-up is then to delete the reaches.
	 *
	 * @param modelId	The id of the model to be deleted.
	 * @param keepModelRecord If true, the model record in SPARROW_MODEL is kept.
	 */
	public static void deleteModel(Connection conn, long modelId, boolean keepModelRecord) throws SQLException {
		
		String  listSourceIds = "SELECT SOURCE_ID FROM SOURCE WHERE SPARROW_MODEL_ID = " + modelId;
		
		
		String rmModel = "DELETE FROM SPARROW_MODEL WHERE SPARROW_MODEL_ID = ?";
		PreparedStatement rmModelStmt = null;
		
		String rmReaches = "DELETE FROM MODEL_REACH WHERE SPARROW_MODEL_ID = ?";
		PreparedStatement rmReachesStmt = null;
		
		String rmSource = "DELETE FROM SOURCE WHERE SOURCE_ID = ?";
		PreparedStatement rmSourceStmt = null;
		
		Data2D srcIds = readAsInteger(conn, listSourceIds, 100);
		
		//Delete each source (Cascades to lots of related data)
		try {
			rmSourceStmt = conn.prepareStatement(rmSource);
			log.debug("Deleting model " + modelId +  " with " + srcIds.getRowCount() + " sources.");
			for (int i = 0; i < srcIds.getRowCount(); i++)  {
				int srcId = srcIds.getInt(i, 0);
				rmSourceStmt.setInt(1, srcId);
				int cnt = rmSourceStmt.executeUpdate();
				log.debug("Source #" + (i + 1) + " deleted (Source ID = '" + srcId + "')");
			}
		} finally {
			try {
				rmSourceStmt.close();
			} catch (SQLException e) {
				log.error("Exception while closing statement", e);
			}
		}
		
		//Delete all the reaches in one shot (Cascades to some related data)
		try {
			rmReachesStmt = conn.prepareStatement(rmReaches);
			log.debug("Deleting all model reaches...");
			rmReachesStmt.setInt(1, (int) modelId);
			int cnt = rmReachesStmt.executeUpdate();
			log.debug("Reaches deleted.  " + cnt + " records.");
		} finally {
			try {
				rmReachesStmt.close();
			} catch (SQLException e) {
				log.error("Exception while closing statement", e);
			}
		}
		
		//Optionally, delete the model record.
		if (!keepModelRecord) {
			try {
				rmModelStmt = conn.prepareStatement(rmModel);
				log.debug("Deleting all model record...");
				rmModelStmt.setInt(1, (int) modelId);
				int cnt = rmModelStmt.executeUpdate();
				log.debug("Model deleted.  " + cnt + " records.");
			} finally {
				try {
					rmModelStmt.close();
				} catch (SQLException e) {
					log.error("Exception while closing statement", e);
				}
			}
		} else {
			log.debug("Model record kept.");
		}

	}
	
	/**
	 * Loads all the model reach in the passed PredictionDataSet into the
	 * SPARROW_DSS.MODEL_REACH table.
	 * 
	 * A Map is returned that maps the reach identifier (the key) to the database
	 * MODEL_REACH_ID from the MODEL_REACH table.  Both values are Integer's.
	 * 
	 * @param data
	 * @param conn
	 * @param batchSize
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, Integer> writeModelReaches(PredictData data, Connection conn, int batchSize)
				throws SQLException {
		
		//return value
		Map<Integer, Integer> modelIdMap;		//Maps IDENTIFIER(key) to the db MODEL_REACH_ID(value)
		
		//STATS
		//These three should total to the number of reaches
		int stdIdMatchCount = 0;	//Number of reaches where the STD_ID matched a enh reach
		int stdIdNullCount = 0;		//Number of reaches where the STD_ID is null (actually, counting zero as null)
		int stdIdNotMatched = 0;	//Number of reaches where the STD_ID is assigned, but not matched.
		
		//Queries and PreparedStatements
		String enhReachQuery = "SELECT IDENTIFIER, ENH_REACH_ID FROM STREAM_NETWORK.ENH_REACH WHERE ENH_NETWORK_ID = " + data.getModel().getEnhNetworkId().longValue();
		Map<Integer, Integer> enhIdMap = buildIntegerMap(conn, enhReachQuery);
		
    String insertReachStr = "INSERT INTO MODEL_REACH (IDENTIFIER, FULL_IDENTIFIER, HYDSEQ, IFTRAN, ENH_REACH_ID, SPARROW_MODEL_ID)" +
                   " VALUES (?,?,?,?,?," + data.getModel().getId().longValue() + ")";                 
    PreparedStatement insertReach = conn.prepareStatement(insertReachStr);
		
		String selectAlleachesQuery = "SELECT IDENTIFIER, MODEL_REACH_ID FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " + data.getModel().getId();	
		
    String insertReachTopoStr = "INSERT INTO MODEL_REACH_TOPO (MODEL_REACH_ID, FNODE, TNODE, IFTRAN) VALUES (?,?,?,?)";
    PreparedStatement insertReachTopo = conn.prepareStatement(insertReachTopoStr);
  
		
    Data2D ancil = data.getAncil();
    Data2D topo = data.getTopo();
		int modelRows = topo.getRowCount();	//# of reaches in model
		int currentBatchCount = 0;	//number of statements added to the current batch
		
    //ancillary headings indexes
    int localIdIndexAnc = ancil.findHeading("local_id");
		int stdIdIndexAnc = ancil.findHeading("std_id");
		int localSameIndexAnc = ancil.findHeading("local_same");
    
		if (localIdIndexAnc < 0) throw new IllegalStateException("local_id heading not found in ancil.txt");
    if (stdIdIndexAnc < 0) throw new IllegalStateException("std_id heading not found in ancil.txt");
		if (localSameIndexAnc < 0) throw new IllegalStateException("local_same heading not found in ancil.txt");
    
    //topographic headings indexes
		int hydseqIndexTopo = topo.findHeading("hydseq");
    int fnodeIndexTopo = topo.findHeading("fnode");
    int tnodeIndexTopo = topo.findHeading("tnode");
		int iftranIndexTopo = topo.findHeading("iftran");
  
    if (hydseqIndexTopo < 0) throw new IllegalStateException("hydseq heading not found in topo.txt");
    if (fnodeIndexTopo < 0) throw new IllegalStateException("fnode heading not found in topo.txt");
    if (tnodeIndexTopo < 0) throw new IllegalStateException("tnode heading not found in topo.txt");
		if (iftranIndexTopo < 0) throw new IllegalStateException("iftran heading not found in topo.txt");
		
		//try block only has a finally clause to ensure statements close
		try {
			log.debug("Adding reaches in batch size of: " + batchSize);
			
			//
			//Insert rows into the MODEL_REACH table.  Total rows inserted == modelRows
			for (int r = 0; r < modelRows; r++)  {
				int identifier = ancil.getInt(r,localIdIndexAnc);	//the reach identifier
				int stdIdentifier = ancil.getInt(r,stdIdIndexAnc);	//the standard identifier
				
				insertReach.setInt(1, identifier);  //identifier
				insertReach.setString(2, Integer.toString(identifier));  //full_identifier
				insertReach.setInt(3, topo.getInt(r,hydseqIndexTopo));  //hydseq
				insertReach.setInt(4, topo.getInt(r,iftranIndexTopo));   //iftran
				
				
				//Assign the enh_reach_id if its found
				if (stdIdentifier == 0) {
					//this is considered null - the STD_ID is not assigned.
					stdIdNullCount++;
				} else if (enhIdMap.containsKey(stdIdentifier)) {
					insertReach.setInt(5, enhIdMap.get(stdIdentifier) );
					stdIdMatchCount++;
				} else {
					insertReach.setNull(5, Types.INTEGER);
					stdIdNotMatched++;
					//TODO need to consult the LOCAL_MATCH PARAM
				}
				
				insertReach.addBatch();
				currentBatchCount++;
				
				if (currentBatchCount >= batchSize) {
					insertReach.executeBatch();
					currentBatchCount = 0;
					if (log.isDebugEnabled()) {
						System.out.print(".");
					}
				}
			}
			
			//Execute remaining batches
			if (currentBatchCount != 0) insertReach.executeBatch();
			
			log.debug("\nReach loading is complete.  Total reaches was " + modelRows + " split up as:");
			log.debug("Reaches that had matched Standard IDs: " + stdIdMatchCount);
			log.debug("Reaches that did not have a Standard ID assigned: " + stdIdNullCount);
			log.debug("Reaches that had a Standard ID that could not be matched (ERROR): " + stdIdNotMatched);
			
			//
			// Load all inserted db row into a Map that maps IDENTIFIER(key) to the db MODEL_REACH_ID(value)
			// We need to use these values to load topo data, and we also return this map.
			modelIdMap = buildIntegerMap(conn, selectAlleachesQuery);
			
			//Test loaded reach count
			if (modelIdMap.size() != modelRows) {
				log.error("The number of reaches in the db does not equal the number loaded!!");
				throw new IllegalStateException("The number of reaches in the db does not equal the number loaded!!");
			}
			
			
			/********************************************
			 *  MODEL_REACH_TOPO INSERT
			 *********************************************/
			log.debug("Adding reach topo rows in batch size of: " + batchSize);
			currentBatchCount = 0;	//reset batch counter for use in another loop
			
			for (int r = 0; r < modelRows; r++)  {
				insertReachTopo.setInt(1, modelIdMap.get(ancil.getInt(r,localIdIndexAnc))); //model reach id
				insertReachTopo.setInt(2, topo.getInt(r,fnodeIndexTopo));  //fnode
				insertReachTopo.setInt(3, topo.getInt(r,tnodeIndexTopo));   //tnode
				insertReachTopo.setInt(4, topo.getInt(r,iftranIndexTopo));   //iftran
				
				insertReach.addBatch();
				currentBatchCount++;
				
				if (currentBatchCount >= batchSize) {
					insertReachTopo.executeBatch();
					currentBatchCount = 0;
					if (log.isDebugEnabled()) {
						System.out.print(".");
					}
				}
			}
			
			if (currentBatchCount != 0) insertReachTopo.executeBatch();
			log.debug("\n");
			
		} finally {
			//Close all STATEMENTS - ignore errors
			try {
				insertReach.close();
				insertReachTopo.close();
				insertReachTopo.close();
			} catch (Exception e) {
				log.warn("Error attempting to close prepared statement", e);
			}
		}

		
		return modelIdMap;
	}
	
	/**
	 * Loads all the model reach in the passed PredictionDataSet into the
	 * SPARROW_DSS.MODEL_REACH table.
	 * 
	 * A Map is returned that maps the source identifier (the key) to the database
	 * SOURCE_ID from the SOURCE table.  Both values are Integer's.
	 * The source IDENTIFIER is 1 based, and follows the column order of the
	 * sources in the text file.  So, the first source column has an identifier
	 * of 1 and so on.
	 * 
	 * @param data
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, Integer> writeModelSources(PredictData data, Connection conn)
				throws SQLException {
		log.debug("Adding sources (one batch)");
		
		//return value
		Map<Integer, Integer> sourceIdMap;		//Maps IDENTIFIER(key) to the db SOURCE_ID(value)
		
    String[] headers = data.getSrc().getHeadings();
    
    String insertSrcStr = "INSERT INTO SOURCE (IDENTIFIER, NAME, DISPLAY_NAME, DESCRIPTION, SORT_ORDER, SPARROW_MODEL_ID) " +
                                "VALUES (?,?,?,?,?," + data.getModel().getId() + ")";
	  PreparedStatement insertSrc = conn.prepareStatement(insertSrcStr);
		
		
		String sourceMapStr = "SELECT IDENTIFIER, SOURCE_ID FROM SOURCE WHERE SPARROW_MODEL_ID = " + data.getModel().getId();

		try {
		
			//
			//Insert all Sources in one batch
			for (int i = 0; i < headers.length; i++) {
				insertSrc.setInt(1,(i+1));					//autogenerate the model-specific identifier
				insertSrc.setString(2,headers[i]);	//SPARROW model name for source
				insertSrc.setString(3,headers[i]);	//Human readable version of name (starts out same as above)
				insertSrc.setString(4,headers[i]);	//...and the same for the description
				insertSrc.setInt(5,(i+1));					//sort order matches the initial load order
				insertSrc.addBatch();
			}
			
			insertSrc.executeBatch();	//run all insert batches
			
			//
			//Read all sources back into a map, which is returned
			sourceIdMap = buildIntegerMap(conn, sourceMapStr);
			
			if (sourceIdMap.size() != headers.length) {
				log.error("The number of sources in the db does not match the number loaded!!!");
				throw new IllegalStateException("The number of sources in the db does not match the number loaded!!!");
			}
		} finally {
			try {
				insertSrc.close();
			} catch (Exception e) {
				log.warn("Error attempting to close prepared statement", e);
			}
		}
		
		
		return sourceIdMap;
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
	 * Returns the number of reaches added
	 * @param data
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static int writePredictDataSet(PredictData data, Connection conn, int batchSize)
			throws SQLException {

    //quick access variables for data tables
    Data2D ancil = data.getAncil();
    Data2D topo = data.getTopo();
    Data2D src = data.getSrc();
    Data2D coef = data.getCoef();
    
		//Basic count values for model data
    int modelRowCnt = topo.getRowCount();	//# of reaches in model
    int coefRowCnt = coef.getRowCount();	//# of coefs (reaches * iterations)
		int iterationCnt = coefRowCnt / modelRowCnt;	//# of iterations
		int modelSourceCnt = src.getColCount();	//# of sources in the dataset

		log.debug("- - Model Startup Stats - -");
		log.debug("- Model reach count: " + modelRowCnt);
		log.debug("- Model coef row count: " + coefRowCnt);
		log.debug("- Model iteration count: " + iterationCnt);
		log.debug("- Model source count: " + modelSourceCnt);
		
		//Check:  coefRows should equal modelRows * iterations AND
		//        coefRows should be an even multiple of modelRows
		if ((coefRowCnt != modelRowCnt * iterationCnt) || (coefRowCnt % modelRowCnt != 0)) {
			throw new IllegalArgumentException("Rows in the coef data must be an even multiple of the number of reaches");
		}
		
		//Check:  The coef table must contain 4 columns more then the number of sources
		if (modelSourceCnt != coef.getColCount() - 4) {
			throw new IllegalArgumentException("There should be four more columns in the coef data then the number of sources");
		}
		
		//Check:  ancil, topo, and src should contain modelRowCnt number of rows.
		//Note:  number of model rows is based on topo.
		if (ancil.getRowCount() != modelRowCnt || src.getRowCount() != modelRowCnt) {
			throw new IllegalArgumentException("The number of rows in ancil, src, and topo do not match");
		}
  
		//
		//Named columns                    
  
		//Ancil columns - really just need the local id.
    int localIdIndexAnc = ancil.findHeading("local_id");
		if (localIdIndexAnc < 0) throw new IllegalStateException("local_id heading not found in ancil.txt");
		
		
    //topographic headings indexes
		int hydseqIndexTopo = topo.findHeading("hydseq");
    int fnodeIndexTopo = topo.findHeading("fnode");
    int tnodeIndexTopo = topo.findHeading("tnode");
		int iftranIndexTopo = topo.findHeading("iftran");
  
    if (hydseqIndexTopo < 0) throw new IllegalStateException("hydseq heading not found in topo.txt");
    if (fnodeIndexTopo < 0) throw new IllegalStateException("fnode heading not found in topo.txt");
    if (tnodeIndexTopo < 0) throw new IllegalStateException("tnode heading not found in topo.txt");
		if (iftranIndexTopo < 0) throw new IllegalStateException("iftran heading not found in topo.txt");
  
		/********************************************
		 *  MODEL_REACH and SOURCE
		 *********************************************/
		Map<Integer, Integer> reachDbIdMap = writeModelReaches(data, conn, batchSize);
		Map<Integer, Integer> sourceDbIdMap = writeModelSources(data, conn);
  
		
		/********************************************
		 *  REACH_COEF INSERT - Into the REACH_COEF table
		 *  These are the decay coefs
		 *********************************************/
		{
			log.debug("Adding decay coefs in batch size of: " + batchSize);
			
			String reachCoefStr = "INSERT INTO REACH_COEF (ITERATION, INC_DELIVERY, TOTAL_DELIVERY, BOOT_ERROR, MODEL_REACH_ID) " +
															 "VALUES (?,?,?,?,?)";
			PreparedStatement pstmtInsertReachCoef = conn.prepareStatement(reachCoefStr);
			
			int curReachRow = -1;	//current row of reach (ignoring iterations)
			int localId = -1;	//local id for this reach
			int reachDbId = -1;	//db ID for this reach
			int currentBatchCount = 0;	//number of rows added in this batch
			
			try {
				for (int coefRow=0; coefRow < coefRowCnt; coefRow++) {
				
					curReachRow = coefRow % modelRowCnt;
					localId = ancil.getInt(curReachRow, localIdIndexAnc);
					reachDbId = reachDbIdMap.get(localId);
					
					pstmtInsertReachCoef.setInt(1,coef.getInt(coefRow,0));  //ITER
					pstmtInsertReachCoef.setDouble(2,coef.getDouble(coefRow,1));  //INC_DELIVF
					pstmtInsertReachCoef.setDouble(3,coef.getDouble(coefRow,2));  //TOT_DELIVF
					pstmtInsertReachCoef.setDouble(4,coef.getDouble(coefRow,3));  //BOOT_ERROR
					pstmtInsertReachCoef.setInt(5, reachDbId);  //MODEL_REACH_ID
				 
					pstmtInsertReachCoef.addBatch();
					currentBatchCount++;
					
					if (currentBatchCount >= batchSize) {
						pstmtInsertReachCoef.executeBatch();
						currentBatchCount = 0;
						if (log.isDebugEnabled()) {
							System.out.print(".");
						}
					}
				}	//coef row loop (one for reach * iteration)
				
				if (currentBatchCount != 0) pstmtInsertReachCoef.executeBatch();
			  log.debug("\n");
				
			} finally {
				try {
					pstmtInsertReachCoef.close();
				} catch (Exception e) {
					log.warn("Error attempting to close prepared statement", e);
				}
			}

		}
		
		
	 /********************************************
		*  SOURCE_REACH_COEF INSERT
		*********************************************/
		{
			log.debug("Adding source reach coefs in batch size of: " + batchSize);
			
			String srcReachCoefStr = "INSERT INTO source_reach_coef (ITERATION, VALUE, SOURCE_ID, MODEL_REACH_ID) VALUES " +
																		"(?,?,?,?)";
			PreparedStatement srcReachCoef = conn.prepareStatement(srcReachCoefStr);
			
			int curReachRow = -1;	//current row of reach (ignoring iterations)
			int localId = -1;	//local id for this reach
			int reachDbId = -1;	//db ID for this reach
			int currentBatchCount = 0;	//number of rows added in this batch
			
			try {
				for (int coefRow=0; coefRow < coefRowCnt; coefRow++) {
				
					curReachRow = coefRow % modelRowCnt;
					localId = ancil.getInt(curReachRow, localIdIndexAnc);
					reachDbId = reachDbIdMap.get(localId);
				
				
					//loop to get values (sources) from the fourth column on
					int curSourceId = -1;	//IDENTIFIER of the current source
					int curSourceDbId = -1;	//DB ID of the current source
					for (int srcIndex = 0; srcIndex < modelSourceCnt; srcIndex++) {
					
						curSourceId = srcIndex + 1;	//The source ids are 1 based
						curSourceDbId = sourceDbIdMap.get(curSourceId);

						srcReachCoef.setInt(1,coef.getInt(coefRow,0));  //iteration
						srcReachCoef.setDouble(2, coef.getDouble(coefRow,srcIndex + 4));  //value (4 previous columns)
						srcReachCoef.setInt(3, curSourceDbId);
						srcReachCoef.setInt(4, reachDbId);
						srcReachCoef.addBatch();
						
						currentBatchCount++;
						
						if (currentBatchCount >= batchSize) {
							srcReachCoef.executeBatch();
							currentBatchCount = 0;
							if (log.isDebugEnabled()) {
								System.out.print(".");
							}
						}

					}	//source loop
				
				}	//coef row lop (one per reach * iteration)
				
				if (currentBatchCount != 0) srcReachCoef.executeBatch();
			  log.debug("\n");
				
			} finally {
				try {
					srcReachCoef.close();
				} catch (Exception e) {
					log.warn("Error attempting to close prepared statement", e);
				}
			}
		}
	
	 /********************************************
		*  SOURCE_VALUE INSERT
		*********************************************/
		{
			log.debug("Adding source values in batch size of: " + batchSize);
			
			String srcValueStr = "INSERT INTO source_value (VALUE, SOURCE_ID, MODEL_REACH_ID) VALUES " +
																 "(?,?,?)";
			PreparedStatement srcValue = conn.prepareStatement(srcValueStr);
		
			int currentBatchCount = 0;	//number of rows added in this batch
			
			try {
				//BEGIN TABLE LOADING LOOP...
				for (int curRowIndex = 0; curRowIndex < modelRowCnt; curRowIndex++) {
						
					//Current reach IDs
					int localId = ancil.getInt(curRowIndex, localIdIndexAnc);	//local id for this row
					int reachDbId = reachDbIdMap.get(localId);
						 
					//insert into SOURCE_VALUE -- LOOP THROUGH EACH COLUMN
					//value = get from src Data2D
					int curSourceId = -1;	//IDENTIFIER of the current source
					int curSourceDbId = -1;	//DB ID of the current source
					for (int curSourceIndex = 0; curSourceIndex < modelSourceCnt; curSourceIndex++) {
						curSourceId = curSourceIndex + 1;	//source ids are 1 based
						curSourceDbId = sourceDbIdMap.get(curSourceId); 

						srcValue.setDouble(1, src.getDouble(curRowIndex,curSourceIndex));  //value
						srcValue.setInt(2, curSourceDbId);  //source_id
						srcValue.setInt(3, reachDbId);  //model_reach_id
						
						srcValue.addBatch();
						currentBatchCount++;
						
						if (currentBatchCount >= batchSize) {
							srcValue.executeBatch();
							currentBatchCount = 0;
							if (log.isDebugEnabled()) {
								System.out.print(".");
							}
						}

					}	//source column loop
				}	//model row loop (one per reach)
				
				if (currentBatchCount != 0) srcValue.executeBatch();
			  log.debug("\n");
				
			} finally {
				try {
					srcValue.close();
				} catch (Exception e) {
					log.warn("Error attempting to close prepared statement", e);
				}
			}
		
		}

    
		return modelRowCnt;
	}
	
	
	/**
	 * Returns a Int2D table of all System info
	 * <h4>Data Columns, sorted by HYDSEQ.  One row per reach (i = reach index)</h4>
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
	public static Int2DImm loadSystemInfo(Connection conn, int modelId) throws SQLException {
		String query =
			"SELECT MODEL_REACH_ID as MODEL_REACH, HYDSEQ FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " +  modelId + " ORDER BY HYDSEQ";
	
		return readAsInteger(conn, query, 2000, 0);
		
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
	public static Int2DImm loadTopo(Connection conn, int modelId) throws SQLException {
		String query =
			"SELECT FNODE, TNODE, IFTRAN, HYDSEQ FROM ALL_TOPO_VW WHERE SPARROW_MODEL_ID = " +  modelId + " ORDER BY HYDSEQ";
	
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
	public static Data2D loadSourceReachCoef(Connection conn, int modelId, int iteration, Data2D sources) throws SQLException {
	
		if (iteration < 0) {
			throw new IllegalArgumentException("The iteration cannot be less then zero");
		}
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	
		String reachCountQuery =
			"SELECT COUNT(*) FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelId;
		Data2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Data2DBuilder sourceReachCoef = new Data2DBuilder(new double[reachCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				"SELECT coef.VALUE AS Value " +
				"FROM SOURCE_REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
				"WHERE " +
				"rch.SPARROW_MODEL_ID = " +  modelId + " AND " +
				"coef.Iteration = " +  iteration + " AND " +
				"coef.SOURCE_ID = " +  sources.getInt(srcIndex, 1) + " " +
				"ORDER BY rch.HYDSEQ";
			
			
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
	public static Data2D loadSourceReachCoef(Connection conn, int modelId, Data2D sources) throws SQLException {
	
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	
		String reachCountQuery =
			"SELECT COUNT(*) FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelId;
		String itCountQuery =
			"SELECT COUNT(*) FROM (SELECT DISTINCT coef.Iteration " +
			"FROM SOURCE_REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
			"WHERE rch.SPARROW_MODEL_ID = " +  modelId + ")";
			
		Data2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		Data2D itCountData = readAsInteger(conn, itCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int itCount = itCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Data2DBuilder sourceReachCoef = new Data2DBuilder(new double[reachCount * itCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				"SELECT coef.VALUE AS Value " +
				"FROM SOURCE_REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
				"WHERE " +
				"rch.SPARROW_MODEL_ID = " +  modelId + " AND " +
				"coef.SOURCE_ID = " +  sources.getInt(srcIndex, 0) + " " +
				"ORDER BY coef.Iteration, rch.HYDSEQ";
			
			
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
	public static Double2DImm loadDecay(Connection conn, int modelId, int iteration) throws SQLException {
		String query =
			"SELECT coef.INC_DELIVERY, coef.TOTAL_DELIVERY " +
			"FROM REACH_COEF coef INNER JOIN MODEL_REACH rch ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
			"WHERE rch.SPARROW_MODEL_ID = " +  modelId + " AND coef.ITERATION = " + iteration + " " +
			"ORDER BY rch.HYDSEQ";
	
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
	public static Data2D loadSourceValues(Connection conn, int modelId, Data2D sources) throws SQLException {
	
		if (sources.getRowCount() == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
	
		String reachCountQuery =
			"SELECT COUNT(*) FROM MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelId;
		Data2D reachCountData = readAsInteger(conn, reachCountQuery, 1);
		int reachCount = reachCountData.getInt(0, 0);
		int sourceCount = sources.getRowCount();
		
		Data2DBuilder sourceValue = new Data2DBuilder(new double[reachCount][sourceCount]);
		
	
		for (int srcIndex=0; srcIndex<sourceCount; srcIndex++) {
		
			String query =
				"SELECT src.VALUE AS Value " +
				"FROM SOURCE_VALUE src INNER JOIN MODEL_REACH rch ON src.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
				"WHERE " +
				"rch.SPARROW_MODEL_ID = " +  modelId + " AND " +
				"src.SOURCE_ID = " +  sources.getInt(srcIndex, 1) + " " +
				"ORDER BY rch.HYDSEQ";
			
			
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
	public static Int2DImm loadSourceIds(Connection conn, int modelId) throws SQLException {
		String query =
			"SELECT IDENTIFIER, SOURCE_ID FROM SOURCE WHERE SPARROW_MODEL_ID = " +  modelId + " ORDER BY SORT_ORDER";
	
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
		
		Int2DImm data2D = new Int2DImm(data, headings, indexCol);
		
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
}
