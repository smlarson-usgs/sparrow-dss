package gov.usgswim.sparrow.datatable;

import gov.usgswim.sparrow.domain.UnitAreaType;
import gov.usgswim.sparrow.parser.BaseDataSeriesType;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.service.predict.aggregator.AggregateType;

public enum TableProperties implements NamedEnum<TableProperties>{
	ROW_LEVEL("row_level", UnitAreaType.class, "Each row in the table represents a reach, a huc2/4/6/8, other?"),
	CONSTITUENT("constituent", null, "Name of the thing being measured."),
	PRECISION("precision", null, "Number of significant figures prefered for display."),
	DATA_TYPE("data_type", BaseDataSeriesType.class, "A broad classification of the data is incremental, total, or other."),
	DATA_SERIES("data_series", DataSeriesType.class, "The specific dataseries of the data."),
	COLUMN_AGG_TYPE("column_agg_type", AggregateType.class, "Indicates a value is an aggregation of other values in the column."),
	ROW_AGG_TYPE("row_agg_type", AggregateType.class, "Indicates a value is an aggregation of other values in the row.");

	
	private String name;
	private String description;
	private Class<?> valueType;
	
	TableProperties(String name, Class<?> valueType, String description) {
		this.name = name;
		this.description = description;
		this.valueType = valueType;
	}

	@Override
	public TableProperties fromString(String name) {
		for (TableProperties val : values()) {
			if (val.name.equals(name)) {
				return val;
			}
		}
		return null;
	}
	
	@Override
	public TableProperties fromStringIgnoreCase(String name) {
		for (TableProperties val : values()) {
			if (val.name.equalsIgnoreCase(name)) {
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
	
	public String getPublicName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public Class<?> getValueType() {
		return valueType;
	}
}