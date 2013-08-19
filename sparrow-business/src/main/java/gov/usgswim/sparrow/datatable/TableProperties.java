package gov.usgswim.sparrow.datatable;

import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgs.cida.datatable.AggregateType;

public enum TableProperties implements NamedEnum<TableProperties>{

	MODEL_ID("model_id", Long.class, "ID of the associated model"),
	CONTEXT_ID("context_id", Long.class, "ID of the associated context"),
	ROW_LEVEL("row_level", AggregationLevel.class, "Each row in the table represents a reach, a huc2/4/6/8, other?"),
	CONSTITUENT("constituent", null, "Name of the thing being measured."),
	PRECISION("precision", null, "Number of significant figures prefered for display."),
	DATA_TYPE("data_type", BaseDataSeriesType.class, "A broad classification of the data is incremental, total, or other."),
	DATA_SERIES("data_series", DataSeriesType.class, "The specific dataseries of the data."),
	COLUMN_AGG_TYPE("column_agg_type", AggregateType.class, "Indicates a value is an aggregation of other values in the column."),
	DOC_ID("doc_id", String.class, "The key used to look up documentation relevant to a column."),
	ROW_AGG_TYPE("row_agg_type", AggregateType.class, "Indicates a value is an aggregation of other values in the row.");


	private String publicName;
	private String description;
	private Class<?> valueType;

	TableProperties(String name, Class<?> valueType, String description) {
		this.publicName = name;
		this.description = description;
		this.valueType = valueType;
	}

	@Override
	public TableProperties fromString(String name) {
		for (TableProperties val : values()) {
			if (val.publicName.equals(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public TableProperties fromStringIgnoreCase(String name) {
		for (TableProperties val : values()) {
			if (val.publicName.equalsIgnoreCase(name)) {
				return val;
			}
		}
		return null;
	}

	@Override
	public TableProperties getDefault() {
		return null;
	}

	@Override
	public String getDescription() {
		return description;
	}

//	public String getPublicName() {
//		return publicName;
//	}

	@Override
	public String toString() {
		return publicName;
	}

	public Class<?> getValueType() {
		return valueType;
	}
}