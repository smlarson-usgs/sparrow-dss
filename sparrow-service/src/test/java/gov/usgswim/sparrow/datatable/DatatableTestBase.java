package gov.usgswim.sparrow.datatable;

import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;

public class DatatableTestBase {

	
	/**
	 * Quick build a double table with basic data.
	 * 
	 * Table Data:
	 * 		___________________________
	 *		.1	|	.2	|	.3	|	.4
	 *		1.1	|	1.2	|	1.3	|	1.4
	 *		2.1	|	2.2	|	2.3	|	2.4
	 *		0	|	3.2	|	3.3	|	3.4
	 *		0	|	4.2	|	4.3	|	4.4
	 *		___________________________
	 * 
	 * @param tableName
	 * @return
	 */
	public DataTableWritable buildDoubleTable(String tableName) {
		
		//String[]  baseHeadings = new String[] { "one", "two", "three", "four" };
		double[][] baseData = new double[][] {
				{ .1, .2, .3, .4 },
				{ 1.1, 1.2, 1.3, 1.4 },
				{ 2.1, 2.2, 2.3, 2.4 },
				{ 0, 3.2, 3.3, 3.4 },
				{ 0, 4.2, 4.3, 4.4 }
		};
		int[] baseRowIds = new int[] {1, 2, 3, 4, 5};
		SimpleDataTableWritable rwTable = 
			new SimpleDataTableWritable(baseData, null, baseRowIds);
		rwTable.setName(tableName);
		rwTable.setDescription("The Table '" + tableName + "'");
		rwTable.setProperty("prop1", tableName + "Prop1Value");
		
		for (int i=0; i<4; i++) {
			rwTable.getColumns()[i].setName(tableName + i + "ColName");
			rwTable.getColumns()[i].setDescription(tableName + i + "ColDesc");
			rwTable.getColumns()[i].setUnits(tableName + i + "ColUnits");
			rwTable.getColumns()[i].setProperty(tableName + i + "ColPropName", tableName + i + "ColPropVal");
			rwTable.getColumns()[i].setProperty(tableName + i + "ColPropName2", tableName + i + "ColPropVal2");
		}
		
		return rwTable;
	}
	


}
