package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.ModelBBox;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Loads all the reach ids for a bounding box for a given model.
 * The returned array are sorted by the identifier.
 * 
 * @author eeverman
 *
 */
public class LoadReachesInBBox extends Action<Long[]> {
	
	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "sql";
	
	protected ModelBBox modelBBox;
	
	public LoadReachesInBBox() {
		super();
	}


	@Override
	public Long[] doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", modelBBox.getModelId());
		params.put("leftLong", modelBBox.getLeftLongBound());
		params.put("rightLong", modelBBox.getRightLongBound());
		params.put("upperLat", modelBBox.getUpperLatBound());
		params.put("lowerLat", modelBBox.getLowerLatBound());

		PreparedStatement st = getPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		
		ResultSet rset = null;


		rset = st.executeQuery();	//auto-closed
		
		ArrayList<Long> aList = new ArrayList<Long>();
		
		//This dataset is returned sorted by identifier and has the identifier 
		//in the first column
		while (rset.next()) {
			aList.add(rset.getLong(1));
		}
		
		//close rset - the statement and connection are auto-closed
		rset.close();
		
		Long[] results = aList.toArray(new Long[aList.size()]);
		
		return results;
	}


	/**
	 * @return the modelBBox
	 */
	public ModelBBox getModelBBox() {
		return modelBBox;
	}


	/**
	 * @param modelBBox the modelBBox to set
	 */
	public void setModelBBox(ModelBBox modelBBox) {
		this.modelBBox = modelBBox;
	}



	
}
