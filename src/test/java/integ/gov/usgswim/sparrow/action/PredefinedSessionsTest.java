package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * An integration test of the CRUD actions for PredefinedSession.
 * @author eeverman
 *
 */
public class PredefinedSessionsTest extends SparrowDBTest {
	
	private PredefinedSession ps1;
	private PredefinedSession ps2;
	private PredefinedSession ps3;
	private PredefinedSession savedPs1;
	private PredefinedSession savedPs2;
	private PredefinedSession savedPs3;
	private GregorianCalendar today;
	private GregorianCalendar yesterday;
	
	@Before
	public void initSessions() throws Exception {
		
		this.setLogLevel(Level.DEBUG);
		
		//Construct a calendar date for today that does not include time.
		today = new GregorianCalendar();
		
		today = new GregorianCalendar(
				today.get(Calendar.YEAR),
				today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));
		yesterday = new GregorianCalendar(
				today.get(Calendar.YEAR),
				today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		
		
		ps1 = new PredefinedSession();
		ps1.setAddBy("Eric");
		ps1.setAddContactInfo("608.821.1111");
		//ps1.setAddDate(new Date(today.getTimeInMillis()));	//is autoset
		ps1.setAddNote("Please approve me");
		//ps1.setApproved(false);	//should default to false and not allow 'true' on new records
		ps1.setContextString("context");
		ps1.setDescription("desc");
		ps1.setGroupName("myGroup");
		ps1.setModelId(50L);
		ps1.setName("Session 1");
		ps1.setPredefinedSessionType(PredefinedSessionType.FEATURED);
		ps1.setSortOrder(1);
		//ps1.setUniqueCode("veryUnique1");	//auto-create unique code
		
		//
		ps2 = new PredefinedSession();
		ps2.setAddBy("I-Lin");
		ps2.setAddContactInfo("608.821.1112");
		ps2.setAddDate(new Date(yesterday.getTimeInMillis()));	//should be ignored - reset to today
		ps2.setAddNote("Please approve me");
		ps2.setApproved(true);	//ignored and set to false
		ps2.setContextString("context");
		ps2.setDescription("desc");
		ps2.setGroupName("myGroup");
		ps2.setModelId(50L);
		ps2.setName("Session 2");
		ps2.setPredefinedSessionType(PredefinedSessionType.LISTED);
		ps2.setSortOrder(4);
		ps2.setUniqueCode("veryUnique2");	//auto-create unique code
		
		//
		ps3 = new PredefinedSession();
		ps3.setAddBy("Lorraine");
		ps3.setAddContactInfo("608.821.1113");
		//ps3.setAddDate(new Date(yesterday.getTimeInMillis()));	//should be ignored - reset to today
		ps3.setAddNote("Please approve me");
		ps3.setApproved(false);
		ps3.setContextString("context");
		ps3.setDescription("desc");
		ps3.setGroupName("myGroup");
		ps3.setModelId(50L);
		ps3.setName("Session 3");
		ps3.setPredefinedSessionType(PredefinedSessionType.UNLISTED);
		ps3.setSortOrder(6);
		ps3.setUniqueCode("veryUnique3");	//auto-create unique code
		
		//Save them all
		SavePredefinedSession saveAction3 = new SavePredefinedSession(ps3);
		SavePredefinedSession saveAction2 = new SavePredefinedSession(ps2);
		SavePredefinedSession saveAction1 = new SavePredefinedSession(ps1);
		
		savedPs3 = saveAction3.run();
		savedPs2 = saveAction2.run();
		savedPs1 = saveAction1.run();
	}
	
	@After
	public void cleanup() throws Exception {
		deleteSessions(savedPs1, savedPs2, savedPs3);
	}
	
	
	
