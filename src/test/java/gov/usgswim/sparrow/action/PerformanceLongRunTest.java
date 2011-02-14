package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.cachefactory.PredictResultFactory;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.Test;

/**
 * Delivery seems to perform really slowly, so this test compares the time
 * to that of a standard analysis.
 * 
 * @author eeverman
 */
public class PerformanceLongRunTest extends SparrowDBTestBaseClass {
	
	static LifecycleListener lifecycle = new LifecycleListener();
	
	static PredictionContext context;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		
		//Turns on detailed logging
		//log.setLevel(Level.DEBUG);
		//log.getLogger(Action.class).setLevel(Level.DEBUG);
		//log.getLogger(SharedApplication.class).setLevel(Level.TRACE);
		
		AdjustmentGroups emptyAdjustments = new AdjustmentGroups(TEST_MODEL_ID);
		context = new PredictionContext(TEST_MODEL_ID, emptyAdjustments, null,
				null, null, null);
		SharedApplication.getInstance().putPredictionContext(context);
		
	}
	
	
	//@Test
	public void testComparison() throws Exception {

		long startTime = 0;
		long endTime = 0;
		//Number of times to loop
		final int ITERATION_COUNT = 100;
		
		
		//Force the predict data to be loaded
		startTime = System.currentTimeMillis();
		PredictData predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		endTime = System.currentTimeMillis();
		report(endTime - startTime, "Initial Load of model data", 1);
		assertTrue("It should take less then 50 seconds to load model 50, even on a slow connection.", 
				(endTime - startTime) < 50000);
		
		
		startTime = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		}
		endTime = System.currentTimeMillis();
		report(endTime - startTime, "Reload model data from cache", 100);
		assertTrue("Predict data is now cached, it should be faster than 100ms for 100 'loads'.", 
				(endTime - startTime) < 50);
		
		
		PredictResultFactory prFactory = new PredictResultFactory();
		AdjustmentGroups emptyAdjustments = new AdjustmentGroups(TEST_MODEL_ID);
		PredictResult predictResults = null;
		CalcDeliveryFraction delAction = new CalcDeliveryFraction();
		DataColumn dataColumn = null;
		
		//Run the prediction
		startTime = System.currentTimeMillis();
		for (int i=0; i< ITERATION_COUNT;  i++) {
			predictResults = prFactory.createEntry(emptyAdjustments);
		}
		endTime = System.currentTimeMillis();
		long predictTotalTime = endTime - startTime;
		report(predictTotalTime, "Run Prediction", ITERATION_COUNT);
		assertTrue("Run Prediction should be less than 5 seconds for 100 runs.", 
				predictTotalTime < 5000);
		
		
		//Copy the prediction results to an NSDataSet
		dataColumn =
			new DataColumn(predictResults, predictResults.getTotalCol(), context.getId());
		
		startTime = System.currentTimeMillis();
		for (int i=0; i< ITERATION_COUNT;  i++) {
			NSDataSetBuilder nsDataSetBuilder = new NSDataSetBuilder();
			nsDataSetBuilder.setData(dataColumn);
			nsDataSetBuilder.run();
		}
		endTime = System.currentTimeMillis();
		long predictCopyTotalTime = endTime - startTime;
		report(predictCopyTotalTime, "Copy predict result to NSDataset", ITERATION_COUNT);
		assertTrue("Copying the data to the NSDataset should be less than 500ms for 100 iterations.", 
				predictCopyTotalTime < 500);
		
		
		//Run Delivery
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		delAction.setPredictData(predictData);
		delAction.setTargetReachIds(targets.asSet());
		ColumnData deliveryFrac = null;
		startTime = System.currentTimeMillis();
		for (int i=0; i< ITERATION_COUNT;  i++) {
			deliveryFrac = delAction.run();
		}
		endTime = System.currentTimeMillis();
		long deliveryTotalTime = endTime - startTime;
		report(deliveryTotalTime, "Run Delivery", ITERATION_COUNT);
		assertTrue("Delivery calc time should be less than 500ms for 100 iterations.", 
				deliveryTotalTime < 500);
		
		
		
		assertTrue("Topo table should be indexed", predictData.getTopo().isIndexed(PredictData.TNODE_COL));

	}
	
	protected void report(long time, String description, int iterationCount) {
		log.debug("Total time for '"
				+ description + "' (" + iterationCount + " times) "
				+ time + "ms");
	}
	
}

