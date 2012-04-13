package gov.usgswim.datatable.utils;


import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AnalyzerTest {

//	public void testAnalyze() throws IOException {
////		File file= new File(".");
////		System.out.println(file.getAbsolutePath());
//		File file= new File("src/gov/usgswim/sparrow/test/loader/standardTestFile.txt");
//		assertTrue(file.exists());
//		DataFileDescriptor analysisResult = Analyzer.analyze(file);
//		System.out.println(analysisResult);
//	}
	
    @Test
	public void testAnalyzeSedimentFiles() throws IOException {
		File baseDir = new File("C:\\Documents and Settings\\ilinkuo\\Desktop\\Decision_support_files_sediment\\sparrow_ds_sediment");
		for (DataFileDescriptor analysis: Analyzer.analyzeDirectory(baseDir)) {
			System.out.println(analysis);
		}
	}
/*
	public void testInferTypes() {
		fail("Not yet implemented");
	}

	public void testGatherStats() {
		fail("Not yet implemented");
	}

	public void testInferType() {
		fail("Not yet implemented");
	}

	public void testAnalyzeDelimiter() {
		fail("Not yet implemented");
	}
*/
}
