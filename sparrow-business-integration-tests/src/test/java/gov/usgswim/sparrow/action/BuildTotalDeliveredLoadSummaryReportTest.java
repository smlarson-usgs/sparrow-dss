package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableSet;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author eeverman
 */
public class BuildTotalDeliveredLoadSummaryReportTest extends DeliveryBase {


	static final double COMP_ERROR = .0000001d;


	@Test
	public void dataSanityCheck() throws Exception {

		//Switches the predict data in the cache to the modified copy, which
		//has transport turned off above reach 9681.
		switchToModifiedPredictData();

		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);

		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, false);

		BuildTotalDeliveredLoadSummaryReport action = new BuildTotalDeliveredLoadSummaryReport(req);

		DataTableSet result = action.run();
		DataTable dataTable = result.getTable(1);

		assertNotNull(result);
		assertEquals(13, result.getColumnCount());
		boolean containsSomeNonZeroData = false;

		//All the first columns should total to the last column
		for (int r=0; r < dataTable.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=0; c<(dataTable.getColumnCount() - 1); c++) {
				srcTotal += dataTable.getDouble(r, c);
			}

			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;

			//SUM of non-last columns should equal the last column

			assertEquals(dataTable.getDouble(r, dataTable.getColumnCount() - 1), srcTotal, COMP_ERROR);

		}

		assertTrue(containsSomeNonZeroData);


	}



}

