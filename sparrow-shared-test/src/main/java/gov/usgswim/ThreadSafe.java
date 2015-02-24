package gov.usgswim;

/**
 * Annotation indicating that a class is threadsafe.
 * 
 * This annotation is just a marker indicating intent, it does not enforce anything.
 * 
 * Convention take from _Java Concurrency in Practive_ by Brian Goetz.
 * See page 353, Appendix A.
 * 
 * Sample usage:
 * 
 * import gov.usgswim;
 * 
 * @ThreadSafe
 * public class MyClass { ... }
 */
public @interface ThreadSafe {
}
