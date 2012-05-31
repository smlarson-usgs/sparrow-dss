package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import org.apache.log4j.Level;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author eeverman
 */
public class BuildTotalDeliveredLoadByStateSummaryReportTest extends DeliveryBase {

	
	static final double COMP_ERROR = .0000001d;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
		super.doOneTimeCustomSetup();
	}
	
	@Test
	public void dataSanityCheckForStates() throws Exception {
		
		//Switches the predict data in the cache to the modified copy, which
		//has transport turned off above reach 9681.
		switchToModifiedPredictData();
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, AggregationLevel.STATE);
		
		BuildTotalDeliveredLoadByStateSummaryReport action =
				new BuildTotalDeliveredLoadByStateSummaryReport(req);
		
		DataTable result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The State Delivery Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(9, result.getColumnCount());
		
		boolean containsSomeNonZeroData = false;
		
		//All the first columns should total to the last column
		for (int r=0; r < result.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=2; c<7; c++) {
				srcTotal += result.getDouble(r, c);
			}
			
			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;
			
			//SUM of first 5 columns should equal the last column
			assertEquals(result.getDouble(r, 7), srcTotal, COMP_ERROR);
			
		}
		
		assertTrue(containsSomeNonZeroData);
		
	}
	
	@Test
	public void dataSanityCheckForHUC2() throws Exception {
		
		//Switches the predict data in the cache to the modified copy, which
		//has transport turned off above reach 9681.
		switchToModifiedPredictData();
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, AggregationLevel.HUC2);
		
		BuildTotalDeliveredLoadByStateSummaryReport action =
				new BuildTotalDeliveredLoadByStateSummaryReport(req);
		
		DataTable result = action.run();
		
		System.out.println("Dumping Table");
		DataTablePrinter.printDataTable(result, "The HUC2 Delivery Summary");
		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(9, result.getColumnCount());
		
		boolean containsSomeNonZeroData = false;
		
		//All the first columns should total to the last column
		for (int r=0; r < result.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=2; c<7; c++) {
				srcTotal += result.getDouble(r, c);
			}
			
			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;
			
			//SUM of first 5 columns should equal the last column
			assertEquals(result.getDouble(r, 7), srcTotal, COMP_ERROR);
			
		}
		
		assertTrue(containsSomeNonZeroData);
		
	}
	
	
}

