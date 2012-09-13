package gov.usgswim.sparrow.validation.tests;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;

/**
 *
 * @author eeverman
 */
public abstract class BaseTextFileTester extends SparrowModelValidationBase {

	public BaseTextFileTester() {
	}

	public int getIncAllCol(DataTable txt) throws Exception {
		String incName = "ia";
		Integer incCol = txt.getColumnByName(incName);
		if (incCol == null) {
			//try alt name
			incCol = txt.getColumnByName("PLOAD_INC_TOTAL");
			if (incCol == null) {
				throw new Exception("The incremental column for all sources " + " (ia) was not found in the file.");
			}
		}
		return incCol;
	}

	public int getIncCol(int srcNum, DataTable txt, PredictData predictData) throws Exception {
		String incName = "i" + srcNum;
		Integer incCol = txt.getColumnByName(incName);
		if (incCol == null) {
			//try alt name
			incName = "PLOAD_INC_" + predictData.getSrcMetadata().getString(srcNum - 1, 1);
			incCol = txt.getColumnByName(incName.toUpperCase());
			if (incCol == null) {
				throw new Exception("The incremental column for source " + srcNum + " (i" + srcNum + ") was not found in the file.");
			}
		}
		return incCol;
	}

	public int getTotalAllCol(DataTable txt) throws Exception {
		String totalName = "ta";
		Integer totalCol = txt.getColumnByName(totalName);
		if (totalCol == null) {
			//try alt name
			totalCol = txt.getColumnByName("PLOAD_TOTAL");
			if (totalCol == null) {
				throw new Exception("The total column for all sources" + " (ta) was not found in the file.");
			}
		}
		return totalCol;
	}

	public int getTotalCol(int srcNum, DataTable txt, PredictData predictData) throws Exception {
		String totalName = "t" + srcNum;
		Integer totalCol = txt.getColumnByName(totalName);
		if (totalCol == null) {
			//try alt name
			totalName = "PLOAD_" + predictData.getSrcMetadata().getString(srcNum - 1, 1);
			totalCol = txt.getColumnByName(totalName.toUpperCase());
			if (totalCol == null) {
				throw new Exception("The total column for source " + srcNum + " (t" + srcNum + ") was not found in the file.");
			}
		}
		return totalCol;
	}

}
