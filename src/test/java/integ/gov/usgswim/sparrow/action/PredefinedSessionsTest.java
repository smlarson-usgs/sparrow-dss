package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.parser.PredefinedSession;
import gov.usgswim.sparrow.parser.PredefinedSessionType;
import gov.usgswim.sparrow.service.idbypoint.FindReachRequest;

import org.apache.log4j.Level;
import org.junit.Test;
import org.junit.Before;

/**
 * An integration test of the CRUD operation actions for PredefinedSession.
 * @author eeverman
 *
 */
public class PredefinedSessionsTest extends SparrowDBTest {
	
	private PredefinedSession ps1;
	private PredefinedSession ps2;
	private PredefinedSession ps3;
	private GregorianCalendar today;
	private GregorianCalendar yesterday;
	
	@Before
	public void initSessions() {
		
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
	}
	
	
	@Test
	public void saveAndDeleteSessions() throws Exception {
		
		SavePredefinedSession saveAction3 = new SavePredefinedSession(ps3);
		SavePredefinedSession saveAction2 = new SavePredefinedSession(ps2);
		SavePredefinedSession saveAction1 = new SavePredefinedSession(ps1);
		
		PredefinedSession newPs3 = saveAction3.run();
		PredefinedSession newPs2 = saveAction2.run();
		PredefinedSession newPs1 = saveAction1.run();
		
		
		//
		assertEquals(ps1.getAddBy(), newPs1.getAddBy());
		assertEquals(ps1.getAddContactInfo(), newPs1.getAddContactInfo());
		assertEquals(today.getTime().getTime(), newPs1.getAddDate().getTime());
		assertEquals(ps1.getAddNote(), newPs1.getAddNote());
		assertEquals(false, newPs1.getApproved());
		assertEquals(ps1.getContextString(), newPs1.getContextString());
		assertEquals(ps1.getDescription(), newPs1.getDescription());
		assertEquals(ps1.getGroupName(), newPs1.getGroupName());
		assertEquals(ps1.getModelId(), newPs1.getModelId());
		assertEquals(ps1.getName(), newPs1.getName());
		assertEquals(ps1.getPredefinedSessionType(), newPs1.getPredefinedSessionType());
		assertEquals(ps1.getSortOrder(), newPs1.getSortOrder());
		assertTrue(newPs1.getUniqueCode().length() == 5);
		assertTrue(newPs1.getId() != 0 && newPs1.getId() != null);
		
		//
		assertEquals(ps2.getAddBy(), newPs2.getAddBy());
		assertEquals(ps2.getAddContactInfo(), newPs2.getAddContactInfo());
		assertEquals(today.getTime().getTime(), newPs2.getAddDate().getTime());
		assertEquals(ps2.getAddNote(), newPs2.getAddNote());
		assertEquals(false, newPs2.getApproved());
		assertEquals(ps2.getContextString(), newPs2.getContextString());
		assertEquals(ps2.getDescription(), newPs2.getDescription());
		assertEquals(ps2.getGroupName(), newPs2.getGroupName());
		assertEquals(ps2.getModelId(), newPs2.getModelId());
		assertEquals(ps2.getName(), newPs2.getName());
		assertEquals(ps2.getPredefinedSessionType(), newPs2.getPredefinedSessionType());
		assertEquals(ps2.getSortOrder(), newPs2.getSortOrder());
		assertEquals(ps2.getUniqueCode(), newPs2.getUniqueCode());
		assertTrue(newPs2.getId() != 0 && newPs2.getId() != null);
		
		//
		assertEquals(ps3.getAddBy(), newPs3.getAddBy());
		assertEquals(ps3.getAddContactInfo(), newPs3.getAddContactInfo());
		assertEquals(today.getTime().getTime(), newPs3.getAddDate().getTime());
		assertEquals(ps3.getAddNote(), newPs3.getAddNote());
		assertEquals(false, newPs3.getApproved());
		assertEquals(ps3.getContextString(), newPs3.getContextString());
		assertEquals(ps3.getDescription(), newPs3.getDescription());
		assertEquals(ps3.getGroupName(), newPs3.getGroupName());
		assertEquals(ps3.getModelId(), newPs3.getModelId());
		assertEquals(ps3.getName(), newPs3.getName());
		assertEquals(ps3.getPredefinedSessionType(), newPs3.getPredefinedSessionType());
		assertEquals(ps3.getSortOrder(), newPs3.getSortOrder());
		assertEquals(ps3.getUniqueCode(), newPs3.getUniqueCode());
		assertTrue(newPs3.getId() != 0 && newPs3.getId() != null);
		
		//////////////////
		//Try loading all three sessions
		LoadPredefinedSessions loadAction = new LoadPredefinedSessions();
		List<PredefinedSession> sessionList = loadAction.run();
		
		//They should be in the specified sort order
		assertEquals(newPs1.getId(), sessionList.get(0).getId());
		assertEquals(newPs2.getId(), sessionList.get(1).getId());
		assertEquals(newPs3.getId(), sessionList.get(2).getId());
		assertTrue(sessionList.size() == 3);
		
		//////////////////
		//Try loading a single session by the unique code
		loadAction = new LoadPredefinedSessions(newPs1.getUniqueCode());
		sessionList = loadAction.run();
		
		//They should be in the specified sort order
		assertEquals(newPs1.getId(), sessionList.get(0).getId());
		assertTrue(sessionList.size() == 1);
		
		
		//now delete each record
		DeletePredefinedSession deleteAction = new DeletePredefinedSession(newPs1);
		PredefinedSession deletedPs1 = deleteAction.run();
		assertEquals(newPs1.getId(), deletedPs1.getId());
		deleteAction = new DeletePredefinedSession(newPs2);
		deleteAction.run();
		deleteAction = new DeletePredefinedSession(newPs3);
		deleteAction.run();
		
		//The db should now be empty
		loadAction = new LoadPredefinedSessions();
		sessionList = loadAction.run();
		assertTrue(sessionList.size() == 0);
	}
	
