package gov.usgswim.sparrow.action;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;

import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.cachefactory.ModelRequestCacheKey;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.domain.SourceBuilder;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.SparrowModelBuilder;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

/**
 * Deletes the passed PredefinedSession from the db and returns it.
 * Only the ID is used from the session, so its ok to pass 'fake' instances.
 * @author eeverman
 *
 */
public class DeletePredefinedSession extends Action<PredefinedSession> {

	PredefinedSession session;

	public DeletePredefinedSession(PredefinedSession session) {
		this.session = session;
	}

	/**
	 * @return The predefined session that was deleted.
	 */
	@Override
	public PredefinedSession doAction() throws Exception {


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
			throw new Exception("The delete should effect exactly one record. " +
				"Instead, " + updateCount + " records were affected.");
		}
		
		return session;
	}

}
