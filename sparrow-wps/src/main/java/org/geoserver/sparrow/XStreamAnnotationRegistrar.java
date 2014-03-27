package org.geoserver.sparrow;

import com.thoughtworks.xstream.XStream;

/**
 * Takes any number of classes and ensures that they are registered w/ XStream
 * for annotation processing.
 * 
 * @author eeverman
 */
public class XStreamAnnotationRegistrar {
	
	public XStreamAnnotationRegistrar(Class... clazz) {
		register(clazz);
	}
	
	public XStreamAnnotationRegistrar(String... classNames) throws Exception {
		register(classNames);
	}
	
	public static void register(Class... clazz) {
		
		XStream xstream = new XStream();
			
		for (Class c : clazz) {
			xstream.processAnnotations(c);
		}
	}
	
	public static void register(String... classNames) throws Exception {
		
		XStream xstream = new XStream();
			
		for (String n : classNames) {
			Class c = Class.forName(n);
			xstream.processAnnotations(c);
		}
	}
}
