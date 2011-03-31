package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.service.idbypoint.FindReachRequest;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class FindReaches extends Action<DataTable> {

	private FindReachRequest reachRequest;
	private int pageSize = 50;
	private int recordStart = 0;
	private String dir = "DESC";
	String sort = "";
	
	private List<String> errors = new ArrayList<String>();
	
	public void setPageSize(int size) { 
		this.pageSize = size;
	}
	
	public void setRecordStart(int start) {
		this.recordStart = start;
	}
	
	public void setSort(String sort) { //TODO, these are magic strings from the extjs front end may want reconsider placement here
		if(sort==null) sort = "";
		if(sort.equals("id")) {
			sort = "FULL_IDENTIFIER";
		} else if(sort.equals("name")) {
			sort = "REACH_NAME";
		} else if(sort.equals("huc8")) {
			sort = "HUC8";
		} else if(sort.equals("watershed-area")) {
			sort = "CUM_CATCH_AREA";
		} else if(sort.equals("meanq")) {
			sort = "MEANQ";
		} else {
			sort = "";
		}
		this.sort = sort;
	}
	
	public void setSortDir(String dir) {
		if(dir==null) dir = "DESC";
		if(!dir.equals("DESC") && !dir.equals("ASC")) dir = "DESC"; //default and sql injection protection
		this.dir = dir;
	}
	
	public String getSortColumn() {
		if(!sort.equals(""))
			return sort + " "+dir+", ";
		return "";
	}
	
	@Override
	public DataTable doAction() throws Exception {

		errors = cleanAndCheckValidity(reachRequest);
		
		
		if (errors.size() == 0) {
			DynamicParamQuery query = createFindReachWhereClause(reachRequest);
			
			if (query.wheres.size() == 0) {
				errors.add("No criteria was specified");
				return null;
			} else {
				 
				String sql = "SELECT FULL_IDENTIFIER, REACH_NAME, MEANQ, CATCH_AREA, CUM_CATCH_AREA, HUC2, HUC4, HUC6, HUC8 from model_attrib_vw " +
				" WHERE " + query.buildWhere() + " " +
				" ORDER BY " + getSortColumn() + " reach_name,  identifier";
				
				String countQuery = "SELECT COUNT(*) FROM model_attrib_vw WHERE " + query.buildWhere();
				
				sql = "SELECT  /*+ first_rows(" + pageSize + ") */  * FROM "+
					"( SELECT a.*, ROWNUM rn, (" + countQuery + ") TOTAL_COUNT FROM ("+ 
					sql +
					") a "+
					" WHERE ROWNUM <=  " + (recordStart+pageSize) + " )" + 
					" WHERE rn > "+recordStart;
			
				PreparedStatement ps = getROPSFromString(sql, query.props);
				
				ResultSet rs = ps.executeQuery();
				
				DataTable dt = DataTableConverter.toDataTable(rs);
				rs.close();
				
				return dt;
			
			}
		} else {
			return null;
		}
	}
	

	/**
	 * @return the findReachRequest
	 */
	public FindReachRequest getReachRequest() {
		return reachRequest;
	}


	/**
	 * @param findReachRequest the findReachRequest to set
	 */
	public void setReachRequest(FindReachRequest findReachRequest) {
		this.reachRequest = findReachRequest;
	}
	
	public DynamicParamQuery createFindReachWhereClause(FindReachRequest frReq) {
		DynamicParamQuery query = new DynamicParamQuery();
		
		buildEquals(query, "SPARROW_MODEL_ID", frReq.modelID);
		buildIn(query, "IDENTIFIER", frReq.getReachIDArray());
		buildLike(query, "REACH_NAME", frReq.reachName);
		buildLessThan(query, "CUM_CATCH_AREA", frReq.basinAreaHi);
		buildGreaterThan(query, "CUM_CATCH_AREA", frReq.basinAreaLo);
		buildLessThan(query, "MEANQ", frReq.meanQHi);
		buildGreaterThan(query, "MEANQ", frReq.meanQLo);
		buildLikeRight(query, "HUC8", frReq.huc);
		buildIn(query, "EDACODE", frReq.getEdaCodeArray());
		buildIn(query, "EDANAME", frReq.getEdaNameArray());

		//whereClause += " and rownum <= " + Integer.toString(maxReturnSize+1);
		return query;
	}
	
	public List<String> cleanAndCheckValidity(FindReachRequest frReq) throws IOException {
		List<String> errors = new ArrayList<String>();

		frReq.trimToNull();

		errors = checkHiLo(errors, frReq.basinAreaHi, frReq.basinAreaLo, "incremental area");
		errors = checkHiLo(errors, frReq.meanQHi, frReq.meanQLo, "average flow");
		
		if (frReq.modelID == null) {
			errors.add("A Model ID is required.");
		} else if (frReq.isEmptyRequest()) {
			errors.add("No criteria was specified.");
		} else {
			//Check numbers
			checkPositiveInteger(errors, "Model ID", frReq.modelID);
			checkPositiveInteger(errors, "Reach ID", frReq.getReachIDArray());
			checkPositiveNumber(errors, "upper " + Action.getDataSeriesProperty(DataSeriesType.watershed_area, false), frReq.basinAreaHi);
			checkPositiveNumber(errors, "lower " + Action.getDataSeriesProperty(DataSeriesType.watershed_area, false), frReq.basinAreaLo);
			checkPositiveNumber(errors, "upper " + Action.getDataSeriesProperty(DataSeriesType.flux, false), frReq.basinAreaHi);
			checkPositiveNumber(errors, "lower " + Action.getDataSeriesProperty(DataSeriesType.flux, false), frReq.basinAreaLo);
		}
		
		// TODO check bbox
		return errors;
	}
	
	public void checkPositiveInteger(List<String> errors, String fieldName, String[] val) {
		if (val == null || val.length == 0) return;
		
		for (String s: val) {
			checkPositiveInteger(errors, fieldName, s);
		}
	}
	
	public void checkPositiveInteger(List<String> errors, String fieldName, String val) {
		if (val == null) return;
		
		if (! NumberUtils.isDigits(val) || val.startsWith("0")) {
			errors.add("The " + fieldName + " must be a positive integer.");
		}
	}
	
	public void checkPositiveNumber(List<String> errors, String fieldName, String val) {
		if (val == null) return;
		
		if (! NumberUtils.isNumber(val) || val.startsWith("-")) {
			errors.add("The " + fieldName + " must be a positive number.");
		}
	}
	
	public static List<String> checkHiLo(List<String> errors, String hiValue,
			String loValue, String valueName) {
		if (hiValue == null || loValue == null) return errors;
		
		float hi = Float.parseFloat(hiValue);
		float lo = Float.parseFloat(loValue);
		if (hi <= lo){
			errors.add("The " + valueName + " high value needs to be larger than the " + valueName + " low value.");
		}
		return errors;
	}
	
	public DynamicParamQuery buildEquals(DynamicParamQuery query, String columnName, String value) {
		value = StringUtils.trimToNull(value);
		if (value == null) return query;
		
		String where = columnName + " = $" + columnName + "$";
		query.wheres.add(where);
		query.props.put(columnName, value);
		return query;
	}
	
	public DynamicParamQuery buildLike(DynamicParamQuery query, String columnName, String value) {
		value = StringUtils.trimToNull(value);
		if (value == null) return query;
		
		String where = "UPPER(" + columnName + ") LIKE $" + columnName + "$";
		query.wheres.add(where);
		query.props.put(columnName, "%" + value.toUpperCase() + "%");
		return query;
	}
	
	public DynamicParamQuery buildLikeRight(DynamicParamQuery query, String columnName, String value) {
		value = StringUtils.trimToNull(value);
		if (value == null) return query;
		
		String where = "UPPER(" + columnName + ") LIKE $" + columnName + "$";
		query.wheres.add(where);
		query.props.put(columnName, value.toUpperCase() + "%");
		return query;
	}
	
	public DynamicParamQuery buildLessThan(DynamicParamQuery query, String columnName, String value) {
		value = StringUtils.trimToNull(value);
		if (value == null) return query;
		
		String fieldName = columnName + "_LESS_THAN";
		
		String where = columnName + " < $" + fieldName + "$";
		query.wheres.add(where);
		query.props.put(fieldName, value);
		return query;
	}
	
	public DynamicParamQuery buildGreaterThan(DynamicParamQuery query, String columnName, String value) {
		value = StringUtils.trimToNull(value);
		if (value == null) return query;
		
		String fieldName = columnName + "_GREATER_THAN";
		
		String where = columnName + " > $" + fieldName + "$";
		query.wheres.add(where);
		query.props.put(fieldName, value);
		return query;
	}
	
	public DynamicParamQuery buildIn(DynamicParamQuery query, String columnName, String[] value) {
		
		if (value == null || value.length < 1) return query;
		
		StringBuilder where = new StringBuilder();
		where.append(columnName + " IN (");
		boolean atLeastOneEntry = false;
		
		for (int i = 0; i < value.length; i++) {
			String v = StringUtils.trimToNull(value[i]);
			if (v != null) {
				String parmName = columnName + "_IN_" + i;
				where.append("$" + parmName + "$");
				query.props.put(parmName, v);
				where.append(", ");
				atLeastOneEntry = true;
			}

		}
		
		if (atLeastOneEntry) {
			//rm ', '
			where.delete(where.length() - 2, where.length());
			
			where.append(")");
			query.wheres.add(where.toString());
		}
		
		
		return query;
	}
	
	
	public class DynamicParamQuery {
		Map<String, Object> props = new HashMap<String, Object>();
		ArrayList<String> wheres = new ArrayList<String>();
		
		/**
		 * Returns the WHERE clause w/o the starting 'WHERE' text.
		 * @return
		 */
		public String buildWhere() {
			StringBuilder where = new StringBuilder();
			for (String w: wheres) {
				where.append(w).append(" AND ");
			}
			
			if (where.length() > 5) {
				where.delete(where.length() - 5, where.length());	//rm last 'AND'
			}
			
			return where.toString();
		}
	}

	/**
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}

}
