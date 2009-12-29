package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.DeliveryRunner;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.action.CalcDeliveryFraction;
import gov.usgswim.sparrow.cachefactory.PredictResultFactory;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.Comparison;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.test.TestHelper;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Delivery seems to perform really slowly, so this test compares the time
 * to that of a standard analysis.
 * 
 * @author eeverman
 */
public class DeliveryPerformanceTest {
	protected static Logger log =
		Logger.getLogger(DeliveryPerformanceTest.class); //logging for this class
	
	static LifecycleListener lifecycle = new LifecycleListener();
	
	static final Long MODEL_ID = 50L;
	static PredictionContext context;
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		//Turns on detailed logging
		log.setLevel(Level.DEBUG);
		
		//Turn off logging for actions, which might affect performance
		log.getLogger(Action.class).setLevel(Level.ERROR);
		
		lifecycle.contextInitialized(null, true);
		
		AdjustmentGroups emptyAdjustments = new AdjustmentGroups(MODEL_ID);
		context = new PredictionContext(MODEL_ID, emptyAdjustments, null,
				null, null, null);
		SharedApplication.getInstance().putPredictionContext(context);
		
		
		XMLUnit.setIgnoreWhitespace(true);
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		lifecycle.contextDestroyed(null, true);
	}
	
	@Test
	public void testComparison() throws Exception {

		//Number of times to loop
		final int ITERATION_COUNT = 100;
		
		
		//Force the predict data to be loaded
		PredictData predictData = SharedApplication.getInstance().getPredictData(MODEL_ID);
		
		PredictResultFactory prFactory = new PredictResultFactory();
		AdjustmentGroups emptyAdjustments = new AdjustmentGroups(MODEL_ID);
		PredictResult predictResults = null;
		NSDataSetBuilder nsDataSetBuilder = new NSDataSetBuilder();
		CalcDeliveryFraction delAction = new CalcDeliveryFraction();
		DataColumn dataColumn = null;
		
		//Run the prediction
		long startTime = System.currentTimeMillis();
		for (int i=0; i< ITERATION_COUNT;  i++) {
			predictResults = prFactory.createEntry(emptyAdjustments);
		}
		long endTime = System.currentTimeMillis();
		long predictTotalTime = endTime - startTime;
		report(predictTotalTime, "Run Prediction", ITERATION_COUNT);
		
		//Copy the prediction results to an NSDataSet
		dataColumn =
			new DataColumn(predictResults, predictResults.getTotalCol(), context.getId());
		nsDataSetBuilder.setData(dataColumn);
		startTime = System.currentTimeMillis();
		for (int i=0; i< ITERATION_COUNT;  i++) {
			nsDataSetBuilder.run();
		}
		endTime = System.currentTimeMillis();
		long predictCopyTotalTime = endTime - startTime;
		report(predictCopyTotalTime, "Copy predict result to NSDataset", ITERATION_COUNT);
		
		//Run Delivery
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		TerminalReaches targets = new TerminalReaches(MODEL_ID, targetList);
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
		
		//Copy the delivery to NSDataSet
		SimpleDataTable delFracTable = new SimpleDataTable(
				new ColumnData[] {deliveryFrac}, "Delivery Fraction",
				"A single column table containing the delivery fraction" +
				" to a target reach or reaches.", null, null
			);
		dataColumn = new DataColumn(delFracTable, 0, context.getId());
		nsDataSetBuilder.setData(dataColumn);
		startTime = System.currentTimeMillis();
		for (int i=0; i< ITERATION_COUNT;  i++) {
			nsDataSetBuilder.run();
		}
		endTime = System.currentTimeMillis();
		long deliveryCopyTotalTime = endTime - startTime;
		report(deliveryCopyTotalTime, "Copy Delivery to NSDataset", ITERATION_COUNT);
		
		double calcTimeRatio = Math.abs(predictTotalTime - deliveryTotalTime)/
			(double)predictTotalTime;
		double copyTimeRatio = Math.abs(predictCopyTotalTime - deliveryCopyTotalTime)/
			(double)predictCopyTotalTime;
	
		int countNonZeroDels = 0;
		
		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			if (deliveryFrac.getDouble(r) > 0d) countNonZeroDels++;
		}
		
		
		System.out.println("Calc time ratio = " + calcTimeRatio);
		System.out.println("Copy time ratio = " + copyTimeRatio);
		System.out.println("Number of non-zero delivery fractions = " + countNonZeroDels);
		System.out.println("Topo is indexed? " + predictData.getTopo().isIndexed(PredictData.TNODE_COL));
		
		//The time to run the delivery should not be an increase of more than
		//150% of the time to run the prediction (2.5 times as long)
		assertTrue(calcTimeRatio < 1.5d);
		
		//The time to run the delivery should not be an increase of more than
		//100% of the time to run the prediction (2 times as long)
		assertTrue(copyTimeRatio < 1d);
	}
	
	protected void report(long time, String description, int iterationCount) {
		log.debug("Total time for '"
				+ description + "' (" + iterationCount + " times) "
				+ time + "ms");
	}
	
}

