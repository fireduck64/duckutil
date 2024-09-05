package duckutil.bloomtime;
import java.io.File;
import java.util.TreeSet;
import java.nio.ByteBuffer;
import java.util.stream.LongStream;

public class BloomFilter
{
  private final long filter_size_bits;
  private final int k_hashes;
  private final LongFile bit_file;
  private TreeSet<Long> set_buffer;

  public BloomFilter(File back, long filter_size_bits, int k_hashes)
    throws Exception
  {
    this.filter_size_bits = filter_size_bits;
    this.k_hashes = k_hashes;

    long sz = filter_size_bits/8L;
    if (filter_size_bits % 8 != 0) sz++;

    this.bit_file = new LongMappedBuffer(back, sz);

    set_buffer = new TreeSet<Long>();

  }

  public void add(String item)
  {
    for(long v : resolve(item).toArray())
    {
      bit_file.setBit(v);
    }

  }

  public boolean check(String item)
  {
    for(long v : resolve(item).toArray())
    {
      if (!bit_file.getBit(v)) return false;
    }
    return true;
  }

  public void flush()
  {
    bit_file.flush();
  }

  protected LongStream resolve(String item)
  {
    return new DeterministicStream(item.getBytes())
      .longs(0, filter_size_bits)
      .limit(k_hashes);
  }


}
