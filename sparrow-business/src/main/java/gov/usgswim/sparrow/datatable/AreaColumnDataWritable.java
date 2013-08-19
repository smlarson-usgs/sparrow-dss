
package gov.usgswim.sparrow.datatable;

import gov.usgs.cida.datatable.impl.SparseDoubleColumnDataWritable;
import gov.usgswim.sparrow.AreaType;
import gov.usgswim.sparrow.SparrowUnits;
import java.util.HashMap;

/**
 * Encapsulates the instantiation of SparseDoubleColumnDataWriteables for
 * areas.
 * @author cschroed
 */
public class AreaColumnDataWritable extends SparseDoubleColumnDataWritable{
	public AreaColumnDataWritable(AreaType areaType, int rowCount){
		super(
			new HashMap<Integer, Double>(),
			areaType.getName(),
			SparrowUnits.SQR_KM.toString(),
			areaType.getDescription(),
			new HashMap<String, String>(),
			new HashMap<Object, int[]>(),
			rowCount,
			0D);
		this.setProperty(TableProperties.DATA_SERIES.toString(), areaType.getName());
		this.setProperty(TableProperties.DATA_TYPE.toString(), areaType.getDataSeriesType().getBaseType().toString());
//		this.setProperty(TableProperties.PRECISION.toString(), "4");
	}
}
