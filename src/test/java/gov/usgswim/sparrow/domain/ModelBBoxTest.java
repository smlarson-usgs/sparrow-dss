package gov.usgswim.sparrow.domain;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.usgswim.sparrow.SparrowUnitTestBaseClass;

public class ModelBBoxTest extends SparrowUnitTestBaseClass {

	final double err = .00000000001d;
	final double LEFT_LONG = -83.54141235351562d;
	final double LOWER_LAT = 27.428741455078125d;
	final double RIGHT_LONG = -81.94839477539062d;
	final double UPPER_LAT = 28.291168212890625d;
	
	@Test
	public void explicitBounds() throws Exception {
		ModelBBox modelBBox = new ModelBBox(TEST_MODEL_ID,
				-83.54141235351562d, 27.428741455078125d,
				-81.94839477539062d, 28.291168212890625d);
		
		assertEquals(TEST_MODEL_ID, modelBBox.getModelId());
		assertEquals(LEFT_LONG, modelBBox.getLeftLongBound(), err);
		assertEquals(LOWER_LAT, modelBBox.getLowerLatBound(), err);
		assertEquals(RIGHT_LONG, modelBBox.getRightLongBound(), err);
		assertEquals(UPPER_LAT, modelBBox.getUpperLatBound(), err);
	}
	
	@Test
	public void stringParseSameAsExplicit() throws Exception {
		
		String bnd = 
			Double.toString(LEFT_LONG) + "," +
			Double.toString(LOWER_LAT) + "," +
			Double.toString(RIGHT_LONG) + "," +
			Double.toString(UPPER_LAT);
			
		ModelBBox modelBBox = new ModelBBox(TEST_MODEL_ID, bnd);
		
		assertEquals(TEST_MODEL_ID, modelBBox.getModelId());
		assertEquals(LEFT_LONG, modelBBox.getLeftLongBound(), err);
		assertEquals(LOWER_LAT, modelBBox.getLowerLatBound(), err);
		assertEquals(RIGHT_LONG, modelBBox.getRightLongBound(), err);
		assertEquals(UPPER_LAT, modelBBox.getUpperLatBound(), err);
	}
	
	@Test
	public void stringWithSpacesParseSameAsExplicit() throws Exception {
		
		String bnd = 
			Double.toString(LEFT_LONG) + " , " +
			Double.toString(LOWER_LAT) + " , " +
			Double.toString(RIGHT_LONG) + "        , " +
			Double.toString(UPPER_LAT);
			
		ModelBBox modelBBox = new ModelBBox(TEST_MODEL_ID, bnd);
		
		assertEquals(TEST_MODEL_ID, modelBBox.getModelId());
		assertEquals(LEFT_LONG, modelBBox.getLeftLongBound(), err);
		assertEquals(LOWER_LAT, modelBBox.getLowerLatBound(), err);
		assertEquals(RIGHT_LONG, modelBBox.getRightLongBound(), err);
		assertEquals(UPPER_LAT, modelBBox.getUpperLatBound(), err);
	}
	
	@Test
	public void doulbeArraySameAsExplicit() throws Exception {
		
		double[] bnd = new double[] {
				LEFT_LONG, LOWER_LAT, RIGHT_LONG, UPPER_LAT
		};

			
		ModelBBox modelBBox = new ModelBBox(TEST_MODEL_ID, bnd);
		
		assertEquals(TEST_MODEL_ID, modelBBox.getModelId());
		assertEquals(LEFT_LONG, modelBBox.getLeftLongBound(), err);
		assertEquals(LOWER_LAT, modelBBox.getLowerLatBound(), err);
		assertEquals(RIGHT_LONG, modelBBox.getRightLongBound(), err);
		assertEquals(UPPER_LAT, modelBBox.getUpperLatBound(), err);
	}
}
