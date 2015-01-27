package org.geoserver.sparrow.util;

import java.util.ArrayList;

/**
 * Bean to have a meaningful response from a layer sweep run
 */
public class SweepResponse {
	public ArrayList<DataStoreResponse> deleted = new ArrayList(20);
	public ArrayList<DataStoreResponse> kept = new ArrayList(20);

	public boolean hasDeletions() {
		return deleted.size() > 0;
	}

	public boolean hasKept() {
		return kept.size() > 0;
	}
	
	/**
	 * Simple bean class to aggregate the data from a DataStore deletion
	 */
	public static class DataStoreResponse {
		
		public String dsName = null;
		public String wksName = null;
		public boolean isDeleted = false;
		public Long age = null;	//only valid if kept
		public Exception err = null;
		public ArrayList<String> layersDeleted = new ArrayList(0);	//only valid if deleted
		
		public DataStoreResponse() {}
		
		public DataStoreResponse(String wksName, String dsName) {
			this.dsName = dsName;
			this.wksName = wksName;
		}

	}
	
}
