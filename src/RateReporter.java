package duckutil;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

public class RateReporter
{
  private RateTracker min = new RateTracker(60L * 1000L);
  private RateTracker min5 = new RateTracker(60L * 5L * 1000L);
  private RateTracker min15 = new RateTracker(60L * 15L * 1000L);
  private RateTracker hour = new RateTracker(60L * 60L * 1000L);

  public RateReporter()
  {

  }

  public void record(long count)
  {
    min.record(count);
    min5.record(count);
    min15.record(count);
    hour.record(count);
  }

  public String getReportShort(DecimalFormat df)
  {
    return String.format("1-min: %s, 5-min: %s, hour: %s", 
    getRate(min, df),
    getRate(min5, df),
    getRate(hour, df));
  }
  public String getReportLong(DecimalFormat df)
  {
    return String.format("5-min: %s, 15-min: %s, hour: %s", 
    getRate(min5, df),
    getRate(min15, df),
    getRate(hour, df));
  }

  public Map<Long, Double> getRawRates()
  {
    TreeMap<Long, Double> m = new TreeMap<>();
    m.put(min.getKeepBackMs(), min.getRatePerSecond());
    m.put(min5.getKeepBackMs(), min5.getRatePerSecond());
    m.put(min15.getKeepBackMs(), min15.getRatePerSecond());
    m.put(hour.getKeepBackMs(), hour.getRatePerSecond());

    return m;
  }

  public String getRate(RateTracker r, DecimalFormat df)
  {
    double rt = r.getRatePerSecond();
    String unit="";
    if (rt > 1000) { rt = rt / 1e3; unit = "K"; }
    if (rt > 1000) { rt = rt / 1e3; unit = "M"; }
    if (rt > 1000) { rt = rt / 1e3; unit = "G"; }
    if (rt > 1000) { rt = rt / 1e3; unit = "T"; } 

    return String.format("%s%s/s", df.format(rt), unit);
  }

  public boolean isZero()
  {
    return (hour.getTotal() == 0L);

  }

}
