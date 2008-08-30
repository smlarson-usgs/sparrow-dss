package gov.usgswim.sparrow.test.loader;

import gov.usgswim.sparrow.loader.ModelDataLoader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;

public class ModelDataLoaderTest extends TestCase {
	static File baseDir = new File("C:\\Documents and Settings\\ilinkuo\\Desktop\\Decision_support_files_sediment\\sparrow_ds_sediment");
	static Long modelID = 30L;
	
	public void testDeleteAll() throws IOException {
		File modelMetadata = new File(baseDir.getAbsolutePath() + "/model_metadata.txt");
		ModelDataLoader.deleteModel(modelID, modelMetadata);
	}
	
	public void testInsertModelMetadata() throws IOException {
		File modelMetadata = new File(baseDir.getAbsolutePath() + "/model_metadata.txt");
		ModelDataLoader.insertModelMetadata(null, modelID, modelMetadata);
	}
	
	public void testInsertSources() throws IOException {
		File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
		ModelDataLoader.insertSources(null, modelID, sourceMetadata);
	}
	
	public void testInsertReaches() throws IOException, SQLException {
		File topoData = new File(baseDir.getAbsolutePath() + "/topo.txt");
		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
		ModelDataLoader.insertReaches(null, modelID, topoData, ancillaryData);
	}

	public void testInsertReachDecayCoefs() throws IOException, SQLException {
		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
		File coefData = new File(baseDir.getAbsolutePath() + "/coef.txt");
		Connection conn = ModelDataLoader.getDevelopmentConnection();
		ModelDataLoader.insertReachDecayCoefs(conn, modelID, coefData, ancillaryData);
	}

	public void testInsertSourceReachCoefs() throws IOException, SQLException {
		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
		File coefData = new File(baseDir.getAbsolutePath() + "/coef.txt");
		File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
		Connection conn = ModelDataLoader.getDevelopmentConnection();
		ModelDataLoader.insertSourceReachCoefs(conn, modelID, coefData, sourceMetadata, ancillaryData);
	}
	
	public void testInsertSourceValues() throws IOException, SQLException {
		File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
		File sourceValuesData = new File(baseDir.getAbsolutePath() + "/src.txt");
		File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
		Connection conn = ModelDataLoader.getDevelopmentConnection();
		ModelDataLoader.insertSourceValues(conn, modelID, sourceValuesData, sourceMetadata, ancillaryData);
	}
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
}
