package duckutil.bloomtime;

import duckutil.TimeRecord;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.BitSet;

public class LongMappedBuffer implements LongFile
{
  public static final long MAP_SIZE = 64 * 1024 * 1024;
  private ArrayList<MappedByteBuffer> map_list;
  private long total_size;

  private byte[] byte_mappings;

  public LongMappedBuffer(File f, long total_size)
    throws IOException
  {
    this(f, total_size, true);
  }
  public LongMappedBuffer(File f, long total_size, boolean enable_write)
    throws IOException
  {
    String mode = "r";
    FileChannel.MapMode fc_mode = FileChannel.MapMode.READ_ONLY;

    if(enable_write)
    {
      mode="rw";
      fc_mode = FileChannel.MapMode.READ_WRITE;
    }

    RandomAccessFile raf = new RandomAccessFile(f, mode);
    FileChannel chan = raf.getChannel();

    this.total_size = total_size;

    map_list=new ArrayList<>();

    long opened = 0;
    while(opened < total_size)
    {
      long len = Math.min(total_size - opened, MAP_SIZE);
      MappedByteBuffer buf = chan.map(fc_mode, opened, len);

      opened += len;

      map_list.add(buf);
    }

    byte_mappings = new byte[8];
    for(int i=0; i<8; i++)
    {
      BitSet bs = new BitSet(8);
      bs.set(i);
      byte[] b = bs.toByteArray();
      byte_mappings[i]=b[0];
    }
  }
  public void flush()
  {
    for(MappedByteBuffer mbb : map_list)
    {
      mbb.force();
    }

  }

  public void getBytes(long position, byte[] buff)
  {
    long t1 = System.nanoTime();

    if (buff.length == 0) return;

    int to_read=buff.length;

    int start_file = (int) (position / MAP_SIZE);
    int start_offset = (int) (position % MAP_SIZE);

    MappedByteBuffer map = map_list.get(start_file);

    map.position(start_offset);
    int len = Math.min(to_read, (int) (MAP_SIZE - start_offset));

    map.get(buff, 0, len);
    if (len < to_read)
    {
      map = map_list.get(start_file + 1);
      map.position(0);
      map.get(buff, len, to_read - len);
    }
    TimeRecord.record(t1, "long_map_get_bytes");
  }

  public void putBytes(long position, byte[] buff)
  {
    long t1 = System.nanoTime();

    int to_write=buff.length;

    int start_file = (int) (position / MAP_SIZE);
    int start_offset = (int) (position % MAP_SIZE);

    MappedByteBuffer map = map_list.get(start_file);

    map.position(start_offset);
    int len = Math.min(to_write, (int) (MAP_SIZE - start_offset));

    map.put(buff, 0, len);
    if (len < to_write)
    {
      map = map_list.get(start_file + 1);
      map.position(0);
      map.put(buff, len, to_write - len);
    }
    TimeRecord.record(t1, "long_map_put_bytes");
  }

  public void setBit(long bit)
  {
    long t1=System.nanoTime();
    long data_pos = bit / 8;
    int file = (int) (data_pos / MAP_SIZE);
    int file_offset = (int) (data_pos % MAP_SIZE);

    int bit_in_byte = (int)(bit % 8);

    long t1_read = System.nanoTime();
    byte[] b = new byte[1];
    MappedByteBuffer map = map_list.get(file);
    synchronized(map)
    {

      b[0]=map.get(file_offset);
      TimeRecord.record(t1_read, "long_map_set_bit_read");

      byte n = (byte)(b[0] | byte_mappings[bit_in_byte]);

      if (b[0] != n)
      {
        long t1_write = System.nanoTime();
        map.put(file_offset, n);
        TimeRecord.record(t1_write, "long_map_set_bit_write");
      }

      TimeRecord.record(t1, "long_map_set_bit");
    }
  }

  public boolean getBit(long bit)
  {
    long t1=System.nanoTime();
    long data_pos = bit / 8;
    int file = (int) (data_pos / MAP_SIZE);
    int file_offset = (int) (data_pos % MAP_SIZE);

    int bit_in_byte = (int)(bit % 8);

    long t1_read = System.nanoTime();
    byte[] b = new byte[1];
    MappedByteBuffer map = map_list.get(file);

    b[0]=map.get(file_offset);
    TimeRecord.record(t1_read, "long_map_get_bit_read");

    byte n = (byte)(b[0] & byte_mappings[bit_in_byte]);

    return (n != 0);
  }

}
