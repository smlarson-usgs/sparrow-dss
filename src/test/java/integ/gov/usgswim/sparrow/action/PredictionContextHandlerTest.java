package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.request.PredictionContextRequest;
import gov.usgswim.sparrow.util.ParserHelper;

import org.junit.Test;

/**
 * @author eeverman
 */

public class PredictionContextHandlerTest extends SparrowDBTest {

	
	
	/* We are actually testing the storage mechanism, so do not disable
	 * storage to the db.  (non-Javadoc)
	 * @see gov.usgswim.sparrow.SparrowDBTest#disablePredictionContextPersistentStorage()
	 */
	@Override
	public boolean disablePredictionContextPersistentStorage() {
		return false;
	}

	/**
	 * Tests inserting and retrieving from the db.
	 * @throws Exception
	 */
	@Test
	public void addAndFetchFromDB() throws Exception {

		XMLStreamReader contextReader = getSharedXMLAsReader("predict-context-1.xml");
		ParserHelper.parseToStartTag(contextReader, PredictionContext.MAIN_ELEMENT_NAME);
		PredictionContext pc = PredictionContext.parseStream(contextReader);
		
		//Save to db
		PredictionContextRequest req = new PredictionContextRequest(pc);
		PredictionContextHandler action = new PredictionContextHandler(req);
		List<PredictionContext> list = action.run();
		assertTrue(list.size() == 1);
		PredictionContext pcFromDb = list.get(0);
		
		assertEquals(pc.getId(), pcFromDb.getId());
		
		//Fetch from db
		req = new PredictionContextRequest(new Long(pc.getId()));
		action = new PredictionContextHandler(req);
		list = action.run();
		assertTrue(list.size() == 1);
		pcFromDb = list.get(0);
		
		assertEquals(pc.getId(), pcFromDb.getId());
		assertEquals(pc.getModelID(), pcFromDb.getModelID());
		assertEquals(pc.getAnalysis().getDataSeries(), pcFromDb.getAnalysis().getDataSeries());
		
	}
	
	
}

