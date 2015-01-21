package gov.usgs.cida.sparrow.service.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * A wrapper class for object or value returned by a service.
 * 
 * It provides a standard container and serialization format for basic service
 * response metadata.
 * @author eeverman
 *
 * @param <T>
 */
@XStreamAlias("ServiceResponseWrapper")
@XStreamInclude({ServiceResponseEntityList.class})
public class ServiceResponseWrapper {
	
	//Transient response flags used for serialization
	private transient String encoding = "UTF-8";
	private transient ServiceResponseMimeType mimeType = ServiceResponseMimeType.XML;
	
	
	private ServiceResponseStatus status;
	private ServiceResponseOperation operation;
	private String message;
	
	//Error fields
	private String errorType;
	private String errorMessage;
	private String errorTrace;
	private transient Throwable error;
	
	
	private String entityClass;
	
	private Long entityId;
	
	ServiceResponseEntityList entityList;

	
	public ServiceResponseWrapper() {
		//no arg
	}
	
	@SuppressWarnings("unchecked")
	public ServiceResponseWrapper(Object entity, Class entityClass, Long entityId, ServiceResponseStatus status,
			ServiceResponseOperation operation) {
		
		//this.entity = entity;
		this.entityId = entityId;
		this.status = status;
		this.operation = operation;
		this.message = null;
		
		if (entityClass != null) {
			this.entityClass = entityClass.getCanonicalName();
		} else if (entity != null) {
			this.entityClass = entity.getClass().getCanonicalName();
		}
	
		if (entity != null) {
			entityList = new ServiceResponseEntityList(entity);
		}
	}
	
	public ServiceResponseWrapper(Object entity, Long entityId, ServiceResponseStatus status,
			ServiceResponseOperation operation) {
		
		//this.entity = entity;
		this.entityId = entityId;
		this.status = status;
		this.operation = operation;
		this.message = null;
		
		if (entity != null) {
			entityClass = entity.getClass().getCanonicalName();
			entityList = new ServiceResponseEntityList(entity);
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public ServiceResponseWrapper(Class entityClass, ServiceResponseOperation operation) {
		
		//this.entity = null;
		this.entityId = null;
		this.status = ServiceResponseStatus.UNKNOWN;
		this.operation = operation;
		this.message = null;
		
		if (entityClass != null) {
			this.entityClass = entityClass.getCanonicalName();
		}
	}

	public void addEntity(Object entity) {
		if (entity != null) {
			if (entityList == null) {
				entityList = new ServiceResponseEntityList(entity);
			} else {
				entityList.add(entity);
			}
		}
	}
	
	public void addAllEntities(List<?> entities) {
		if (entities != null) {
			if (entityList == null) {
				entityList = new ServiceResponseEntityList();
			}
			
			for (Object o : entities) {
				entityList.add(o);
			}
		}
	}
	
	/**
	 * Returns the list of entities.
	 * This method will never return null.  If no entities exist, an empty
	 * list is returned.
	 * @return
	 */
	public List<Object> getEntityList() {
		if (entityList != null) {
			return entityList.getList();
		} else {
			return Collections.emptyList();
		}
	}
	

	/**
	 * @return the entityId
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	/**
	 * @return the status
	 */
	public ServiceResponseStatus getStatus() {
		return status;
	}
	
	/**
	 * If an error was recorded, this retrieves it.
	 * 
	 * Note that this is based on a transient field, so will not be serialized.
	 * @return 
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(ServiceResponseStatus status) {
		this.status = status;
	}

	/**
	 * @return the operation
	 */
	public ServiceResponseOperation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(ServiceResponseOperation operation) {
		this.operation = operation;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the entityClass
	 */
	public String getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}
	
	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass.getCanonicalName();
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * @return the mimeType
	 */
	public ServiceResponseMimeType getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(ServiceResponseMimeType mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @param error the errorDetail to set
	 */
	public void setError(Throwable error) {
		
		this.error = error;
		
		errorMessage = error.getLocalizedMessage();
		
		if (error.getCause() != null) {
			errorType = error.getCause().getClass().getCanonicalName();
		} else {
			errorType = error.getClass().getCanonicalName();
		}
		
		
		StringWriter sw = new StringWriter();
		PrintWriter ps = new PrintWriter(sw);
		error.printStackTrace(ps);
		
		errorTrace = sw.toString();
	}
	
	/**
	 * Returns true ONLY if the status has been set to one of the OK statuses.
	 * 
	 * If the status has not been set (null) this will return false;
	 * @return 
	 */
	public boolean isOK() {
		if (status != null) {
			return (status.toString().startsWith("OK"));
		} else {
			return false;
		}
	}
	
	/**
	 * In cases where one action invokes another, each returning wrappers, this
	 * method makes it easy to let the parent wrapper inherit the appropriate fields
	 * from the chained response if the chainedWrapper failed.
	 * 
	 * These fields are copied from the chainedWrapper to this, only if the
	 * chained instance is not OK (isOK() returns false):
	 * 
	 * * status
	 * * error
	 * * message (prepended with prependToMessage).
	 * 
	 * Copy on failure only ensures that a single delegation operation's success
	 * does not flip the parent operation to success if it has already failed.
	 * 
	 * If the prepend is specified by the chained wrapper has no message, a
	 * [no message from chained operation] note is added.
	 * 
	 * @param prependToMessage
	 * @param chainedWrapper 
	 */
	public void inheritChainedFailure(String prependToMessage, ServiceResponseWrapper chainedWrapper) {
		if (! chainedWrapper.isOK()) {
			this.status = chainedWrapper.getStatus();
			this.error = chainedWrapper.getError();
			
			if (prependToMessage != null) prependToMessage+=": ";
			
			if (chainedWrapper.getMessage() != null) {
				this.message = prependToMessage + chainedWrapper.getMessage();
			} else {
				this.message = prependToMessage + "[No messaged from chained action]";
			}
		}
	}
	
	
}
