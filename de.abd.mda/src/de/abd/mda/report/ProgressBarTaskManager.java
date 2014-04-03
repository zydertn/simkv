package de.abd.mda.report;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icefaces.application.PortableRenderer;
import org.icefaces.application.PushRenderer;

import de.abd.mda.persistence.dao.Configuration;
import de.abd.mda.persistence.hibernate.SessionFactoryUtil;
import de.abd.mda.util.FacesUtil;



@ManagedBean(name = ProgressBarTaskManager.BEAN_NAME)
@CustomScoped(value = "#{window}")
public class ProgressBarTaskManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8740251659159740688L;

    public static final String BEAN_NAME = "longtask";

    public static final String PUSH_GROUP = "ourUser"
            + System.currentTimeMillis();
    private static final int MAX_PERCENT = 100;

    private boolean taskRunning = false;

    private int progress = 0;
    private String customer = "";

    private Random randomizer;
    
    @PostConstruct
    private void init() {
        // Add our session
        PushRenderer.addCurrentSession(PUSH_GROUP);
        // Prep the generator
        randomizer = new Random(System.nanoTime());
    }

    @PreDestroy
    private void deinit() {
        // Ensure our task is stopped
        setTaskRunning(false);
    }

    public void stopTask(ActionEvent event) {
        setTaskRunning(false);
    }

    public void resetTask(ActionEvent event) {
        // Reset to zero
        progress = 0;
    }

    public void stopAndResetTask(ActionEvent event) {
        stopTask(event);
        resetTask(event);
    }

    public void startThread(int minIncrease, int maxIncrease, int sleepAmount) {
        internalThreadMethod(minIncrease, maxIncrease, sleepAmount);
    }

    private void internalThreadMethod(final int minIncrease,
            final int maxIncrease, final int sleepAmount) {
        // Reset the progress if it is at the maximum
        // Otherwise leave them alone as the user may have stopped/started the
        // progress bar
        // and in that case we want it to continue from the previous percent
//		HttpSession facesSession = (new FacesUtil()).getSession();

    	if (progress == MAX_PERCENT) {
            progress = 0;
        }

        // Ensure we only have one thread going at once
        if (!taskRunning) {
            // Use our global application wide thread pool
            LongTaskPool pool = (LongTaskPool) FacesUtil
                    .getManagedBean(LongTaskPool.BEAN_NAME);
            final PortableRenderer renderer = PushRenderer
                    .getPortableRenderer();

    		Transaction tx = null;
    		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
    		tx = session.beginTransaction();
    		String select = "from Configuration";
    		List<Configuration> list = session.createQuery(select).list();
    		Configuration c = null;
    		if (list.size() > 0) {
    			c = list.get(0);
    		}
    		c.setCustomer(0);
    		c.setReportProgress(0);
    		tx.commit();
    		
            // Start a new long running process to simulate a delay
            pool.getThreadPool().execute(new Runnable() {
                public void run() {
                    setTaskRunning(true);
                    // Loop until a break condition inside

//                    HttpSession facesSession = (new FacesUtil()).getSession();
                    
                    while (true) {
                		Transaction tx = null;
                		Session session = SessionFactoryUtil.getInstance().getCurrentSession();
                		tx = session.beginTransaction();
                		String select = "from Configuration";
                		List<Configuration> list = session.createQuery(select).list();
                		Configuration c = null;
                		if (list.size() > 0) {
                			c = list.get(0);
                		}

                		if (c != null) {
                			progress = c.getReportProgress();
                			customer = "" + c.getCustomer();
                		}

                		tx.commit();
//                    	progress += minIncrease
//                                + randomizer.nextInt(maxIncrease);
                        // Ensure that we don't break the max
                        // Also we can stop if we reach the top, instead of
                        // having an extra Thread.sleep
                        if (progress >= MAX_PERCENT) {
                            progress = MAX_PERCENT;
                            break;
                        }

                        // Render the updated progress
                        renderer.render(PUSH_GROUP);
                        // Simulate a pause
                        try {
                            Thread.sleep(sleepAmount);
                        } catch (Exception ignored) {
                        }

                        // Ensure we're not supposed to stop
                        if (!taskRunning) {
                            break;
                        }
                    }
                    // Complete the task and update the page
                    setTaskRunning(false);
                    renderer.render(PUSH_GROUP);
                }
            });
        }
    }

    // //////------------------GETTERS & SETTERS BEGIN
    public boolean getTaskRunning() {
        return taskRunning;
    }

    public int getProgress() {
        return progress;
    }

    public void setTaskRunning(boolean taskRunning) {
        this.taskRunning = taskRunning;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	
}
