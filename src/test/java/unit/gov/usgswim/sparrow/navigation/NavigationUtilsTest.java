package gov.usgswim.sparrow.navigation;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class NavigationUtilsTest extends TestCase {
	int[][] BinaryTreeFlowTopo = {
			{1111,11124,0,1,1},
			{0100  ,16124,5,6,1}, 
			{1110,11124,0,1,1}, 
			{0110,11124,0,1,1}, 
			{0111,11124,0,1,1},//011
			{1010,11124,0,1,1}, 
			{111 ,11124,0,1,1}, 
			{1011,11124,0,1,1}, //101
			{0001,11124,0,1,1}, 
			{1000  ,16124,5,6,1}, 
			
			{11  ,11024,3,4,1}, 
			{0000,12124,4,5,1}, //000
			{1100,12124,4,5,1}, 
			{1101,12124,4,5,1}, 
			{110 ,10124,2,3,1}, // 11
			{0010  ,16124,5,6,1}, 
			{0011  ,16124,5,6,1}, //001
			{1001  ,16124,5,6,1}, //100

			{0101  ,16124,5,6,1}, // 010
 
	};
	/* PICTURE of Binary Tree Flow Topo (flow upward)
    0/\1
-----    ---------
/                   \
0/\1                 0/\1
-----    \               |    ----
/          \             /          \
0/\1        0/\1          0/\1         0/\1  
/    \       /    \       /    \       /    \
0/\1  0/\1   0/\1  0/\1   0/\1  0/\1   0/\1  0/\1


	1000
	0010
	0011
	0100
	001
	0001
	0000
	0111
	1111
	1001
	100
	1010
	1100
	0101
	0110
	011
	1110
	010
	111
	000
	01
	1101
	1011
	110
	11
	00
	101
	0
	10
	1



*/
/* PICTURE of Binary Tree Flow Topo (flow upward)
                     0/\1
                -----    ---------
              /                   \
            0/\1                 0/\1
       -----    \               |    ----
      /          \             /          \
    0/\1        0/\1          0/\1         0/\1  
   /    \       /    \       /    \       /    \
 0/\1  0/\1   0/\1  0/\1   0/\1  0/\1   0/\1  0/\1
 
 */
	
	String[] topoHeaders = {"reach_id", "fnode", "tnode", "iftran"};
	
	public void testFindUpstreamReachesSimpleLinearFlow() {
		
		DataTable topo = new SimpleDataTableWritable(PredictDataTestScenarios.linearFlowTopo, null, 0);
		PredictData pd = new PredictDataImm(topo, null, null, null, null, null, null);
		
		Long modelID = 22L;
		{	// find upstream reaches of the source reach
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(11124L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			assertEquals("the source reach should return only itself", 1, result.size());
		}
		
		{	// find upstream reaches of the destination reach
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(16124L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			assertEquals("the source reach should return only itself", 6, result.size());
		}
		
		{	// find upstream reaches of reach C
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(11024L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			assertEquals("the source reach should return only itself", 4, result.size());
		}
	}
	
	public void testFindUpstreamReachesSimpleLinearFlowWithIftranZero() {
		
		SimpleDataTableWritable topo = new SimpleDataTableWritable(PredictDataTestScenarios.linearFlowTopo, null, 0);
		int brokenRow = 2;
		topo.setValue(0, brokenRow, PredictData.IFTRAN_COL);
		PredictData pd = new PredictDataImm(topo, null, null, null, null, null, null);
		
		Long modelID = 22L;
		{	// find upstream reaches of the source reach
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(11124L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			// no effect here, as it occurs before iftran
			assertEquals("the source reach should return only itself", 1, result.size());
		}
		
		{	// find upstream reaches of the destination reach
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(16124L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			// note that the broken reach itself is included as an upstream, as it contributes to an iftran=1 reach
			assertEquals("the source reach should return only itself", 6 - brokenRow, result.size());
		}
		
		{	// find upstream reaches of reach C
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(11024L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			// note that the broken reach itself is included as an upstream, as it contributes to an iftran=1 reach
			assertEquals("the source reach should return only itself", 4 - brokenRow, result.size());
		}
	}
	
	public void testFindUpstreamReachesBinaryTreeFlow() {
		
		SimpleDataTableWritable topo = new SimpleDataTableWritable(BinaryTreeFlowTopo, null, 0);
		PredictData pd = new PredictDataImm(topo, null, null, null, null, null, null);
		
		Long modelID = 22L;
		{	// find upstream reaches of the source reach
			Set<Long> targetReach = new HashSet<Long>();
			targetReach.add(11124L); // source reach
			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
			// no effect here, as it occurs before iftran
			assertEquals("the source reach should return only itself", 1, result.size());
		}
		
//		{	// find upstream reaches of the destination reach
//			Set<Long> targetReach = new HashSet<Long>();
//			targetReach.add(16124L); // source reach
//			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
//			// note that the broken reach itself is included as an upstream, as it contributes to an iftran=1 reach
//			assertEquals("the source reach should return only itself", 6 - brokenRow, result.size());
//		}
		
//		{	// find upstream reaches of reach C
//			Set<Long> targetReach = new HashSet<Long>();
//			targetReach.add(11024L); // source reach
//			Set<Long> result = NavigationUtils.findUpStreamReaches(modelID, targetReach , pd);
//			// note that the broken reach itself is included as an upstream, as it contributes to an iftran=1 reach
//			assertEquals("the source reach should return only itself", 4 - brokenRow, result.size());
//		}
	}
}
