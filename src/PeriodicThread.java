package duckutil;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PeriodicThread extends Thread
{
  private static final Logger logger = Logger.getLogger("periodicthread");
  
  private final long desired_period_ms;
  private volatile boolean stopped = false;

  public PeriodicThread(long desired_period_ms)
  {
    this.desired_period_ms = desired_period_ms;

  }

  public void run()
  {
    while(!stopped)
    {
      long start = System.currentTimeMillis();
      long end_time = start + desired_period_ms;
      
      try
      {
        runPass();
      }
      catch(Throwable t)
      {
        logger.log(Level.WARNING, "Periodic thread exception", t);
      }

      long tm = System.currentTimeMillis();
      long sleep_tm = end_time - tm;

      if (sleep_tm > 0)
      {
        try
        {
          sleep(sleep_tm);
        }
        catch(InterruptedException e)
        {
          logger.log(Level.WARNING, "Periodic thread exception: " + e);
        }
      }
    }
  }

  public void halt()
  {
    stopped=true;
  }

  public abstract void runPass() throws Exception;


}
