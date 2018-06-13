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
    getRate(min, df),
    getRate(min5, df),
    getRate(hour, df));
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

}
