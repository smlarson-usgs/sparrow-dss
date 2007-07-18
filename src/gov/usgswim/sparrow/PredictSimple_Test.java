package gov.usgswim.sparrow;

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
		
		Int2D topoD = new Int2D(topo);
	  Double2DImm coefD = new Double2DImm(coef);
	  Double2DImm decayD = new Double2DImm(decay);
	  Double2DImm srcD = new Double2DImm(src);
		
		PredictSimple predictor = new PredictSimple(topoD, coefD, srcD, decayD);
		Double2DImm pred = predictor.doPredict();

		SparrowUtil.print2DArray(pred, "Predicted Values"); 
		
	}
}
