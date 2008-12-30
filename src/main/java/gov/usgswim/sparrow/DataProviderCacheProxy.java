package gov.usgswim.sparrow;

/**
 * Wrapper class for items in the data cache.
 * The wrapper can be placed in the cache immediately at the start of a request.
 * It is marked as containing bogus data.
 * On a getData() call, if the content is bogus, the calling thread is sleeped
 * for a period, then it checks again for non-bogus data (this will eventually
 * error out).
 * 
 * This provides a questionable but effective way of allowing async requests
 * and still preventing duplication calculations in the cache.
 */
public class DataProviderCacheProxy {
	private static Object bogusData = new Exception("Placeholder");
	private static int DEFAULT_SLEEP_MILISEC = 2000;
	//private static int DEFAULT_SLEEP_INTERVALS_SECS = 60;

	private Object key;
	private Object data = bogusData;
	private boolean invalid = false;
	

	
	public DataProviderCacheProxy(Object key) {
		this.key = key;
	}
	
	public DataProviderCacheProxy(Object key, Object data) {
		this.key = key;
		this.data = data;
	}


	public Object getKey() {
		return key;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getData() {
	
		synchronized (key) {
			if (data == bogusData) {
				int sleepCount = 0;
				while (data == bogusData && sleepCount < 60 && (! invalid)) {
					noErrorSleep(DEFAULT_SLEEP_MILISEC);
					sleepCount++;
				}
			}

			if (data == bogusData) {
				invalid = true;
				return null;
			}
			return data;
		}
	}
	
  /**
   * Utility method to Sleep the current thread for a set time without throwing any errors.
   * 
   * @param time Time in milliseconds to sleep the thread.
   */
  protected void noErrorSleep(long time) {
    if (time > 0) {
      long start = System.currentTimeMillis();

      try {
        Thread.sleep(time);
      } catch (Exception e) { //woken up
        long now = System.currentTimeMillis();
        if ((now - start) < time) {
          noErrorSleep(time - (now - start));
        }
      }
    }
  }


}
