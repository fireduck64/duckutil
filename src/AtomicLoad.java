package duckutil;

import java.util.concurrent.atomic.AtomicLong;
import java.util.LinkedList;

public class AtomicLoad
{
  public static void main(String args[]) throws Exception
  {
    new AtomicLoad();
  }

  private AtomicLong al = new AtomicLong(0L);
  private MultiAtomicLong mal = new MultiAtomicLong();

  public AtomicLoad() throws Exception
  {
    for(int i=0; i<100; i++)
    {
      new WorkerThread().start();
    }

    while(true)
    {
      Thread.sleep(5000);
      long v = al.getAndSet(0L);
      v+= mal.sumAndReset();
      System.out.println(v / 5);
    }


  }

  public class WorkerThread extends Thread
  {
    public WorkerThread()
    {
      setPriority(3);
      setDaemon(true);
    }

    public void run()
    {
      while(true)
      {
        //al.getAndAdd(1L);
        mal.add(1L);
        //yield();
      }
    }
  }

  public class LongHolder
  {
    public long v;
  }


}
