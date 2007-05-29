package gov.usgswim.sparrow;

/**
 * Allows a class to return an immutable version of itself.
 * 
 * Instances which are already immutable may return themselves.
 */
public interface ImmutableBuilder<T> {
	public T getImmutable() throws IllegalStateException;
}
