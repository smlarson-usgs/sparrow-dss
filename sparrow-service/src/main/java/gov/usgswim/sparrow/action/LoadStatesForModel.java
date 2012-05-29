package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.HucAggregationLevel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Loads all the states for a given model.
 * 
 * Only states that have reaches at least partially contained in the state
 * will be loaded.
 * 
 * Row ID:  state.PK_ISN (db id from table)
 * Columns are in this order:
 * <ul>
 * <li>State Name - state.STATE_NAME (full name)
 * <li>State FIPS Code - state.FIPS_STATE_CODE (numeric FIPS code for state)
 * <li>Country Code - state.COUNTRY_CODE (Two letter country code, eg US)
 * <li>Postal Code - state.STATE_POSTAL_CODE (Familiar two letter state code)
 * </ul>
 *  
 * @author eeverman
 */
public class LoadStatesForModel extends Action<DataTable> {
	
	
	private static final String QUERY_NAME = "query";
	protected Long modelId;
	
	
	public LoadStatesForModel(Long modelId) {
		super();
		this.modelId = modelId;
	}



	public LoadStatesForModel() {
		super();
	}



	@Override
	public DataTable doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", modelId);
		
		ResultSet rset = getROPSFromPropertiesFile(QUERY_NAME, getClass(), params).executeQuery();
		addResultSetForAutoClose(rset);
		
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, true);
		values.setName("States for model");
		values.setDescription(
						"Complete list of all states that reaches have catchment area in, for model " + modelId);
		
		return values.toImmutable();
		
	}

	@Override
	protected void validate() {
		if (modelId == null) {
			this.addValidationError("The modelId parameter cannot be null");
		}
	}


	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}


}
