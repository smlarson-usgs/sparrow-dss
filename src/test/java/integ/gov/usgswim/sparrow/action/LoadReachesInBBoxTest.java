package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.domain.ModelBBox;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.domain.SparrowModel;

import java.util.List;

import org.junit.Test;

/**
 * Verifies that we load the correct reaches in a bbox listing.
 * 
 * @author eeverman
 */
public class LoadReachesInBBoxTest extends SparrowDBTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testLoadABBox() throws Exception {
		
		LoadReachesInBBox action = new LoadReachesInBBox();
		
		ModelBBox modelBBox = new ModelBBox(TEST_MODEL_ID,
				-83.54141235351562d, 27.428741455078125d,
				-81.94839477539062d, 28.291168212890625d);
		action.setModelBBox(modelBBox);
		
		Long[] actual = action.run();
		Long[] expected = buildSampleList();
		
//		for (Long L : actual) {
//			System.out.println(L);
//		}
		
		assertArrayEquals(expected, actual);
	}
	
	
	private Long[] buildSampleList() {
		Long[] result = new Long[] {
				7546L,
				7550L,
				7551L,
				7556L,
				7567L,
				7568L,
				7569L,
				7570L,
				7571L,
				7572L,
				7573L,
				7574L,
				7575L,
				7576L,
				7577L,
				7578L,
				7579L,
				7580L,
				7581L,
				7582L,
				7583L,
				7584L,
				7585L,
				7586L,
				7587L,
				7588L,
				7589L,
				7590L,
				7591L,
				7592L,
				7593L,
				7594L,
				7595L,
				7596L,
				7597L,
				7598L,
				7599L,
				7600L,
				7601L,
				7602L,
				7603L,
				7604L,
				7605L,
				7609L,
				10124L,
				10737L,
				10738L,
				10739L,
				10740L,
				81137L,
				81138L,
				81139L,
				81140L,
				81141L,
				81142L,
				81143L,
				81144L,
				81145L,
				81146L,
				81147L,
				81149L,
				81151L,
				81152L,
				81819L,
				81820L,
				81821L,
				81822L,
				81823L,
				657890L,
				657940L,
				657950L,
				657960L,
				657970L,
				657980L,
				657990L,
				658010L,
				658020L,
				658030L,
				658040L,
				658060L,
				661550L,
				661560L,
				661570L,
				661640L,
				661720L,
				661730L,
				661940L,
				664280L,
				664290L
		};
		return result;
	}
	
}