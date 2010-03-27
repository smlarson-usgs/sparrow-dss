package gov.usgswim.sparrow.navigation;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.util.DLUtils;

public class PredictDataTestScenarios {

	public static int[][] linearFlowTopo = {
		{11124,11124,0,1,1}, // source
		{13124,13124,1,2,1}, // A
		{10124,10124,2,3,1}, // B
		{11024,11024,3,4,1}, // C
		{12124,12124,4,5,1}, // D
		{16124,16124,5,6,1}, // destination
	};
	// PICTURE of Linear Flow Topo
	// source -> A -> B -> C -> D -> destination
	public static double[][] singleSource = {
		// source values are irrelevant for this test
		{1.1},
		{1.2},
		{1.3},
		{1.4},
		{1.5},
		{1.6}
	};
	public static String[] singleSourceHeadings = {"knowledge"};
	public static double[][] singleDecay = {
		{.1, .01},
		{.2, .04},
		{.3, .09},
		{.4, .16},
		{.5, .25},
		{.6, .36}
	};

	public static PredictData makeLinearFlowPredictData() {
		PredictDataBuilder pd = new PredictDataBuilder();
		{		
			DataTable topo = new SimpleDataTableWritable(PredictDataTestScenarios.linearFlowTopo, null, 0);
			DataTable coef = null;
			DataTable src = new SimpleDataTableWritable(PredictDataTestScenarios.singleSource, PredictDataTestScenarios.singleSourceHeadings, DLUtils.DO_NOT_INDEX);
			DataTable decay = new SimpleDataTableWritable(PredictDataTestScenarios.singleDecay, null, DLUtils.DO_NOT_INDEX);

			pd.setSrcMetadata( null);
			pd.setTopo( topo);
			pd.setCoef( src );
			pd.setDelivery( decay );
			pd.setSrc( src);
		}
		return pd;
	}

}
