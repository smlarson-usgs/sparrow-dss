package gov.usgswim.sparrow;

import gov.usgswim.sparrow.navigation.PredictDataTestScenarios;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class DeliveryRunnerTest extends TestCase {
	static final double TOLERANCE = .0000001;

	public void testDeliveryRunnerWithLinearFlow() {
		DeliveryRunner dr = new DeliveryRunner(PredictDataTestScenarios.makeLinearFlowPredictData());

		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(11124L); // source reach
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01, result[0], TOLERANCE);
			assertEquals(1 , result[1], TOLERANCE);
		}
		
		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(13124L); // reach A
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01 * .04 , result[0], TOLERANCE);
			assertEquals(.04 , result[1], TOLERANCE);
			assertEquals(1 , result[2], TOLERANCE);
		}
		
		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(10124L); // reach B
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01 * .04 * .09, result[0], TOLERANCE);
			assertEquals(.04  * .09, result[1], TOLERANCE);
			assertEquals(.09 , result[2], TOLERANCE);
			assertEquals(1 , result[3], TOLERANCE);
		}
		
		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(11024L); // reach C
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01 * .04 * .09 * .16, result[0], TOLERANCE);
			assertEquals(.04  * .09 * .16, result[1], TOLERANCE);
			assertEquals(.09 * .16, result[2], TOLERANCE);
			assertEquals(.16, result[3], TOLERANCE);
			assertEquals(1, result[4], TOLERANCE);
		}
		
//		Set<Long> targetReaches = new HashSet<Long>();
//		targetReaches.add(10124L); // reach A
//		double[][] result = dr.calculateDeliveryCoefficients(targetReaches);
//
//		for (double[] delCoefs: result) {
//			for (double delCoef: delCoefs) {
//				System.out.print(delCoef);
//				System.out.print("	");
//			}
//			System.out.println();
//		}
	}
	
	public void testDeliveryRunnerWithLinearFlowAndNonTransmit() {

		DeliveryRunner dr = new DeliveryRunner(PredictDataTestScenarios.makeLinearFlowPredictData());

		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(11124L); // source reach
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01, result[0], TOLERANCE);
			assertEquals(1 , result[1], TOLERANCE);
		}
		
		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(13124L); // reach A
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01 * .04 , result[0], TOLERANCE);
			assertEquals(.04 , result[1], TOLERANCE);
			assertEquals(1 , result[2], TOLERANCE);
		}
		
		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(10124L); // reach B
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01 * .04 * .09, result[0], TOLERANCE);
			assertEquals(.04  * .09, result[1], TOLERANCE);
			assertEquals(.09 , result[2], TOLERANCE);
			assertEquals(1 , result[3], TOLERANCE);
		}
		
		{
			Set<Long> targetReaches = new HashSet<Long>();
			targetReaches.add(11024L); // reach C
			double[] result = dr.calculateNodeTransportFraction(targetReaches);
			assertEquals("7 nodes returned", 7, result.length);
			assertEquals(.01 * .04 * .09 * .16, result[0], TOLERANCE);
			assertEquals(.04  * .09 * .16, result[1], TOLERANCE);
			assertEquals(.09 * .16, result[2], TOLERANCE);
			assertEquals(.16, result[3], TOLERANCE);
			assertEquals(1, result[4], TOLERANCE);
		}
	}
}
