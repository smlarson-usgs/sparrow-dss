package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * Deletes the passed PredefinedSession from the db and returns it.
 * Only the ID is used from the session, so its ok to pass 'fake' instances.
 * @author eeverman
 *
 */
public class DeletePredefinedSession extends Action<IPredefinedSession> {

	PredefinedSessionBuilder session;

	public DeletePredefinedSession(IPredefinedSession session) {
		this.session = new PredefinedSessionBuilder(session);
	}

	/**
	 * @return The predefined session that was deleted.
	 */
	@Override
	public IPredefinedSession doAction() throws Exception {


		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		int updateCount = 0;	//The number of rows updated/inserted
		PreparedStatement statement = null;
		
		paramMap.put("PREDEFINED_SESSION_ID", session.getId());


		statement = getRWPSFromPropertiesFile("DeleteSQL", null, paramMap);

		try {
			updateCount = statement.executeUpdate();
		} catch (Exception e) {
			setPostMessage("Could not run the delete statement.");
			throw e;
		}
		
		if (updateCount != 1) {
			log.warn("No matching record was found to delete for ID: " +
				session.getId());
		}
		
		//The ID is no longer valid, so zap it.
		session.setId(null);
		
		return session.toImmutable();
	}

}
