
package gov.usgswim.sparrow.util;

import java.util.Set;
import java.util.Map.Entry;

import org.junit.Test;
import static org.junit.Assert.*;


public class SparrowResourceUtilsTest {

	public final String NONEXISTENT_MODEL = "-666";

	@Test
	public void testModelResourceFilePathReturnsNullIfAnyArgumentIsNull() {
		String result = SparrowResourceUtils.getModelResourceFilePath(null, "filename.txt");
		assertNull(result);

		result = SparrowResourceUtils.getModelResourceFilePath(1L, null);
		assertNull(result);
	}

	@Test
	public void testRetrieveAllSavedSessionsForNonExistentModel() {
		Set<Entry<Object, Object>> sessions = SparrowResourceUtils.retrieveAllSavedSessions(NONEXISTENT_MODEL);
		assertNotNull(sessions);
		assertTrue(sessions.isEmpty());
	}

}
