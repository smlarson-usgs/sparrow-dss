package gov.usgswim.sparrow.loader;

import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.DataTableUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ModelDataStateChecker {
	public static Connection conn = ModelDataLoader.getWIDWConnection();
//	public static Connection conn = ModelDataLoader.getWIMAPConnection();
	
	public static void main(String[] args) throws SQLException, IOException {
		
		
		System.out.println("==== Checking sequence consistency ====");
		DataTable dt = checkSequenceConsistency(conn);
		TemporaryHelper.printDataTable(dt, "Sequence Consistency");
		
		System.out.println("==== Checking Total table counts ====");
		dt = checkTotalTableCounts(conn);
		TemporaryHelper.printDataTable(dt, "Total counts");
		
		DataTable models = getModels(conn);
		Integer modelIDColIndex = models.getColumnByName("SPARROW_MODEL_ID");
		for (int row=0; row<models.getRowCount(); row++) {
			Long modelID = models.getLong(row, modelIDColIndex);
			
			System.out.println("==== Checking Sequence range for model " + modelID + " ====");
			dt = checkPerModelIdentifierRanges(conn, modelID);
			TemporaryHelper.printDataTable(dt, "Model " + modelID + " ranges");
			
			System.out.println("==== Checking table counts for model " + modelID + " ====");
			dt = checkPerModelTableCounts(conn, modelID);
			TemporaryHelper.printDataTable(dt, "Table counts for model " + modelID);
		}
		
	}
	
	public static DataTableWritable getModels(Connection conn) throws SQLException {
		String sql = "select * from SPARROW_DSS.SPARROW_MODEL order by SPARROW_MODEL_ID";
		Statement stmt= null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			DataTableWritable result = DataTableUtils.toDataTable(rset);
			return result;
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	public static DataTableWritable checkSequenceConsistency(Connection conn) throws SQLException {
		/*
		select 'MODEL_REACH.MODEL_REACH_ID' as sequence_name, 'MAX' as type, max(model_reach_id) as val from model_reach
	union all
		 select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences
			  where sequence_name = 'MODEL_REACH_SEQ'
	union all
		 select 'REACH_COEF.REACH_COEF_ID' as sequence_name, 'MAX' as type, max(REACH_COEF_ID) as val from REACH_COEF
	union all
		 select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences
			  where sequence_name = 'REACH_COEF_SEQ'
	union all
		 select 'SOURCE_REACH_COEF.SOURCE_REACH_COEF_ID' as sequence_name, 'MAX' as type, max(SOURCE_REACH_COEF_ID) as val from SOURCE_REACH_COEF
	union all
		 select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences
			  where sequence_name = 'SOURCE_REACH_COEF_SEQ'
	union all
		 select 'SOURCE.SOURCE_ID' as sequence_name, 'MAX' as type, max(SOURCE_ID) as val from SOURCE
	union all
		 select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences
			  where sequence_name = 'SOURCE_SEQ'
	union all
		 select 'SOURCE_VALUE.SOURCE_VALUE_ID' as sequence_name, 'MAX' as type, max(SOURCE_VALUE_ID) as val from SOURCE_VALUE
	union all
		 select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences
			  where sequence_name = 'SOURCE_VALUE_SEQ'
	union all
		 select 'SPARROW_MODEL.SPARROW_MODEL_ID' as sequence_name, 'MAX' as type, max(SPARROW_MODEL_ID) as val from SPARROW_MODEL
	union all
		 select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences
			  where sequence_name = 'SPARROW_MODEL_SEQ';
		 */
		String sql = "    select 'MODEL_REACH.MODEL_REACH_ID' as sequence_name, 'MAX' as type, max(model_reach_id) as val from model_reach "
			+ "union all "
			+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
			+ "        where sequence_name = 'MODEL_REACH_SEQ' "
			+ "union all "
			+ "    select 'REACH_COEF.REACH_COEF_ID' as sequence_name, 'MAX' as type, max(REACH_COEF_ID) as val from REACH_COEF "
			+ "union all "
			+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
			+ "        where sequence_name = 'REACH_COEF_SEQ' "
			+ "union all "
			+ "    select 'SOURCE_REACH_COEF.SOURCE_REACH_COEF_ID' as sequence_name, 'MAX' as type, max(SOURCE_REACH_COEF_ID) as val from SOURCE_REACH_COEF "
			+ "union all "
			+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
			+ "        where sequence_name = 'SOURCE_REACH_COEF_SEQ' "
			+ "union all "
			+ "    select 'SOURCE.SOURCE_ID' as sequence_name, 'MAX' as type, max(SOURCE_ID) as val from SOURCE "
			+ "union all "
			+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
			+ "        where sequence_name = 'SOURCE_SEQ' "
			+ "union all "
			+ "    select 'SOURCE_VALUE.SOURCE_VALUE_ID' as sequence_name, 'MAX' as type, max(SOURCE_VALUE_ID) as val from SOURCE_VALUE "
			+ "union all "
			+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
			+ "        where sequence_name = 'SOURCE_VALUE_SEQ' "
			+ "union all "
			+ "    select 'SPARROW_MODEL.SPARROW_MODEL_ID' as sequence_name, 'MAX' as type, max(SPARROW_MODEL_ID) as val from SPARROW_MODEL "
			+ "union all "
			+ "    select sequence_name, 'SEQUENCE' as type, last_number as val from user_sequences "
			+ "        where sequence_name = 'SPARROW_MODEL_SEQ' ";

		Statement stmt= null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			DataTableWritable result = DataTableUtils.toDataTable(rset);
			return result;
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	public static DataTableWritable checkTotalTableCounts(Connection conn) throws SQLException {
		/*
			 select 'SPARROW_MODEL' as table_name, count(*) from SPARROW_DSS.SPARROW_MODEL
	union all
	select 'MODEL_REACH' as table_name, count(*) from SPARROW_DSS.MODEL_REACH
	union all
	select 'MODEL_REACH_ATTRIB' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_ATTRIB
	union all
	select 'MODEL_REACH_GEOM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_GEOM
	union all
	select 'MODEL_REACH_TOPO' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_TOPO
	union all
	select 'MODEL_REACH_UPSTREAM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_UPSTREAM
	union all
	select 'REACH_COEF' as table_name, count(*) from SPARROW_DSS.REACH_COEF
	union all
	select 'SOURCE' as table_name, count(*) from SPARROW_DSS.SOURCE
	union all
	select 'SOURCE_REACH_COEF' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_COEF
	union all
	select 'SOURCE_REACH_PREDICT' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_PREDICT
	union all
	select 'SOURCE_VALUE' as table_name, count(*) from SPARROW_DSS.SOURCE_VALUE
	union all
	select 'TOTAL_PREDICT' as table_name, count(*) from SPARROW_DSS.TOTAL_PREDICT
		 */
		String sql = " select 'SPARROW_MODEL' as table_name, count(*) from SPARROW_DSS.SPARROW_MODEL"
			+ " union all"
			+ " select 'MODEL_REACH' as table_name, count(*) from SPARROW_DSS.MODEL_REACH"
			+ " union all"
			+ " select 'MODEL_REACH_ATTRIB' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_ATTRIB"
			+ " union all"
			+ " select 'MODEL_REACH_GEOM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_GEOM"
			+ " union all"
			+ " select 'MODEL_REACH_TOPO' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_TOPO"
			+ " union all"
			+ " select 'MODEL_REACH_UPSTREAM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_UPSTREAM"
			+ " union all"
			+ " select 'REACH_COEF' as table_name, count(*) from SPARROW_DSS.REACH_COEF"
			+ " union all"
			+ " select 'SOURCE' as table_name, count(*) from SPARROW_DSS.SOURCE"
			+ " union all"
			+ " select 'SOURCE_REACH_COEF' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_COEF"
			+ " union all"
			+ " select 'SOURCE_REACH_PREDICT' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_PREDICT"
			+ " union all"
			+ " select 'SOURCE_VALUE' as table_name, count(*) from SPARROW_DSS.SOURCE_VALUE"
			+ " union all"
			+ " select 'TOTAL_PREDICT' as table_name, count(*) from SPARROW_DSS.TOTAL_PREDICT";	 

		Statement stmt= null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			DataTableWritable result = DataTableUtils.toDataTable(rset);
			return result;
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	public static DataTableWritable checkPerModelTableCounts(Connection conn, Long modelID) throws SQLException {
		/*
	select 'SPARROW_MODEL' as table_name, count(*) from SPARROW_DSS.SPARROW_MODEL
		where sparrow_model_id = 30
	union all
	select 'MODEL_REACH' as table_name, count(*) from SPARROW_DSS.MODEL_REACH
		where sparrow_model_id = 30
	union all
	select 'MODEL_REACH_ATTRIB' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_ATTRIB MRA
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = MRA.model_reach_id)
	union all
	select 'MODEL_REACH_GEOM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_GEOM MRG
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = MRG.model_reach_id)
	union all
	select 'MODEL_REACH_TOPO' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_TOPO MRT
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = MRT.model_reach_id)
	union all
	select 'MODEL_REACH_UPSTREAM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_UPSTREAM
	union all
	select 'REACH_COEF' as table_name, count(*) from SPARROW_DSS.REACH_COEF RC
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = RC.model_reach_id)
	union all
	select 'SOURCE' as table_name, count(*) from SPARROW_DSS.SOURCE
		where sparrow_model_id = 30
	union all
	select 'SOURCE_REACH_COEF' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_COEF SRC
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = SRC.model_reach_id)
	union all
	select 'SOURCE_REACH_PREDICT' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_PREDICT SRP
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = SRP.model_reach_id)
	union all
	select 'SOURCE_VALUE' as table_name, count(*) from SPARROW_DSS.SOURCE_VALUE SV
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR where MR.model_reach_id = SV.model_reach_id)
	union all
	select 'TOTAL_PREDICT' as table_name, count(*) from SPARROW_DSS.TOTAL_PREDICT
		where sparrow_model_id = 30
		 */
		String sql = " select 'SPARROW_MODEL' as table_name, count(*) from SPARROW_DSS.SPARROW_MODEL "
			+ " 	where sparrow_model_id = " + modelID + " "
			+ " union all "
			+ " select 'MODEL_REACH' as table_name, count(*) from SPARROW_DSS.MODEL_REACH "
			+ " 	where sparrow_model_id = " + modelID + " "
			+ " union all "
			+ " select 'MODEL_REACH_ATTRIB' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_ATTRIB MRA "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = MRA.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'MODEL_REACH_GEOM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_GEOM MRG "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = MRG.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'MODEL_REACH_TOPO' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_TOPO MRT "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = MRT.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'MODEL_REACH_UPSTREAM' as table_name, count(*) from SPARROW_DSS.MODEL_REACH_UPSTREAM MRU "
			+ "		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR "
			+ " 		where MR.model_reach_id = MRU.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'REACH_COEF' as table_name, count(*) from SPARROW_DSS.REACH_COEF RC "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = RC.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'SOURCE' as table_name, count(*) from SPARROW_DSS.SOURCE "
			+ " 	where sparrow_model_id = " + modelID + " "
			+ " union all "
			+ " select 'SOURCE_REACH_COEF' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_COEF SRC "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = SRC.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'SOURCE_REACH_PREDICT' as table_name, count(*) from SPARROW_DSS.SOURCE_REACH_PREDICT SRP "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = SRP.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'SOURCE_VALUE' as table_name, count(*) from SPARROW_DSS.SOURCE_VALUE SV "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = SV.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'TOTAL_PREDICT' as table_name, count(*) from SPARROW_DSS.TOTAL_PREDICT "
			+ " 	where sparrow_model_id = " + modelID + " ";

		Statement stmt= null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			DataTableWritable result = DataTableUtils.toDataTable(rset);
			return result;
		} finally {
			if (stmt != null) stmt.close();
		}
	}

	public static DataTableWritable checkPerModelIdentifierRanges(Connection conn, long modelID) throws SQLException {
		/*
	select 'MODEL_REACH.MODEL_REACH_ID' as sequence_name, min(model_reach_id) as min, max(model_reach_id) as max from model_reach
		where sparrow_model_id = 22
	union all
	select 'REACH_COEF.REACH_COEF_ID' as sequence_name, min(REACH_COEF_ID) as min, max(REACH_COEF_ID) as max from REACH_COEF RC
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR 
			where MR.model_reach_id = RC.model_reach_id and MR.sparrow_model_id = 22)
	union all
	select 'SOURCE_REACH_COEF.SOURCE_REACH_COEF_ID' as sequence_name, min(SOURCE_REACH_COEF_ID) as min, max(SOURCE_REACH_COEF_ID) as max from SOURCE_REACH_COEF SRC
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR 
			where MR.model_reach_id = SRC.model_reach_id and MR.sparrow_model_id = 22)
	union all
		select 'SOURCE.SOURCE_ID' as sequence_name, min(SOURCE_ID) as min, max(SOURCE_ID) as max from SOURCE
			where sparrow_model_id = 22
	union all
	select 'SOURCE_VALUE.SOURCE_VALUE_ID' as sequence_name, min(SOURCE_VALUE_ID) as min, max(SOURCE_VALUE_ID) as max from SOURCE_VALUE SV
		where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR 
			where MR.model_reach_id = SV.model_reach_id and MR.sparrow_model_id = 22)
		 */
		String sql = " select 'MODEL_REACH.MODEL_REACH_ID' as sequence_name, min(model_reach_id) as min, max(model_reach_id) as max from model_reach "
			+ " 	where sparrow_model_id = " + modelID + " "
			+ " union all "
			+ " select 'REACH_COEF.REACH_COEF_ID' as sequence_name, min(REACH_COEF_ID) as min, max(REACH_COEF_ID) as max from REACH_COEF RC "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = RC.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " select 'SOURCE_REACH_COEF.SOURCE_REACH_COEF_ID' as sequence_name, min(SOURCE_REACH_COEF_ID) as min, max(SOURCE_REACH_COEF_ID) as max from SOURCE_REACH_COEF SRC "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = SRC.model_reach_id and MR.sparrow_model_id = " + modelID + ") "
			+ " union all "
			+ " 	select 'SOURCE.SOURCE_ID' as sequence_name, min(SOURCE_ID) as min, max(SOURCE_ID) as max from SOURCE "
			+ " 		where sparrow_model_id = " + modelID + " "
			+ " union all "
			+ " select 'SOURCE_VALUE.SOURCE_VALUE_ID' as sequence_name, min(SOURCE_VALUE_ID) as min, max(SOURCE_VALUE_ID) as max from SOURCE_VALUE SV "
			+ " 	where exists (select MR.model_reach_id from SPARROW_DSS.MODEL_REACH MR  "
			+ " 		where MR.model_reach_id = SV.model_reach_id and MR.sparrow_model_id = " + modelID + ") ";

		Statement stmt= null;
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			DataTableWritable result = DataTableUtils.toDataTable(rset);
			return result;
		} finally {
			if (stmt != null) stmt.close();
		}
	}

}
