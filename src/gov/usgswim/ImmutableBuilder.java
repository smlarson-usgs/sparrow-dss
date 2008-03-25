package gov.usgswim;

/**
 * Allows a class to return an immutable version of itself.
 * 
 * Instances which are already immutable may return themselves. Instances which
 * are not already immutable will invalidate themselves.
 */
public interface ImmutableBuilder<T> {
	public T toImmutable() throws IllegalStateException;
}
