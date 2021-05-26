package duckutil;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PeriodicThread extends Thread
{
  private static final Logger logger = Logger.getLogger("periodicthread");
  
  private final long desired_period_ms;
  private volatile boolean stopped = false;
  private Object wake_obj;

  private MetricLog run_mlog;

  public PeriodicThread(long desired_period_ms)
  {
    wake_obj = new Object();
    this.desired_period_ms = desired_period_ms;

  }

  public void run()
  {
    while(!stopped)
    {
      long start = System.currentTimeMillis();
      long end_time = start + desired_period_ms;
      
      try(MetricLog mlog = new MetricLog()) 
      {
        run_mlog = mlog;
        mlog.setOperation("periodic_run");
        mlog.setModule(getName());
        try
        {
          runPass();
          mlog.set("error",0);
        }
        catch(Throwable t)
        {
          mlog.set("error",1);
          logger.log(Level.WARNING, "Periodic thread exception", t);
        }
        run_mlog = null;
      }
      synchronized(wake_obj)
      {
        wake_obj.notifyAll();
      }

      long tm = System.currentTimeMillis();
      long sleep_tm = end_time - tm;

      if (sleep_tm > 0)
      {
        try
        {
          synchronized(wake_obj)
          {
            wake_obj.wait(sleep_tm);
          }
        }
        catch(InterruptedException e)
        {
          logger.log(Level.WARNING, "Periodic thread exception: " + e);
        }
      }
    }
  }

  /**
   * Get the metric log for the current run.  Should only be called during runPass()
   */
  protected MetricLog getMlog()
  {
    return run_mlog;
  }

  /**
   * Wake this task and get it to execute if it is sleeping.
   * If it is already running there there no guarantee of an execution starting after the wake() call.
   */
  public void wake()
  {
    synchronized(wake_obj)
    {
      wake_obj.notifyAll();
    }
  }
  public void wakeAndWait()
    throws InterruptedException
  {
    synchronized(wake_obj)
    {
      wake_obj.notifyAll();
      wake_obj.wait();
    }

  }

  public void halt()
  {
    stopped=true;
  }

  public abstract void runPass() throws Exception;


}