	@Test
	public void saveAndUpdateASession() throws Exception {
		
		SavePredefinedSession saveAction3 = new SavePredefinedSession(ps3);
		SavePredefinedSession saveAction2 = new SavePredefinedSession(ps2);
		SavePredefinedSession saveAction1 = new SavePredefinedSession(ps1);
		
		PredefinedSession newPs3 = saveAction3.run();
		PredefinedSession newPs2 = saveAction2.run();
		PredefinedSession newPs1 = saveAction1.run();
		
		newPs1.setApproved(true);
		saveAction1 = new SavePredefinedSession(newPs1);
		saveAction1.run();
		
		//Load the session back and verify
		LoadPredefinedSessions loadAction = new LoadPredefinedSessions();
		List<PredefinedSession> sessionList = loadAction.run();
		sessionList = loadAction.run();
		
		newPs1 = sessionList.get(0);
		
		//
		assertEquals(ps1.getAddBy(), newPs1.getAddBy());
		assertEquals(ps1.getAddContactInfo(), newPs1.getAddContactInfo());
		assertEquals(today.getTime().getTime(), newPs1.getAddDate().getTime());
		assertEquals(ps1.getAddNote(), newPs1.getAddNote());
		assertEquals(true, newPs1.getApproved());
		assertEquals(ps1.getContextString(), newPs1.getContextString());
		assertEquals(ps1.getDescription(), newPs1.getDescription());
		assertEquals(ps1.getGroupName(), newPs1.getGroupName());
		assertEquals(ps1.getModelId(), newPs1.getModelId());
		assertEquals(ps1.getName(), newPs1.getName());
		assertEquals(ps1.getPredefinedSessionType(), newPs1.getPredefinedSessionType());
		assertEquals(ps1.getSortOrder(), newPs1.getSortOrder());
		assertTrue(newPs1.getUniqueCode().length() == 5);
		assertTrue(newPs1.getId() != 0 && newPs1.getId() != null);
		
		//now delete each record
		DeletePredefinedSession deleteAction = new DeletePredefinedSession(newPs1);
		PredefinedSession deletedPs1 = deleteAction.run();
		assertEquals(newPs1.getId(), deletedPs1.getId());
		deleteAction = new DeletePredefinedSession(newPs2);
		deleteAction.run();
		deleteAction = new DeletePredefinedSession(newPs3);
		deleteAction.run();
		
		//The db should now be empty
		loadAction = new LoadPredefinedSessions();
		sessionList = loadAction.run();
		assertTrue(sessionList.size() == 0);
	}
	
}
