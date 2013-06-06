package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import junit.framework.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class CalcContributingFractionedAreaForRegionsTest extends CalcFractionalAreaBaseTest {


	@Test
	public void testTest() {
		assertNotNull(network1_reach_state_relation);
		assertEquals(4, network1_reach_state_relation.getRelationsForReachRow(5).getRelations().size());
		assertNotNull(network1_inc_area);
		assertEquals(14, network1_inc_area.getRowCount());
		assertNotNull(network1_region_detail);
		assertEquals(6, network1_region_detail.getRowCount());
		assertNotNull(SharedApplication.getInstance().getPredictData(network1_model_id));
		assertEquals(14, SharedApplication.getInstance().getPredictData(network1_model_id).getTopo().getRowCount());

	}

	@Test
	public void checkNetwork1DeliveryToReach11() throws Exception {


		CalcContributingFractionedAreaForRegions action = new
				CalcContributingFractionedAreaForRegions(
				to_11_targets,
				AggregationLevel.STATE,
				network1_reach_state_relation,
				watershedAreaFractionMap,
				network1_inc_area,
				network1_region_detail
			);


		ColumnData result = action.run();


		DataTable expected =  loadAreaTable(this.getClass(), "to_11", "tab");

		assertTrue( this.compareColumns(expected.getColumn(1), result, false, false, .0000001D) );


	}


	protected DataTable loadAreaTable(Class<?> forClass, String fileSuffix, String fileExtension)
			throws Exception {

		InputStream fileInputStream = getResource(forClass, fileSuffix, fileExtension);


		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));
		String[] headings = {"ID", "VALUE"};
		Class<?>[] types = {Integer.class, Double.class};
		DataTableWritable dtw = new SimpleDataTableWritable(headings, null, types);
		dtw.setName("values");
		DataTableUtils.fill(dtw, fileReader, false, "\t", true);
		fileReader.close();

		return dtw.toImmutable();
	}
}
