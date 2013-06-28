package gov.usgswim.sparrow;

import org.junit.*;
import static org.junit.Assert.assertEquals;

public class UncertaintyDataImmTest {

	private float uncertaintyArray[][];
	UncertaintyData uncertaintyData;
	
	@Test
	public void basicTest() throws Exception {
		
		//The first value
		assertEquals(1f, uncertaintyData.getMean(0), 0f);
		assertEquals(.1f, uncertaintyData.getStandardError(0), 0f);
		assertEquals((.1f/1f), uncertaintyData.calcCoeffOfVariation(0), 0f);
		
		//A zero value for the mean
		assertEquals(0f, uncertaintyData.getMean(2), 0f);
		assertEquals(.3f, uncertaintyData.getStandardError(2), 0f);
		assertEquals(0d, uncertaintyData.calcCoeffOfVariation(2), 0f);
		
		//A negative value for mean
		assertEquals(-4f, uncertaintyData.getMean(3), 0f);
		assertEquals(.4f, uncertaintyData.getStandardError(3), 0f);
		assertEquals(0d, uncertaintyData.calcCoeffOfVariation(3), 0f);
		
		//The last value
		assertEquals(5f, uncertaintyData.getMean(4), 0f);
		assertEquals(-.5f, uncertaintyData.getStandardError(4), 0f);
		assertEquals(-.5f/5f, uncertaintyData.calcCoeffOfVariation(4), 0.00000001f);
	}
	
	@Before
	public void buildDataset() {
		uncertaintyArray = new float[][] {
			new float[] {1f,  2f,   0f,  -4f, 5f}, /* mean (bias adj) */
			new float[] {.1f, .2f, .3f, .4f, -.5f} /* standard error */
		};

		uncertaintyData = new UncertaintyDataImm(uncertaintyArray);
	}
	
	
}
