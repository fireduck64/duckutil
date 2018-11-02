package duckutil;

import java.util.concurrent.atomic.AtomicLong;
import java.util.LinkedList;

/**
 * High performance atomiclong for multiple threads to write to quickly.
 * Does not do well if there are many threads being created as each one
 * will leave behind an entry.  Works great with consistent thread pools.
 *
 * In testing on a reasonable multicpu machine with 100 threads,
 * got 60M/s with normal AtomicLong and 650M/s with this.
 */
public class MultiAtomicLong
{

  private LinkedList<AtomicLong> al_list = new LinkedList<>();
  private ThreadLocal<AtomicLong> al_local = new ThreadLocal<>();

  /**
   * Get the sum of all values and reset to zero
   */
  public long sumAndReset()
  {
    long v = 0;
    synchronized(al_list)
    {
      for(AtomicLong l : al_list)
      {
        v+= l.getAndSet(0L);
      }
    }
    return v;
  }

  public long sum()
  {
    long v = 0;
    synchronized(al_list)
    {
      for(AtomicLong l : al_list)
      {
        v+= l.get();
      }
    }
    return v;
 
  }

  public void add(Long v)
  {
    AtomicLong l = al_local.get();
    if (l == null)
    {
      l = new AtomicLong(0L);
      al_local.set(l);
      synchronized(al_list) { al_list.add(l); }
    }
    
    l.getAndAdd(v);
  }


}
