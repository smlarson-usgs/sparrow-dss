package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author eeverman
 */
public class BuildAnalysisForAllSourcesTest extends SparrowTestBase {

	
	static final double COMP_ERROR = .0000001d;
	
	
	@Test
	public void dataSanityCheck() throws Exception {
		
		AdjustmentGroups testModelNoAdjustmentsGroup = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);

		PredictionContext basicPredictContext = new PredictionContext(
				SparrowTestBase.TEST_MODEL_ID,
				testModelNoAdjustmentsGroup,
				new BasicAnalysis(DataSeriesType.total, null, null, null),
				new TerminalReaches(SparrowTestBase.TEST_MODEL_ID),
				null,
				NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action = new BuildAnalysisForAllSources();
		
		action.setContext(basicPredictContext);
		
		List<ColumnData> result = action.run();
		
		//Expected Values
		PredictData predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		PredictResult predictResult = SharedApplication.getInstance().getPredictResult(testModelNoAdjustmentsGroup);
		
		int sourceCount = predictData.getSrcMetadata().getRowCount();
		int rowCount = predictData.getTopo().getRowCount();
		
		assertNotNull(result);
		assertEquals(sourceCount + 1, result.size());
		
		//Check all columns for the correct row count
		for (int c = 0; c < sourceCount + 1; c++) {
			assertEquals(rowCount, result.get(c).getRowCount().intValue());
		}
		
		
		//Check that values match the PredictResult
		for (int r = 0; r < rowCount; r++) {
			
			//Check individual sources
			for (int src = 0; src < sourceCount; src++) {
				Double expect = predictResult.getTotalForSrc(r, predictData.getSourceIdForSourceIndex(src));
				Double actual = result.get(src).getDouble(r);
				assertEquals(expect, actual);
			}
			
			//Check total for all sources
			Double expect = predictResult.getTotal(r);
			Double actual = result.get(sourceCount).getDouble(r);
			assertEquals(expect, actual);
		}
		
		
		
	}
	
	@Test
	public void aNullAnalysisShouldBeInvalid() throws Exception {
		
		AdjustmentGroups testModelNoAdjustmentsGroup = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);

		PredictionContext basicPredictContext = new PredictionContext(
				SparrowTestBase.TEST_MODEL_ID,
				testModelNoAdjustmentsGroup,
				null,
				new TerminalReaches(SparrowTestBase.TEST_MODEL_ID),
				null,
				NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action = new BuildAnalysisForAllSources();
		
		action.setContext(basicPredictContext);
		
		List<ColumnData> result = action.run();
		
		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);
		
	}
	
	@Test
	public void aNonSourceBasedDataSeriesShouldBeInvalid() throws Exception {
		
		AdjustmentGroups testModelNoAdjustmentsGroup = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);

		PredictionContext basicPredictContext = new PredictionContext(
				SparrowTestBase.TEST_MODEL_ID,
				testModelNoAdjustmentsGroup,
				new BasicAnalysis(DataSeriesType.catch_area, null, null, null),
				new TerminalReaches(SparrowTestBase.TEST_MODEL_ID),
				null,
				NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action = new BuildAnalysisForAllSources();
		
		action.setContext(basicPredictContext);
		
		List<ColumnData> result = action.run();
		
		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);
		
	}
	
	@Test
	public void aNonNullSourceShouldBeInvalid() throws Exception {
		
		AdjustmentGroups testModelNoAdjustmentsGroup = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);

		PredictionContext basicPredictContext = new PredictionContext(
				SparrowTestBase.TEST_MODEL_ID,
				testModelNoAdjustmentsGroup,
				new BasicAnalysis(DataSeriesType.total, 0, null, null),
				new TerminalReaches(SparrowTestBase.TEST_MODEL_ID),
				null,
				NoComparison.NO_COMPARISON);
		
		BuildAnalysisForAllSources action = new BuildAnalysisForAllSources();
		
		action.setContext(basicPredictContext);
		
		List<ColumnData> result = action.run();
		
		assertNull(result);
		assertEquals(1, action.getValidationErrors().length);
		
	}
	
	

	
}

