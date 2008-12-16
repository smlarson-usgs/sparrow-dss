package gov.usgswim.sparrow.test.loader;

import static gov.usgswim.sparrow.loader.ModelDataLoadComparison.outputCoefAsTextFiles;
import static gov.usgswim.sparrow.loader.ModelDataLoadComparison.outputSourceValuesAsTextFile;
import static gov.usgswim.sparrow.loader.ModelDataLoadComparison.outputTopoAsTextFile;
import gov.usgswim.sparrow.loader.ModelDataAssumptions;
import gov.usgswim.sparrow.loader.ModelDataLoader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.junit.Ignore;

@Ignore
public class ModelLoaderComparisonTest extends TestCase {
	
	public void testOutputModelAsTextFiles() throws SQLException, IOException {
		File outputDirectory  = new File(ModelDataLoaderTest.baseDir.getAbsolutePath() + "/out");
		if (!outputDirectory.exists()) {
			throw new RuntimeException("Please create an output directory for the test at " + outputDirectory.getAbsolutePath());
		}
		Connection conn = ModelDataLoader.getWIDWConnection();
		
		outputTopoAsTextFile(outputDirectory, conn, ModelDataAssumptions.MODEL_ID);
		outputSourceValuesAsTextFile(outputDirectory, conn, ModelDataAssumptions.MODEL_ID);
		outputCoefAsTextFiles(outputDirectory, conn, ModelDataAssumptions.MODEL_ID);
	}
}
