package duckutil.bloomtime;


public interface LongFile
{

  public void getBytes(long position, byte[] buff);

  public void putBytes(long position, byte[] buff);

  public void setBit(long bit);
  public boolean getBit(long bit);

  public void flush();

}
