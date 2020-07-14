package duckutil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import net.minidev.json.JSONObject;

public class MetricLog implements AutoCloseable
{
  private JSONObject json;
  private long start_time_nanos;

  public MetricLog()
  {
    start_time_nanos = System.nanoTime();
    json = new JSONObject();
  }

  public MetricLog setOperation(String op)
  {
    json.put("operation", op);
    return this;
  }

  public MetricLog setModule(String mod)
  {
    json.put("module", mod);
    return this;
  }

  public MetricLog set(String k, String v)
  {
    json.put(k, v);
    return this;
  }
  public MetricLog set(String k, long v)
  {
    json.put(k, v);
    return this;
  }
  public MetricLog set(String k, double v)
  {
    json.put(k, v);
    return this;
  }

  public String getLine()
  {
    return json.toJSONString();
  }

  @Override
  public void close()
  {
    double nano_delta = System.nanoTime() - start_time_nanos;
    double ms_delta = nano_delta / 1e6;
    json.put("op_duration_ms", ms_delta);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

    json.put("time", sdf.format(new Date()));

    MetricLogger.record(this);
    
  }
}
