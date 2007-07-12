package gov.usgswim.sparrow;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * A cache that takes Computable units of work, runs them, and stores the results.
 * 
 * The cache is thread safe such that if one Computable is actively processing
 * an identical request, the added computable will block until the other completes
 * and will return the completed result once it is available.
 * 
 * Concurrency utility classes from Java 1.5 are used.
 * 
 * This code was take from _Java Concurrency in Practice_ by Brian Goetz
 * Addison-Wesley, 2006.
 */
public class ComputableCache<A, V> implements Computable<A, V> {
	private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
		 
	private final Computable<A, V> c;

	//TODO  Need to make this take a max number of items argument and a FIFO usage
	//buffer to determine which (oldest) item to remove
	public ComputableCache(Computable<A, V> c) {
		this.c = c;
	}

	public V compute(final A arg) throws Exception {
		while (true) {
			Future<V> f = cache.get(arg);
			if (f == null) {
			
				Callable<V> eval = new Callable<V>() {
						public V call() throws Exception {
							return c.compute(arg);
						}
					};
					
				FutureTask<V> ft = new FutureTask<V>(eval);
				f = cache.putIfAbsent(arg, ft);
				if (f == null) {
					//It was not already in the cache
					f = ft;
					ft.run();
				} /* else {
					//Added by Eric Everman
					//If a non-null value is returned, it means that our requested
					//computation was added while this thread was setting up for the run.
					//recall this method, which will block until the result is ready.
					compute(arg);
					
					//not needed b/c the result is returned from f.get() below.
					
				} */
			}
			
			try {
				return f.get();	//This will block until the result is available
			} catch (CancellationException e) {
				//Cancelled the request
				cache.remove(arg, f);
				
				throw e;	//pass it on...
			} catch (InterruptedException e) {
				//For some reason, the call was interupted
				cache.remove(arg, f);
				
				throw e;	//pass it on...
			} catch (ExecutionException e) {
				//An error occured during the execution of the request.
				//Not sure what to do here: if its a data error in the request, it will
				//probably recurr if its tried again.  Otherwise, it could be a
				//db connection problem or something...
				cache.remove(arg, f);
				
				throw new Exception("The process threw an exception during its execution", e.getCause());
			}
		}
	}
	
	/**
	 * Removes the task from the Cache.
	 * 
	 * If the task is not yet completed, it will continue to run until it completes
	 * and will notify its caller when complete, it will just no longer be cached.
	 * 
	 * @param arg
	 * @return
	 */
	public boolean remove(final A arg) {
		Future<V> f = cache.remove(arg);
		return (f != null);
	}
	
	/**
	 * Removes all items from the cache.
	 */
	public void clear() {
		cache.clear();
	}
	
	/**
	 * Returns a snapshot of the number of items in the cache.
	 * 
	 * The returned size is just an estimate, since the size continually changes.
	 * @return
	 */
	public int size() {
		return cache.size();
	}
}
