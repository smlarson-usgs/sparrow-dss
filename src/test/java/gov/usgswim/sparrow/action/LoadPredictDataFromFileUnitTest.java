package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test (offline) of the CalcAggregate Action.
 * 
 * TODO:  Nulls are not tested because some of the data2D classes choke on null
 * values, even though some of our values may actually be nulls.
 * 
 * @author eeverman
 *
 */
public class LoadPredictDataFromFileUnitTest {

	PredictData model50;

	@Before
	public void setup() throws Exception {
		
//		InputStream stream =
//			getClass().getResourceAsStream("/gov/usgswim/sparrow/test/shared/model50/src_metadata.txt");
//		assertNotNull(stream);
//		stream.close();
//		stream = null;
//		
//		stream =
//			Thread.currentThread().getContextClassLoader().getResourceAsStream("/gov/usgswim/sparrow/test/sample/tab_delimit_sample_heading.txt");
//		assertNotNull(stream);
//		stream.close();
//		stream = null;
		
		
		
		LoadModelPredictDataFromFile action = new LoadModelPredictDataFromFile(50L);
		model50 = action.run();
	}
	
	
	@Test
	public void basicTest() throws Exception {
		assertNotNull(model50.getSrcMetadata());
		assertNotNull(model50.getTopo());
		assertNotNull(model50.getCoef());
		assertNotNull(model50.getDelivery());
		assertNotNull(model50.getSrc());
		
		//Do we have rows and sources > 0?
		assertTrue(model50.getTopo().getRowCount() > 0);
		assertTrue(model50.getSrcMetadata().getRowCount() > 0);
		
		int rowCount = model50.getTopo().getRowCount();
		int srcCount = model50.getSrcMetadata().getRowCount();
		
		//Check the coef table
		assertEquals(rowCount, model50.getCoef().getRowCount());
		assertEquals(srcCount, model50.getCoef().getColumnCount());
		
		//Check the delivery table
		assertEquals(rowCount, model50.getDelivery().getRowCount());
		assertEquals(2, model50.getDelivery().getColumnCount());
		
		//Check the src table
		assertEquals(rowCount, model50.getSrc().getRowCount());
		assertEquals(srcCount, model50.getSrc().getColumnCount());
		
		//Check some actual values
		assertEquals(SparrowUnits.KG_PER_YEAR, model50.getModel().getUnits());
		
		//Check model.source metadata some source values
		assertEquals(SparrowUnits.KG_PER_YEAR, model50.getModel().getSource(1).getUnits());
		assertEquals(SparrowUnits.KG_PER_YEAR, model50.getModel().getSource(2).getUnits());
		assertEquals(SparrowUnits.SQR_KM, model50.getModel().getSource(3).getUnits());
		assertEquals(SparrowUnits.KG_PER_YEAR, model50.getModel().getSource(4).getUnits());
		assertEquals(SparrowUnits.KG_PER_YEAR, model50.getModel().getSource(5).getUnits());
		
		//Check source metadata
		assertEquals(SparrowUnits.KG_PER_YEAR.getUserName(), model50.getSrcMetadata().getString(0, 5));
		assertEquals(SparrowUnits.KG_PER_YEAR.getUserName(), model50.getSrcMetadata().getString(1, 5));
		assertEquals(SparrowUnits.SQR_KM.getUserName(), model50.getSrcMetadata().getString(2, 5));
		assertEquals(SparrowUnits.KG_PER_YEAR.getUserName(), model50.getSrcMetadata().getString(3, 5));
		assertEquals(SparrowUnits.KG_PER_YEAR.getUserName(), model50.getSrcMetadata().getString(4, 5));
	}
}
