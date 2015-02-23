package gov.usgswim;

/**
 * Annotation indicating what lock a field is gaurded by.
 * 
 * This annotation is just a marker indicating intent, it does not enforce anything.
 * 
 * Convention take from _Java Concurrency in Practive_ by Brian Goetz.
 * See page 354, Appendix A.
 * 
 * Sample usage:
 * 
 * import gov.usgswim;
 * 
 * ...
 * 
 * @GuardedBy("this") private long counter;	//The enclosing object instance
 * @GuardedBy("Classname.class") private HashMap values;	//The class object
 * @GuardedBy("fieldname") private HashMap values;	//The lock associated w/ the object reference.
 */
public @interface GuardedBy {
	String value();
}
