package gov.usgswim.sparrow.test;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.util.SparrowUtil;
import junit.framework.TestCase;

public class PredictSimple_Test extends TestCase {


	public PredictSimple_Test(String testName) {
		super(testName);
	}


	public void testBasic1() throws Exception {
		int[][] topo = new int[][] {
				{0, 2, 1},
				{1, 2, 1},
				{2, 4, 1},
				{3, 4, 1},
				{5, 6, 1},
				{4, 6, 1},
				{6, 7, 1},
		};

		/* 3 sources */
		double[][] coef = new double[][] {
				{.2d, .3d, .4d},
				{.2, .3, .4},
				{.2, .3, .4},
				{.2, .3, .4},
				{.2, .3, .4},
				{.2, .3, .4},
				{.2, .3, .4},
		};

		/* 3 sources */
		double[][] decay = new double[][] {
				{.2d, .4d},
				{.2, .4},
				{.2, .4},
				{.2, .4},
				{.2, .4},
				{.2, .4},
				{.2, .4},
		};

		/* 3 sources */
		double[][] src = new double[][] {
				{1d, 2d, 3d},
				{1, 2, 3},
				{1, 2, 3},
				{1, 2, 3},
				{1, 2, 3},
				{1, 2, 3},
				{1, 2, 3},
		};

		DataTableWritable topoDT = new SimpleDataTableWritable(topo, null);
		DataTableWritable coefDT = new SimpleDataTableWritable(coef, null);
		DataTableWritable decayDT = new SimpleDataTableWritable(decay, null);
		DataTableWritable srcDT = new SimpleDataTableWritable(src, null);

		PredictRunner predictor = new PredictRunner(topoDT, coefDT, srcDT, decayDT);
		DataTable pred = predictor.doPredict();

		SparrowUtil.printDataTable(pred, "Predicted Values"); 

	}
}
