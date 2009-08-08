package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.loader.ModelDataAssumptions;
import gov.usgswim.sparrow.loader.ModelDataLoader;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.junit.Ignore;

@Ignore
public class ModelDataLoaderTest extends TestCase {
	static public File baseDir = new File("C:\\Documents and Settings\\ilinkuo\\Desktop\\Decision_support_files_sediment\\sparrow_ds_sediment");
	static File verificationDir = new File(baseDir.getAbsolutePath() + "/vOutput" );

//	public void testDeleteAll() throws IOException {
//		File modelMetadata = new File(baseDir.getAbsolutePath() + "/model_metadata.txt");
//		ModelDataLoader.deleteModel(ModelDataAssumptions.MODEL_ID, modelMetadata);
//	}
//
//	public void testInsertModelMetadata() throws IOException {
//		File modelMetadata = new File(baseDir.getAbsolutePath() + "/model_metadata.txt");
//		ModelDataLoader.insertModelMetadata(ModelDataAssumptions.MODEL_ID, modelMetadata);
//	}
//
//	public void testInsertSources() throws IOException {
//		File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
//		ModelDataLoader.insertSources(ModelDataAssumptions.MODEL_ID, sourceMetadata);
//	}
//

	public void testInsertReaches() throws IOException, SQLException {
		File topoData = new File(baseDir.getAbsolutePath() + "/topo.txt");
		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
		ModelDataLoader.insertReaches(ModelDataAssumptions.MODEL_ID, topoData, ancillaryData);
	}
//
//	public void testInsertReachDecayCoefs() throws IOException, SQLException {
//		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
//		File coefData = new File(baseDir.getAbsolutePath() + "/coef.txt");
////		Connection conn = ModelDataLoader.getDevelopmentConnection();
//		Connection conn = ModelDataLoader.getWIDWConnection();
//		ModelDataLoader.insertReachDecayCoefs(conn, ModelDataAssumptions.MODEL_ID, coefData, ancillaryData);
//		conn.close();
//	}
//
//	public void testInsertSourceReachCoefs() throws IOException, SQLException {
//		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
//		File coefData = new File(baseDir.getAbsolutePath() + "/coef.txt");
//		File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
////		Connection conn = ModelDataLoader.getDevelopmentConnection();
//		Connection conn = ModelDataLoader.getWIDWConnection();
//		ModelDataLoader.insertSourceReachCoefs(conn, ModelDataAssumptions.MODEL_ID, coefData, sourceMetadata, ancillaryData);
//		conn.close();
//	}
//
//	public void testInsertSourceValues() throws IOException, SQLException {
//		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
//		File sourceValuesData = new File(baseDir.getAbsolutePath() + "/src.txt");
//		File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
////		Connection conn = ModelDataLoader.getDevelopmentConnection();
//		Connection conn = ModelDataLoader.getWIDWConnection();
//		ModelDataLoader.insertSourceValues(conn, ModelDataAssumptions.MODEL_ID, sourceValuesData, sourceMetadata, ancillaryData);
//		conn.close();
//	}



/*
	public void testAddPrefixAndCapitalize() {
		fail("Not yet implemented");
	}

	public void testQuoteForSQL() {
		fail("Not yet implemented");
	}

	public void testRetrieveModelReachIDLookup() {
		fail("Not yet implemented");
	}

	public void testRetrieveSourceIDLookup() {
		fail("Not yet implemented");
	}
*/
	// ========
	// UTILITY METHOD
	// ==============

}
