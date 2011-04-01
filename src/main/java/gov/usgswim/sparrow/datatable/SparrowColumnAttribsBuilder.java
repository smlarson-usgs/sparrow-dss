package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.ColumnAttribsBuilder;
import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;

/**
 * Structured properties for Sparrow tables and columns.
 * 
 * This class should serve to prompt for all the approprate metadata to include
 * in a sparrow table or column.
 * 
 * @author eeverman
 *
 */
public class SparrowColumnAttribsBuilder extends ColumnAttribsBuilder {

	
	public SparrowColumnAttribsBuilder() {
		
	}
	
	public void setModelId(Long modelId) {
		if (modelId != null) {
			setProperty(TableProperties.MODEL_ID.toString(), modelId.toString());
		} else {
			setProperty(TableProperties.MODEL_ID.toString(), null);
		}
	}
	
	public Long getModelId() {
		return getCleanLong(TableProperties.MODEL_ID.toString());
	}
	
	public void setContextId(Long contextId) {
		if (contextId != null) {
			setProperty(TableProperties.CONTEXT_ID.toString(), contextId.toString());
		} else {
			setProperty(TableProperties.CONTEXT_ID.toString(), null);
		}
	}
	
	public Long getContextId() {
		return getCleanLong(TableProperties.CONTEXT_ID.toString());
	}
	
	public void setConstituent(String constituent) {
		setProperty(TableProperties.CONSTITUENT.toString(), constituent);
	}
	
	public String getConstituent() {
		return getProperty(TableProperties.CONSTITUENT.toString());
	}
	
	public void setBaseDataSeriesType(BaseDataSeriesType baseDataSeriesType) {
		if (baseDataSeriesType != null) {
			setProperty(TableProperties.DATA_TYPE.toString(), baseDataSeriesType.toString());
		} else {
			setProperty(TableProperties.DATA_TYPE.toString(), null);
		}
	}
	
	public String getBaseDataSeriesType() {
		return getProperty(TableProperties.DATA_TYPE.toString());
	}
	
	public void setDataSeriesType(DataSeriesType dataSeriesType) {
		if (dataSeriesType != null) {
			setProperty(TableProperties.DATA_SERIES.toString(), dataSeriesType.toString());
		} else {
			setProperty(TableProperties.DATA_SERIES.toString(), null);
		}
	}
	
	public String geDataSeriesType() {
		return getProperty(TableProperties.DATA_SERIES.toString());
	}

	
	private Long getCleanLong(String key) {
		String val = getProperty(key);
		
		if (val != null) {
			try {
				Long lng = Long.parseLong(val);
				return lng;
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}
	

	
	

}
