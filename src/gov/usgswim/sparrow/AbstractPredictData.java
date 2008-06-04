package gov.usgswim.sparrow;

import gov.usgswim.datatable.DataTable;

public abstract class AbstractPredictData implements PredictData {

	/**
	 * Returns the source column corresponding to the passed source ID.
	 * 
	 * @param id The model specific source id (not the db id)
	 * @return
	 * @throws Exception If the source ID cannot be found.
	 */
	public int getSourceColumnForSourceID(Integer id) throws Exception {
		
		DataTable srcMetadata = this.getSrcMetadata();
		
		if (srcMetadata != null) {

			int i = srcMetadata.getRowForId(id.longValue());

			if (i > -1) {
				// Running from database, so has a sourceid table
				return i;
			} else  {
				throw new Exception ("Source for id " + id + " not found");
			}
		} else {
			// Running from text file so assume columns in order
			//In this case, id '1' is column zero.
			if (id > 0) {
				return id.intValue() - 1;
			} else {
				throw new Exception("Invalid source id " + id + ", which must be greater then zero.");
			}
		}
	}
	
	public Long getSourceIdForSourceIndex(int index) throws Exception {
		DataTable srcMetadata = this.getSrcMetadata();
		
		if (srcMetadata != null) {

			return srcMetadata.getIdForRow(index);

		} else {
			// Running from text file so assume columns in order
			//In this case, the id is '1' based while the index is zero based
			//TODO: We are not checking for indexes which are too large
			if (index > -1) {
				return new Long(index + 1);
			} else {
				throw new Exception("Invalid source index " + index + ", which must be greater then zero.");
			}
		}
	}
	
	/**
	 * Returns the row index corresponding to the passed reach id.
	 * 
	 * This row index applies to all 'per-reach' datatables.
	 * 
	 * @param id The model specific reach id (not the db id)
	 * @return
	 * @throws Exception If the reach ID cannot be found.
	 */
	public int getRowForReachID(Integer id) throws Exception {
		return getRowForReachID(id.longValue());
	}
	
	/**
	 * Returns the row index corresponding to the passed reach id.
	 * 
	 * This row index applies to all 'per-reach' datatables.
	 * 
	 * @param id The model specific reach id (not the db id)
	 * @return
	 * @throws Exception If the reach ID cannot be found.
	 */
	public int getRowForReachID(Long id) throws Exception {
		
		DataTable sys = this.getSys();
		int row = sys.getRowForId(id);
		
		if (row > -1) {
			return row;
		} else {
			throw new Exception("Invalid reach id " + id + ", not found in the model.");
		}
	}

}