	@Test
	public void verifyValuesAndDeleteSessions() throws Exception {

		//
		assertEquals(ps1.getAddBy(), savedPs1.getAddBy());
		assertEquals(ps1.getAddContactInfo(), savedPs1.getAddContactInfo());
		assertEquals(today.getTime().getTime(), savedPs1.getAddDate().getTime());
		assertEquals(ps1.getAddNote(), savedPs1.getAddNote());
		assertEquals(false, savedPs1.getApproved());
		assertEquals(ps1.getContextString(), savedPs1.getContextString());
		assertEquals(ps1.getDescription(), savedPs1.getDescription());
		assertEquals(ps1.getGroupName(), savedPs1.getGroupName());
		assertEquals(ps1.getModelId(), savedPs1.getModelId());
		assertEquals(ps1.getName(), savedPs1.getName());
		assertEquals(ps1.getPredefinedSessionType(), savedPs1.getPredefinedSessionType());
		assertEquals(ps1.getSortOrder(), savedPs1.getSortOrder());
		assertTrue(savedPs1.getUniqueCode().length() == 5);
		assertTrue(savedPs1.getId() != 0 && savedPs1.getId() != null);
		
		//
		assertEquals(ps2.getAddBy(), savedPs2.getAddBy());
		assertEquals(ps2.getAddContactInfo(), savedPs2.getAddContactInfo());
		assertEquals(today.getTime().getTime(), savedPs2.getAddDate().getTime());
		assertEquals(ps2.getAddNote(), savedPs2.getAddNote());
		assertEquals(false, savedPs2.getApproved());
		assertEquals(ps2.getContextString(), savedPs2.getContextString());
		assertEquals(ps2.getDescription(), savedPs2.getDescription());
		assertEquals(ps2.getGroupName(), savedPs2.getGroupName());
		assertEquals(ps2.getModelId(), savedPs2.getModelId());
		assertEquals(ps2.getName(), savedPs2.getName());
		assertEquals(ps2.getPredefinedSessionType(), savedPs2.getPredefinedSessionType());
		assertEquals(ps2.getSortOrder(), savedPs2.getSortOrder());
		assertEquals(ps2.getUniqueCode(), savedPs2.getUniqueCode());
		assertTrue(savedPs2.getId() != 0 && savedPs2.getId() != null);
		
		//
		assertEquals(ps3.getAddBy(), savedPs3.getAddBy());
		assertEquals(ps3.getAddContactInfo(), savedPs3.getAddContactInfo());
		assertEquals(today.getTime().getTime(), savedPs3.getAddDate().getTime());
		assertEquals(ps3.getAddNote(), savedPs3.getAddNote());
		assertEquals(false, savedPs3.getApproved());
		assertEquals(ps3.getContextString(), savedPs3.getContextString());
		assertEquals(ps3.getDescription(), savedPs3.getDescription());
		assertEquals(ps3.getGroupName(), savedPs3.getGroupName());
		assertEquals(ps3.getModelId(), savedPs3.getModelId());
		assertEquals(ps3.getName(), savedPs3.getName());
		assertEquals(ps3.getPredefinedSessionType(), savedPs3.getPredefinedSessionType());
		assertEquals(ps3.getSortOrder(), savedPs3.getSortOrder());
		assertEquals(ps3.getUniqueCode(), savedPs3.getUniqueCode());
		assertTrue(savedPs3.getId() != 0 && savedPs3.getId() != null);
		
		//////////////////
		//Try loading all three sessions
		LoadPredefinedSessions loadAction = new LoadPredefinedSessions(50L);
		List<PredefinedSession> sessionList = loadAction.run();
		
		//They should be in the specified sort order
		assertEquals(savedPs1.getId(), sessionList.get(0).getId());
		assertEquals(savedPs2.getId(), sessionList.get(1).getId());
		assertEquals(savedPs3.getId(), sessionList.get(2).getId());
		assertTrue(sessionList.size() == 3);
		
		//////////////////
		//Try loading a single session by the unique code
		loadAction = new LoadPredefinedSessions(savedPs1.getUniqueCode());
		sessionList = loadAction.run();
		
		//They should be in the specified sort order
		assertEquals(savedPs1.getId(), sessionList.get(0).getId());
		assertTrue(sessionList.size() == 1);
		
		
		//now delete each record
		deleteSessions(savedPs1, savedPs2, savedPs3);
		
		//The db should now be empty
		loadAction = new LoadPredefinedSessions(50L);
		sessionList = loadAction.run();
		
		for (PredefinedSession session : sessionList) {
			if (
					session.getId().equals(savedPs1.getId()) ||
					session.getId().equals(savedPs2.getId()) ||
					session.getId().equals(savedPs3.getId()) ) {
				fail("A PredefinedSession that was supposed to be deleted was found in the db.");
			}
		}
		
	}
	
