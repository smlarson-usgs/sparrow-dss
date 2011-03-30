package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.request.PredictionContextRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.ParserHelper;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

/**
 * @author eeverman
 */

public class PredictionContextHandlerLongRunTest extends SparrowTestBaseWithDB {

	
	
	/* We are actually testing the storage mechanism, so do not disable
	 * storage to the db.  (non-Javadoc)
	 * @see gov.usgswim.sparrow.SparrowDBTestBaseClass#disablePredictionContextPersistentStorage()
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
	public void addAndFetchFromDBviaAction() throws Exception {

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
	
	@Test
	public void addAndFetchFromDBviaSharedApplication() throws Exception {

		XMLStreamReader contextReader = getSharedXMLAsReader("predict-context-1.xml");
		ParserHelper.parseToStartTag(contextReader, PredictionContext.MAIN_ELEMENT_NAME);
		PredictionContext pc = PredictionContext.parseStream(contextReader);
		
		//Save to db
		SharedApplication.getInstance().putPredictionContext(pc);
		
		//Fetch from cache
		PredictionContext fromCache = SharedApplication.getInstance().getPredictionContext(pc.getId());
		assertEquals(pc.getId(), fromCache.getId());
		assertEquals(pc.getModelID(), fromCache.getModelID());
		assertEquals(pc.getAnalysis().getDataSeries(), fromCache.getAnalysis().getDataSeries());
		
		//Fetch directly from db
		PredictionContextRequest req = new PredictionContextRequest(new Long(pc.getId()));
		PredictionContextHandler action = new PredictionContextHandler(req);
		List<PredictionContext> list = action.run();
		assertTrue(list.size() == 1);
		PredictionContext pcFromDb = list.get(0);
		pcFromDb = list.get(0);
		
		assertEquals(pc.getId(), pcFromDb.getId());
		assertEquals(pc.getModelID(), pcFromDb.getModelID());
		assertEquals(pc.getAnalysis().getDataSeries(), pcFromDb.getAnalysis().getDataSeries());
	}
}

