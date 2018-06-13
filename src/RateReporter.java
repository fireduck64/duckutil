package duckutil;

import java.text.DecimalFormat;

public class RateReporter
{
  private RateTracker min = new RateTracker(60L * 1000L);
  private RateTracker min5 = new RateTracker(60L * 5L * 1000L);
  private RateTracker hour = new RateTracker(60L * 60L * 1000L);

  public RateReporter()
  {

  }

  public void record(long count)
  {
    min.record(count);
    min5.record(count);
    hour.record(count);
  }

  public String getReport(DecimalFormat df)
  {
    return String.format("1-min: %s, 5-min: %s, hour: %s", 
    df.format(min.getRatePerSecond()),
    df.format(min5.getRatePerSecond()),
    df.format(hour.getRatePerSecond()));

  }

}
