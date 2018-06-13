package duckutil;

import java.util.LinkedList;

public class RateTracker
{
  private final long keep_back_ms;

  private LinkedList<RateEntry> lst;
  private long total;
  public RateTracker(long keep_back_ms)
  {
    this.keep_back_ms = keep_back_ms;
    lst = new LinkedList<>();
  }

  public synchronized void record(long count)
  {
    RateEntry re = new RateEntry(count);
    lst.add(re);
    total += count;

    prune();
  }

  private void prune()
  {
    while((lst.size() > 0) && (lst.peek().tm + keep_back_ms < System.currentTimeMillis()))
    {
      RateEntry re = lst.poll();
      total -= re.count;
    }
  }

  public synchronized long getTotal()
  {
    prune();
    return total;
  }

  public double getRatePerSecond()
  {
    double back = keep_back_ms;
    double back_sec = back / 1000.0;
    double total_d = getTotal();
    return total_d / back_sec;
  }


  public class RateEntry
  {
    public final long tm;
    public final long count;
    public RateEntry(long count)
    {
      this.tm = System.currentTimeMillis();
      this.count = count;
    }
  }
}
