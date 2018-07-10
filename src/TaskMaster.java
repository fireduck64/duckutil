package duckutil;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;

import java.util.List;
import java.util.ArrayList;

public class TaskMaster<V>
{
  private ArrayList<FutureTask<V> > futures = new ArrayList<>();

  public TaskMaster(List<Callable<V> > action_list, Executor exec)
  {
    for(Callable<V> c : action_list)
    {
      FutureTask<V> ft = new FutureTask<V>(c);
      futures.add(ft);
      exec.execute(ft);
    }
  }

  public static ThreadPoolExecutor getBasicExecutor(int threads, String name)
  {
    return new ThreadPoolExecutor(threads, threads, 
      2, TimeUnit.DAYS, 
      new LinkedBlockingQueue<Runnable>(), 
      new DaemonThreadFactory(name));
  }

  public ArrayList<V> getResults()
  {
    return getResults(true);
  }
  public ArrayList<V> getResults(boolean rethrow_exceptions)
  {
    ArrayList<V> results = new ArrayList<V>();
    for(FutureTask<V> ft : futures)
    {
      try
      {
        V val = ft.get();
        results.add(val);
      }
      catch(Exception e)
      {
        if (rethrow_exceptions)
        {
          throw new RuntimeException(e);
        }
        else
        {
          results.add(null);
        }
      }
    }
    return results;

  }



}
