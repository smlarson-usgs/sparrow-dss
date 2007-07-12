package gov.usgswim.sparrow;

/**
 * A processor capable of processing a unit of work.
 * 
 * Intended to be used w/ the ComputableCache.  Implementors of this interface
 * should expect the compute method to be called by multiple threads.
 * 
 * This code was take from _Java Concurrency in Practice_ by Brian Goetz
 * Addison-Wesley, 2006.
 */
public interface Computable<A, V> {
    V compute(A arg) throws Exception;
}
