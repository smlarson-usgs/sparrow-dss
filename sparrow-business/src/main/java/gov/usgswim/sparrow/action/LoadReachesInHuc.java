package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Criteria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Loads all the reach ids in a HUC for a given model.
 * The returned array are sorted by the identifier.
 * 
 * @author eeverman
 *
 */
public class LoadReachesInHuc extends Action<long[]> {
	
	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "select";
	
	protected Criteria criteria;
	
	public LoadReachesInHuc() {
		super();
	}


	@Override
	public long[] doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("HUC_COLUMN_NAME", criteria.getCriteriaType().getName());
		params.put("HUC_CODE", criteria.getValue());
		params.put("MODEL_ID", criteria.getModelID());
		
		PreparedStatement st = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		
		ResultSet rset = null;
		rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		
		ArrayList<Long> aList = new ArrayList<Long>();
		
		//This dataset is returned sorted by identifier and has the identifier 
		//in the first column
		while (rset.next()) {
			aList.add(rset.getLong(1));
		}

		//Copy to a primative array
		long[] results = new long[aList.size()];
		
		for (int i = 0; i < results.length; i++) {
			results[i] = aList.get(i);
		}
		
		return results;
	}


	public Criteria getCriteria() {
		return criteria;
	}


	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public Long getModelId() {
		if (criteria != null) {
			return criteria.getModelID();
		} else {
			return null;
		}
	}
	
}
