package gov.usgswim.sparrow.util;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.DataTableUtils;
import gov.usgswim.sparrow.PredictData;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author eeverman
 * @deprecated functionality replaced by DataLoader and ModelDataLoader
 */
public abstract class JDBCUtil {
	protected static Logger log = Logger.getLogger(LoadTestRunner.class); //logging for this class
	public static int DO_NOT_INDEX = -1;

	public JDBCUtil() {
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
	 * @param optionKeepModelRecord If true, the model record in SPARROW_MODEL is kept.
	 */
	public static void deleteModel(Connection conn, long modelId, boolean optionKeepModelRecord) throws SQLException {

		String  listSourceIds = "SELECT SOURCE_ID FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SOURCE WHERE SPARROW_MODEL_ID = " + modelId;


		String rmModel = "DELETE FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SPARROW_MODEL WHERE SPARROW_MODEL_ID = ?";
		PreparedStatement rmModelStmt = null;

		String rmReaches = "DELETE FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".MODEL_REACH WHERE SPARROW_MODEL_ID = ?";
		PreparedStatement rmReachesStmt = null;

		String rmSource = "DELETE FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SOURCE WHERE SOURCE_ID = ?";
		PreparedStatement rmSourceStmt = null;

		DataTableWritable srcIds = DataLoader.readAsInteger(conn, listSourceIds, 100);

		//Delete each source (Cascades to lots of related data)
		try {
			rmSourceStmt = conn.prepareStatement(rmSource);
			log.debug("deleteModel(): Deleting model " + modelId +  " with " + srcIds.getRowCount() + " sources.");
			for (int i = 0; i < srcIds.getRowCount(); i++)  {
				int srcId = srcIds.getInt(i, 0);
				rmSourceStmt.setInt(1, srcId);
				//int cnt = rmSourceStmt.executeUpdate();
				log.debug("deleteModel(): Source #" + (i + 1) + " deleted (Source ID = '" + srcId + "')");
			}
		} finally {
			try {
				rmSourceStmt.close();
			} catch (SQLException e) {
				log.error("deleteModel(): Exception while closing statement", e);
			}
		}

		//Delete all the reaches in one shot (Cascades to some related data)
		try {
			rmReachesStmt = conn.prepareStatement(rmReaches);
			log.debug("deleteModel(): Deleting all model reaches...");
			rmReachesStmt.setInt(1, (int) modelId);
			int cnt = rmReachesStmt.executeUpdate();
			log.debug("deleteModel(): Reaches deleted.  " + cnt + " records.");
		} finally {
			try {
				rmReachesStmt.close();
			} catch (SQLException e) {
				log.error("deleteModel(): Exception while closing statement", e);
			}
		}

		//Optionally, delete the model record.
		if (!optionKeepModelRecord) {
			try {
				rmModelStmt = conn.prepareStatement(rmModel);
				log.debug("deleteModel(): Deleting all model record...");
				rmModelStmt.setInt(1, (int) modelId);
				int cnt = rmModelStmt.executeUpdate();
				log.debug("deleteModel(): Model deleted.  " + cnt + " records.");
			} finally {
				try {
					rmModelStmt.close();
				} catch (SQLException e) {
					log.error("deleteModel(): Exception while closing statement", e);
				}
			}
		} else {
			log.debug("deleteModel(): Model record kept.");
		}

	}

	
	public static void write(ResultSet rs, FileWriter fw) throws SQLException, IOException {
		while (rs.next()) {
			String row = "";
			ResultSetMetaData metadata = rs.getMetaData();
			for (int i=0; i < metadata.getColumnCount(); i++) {
				row += rs.getString(i);
			}
			fw.write(row);
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
		String enhReachQuery = "SELECT IDENTIFIER, ENH_REACH_ID FROM " + SparrowSchemaConstants.NETWORK_SCHEMA + ".ENH_REACH WHERE ENH_NETWORK_ID = " 
			+ data.getModel().getEnhNetworkId().longValue();
		Map<Integer, Integer> enhIdMap = buildIntegerMap(conn, enhReachQuery);

		String insertReachStr = "INSERT INTO " + SparrowSchemaConstants.SPARROW_SCHEMA + ".MODEL_REACH (IDENTIFIER, FULL_IDENTIFIER, HYDSEQ, IFTRAN, ENH_REACH_ID, SPARROW_MODEL_ID)"
			+ " VALUES (?,?,?,?,?," + data.getModel().getId().longValue() + ")";                 
		PreparedStatement insertReach = conn.prepareStatement(insertReachStr);

		String selectAllReachesQuery = "SELECT IDENTIFIER, MODEL_REACH_ID FROM " + SparrowSchemaConstants.NETWORK_SCHEMA + ".MODEL_REACH WHERE SPARROW_MODEL_ID = " 
			+ data.getModel().getId();	

		// ERROR: TODO: should be MODEL_REACH, as MODEL_REACH_TOPO should be deleted from schema.
		String insertReachTopoStr = "INSERT INTO " + SparrowSchemaConstants.NETWORK_SCHEMA + ".MODEL_REACH_TOPO (MODEL_REACH_ID, FNODE, TNODE, IFTRAN) VALUES (?,?,?,?)";
		PreparedStatement insertReachTopo = conn.prepareStatement(insertReachTopoStr);

		DataTable ancil = data.getAncil();
		DataTable topo = data.getTopo();
		int modelRows = topo.getRowCount();	//# of reaches in model
		int currentBatchCount = 0;	//number of statements added to the current batch

		//ancillary headings indexes
		int localIdIndexAnc = ancil.getColumnByName("local_id");
		int stdIdIndexAnc = ancil.getColumnByName("std_id");
		int localSameIndexAnc = ancil.getColumnByName("local_same");

		if (localIdIndexAnc < 0) throw new IllegalStateException("local_id heading not found in ancil.txt");
		if (stdIdIndexAnc < 0) throw new IllegalStateException("std_id heading not found in ancil.txt");
		if (localSameIndexAnc < 0) throw new IllegalStateException("local_same heading not found in ancil.txt");

		//topographic headings indexes
		int hydseqIndexTopo = topo.getColumnByName("hydseq");
		int fnodeIndexTopo = topo.getColumnByName("fnode");
		int tnodeIndexTopo = topo.getColumnByName("tnode");
		int iftranIndexTopo = topo.getColumnByName("iftran");

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
			modelIdMap = buildIntegerMap(conn, selectAllReachesQuery);

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

		String[] headers = DataTableUtils.getHeadings(data.getSrc());

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
	 * 
	 * TODO [IK] refactor this into DataTableUtils. This method is the last dependency on JDBCUtil
	 * 
	 * @param conn
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, Integer> buildIntegerMap(Connection conn, String query) throws SQLException {
		DataTableWritable data = DataLoader.readAsInteger(conn, query, 1000);
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
		DataTable ancil = data.getAncil();
		DataTable topo = data.getTopo();
		DataTable src = data.getSrc();
		DataTable coef = data.getCoef();

		//Basic count values for model data
		int modelRowCnt = topo.getRowCount();	//# of reaches in model
		int coefRowCnt = coef.getRowCount();	//# of coefs (reaches * iterations)
		int iterationCnt = coefRowCnt / modelRowCnt;	//# of iterations
		int modelSourceCnt = src.getColumnCount();	//# of sources in the dataset

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
		if (modelSourceCnt != coef.getColumnCount() - 4) {
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
		int localIdIndexAnc = ancil.getColumnByName("local_id");
		if (localIdIndexAnc < 0) throw new IllegalStateException("local_id heading not found in ancil.txt");


		//topographic headings indexes
		int hydseqIndexTopo = topo.getColumnByName("hydseq");
		int fnodeIndexTopo = topo.getColumnByName("fnode");
		int tnodeIndexTopo = topo.getColumnByName("tnode");
		int iftranIndexTopo = topo.getColumnByName("iftran");

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
	

}
