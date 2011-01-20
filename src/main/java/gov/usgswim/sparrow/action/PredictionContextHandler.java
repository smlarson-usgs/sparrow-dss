package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.request.PredictionContextRequest;

import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionContextHandler extends Action<List<PredictionContext>> {

	private final static String INSERT_STATEMENT_NAME = "Insert";
	private final static String TOUCH_STATEMENT_NAME = "Touch";
	private final static String SELECT_ONE_STATEMENT_NAME = "SelectOne";
	private final static String SELECT_SINCE_TIME_STATEMENT_NAME = "SelectSinceTime";
	
	PredictionContextRequest request;

	public PredictionContextHandler(PredictionContextRequest request) {
		this.request = request;
	}
	


	/**
	 * @return A list of PredefinedSessions, filtered as requested
	 */
	@Override
	public List<PredictionContext> doAction() throws Exception {

		if (request.getContextId() != null) {
			return selectOne(request.getContextId());
		} else if (request.getContext() != null) {
			saveOne(request.getContext());
			List<PredictionContext> list = new ArrayList<PredictionContext>(1);
			list.add(request.getContext());
			return list;
		} else {
			return selectSinceTime(new Timestamp(request.getTimeSince()));
		}
	}
	
	protected List<PredictionContext> selectOne(Long id) throws Exception {
		
		PreparedStatement statement = null;
		ResultSet rset = null;

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("KEY", id);
		paramMap.put("VALUE_CLASS", PredictionContext.class.getName());
		
		statement = getROPSFromPropertiesFile(SELECT_ONE_STATEMENT_NAME, null, paramMap);
		rset = statement.executeQuery();
		List<PredictionContext> list = hydrate(rset, 1);
		
		return list;
	}
	
	/**
	 * Saves one record to the db and returns the number of records updated.
	 */
	protected int saveOne(PredictionContext pc) throws Exception {
		
		if (selectOne(new Long(pc.hashCode())).size() == 1) {
			//If it already exists, just update the timestamp
			return touch(pc);
		} else {
			PreparedStatement statement = null;
			
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("KEY", pc.hashCode());
			paramMap.put("VALUE_CLASS", PredictionContext.class.getName());
			paramMap.put("VALUE", new SerializableBlobWrapper(pc));
			paramMap.put("LAST_TOUCHED", new Timestamp(System.currentTimeMillis()));
			
			statement = getRWPSFromPropertiesFile(INSERT_STATEMENT_NAME, null, paramMap);
			return statement.executeUpdate();
		}
	}
	
	protected List<PredictionContext> selectSinceTime(Timestamp time) throws Exception {
		

		PreparedStatement statement = null;
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("VALUE_CLASS", PredictionContext.class.getName());
		paramMap.put("LAST_TOUCHED", time);
		
		statement = getRWPSFromPropertiesFile(SELECT_SINCE_TIME_STATEMENT_NAME, null, paramMap);
		ResultSet rset = statement.executeQuery();
		return hydrate(rset, 13);
	}
	
	/**
	 * Updates the time last-touched to now.
	 */
	protected int touch(PredictionContext pc) throws Exception {
		
		PreparedStatement statement = null;
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("KEY", pc.hashCode());
		paramMap.put("VALUE_CLASS", PredictionContext.class.getName());
		paramMap.put("LAST_TOUCHED", new Timestamp(System.currentTimeMillis()));
		
		statement = getRWPSFromPropertiesFile(TOUCH_STATEMENT_NAME, null, paramMap);
		return statement.executeUpdate();
	}
	
	/**
	 * Rehydrate objects serialized in the resultset.
	 * 
	 * @param rst
	 * @param estCount Estimated number of objects expected
	 * @return
	 * @throws Exception
	 */
	List<PredictionContext> hydrate(ResultSet rst, int estCount) throws Exception {
		
		List<PredictionContext> list = new ArrayList<PredictionContext>(estCount);
		
		while (rst.next()) {
			Blob b = rst.getBlob("VALUE");
			
			ObjectInputStream ois = new ObjectInputStream(b.getBinaryStream());
			PredictionContext pc = (PredictionContext) ois.readObject();
			list.add(pc);
		}
		
		return list;
	}

}
