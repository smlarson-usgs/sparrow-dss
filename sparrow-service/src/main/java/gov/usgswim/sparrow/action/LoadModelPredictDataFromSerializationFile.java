package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;

import java.io.*;

/**
 * Loads PredictData instances from a serialization directory.
 * 
 * Optionally if a model is not found in the configured directory, the model
 * can be loaded from the DB and then serialized to the directory.
 * 
 * @author eeverman
 */
public class LoadModelPredictDataFromSerializationFile extends Action<PredictData> implements ILoadModelPredictData {
	
	/**
	 * Set this system property to assign a local caching directory to serialize
	 * model predict data to.  If unassign and not explicitly set for an instance,
	 * the java temp directory will be used.
	 */
	public static final String DATA_DIRECTORY =
			LoadModelPredictDataFromSerializationFile.class.getName() +
			".DATA_DIRECTORY";
	
	/**
	 * Set this system property to true if the model should be fetched from the
	 * db if it is not found locally.
	 */
	public static final String FETCH_FROM_DB_IF_NO_LOCAL_FILE =
			LoadModelPredictDataFromSerializationFile.class.getName() +
			".FETCH_FROM_DB_IF_NO_LOCAL_FILE";
	

	private String serializedModelDirectory = null;
	private Boolean allowFetchFromDb = null;
	
	
	//Instance
	private Long modelId;
	
	public LoadModelPredictDataFromSerializationFile() {
	}
	
	/**
	 * Creates a Action to load the entire model
	 * @param modelId the ID of the Sparrow Model to load
	 * @param bootstrap	<b>true</b>		to load only the data required to run a prediction.
	 * 					<b>false</b>	to load the complete dataset for a model (including bootstrap data). 
	 */
	public LoadModelPredictDataFromSerializationFile(Long modelId) {
		this.modelId = modelId;
	}
	
	@Override
	public PredictData doAction() throws Exception {
		
		PredictData pd = tryToLoad();
		
		if (pd != null) {
			return pd;
		} else {

			if (isAllowFetchFromDb()) {
				LoadModelPredictData action = new LoadModelPredictData();
				action.setModelId(modelId);
				pd = action.run();
				serializeModelToFile(pd, getFilePathForModel(modelId));
				return pd;
			} else {
				setPostMessage("The model was not found as a local serialization file" +
						" and the " + FETCH_FROM_DB_IF_NO_LOCAL_FILE + " flag was not set to true.");
				return null;
			}
		}
		
		
	}
	
	protected PredictData tryToLoad() {
		
		File file = new File(getFilePathForModel(modelId));
		
		if (file.exists()) {
			try {
				InputStream fileInputStream = new FileInputStream(file);
				InputStream buffer = new BufferedInputStream( fileInputStream );
				ObjectInput input = new ObjectInputStream ( buffer );
				try {
					//deserialize the List
					PredictData pd = (PredictData)input.readObject();
					return pd;
				} catch (Exception e) {
					log.error("The serialized model could not be loaded - will grab from db.", e);
					file.delete();
					//do nothing else
				} finally {
					input.close();
				}
			} catch (Exception ee) {
				//do nothing else
			}
		}
		
		return null;
	}
	
	public static void serializeModelToFile(PredictData pd, String fileName) {
		
		
	    try{
	        //use buffering
	        OutputStream file = new FileOutputStream(fileName);
	        OutputStream buffer = new BufferedOutputStream( file );
	        ObjectOutput output = new ObjectOutputStream( buffer );
	        try{
	          output.writeObject(pd);
	        }
	        finally{
	          output.close();
	        }
	      } catch(IOException ex){
	        System.err.print(ex);
	      }

	      //deserialize the quarks.ser file
	      //note the use of abstract base class references
	      

	}
	

	public Long getModelId() {
		return this.modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	public String getSerializedModelDirectory() {
		if (serializedModelDirectory != null) {
			return serializedModelDirectory;
		} else {
			return getDefaultSerializedModelDirectory();
		}
	}

	public void setSerializedModelDirectory(String dir) {
		
		if (! dir.endsWith(File.separator)) dir = dir + File.separator;
		this.serializedModelDirectory = dir;
	}
	
	/**
	 * Returns the default directory that serialized models are stored in.
	 */
	public static synchronized String getDefaultSerializedModelDirectory() {
		String dir = System.getProperty(
				DATA_DIRECTORY,
				System.getProperty("java.io.tmpdir"));
		
		if (! dir.endsWith(File.separator)) dir = dir + File.separator;
		
		return dir;
	}
	
	/**
	 * Retrieves the default path for a model.
	 * @param modelId
	 * @return
	 */
	public static String getDefaultFilePathForModel(Long modelId) {
		return getDefaultSerializedModelDirectory() + "model_" + modelId + ".ser";
	}
	
	public String getFilePathForModel(Long modelId) {
		return getSerializedModelDirectory() + "model_" + modelId + ".ser";
	}

	public boolean isAllowFetchFromDb() {
		if (allowFetchFromDb != null) {
			return allowFetchFromDb;
		} else {
			String allow = System.getProperty(FETCH_FROM_DB_IF_NO_LOCAL_FILE);
			return ("true".equalsIgnoreCase(allow));
		}
	}

	public void setAllowFetchFromDb(boolean allowFetchFromDb) {
		this.allowFetchFromDb = allowFetchFromDb;
	}


}
