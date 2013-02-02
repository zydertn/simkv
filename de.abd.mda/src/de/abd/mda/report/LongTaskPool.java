package de.abd.mda.report;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean(name= LongTaskPool.BEAN_NAME)
@ApplicationScoped
public class LongTaskPool implements Serializable {
    public static final String BEAN_NAME = "longTaskPool";
    
    private ExecutorService threadPool;
    
    public ExecutorService getThreadPool() { return threadPool; }
    
    public void setThreadPool(ExecutorService threadPool) { this.threadPool = threadPool; }
    
	@PostConstruct
	private void init() {
        // Prep the thread pool
	    threadPool = Executors.newCachedThreadPool();
	}
	
	@PreDestroy
	private void deinit() {
	    // Cleanup the thread pool
	    if ((threadPool != null) &&
	        (!threadPool.isShutdown())) {
	        
	        threadPool.shutdown();
	        threadPool = null;
        }
	}
}