package duckutil;

import java.text.DecimalFormat;

public class TimeUtil
{
  public static String durationToString(long ms)
  {
    long round_up = 0;
    if (ms % 1000 > 500) round_up=1;

    long t = ms / 1000 + round_up;

    long sec = t % 60;
    t = t / 60;
    long min = t % 60;
    t = t / 60;
    long hour = t % 24;
    t = t / 24;
    long day = t;

    DecimalFormat df=new DecimalFormat("00");

    StringBuilder sb = new StringBuilder();
    boolean print=false;
    if (print || (day != 0)) { sb.append(df.format(day) + "d"); print=true;}
    if (print || (hour != 0)) { sb.append(df.format(hour) + "h"); print=true;}
    if (print || (min != 0)) { sb.append(df.format(min) + "m"); print=true;}

    sb.append(df.format(sec) + "s");

    return sb.toString();

  }


}