	@Test
	public void saveAndUpdateASession() throws Exception {

		savedPs1.setApproved(true);
		SavePredefinedSession saveAction1 = new SavePredefinedSession(savedPs1);
		saveAction1.run();
		
		//Load the session back and verify
		LoadPredefinedSessions loadAction = new LoadPredefinedSessions(50L);
		List<PredefinedSession> sessionList = loadAction.run();
		sessionList = loadAction.run();
		
		PredefinedSession updatedPs1 = sessionList.get(0);
		
		//
		assertEquals(ps1.getAddBy(), updatedPs1.getAddBy());
		assertEquals(ps1.getAddContactInfo(), updatedPs1.getAddContactInfo());
		assertEquals(today.getTime().getTime(), updatedPs1.getAddDate().getTime());
		assertEquals(ps1.getAddNote(), updatedPs1.getAddNote());
		assertEquals(true, updatedPs1.getApproved());
		assertEquals(ps1.getContextString(), updatedPs1.getContextString());
		assertEquals(ps1.getDescription(), updatedPs1.getDescription());
		assertEquals(ps1.getGroupName(), updatedPs1.getGroupName());
		assertEquals(ps1.getModelId(), updatedPs1.getModelId());
		assertEquals(ps1.getName(), updatedPs1.getName());
		assertEquals(ps1.getPredefinedSessionType(), updatedPs1.getPredefinedSessionType());
		assertEquals(ps1.getSortOrder(), updatedPs1.getSortOrder());
		assertTrue(updatedPs1.getUniqueCode().length() == 5);
		assertTrue(updatedPs1.getId() != 0 && updatedPs1.getId() != null);
		
	}
	
	@Test
	public void loadFromCache() throws Exception {
		
		long startTime = System.currentTimeMillis();
		List<PredefinedSession> sessionList = SharedApplication.getInstance().getPredefinedSessions(50L, true);
		long endTime = System.currentTimeMillis();
		//Very fast - just checks that there is nothing there
		assertTrue(endTime - startTime < 10L);
		
		//Load now for the first time
		startTime = System.currentTimeMillis();
		sessionList = SharedApplication.getInstance().getPredefinedSessions(50L);
		endTime = System.currentTimeMillis();
		//System.out.println("Time to fill cache: " + (endTime - startTime));
		assertEquals(3, sessionList.size());
		
		//Load again - should be instant
		startTime = System.currentTimeMillis();
		sessionList = SharedApplication.getInstance().getPredefinedSessions(50L);
		endTime = System.currentTimeMillis();
		//Very fast - just checks that there is nothing there
		//System.out.println("Time to load from cache (prepopulated): " + (endTime - startTime));
		assertTrue(endTime - startTime < 10L);
		assertEquals(3, sessionList.size());
	}
	
	@Test
	public void cacheIsFlushedAfterUpdateOrDelete() throws Exception {
		
		//Force sessions to be loaded to cache
		List<PredefinedSession> orgSessionList =
			SharedApplication.getInstance().getPredefinedSessions(50L);
		assertFalse(orgSessionList.get(0).getApproved());
		
		//Update one of the sessions
		savedPs1.setApproved(true);
		SharedApplication.getInstance().savePredefinedSession(savedPs1);
		
		//Reload session list and check for update
		List<PredefinedSession> updatedSessionList =
			SharedApplication.getInstance().getPredefinedSessions(50L);
		
		assertTrue(updatedSessionList.get(0).getApproved());
		
		//Delete a session
		SharedApplication.getInstance().deletePredefinedSession(updatedSessionList.get(0));
		
		//Check that its gone
		List<PredefinedSession> deletedSessionList =
			SharedApplication.getInstance().getPredefinedSessions(50L);
		assertTrue(deletedSessionList.size() == orgSessionList.size() - 1);
	}
	
	protected void deleteSessions(PredefinedSession... sessions) throws Exception {
		Exception wasThrown = null;
		
		for (PredefinedSession session : sessions) {
			if (session.getId() != null) {
				//If the session still has an ID, it wasn't deleted
				try {
					DeletePredefinedSession deleteAction = new DeletePredefinedSession(session);
					deleteAction.run();
				} catch (Exception e) {
					wasThrown = e;
				}
			} else {
				//The session was already deleted/disassociated from the db
			}
		}
		
		if (wasThrown != null) {
			throw wasThrown;
		}
	}
	
}
