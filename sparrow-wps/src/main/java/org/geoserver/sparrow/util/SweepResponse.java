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
		public ArrayList<Resource> resources = new ArrayList<>(0);
		public boolean isDbfDeleted = false;
		public ArrayList<String> messages = new ArrayList<>(0);
		public Long age = null;	//only valid if kept
		public Exception err = null;
		
		public DataStoreResponse() {}
		
		public DataStoreResponse(String wksName, String dsName) {
			this.dsName = dsName;
			this.wksName = wksName;
		}
		
		public void addResource(Resource r) {
			resources.add(r);
		}
		
		public void addResource(String name, String note) {
			resources.add(new Resource(name, note));
		}
		
		public void addResource(String name, String note, Throwable t) {
			resources.add(new Resource(name, note, t));
		}
		
		public void addMessage(String msg) {
			messages.add(msg);
		}

	}
	
	public static class Resource {
		String name = null;
		String note = null;
		Throwable exception = null;
		
		public Resource(String name, String note) {
			this.name = name;
			this.note = note;
		}
		
		public Resource(String name, String note, Throwable t) {
			this.name = name;
			this.note = note;
			this.exception = t;
		}
	}
	
}
