package gov.usgswim.sparrow.request;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class PredefinedSessionUniqueRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected final String uniqueCode;
	protected final Long id;

	/**
	 * Default empty constructor needed b/c subclass may need an empty
	 * superclass.
	 */
	public PredefinedSessionUniqueRequest() {
		uniqueCode = null;
		id = null;
	}
	
	public PredefinedSessionUniqueRequest(String uniqueCode) {
		this.uniqueCode = uniqueCode;
		id = null;
	}

	public PredefinedSessionUniqueRequest(Long id) {
		this.id = id;
		uniqueCode = null;
	}
	
	/**
	 * This constructor is contridictory, but allows 'blind' construction
	 * where the validation can happen later.
	 * @param id
	 * @param uniqueCode
	 */
	public PredefinedSessionUniqueRequest(Long id, String uniqueCode) {
		this.id = id;
		this.uniqueCode = uniqueCode;
	}

	/**
	 * @return the uniqueCode
	 */
	public String getUniqueCode() {
		return uniqueCode;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	
	public boolean isPopulated() {
		return uniqueCode != null || id != null;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(197, 1343);
		hash.append(uniqueCode);
		hash.append(id);
		return hash.toHashCode();
	}
}