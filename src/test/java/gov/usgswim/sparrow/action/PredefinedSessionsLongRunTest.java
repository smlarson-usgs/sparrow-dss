package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.request.PredefinedSessionUniqueRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * An integration test of the CRUD actions for PredefinedSession.
 * @author eeverman
 *
 */
public class PredefinedSessionsLongRunTest extends SparrowDBTestBaseClass {
	
	private PredefinedSessionBuilder ps1;
	private PredefinedSessionBuilder ps2;
	private PredefinedSessionBuilder ps3;
	private IPredefinedSession savedPs1;
	private IPredefinedSession savedPs2;
	private IPredefinedSession savedPs3;
	private GregorianCalendar today;
	private GregorianCalendar yesterday;
	
	
	@Before
	public void initSessions() throws Exception {
		
		//this.setLogLevel(Level.DEBUG);
		
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
		
		
		//We do some cache testing that is independant by test method,
		//so we need to clear the cache before each test
		SparrowCacheManager.getInstance().clearAll();
		
		PredefinedSessionBuilder[] pss = createUnsavedPredefinedSessions();
		
		ps1 = pss[0];
		ps2 = pss[1];
		ps3 = pss[2];
		
		//Save them all
		IPredefinedSession[] saved = saveSessions(pss);
		
		savedPs1 = saved[0];
		savedPs2 = saved[1];
		savedPs3 = saved[2];
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
		LoadPredefinedSessions loadAction = new LoadPredefinedSessions(9999L);
		List<IPredefinedSession> sessionList = loadAction.run();
		
		//They should be in the specified sort order
		assertEquals(savedPs1.getId(), sessionList.get(0).getId());
		assertEquals(savedPs2.getId(), sessionList.get(1).getId());
		assertEquals(savedPs3.getId(), sessionList.get(2).getId());
		assertTrue(sessionList.size() == 3);
		
		//////////////////
		//Try loading a single session by the unique code
		PredefinedSessionUniqueRequest psur = new PredefinedSessionUniqueRequest(savedPs1.getUniqueCode());
		LoadPredefinedSession loadUnique = new LoadPredefinedSession(psur);
		IPredefinedSession uniqueSession = loadUnique.run();
		
		//They should be in the specified sort order
		assertEquals(savedPs1.getId(), uniqueSession.getId());
		
		
		//now delete each record
		deleteSessions(savedPs1, savedPs2, savedPs3);
		
		//The db should now be empty
		loadAction = new LoadPredefinedSessions(9999L);
		sessionList = loadAction.run();
		
		for (IPredefinedSession session : sessionList) {
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

		PredefinedSessionBuilder updatedPs1 = new PredefinedSessionBuilder(savedPs1);
		
		updatedPs1.setApproved(true);
		SavePredefinedSession saveAction1 = new SavePredefinedSession(updatedPs1);
		saveAction1.run();
		
		//Load the session back and verify
		LoadPredefinedSessions loadAction = new LoadPredefinedSessions(9999L);
		List<IPredefinedSession> sessionList = loadAction.run();
		sessionList = loadAction.run();
		
		IPredefinedSession reloadedPs1 = sessionList.get(0);
		
		//
		assertEquals(savedPs1.getAddBy(), reloadedPs1.getAddBy());
		assertEquals(savedPs1.getAddContactInfo(), reloadedPs1.getAddContactInfo());
		assertEquals(today.getTime().getTime(), reloadedPs1.getAddDate().getTime());
		assertEquals(savedPs1.getAddNote(), reloadedPs1.getAddNote());
		assertEquals(true, reloadedPs1.getApproved());
		assertEquals(savedPs1.getContextString(), reloadedPs1.getContextString());
		assertEquals(savedPs1.getDescription(), reloadedPs1.getDescription());
		assertEquals(savedPs1.getGroupName(), reloadedPs1.getGroupName());
		assertEquals(savedPs1.getModelId(), reloadedPs1.getModelId());
		assertEquals(savedPs1.getName(), reloadedPs1.getName());
		assertEquals(savedPs1.getPredefinedSessionType(), reloadedPs1.getPredefinedSessionType());
		assertEquals(savedPs1.getSortOrder(), reloadedPs1.getSortOrder());
		assertEquals(savedPs1.getUniqueCode(), reloadedPs1.getUniqueCode());
		assertEquals(savedPs1.getId(), reloadedPs1.getId());
		
	}
	
	@Test
	public void loadFromCache() throws Exception {
		
		
		//Load now for the first time
		long startTime = System.currentTimeMillis();
		PredefinedSessionRequest req = new PredefinedSessionRequest(9999L);
		List<IPredefinedSession> sessionList = SharedApplication.getInstance().getPredefinedSessions(req);
		long endTime = System.currentTimeMillis();
		//System.out.println("Time to fill cache: " + (endTime - startTime));
		assertEquals(3, sessionList.size());
		
		//Load again - should be instant
		startTime = System.currentTimeMillis();
		sessionList = SharedApplication.getInstance().getPredefinedSessions(req);
		endTime = System.currentTimeMillis();
		//Very fast - just checks that there is nothing there
		//System.out.println("Time to load from cache (prepopulated): " + (endTime - startTime));
		assertTrue(endTime - startTime < 10L);
		assertEquals(3, sessionList.size());
	}
	
	@Test
	public void cacheIsFlushedAfterUpdateOrDelete() throws Exception {
		
		//Force sessions to be loaded to cache
		PredefinedSessionRequest req = new PredefinedSessionRequest(9999L);
		List<IPredefinedSession> orgSessionList =
			SharedApplication.getInstance().getPredefinedSessions(req);
		assertFalse(orgSessionList.get(0).getApproved());
		
		//Update one of the sessions
		PredefinedSessionBuilder updatedPs1 = new PredefinedSessionBuilder(savedPs1);
		updatedPs1.setApproved(true);
		SharedApplication.getInstance().savePredefinedSession(updatedPs1);
		
		//Reload session list and check for update
		List<IPredefinedSession> updatedSessionList =
			SharedApplication.getInstance().getPredefinedSessions(req);
		
		assertTrue(updatedSessionList.get(0).getApproved());
		
		//Delete a session
		SharedApplication.getInstance().deletePredefinedSession(updatedSessionList.get(0));
		
		//Check that its gone
		List<IPredefinedSession> deletedSessionList =
			SharedApplication.getInstance().getPredefinedSessions(req);
		assertTrue(deletedSessionList.size() == orgSessionList.size() - 1);
	}
	
	@Test
	public void checkFilteredResults() throws Exception {
		PredefinedSessionBuilder set2ps1 = new PredefinedSessionBuilder(ps1);
		PredefinedSessionBuilder set2ps2 = new PredefinedSessionBuilder(ps2);
		PredefinedSessionBuilder set2ps3 = new PredefinedSessionBuilder(ps3);
		PredefinedSessionBuilder set3ps1 = new PredefinedSessionBuilder(ps1);
		PredefinedSessionBuilder set3ps2 = new PredefinedSessionBuilder(ps2);
		PredefinedSessionBuilder set3ps3 = new PredefinedSessionBuilder(ps3);
		
		PredefinedSessionBuilder[] newSessions = stripUniqueness(
			set2ps1, set2ps2, set2ps3, set3ps1, set3ps2, set3ps3);
		
		//Assign some group names
		newSessions[0].setGroupName("set2");
		newSessions[1].setGroupName("set2");
		newSessions[2].setGroupName("set2");
		newSessions[3].setGroupName("set3");
		newSessions[4].setGroupName("set3");
		newSessions[5].setGroupName("set3");
		
		//Our set2ps1 style references are now old
		newSessions = toBuilder(saveSessions(newSessions));
		
		//Set a few approved
		newSessions[0].setApproved(true);
		newSessions[1].setApproved(false);
		newSessions[2].setApproved(false);
		newSessions[3].setApproved(false);
		newSessions[4].setApproved(true);
		newSessions[5].setApproved(true);
		
		newSessions = toBuilder(saveSessions(newSessions));
		
		PredefinedSessionRequest request = new PredefinedSessionRequest(9999L, (String)null);
		//Should be nine all together (no criteria)
		List<IPredefinedSession> result = 
			SharedApplication.getInstance().getPredefinedSessions(request);
		assertEquals(9, result.size());
		
		//Should be 3 approved
		request = new PredefinedSessionRequest(9999L, true);
		result = SharedApplication.getInstance().getPredefinedSessions(request);
		assertEquals(3, result.size());
		
		//Should be 1 approved & FEATURED
		request = new PredefinedSessionRequest(9999L, true, PredefinedSessionType.FEATURED);
		result = SharedApplication.getInstance().getPredefinedSessions(request);
		assertEquals(1, result.size());
		assertEquals(newSessions[0].getId(), result.get(0).getId());
		
		//Should be 2 approved & in group 'set3'
		request = new PredefinedSessionRequest(9999L, true, "set3");
		result = SharedApplication.getInstance().getPredefinedSessions(request);
		assertEquals(2, result.size());
		assertEquals(newSessions[4].getId(), result.get(0).getId());
		assertEquals(newSessions[5].getId(), result.get(1).getId());
		
		//Should be 2 NOT approved & in group 'set2'
		request = new PredefinedSessionRequest(9999L, false, "set2");
		result = SharedApplication.getInstance().getPredefinedSessions(request);
		assertEquals(2, result.size());
		assertEquals(newSessions[1].getId(), result.get(0).getId());
		assertEquals(newSessions[2].getId(), result.get(1).getId());
		
		//Should be 1 approved, in group 'set3', and UNLISTED
		request = new PredefinedSessionRequest(9999L, true, PredefinedSessionType.UNLISTED, "set3");
		result = SharedApplication.getInstance().getPredefinedSessions(request);
		assertEquals(1, result.size());
		assertEquals(newSessions[5].getId(), result.get(0).getId());
		
		deleteSessions(newSessions);
	}
	
	public static void deleteSessions(IPredefinedSession... sessions) throws Exception {
		Exception wasThrown = null;
		
		for (IPredefinedSession session : sessions) {
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
	
	public static IPredefinedSession[] saveSessions(IPredefinedSession... sessions) throws Exception {
		Exception wasThrown = null;
		
		ArrayList<IPredefinedSession> results = new ArrayList<IPredefinedSession>();
		
		for (IPredefinedSession session : sessions) {

			try {
				SavePredefinedSession saveAction = new SavePredefinedSession(session);
				results.add(saveAction.run());
			} catch (Exception e) {
				wasThrown = e;
			}

		}
		
		if (wasThrown != null) {
			throw wasThrown;
		}
		
		return results.toArray(new IPredefinedSession[0]);
	}
	
	public static PredefinedSessionBuilder[] toBuilder(IPredefinedSession... sessions) {
		ArrayList<PredefinedSessionBuilder> results =
			new ArrayList<PredefinedSessionBuilder>(sessions.length);
		
		for (IPredefinedSession s : sessions) {
			PredefinedSessionBuilder sb = new PredefinedSessionBuilder(s);
			results.add(sb);
		}
		
		return results.toArray(new PredefinedSessionBuilder[0]);
	}
	
	public static PredefinedSessionBuilder[] stripUniqueness(PredefinedSessionBuilder... sessions) {
		
		for (PredefinedSessionBuilder s : sessions) {
			s.setId(null);
			s.setUniqueCode(null);
		}
		
		return sessions;
	}
	
	/**
	 * Creates three PredefinedSEssionBuilders.
	 * This may be used by other classes
	 * @return
	 */
	public static PredefinedSessionBuilder[] createUnsavedPredefinedSessions() {
		
		PredefinedSessionBuilder ps1;
		PredefinedSessionBuilder ps2;
		PredefinedSessionBuilder ps3;
		
		//Construct a calendar date for today that does not include time.
		GregorianCalendar today = new GregorianCalendar();
		
		today = new GregorianCalendar(
				today.get(Calendar.YEAR),
				today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));
		GregorianCalendar yesterday = new GregorianCalendar(
				today.get(Calendar.YEAR),
				today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		
		
		ps1 = new PredefinedSessionBuilder();
		ps1.setAddBy("Eric");
		ps1.setAddContactInfo("608.821.1111");
		//ps1.setAddDate(new Date(today.getTimeInMillis()));	//is autoset
		ps1.setAddNote("Please approve me");
		//ps1.setApproved(false);	//should default to false and not allow 'true' on new records
		ps1.setContextString("context");
		ps1.setDescription("[[TEST USAGE ONLY, DO NOT USE. DELETE ME]]desc");
		ps1.setGroupName("myGroup");
		ps1.setModelId(9999L);
		ps1.setName("Session 1");
		ps1.setPredefinedSessionType(PredefinedSessionType.FEATURED);
		ps1.setSortOrder(1);
		//ps1.setUniqueCode("veryUnique1");	//auto-create unique code
		
		//
		ps2 = new PredefinedSessionBuilder();
		ps2.setAddBy("I-Lin");
		ps2.setAddContactInfo("608.821.1112");
		ps2.setAddDate(new Date(yesterday.getTimeInMillis()));	//should be ignored - reset to today
		ps2.setAddNote("Please approve me");
		ps2.setApproved(true);	//ignored and set to false
		ps2.setContextString("context");
		ps2.setDescription("[[TEST USAGE ONLY, DO NOT USE. DELETE ME]]desc");
		ps2.setGroupName("myGroup");
		ps2.setModelId(9999L);
		ps2.setName("Session 2");
		ps2.setPredefinedSessionType(PredefinedSessionType.LISTED);
		ps2.setSortOrder(4);
		ps2.setUniqueCode("veryUnique2");	//auto-create unique code
		
		//
		ps3 = new PredefinedSessionBuilder();
		ps3.setAddBy("Lorraine");
		ps3.setAddContactInfo("608.821.1113");
		//ps3.setAddDate(new Date(yesterday.getTimeInMillis()));	//should be ignored - reset to today
		ps3.setAddNote("Please approve me");
		ps3.setApproved(false);
		ps3.setContextString("context");
		ps3.setDescription("[[TEST USAGE ONLY, DO NOT USE. DELETE ME]]desc");
		ps3.setGroupName("myGroup");
		ps3.setModelId(9999L);
		ps3.setName("Session 3");
		ps3.setPredefinedSessionType(PredefinedSessionType.UNLISTED);
		ps3.setSortOrder(6);
		ps3.setUniqueCode("veryUnique3");	//auto-create unique code
		
		return new PredefinedSessionBuilder[] {ps1, ps2, ps3};
	}
	
}
