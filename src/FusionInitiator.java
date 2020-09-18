package duckutil;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * This acts as a monitor to facilitate multiple threads who are working on
 * related tasks that we don't want to have be tightly bound but want to be loosely
 * coordinated.
 *
 * Think of it like a chain gang.  One member can get a little ahead of another but not far ahead.
 * Kinda like cyclic barrier, but we don't want everyone waiting for a common point.
 */
public class FusionInitiator extends Thread
{
  private int worker_count;

  private ArrayList<Semaphore> ready_sem;
  private ArrayList<Semaphore> done_sem;

  public FusionInitiator(int worker_count)
  {
    setName("FusionInitiator");
    setDaemon(true);

    this.worker_count = worker_count;

    ready_sem = new ArrayList<>();
    done_sem = new ArrayList<>();

    for(int i=0; i<worker_count; i++)
    {
      ready_sem.add( new Semaphore(1) );
      done_sem.add( new Semaphore(0) );
    }

  }

  /**
   * Marks the given task as complete.  Assumed taskWait() was previously called.
   */
  public void taskComplete(int task_number)
  {
    done_sem.get(task_number).release();

  }

  /**
   * Blocks until it is ok to start task.   taskComplete() is expected to be called when done.
   */
  public void taskWait(int task_number)
  {
    try
    {
      ready_sem.get(task_number).acquire();
    }
    catch(InterruptedException e)
    {
      throw new RuntimeException(e);
    }
    
  }

  public void run()
  {
    while(true)
    {
      for(int i=0; i<worker_count; i++)
      {
        try
        {
          done_sem.get(i).acquire();
        }
        catch(InterruptedException e)
        {
          throw new RuntimeException(e);
        }
        ready_sem.get(i).release();
      }
    }

  }
}
