package duckutil;

public class TimeRecordAuto implements AutoCloseable
{
  private long t1;
  private String label;
  public TimeRecordAuto(String label)
  {
    this.label = label;
    t1 = System.nanoTime();
    
  }

  public void close()
  {
    TimeRecord.record(t1, label);
  }

}
