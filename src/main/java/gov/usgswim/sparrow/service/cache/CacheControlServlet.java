package gov.usgswim.sparrow.service.cache;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.concurrent.Executors;

/**
 * Service endpoint for controlling the cache, including getting status, force
 * loading of models, and unloading.
 * 
 * @author eeverman
 */
public class CacheControlServlet extends AbstractSparrowServlet {
	private ThreadPoolExecutor threadPoolExecuter = null;
	private ConcurrentHashMap<Long, ModelStatus> modelStatusMap = new ConcurrentHashMap<Long, ModelStatus>();

	protected List<SparrowModel> getModelList() {
		ModelRequestCacheKey key = new ModelRequestCacheKey(null, true, false,
				false);

		List<SparrowModel> modelList = SharedApplication.getInstance()
				.getModelMetadata(key);
		return modelList;
	}

	public synchronized ThreadPoolExecutor getThreadPoolExecuter() {
		if (threadPoolExecuter == null) {
			threadPoolExecuter = new ThreadPoolExecutor(2, 2, 100L,
					TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			threadPoolExecuter.allowCoreThreadTimeOut(true);
		}
		return threadPoolExecuter;
	}
	
	class ModelLoaderTask extends FutureTask<Integer> {
		Long modelId;
		
		ModelLoaderTask(Long modelId) {
			super(
					new Callable() {
						public Object call() {
							return null;
						}
					}
			
			);
			this.modelId = modelId;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
	
	class ModelStatus {
		boolean dataLoaded;
		boolean mapLoaded;
	}
}
