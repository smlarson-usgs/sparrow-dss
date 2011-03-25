package gov.usgswim.sparrow.service;

import gov.usgswim.ImmutableBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A subelement of ServiceResponseWrapper, this encapsilated list just bypasses
 * a limitation of XStream that collections are either wrapped in junk elements
 * that XStream uses to indicate collections, such as:
 * root
 * |-ArrayList
 *   |-a
 *     |-Child1
 *   |-a
 *     |-Child2
 * ...Yuck.
 * Or collection items are placed directly in the root w/o a wrapper:
 * root
 * |-child1
 * |-child2
 * |-someOtherNoneCollectionItem
 * 
 * The solution I am using here is to use option 2 (no collection wrapper),
 * but to provide the collection wrapper by nesting the collection in another
 * class (this one).
 * @author eeverman
 *
 */
public class ServiceResponseEntityList {
	
	//A list whos children are placed directly in the parent tag of this class.
	@XStreamImplicit(itemFieldName="entity")
	private List<Object> list;
	
	public ServiceResponseEntityList() {
		
	}
	
	@SuppressWarnings("unchecked")
	public ServiceResponseEntityList(Object entity) {
		list = new ArrayList<Object>(1);
		
		if (entity instanceof ImmutableBuilder) {
			entity = ((ImmutableBuilder)entity).toImmutable();
		}
		list.add(entity);
	}
	
	@SuppressWarnings("unchecked")
	public void add(Object entity) {
		if (list == null) {
			list = new ArrayList<Object>();
		}
		
		if (entity instanceof ImmutableBuilder) {
			entity = ((ImmutableBuilder)entity).toImmutable();
		}
		
		list.add(entity);
	}
	
	public List<Object> getList() {
		if (list != null) {
			return list;
		} else {
			return Collections.emptyList();
		}
	}
	
	
}
