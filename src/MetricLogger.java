package duckutil;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * For ease of use this class violates the normal rules of avoid static things
 */
public class MetricLogger
{

  private static boolean init_done = false;
  private static PrintStream log_out;
  private static LinkedBlockingQueue<MetricLog> log_queue;
  private static boolean shutdown_triggered = false;
  private static boolean shutdown_safe = false;
  private static Object shutdown_wait = new Object();


  public static synchronized void init(String path)
    throws java.io.IOException
  {
    if (init_done) throw new RuntimeException("MetricLogger already initialized");
    log_out = new PrintStream(new FileOutputStream(path, true));
    log_queue = new LinkedBlockingQueue<>();
    init_done = true;
    new MetricLoggerThread().start();

    Runtime.getRuntime().addShutdownHook(new MetricLoggerShutdownThread());

  }

  /**
   * After calling record the MetricLog should no longer be modified.
   * Doing so might produce concurrency problems.
   */
  public static void record(MetricLog log)
  {
    if (init_done)
    {
      log_queue.add(log);
      synchronized(log_queue)
      {
        log_queue.notifyAll();
      }
    }
  }

  public static class MetricLoggerShutdownThread extends Thread
  {
    public MetricLoggerShutdownThread()
    {
      setName("MetricLoggerShutdownThread");
    }

    @Override
    public void run()
    {
      shutdown_triggered = true;

      new MetricLog().setModule("MetricLogger").setOperation("shutdown").close();

      synchronized(shutdown_wait)
      {
        synchronized(log_queue)
        {
          log_queue.notifyAll();
        }
        try
        {
          
          if(!shutdown_safe)
          {
            shutdown_wait.wait(2500);
          }
        }
        catch(InterruptedException e){}

      }

    }

  }


  public static class MetricLoggerThread extends Thread
  {
    public MetricLoggerThread()
    {
      setName("MetricLoggerThread");
      setDaemon(true);

      // TODO add shutdown hook to capture last logs on exit
    }

    @Override
    public void run()
    {
      while(true)
      {
        try
        {
          synchronized(log_queue)
          {
            log_queue.wait();
          }

          boolean shutdown_monitor = false;
          if (shutdown_triggered)
          {
            shutdown_monitor = true;
          }

          while(!log_queue.isEmpty())
          {
            MetricLog log = log_queue.take();
            log_out.println(log.getLine());
          }

          if (shutdown_monitor)
          {
            shutdown_safe=true;

            synchronized(shutdown_wait)
            {
              shutdown_wait.notifyAll();
            }
          }

        }
        catch(InterruptedException e)
        {
          throw new RuntimeException(e);
        }
      }
    }

  }

}
