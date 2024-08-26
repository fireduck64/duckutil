package duckutil;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.Base64;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.ArrayList;
import java.text.DecimalFormat;


/**
 * Kinda like rsync, but designed for large files with few changes
 * Does a better job scanning them both and identifying the changed blocks
 * At least in theory
 */
public class Synco
{
  public static int BLOCK_SIZE=16*1024*1024;
  public static int SRC_SCAN_THREADS=4;
  public static int DST_SCAN_THREADS=4;
  public static int COPY_THREADS=8;

  public static long RATE_LOOK_BACK=30L * 1000L;

  public static void main(String args[]) throws Exception
  {
    if (args.length != 2)
    {
      System.out.println("Syntax: Synco <src_dir> <dest_dir>");
      System.exit(-1);
      return;
    }
    new Synco(new File(args[0]), new File(args[1]));
  }
  
  ThreadPoolExecutor src_scan_exec = TaskMaster.getBasicExecutor(SRC_SCAN_THREADS, "synco_scan_src");
  ThreadPoolExecutor dst_scan_exec = TaskMaster.getBasicExecutor(DST_SCAN_THREADS, "synco_scan_dst");
  ThreadPoolExecutor copy_exec = TaskMaster.getBasicExecutor(COPY_THREADS, "synco_copy");

  RateTracker src_scan_rate = new RateTracker(RATE_LOOK_BACK);
  RateTracker dst_scan_rate = new RateTracker(RATE_LOOK_BACK);
  RateTracker copy_rate = new RateTracker(RATE_LOOK_BACK);


  public Synco(File src_dir, File dst_dir)
    throws Exception
  {
    if (!src_dir.isDirectory())
    {
      System.err.println("Source dir is not a directory: " + src_dir.toString());
      System.exit(-1);
      return;
    }
    if (!dst_dir.isDirectory())
    {
      System.err.println("Destination dir is not a directory: " + dst_dir.toString());
      System.exit(-1);
      return;
    }
    new RatePrintThread().start();
    recursiveCopy(src_dir, dst_dir, src_dir);

    deleteCheck(src_dir, dst_dir, dst_dir);

  }
  private void deleteCheck(File src_dir, File dst_dir, File cur)
    throws Exception
  {
    Path rel_path = dst_dir.toPath().relativize(cur.toPath());
    File src_file = src_dir.toPath().resolve(rel_path).toFile();

    if(cur.isDirectory())
    {
      for(File f : cur.listFiles())
      {
        deleteCheck(src_dir, dst_dir, f);
      }
    }

    if (!src_file.exists())
    {
      System.out.println("  to delete: " + rel_path);
      cur.delete();
    }


  }

  private void recursiveCopy(File src_dir, File dst_dir, File cur)
    throws Exception
  {
    Path rel_path = src_dir.toPath().relativize(cur.toPath());
    File dst_file = dst_dir.toPath().resolve(rel_path).toFile();

    if (cur.isDirectory())
    {
      if (!dst_file.exists())
      {
        dst_file.mkdir();
      }
      for(File f : cur.listFiles())
      {
        recursiveCopy(src_dir, dst_dir, f);
      }
      return;
    }

    monsterCopy(cur, dst_file);
    

  }

  private void monsterCopy(File src, File dst)
    throws Exception
  {
    System.out.println("Monster copy: " + src + " " + dst);

    FileChannel src_f = FileChannel.open(src.toPath(), StandardOpenOption.READ);

    FileChannel dst_f = FileChannel.open(dst.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);

    if (dst_f.size() > src_f.size())
    { 
      dst_f.truncate(src_f.size());
    }

    TaskMaster<String> src_task = new TaskMaster(src_scan_exec);
    TaskMaster<String> dst_task = new TaskMaster(dst_scan_exec);

    {
      long pos = 0;
      while (pos < src_f.size())
      {
        src_task.addTask(new ReadBlockTask(src_f, pos, src_scan_rate));
        pos += BLOCK_SIZE;
      }
    }
    {
      long pos = 0;
      while (pos < dst_f.size())
      {
        dst_task.addTask(new ReadBlockTask(dst_f, pos, dst_scan_rate));
        pos += BLOCK_SIZE;
      }
    }

    ArrayList<String> src_hash_lst = src_task.getResults();
    ArrayList<String> dst_hash_lst = dst_task.getResults();
    
    TaskMaster<Long> copy_task = new TaskMaster(copy_exec);

    int dirty_count=0;
    for(int block_idx = 0; block_idx < src_hash_lst.size(); block_idx++)
    {
      long pos = BLOCK_SIZE;
      pos = pos * block_idx;
      boolean dirty = true;
      if (block_idx < dst_hash_lst.size())
      if (src_hash_lst.get(block_idx).equals(dst_hash_lst.get(block_idx)))
      {
        dirty = false;
      }
     
      if (dirty)
      {
        dirty_count++;
        copy_task.addTask(new CopyTask(src_f, dst_f, pos));
      }

    }
    System.out.println("  dirty count: " + dirty_count);
    copy_task.getResults();

    src_f.close();
    dst_f.close();

    dst.setLastModified( src.lastModified() );



  }

  public class CopyTask implements Callable<Long>
  {
    FileChannel in;
    FileChannel out;
    long pos;

    public CopyTask(FileChannel in, FileChannel out, long pos)
    {
      this.in = in;
      this.out = out;
      this.pos = pos;

    }

    public Long call() throws Exception
    {
      int sz = BLOCK_SIZE;
      if (in.size() < pos + BLOCK_SIZE)
      {
        sz = (int)(in.size() - pos);
      }
      if (sz <= 0) throw new Exception("Read past size");
      byte[] block_b = new byte[BLOCK_SIZE];
      ByteBuffer block = ByteBuffer.wrap(block_b);
      int r = in.read( block, pos);
      if (r != sz) throw new Exception("Incomplete read");

      block.rewind();
      out.write(block, pos);

      copy_rate.record(r);

      return (long)r;

    }

  }

  public class ReadBlockTask implements Callable<String>
  {
    FileChannel in;
    long pos;
    RateTracker tracker;

    public ReadBlockTask(FileChannel in, long pos, RateTracker tracker)
    {
      this.in = in;
      this.pos = pos;
      this.tracker = tracker;
    }

    public String call() throws Exception
    {
      int sz = BLOCK_SIZE;
      if (in.size() < pos + BLOCK_SIZE)
      {
        sz = (int)(in.size() - pos);
      }
      if (sz <= 0) throw new Exception("Read past size");
      byte[] block_b = new byte[BLOCK_SIZE];
      ByteBuffer block = ByteBuffer.wrap(block_b);
      int r = in.read( block, pos);
      if (r != sz) throw new Exception("Incomplete read");

      tracker.record(r);

      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return Base64.getEncoder().encodeToString(md.digest(block_b));
    }

  }

  public class RatePrintThread extends PeriodicThread
  {
    public RatePrintThread()
    {
      super(30000);
      setDaemon(true);
    } 

    @Override
    public void runPass()
    {
      DecimalFormat df=new DecimalFormat("0.0");

      double scan_src = src_scan_rate.getRatePerSecond()/1e6;
      double scan_dst = dst_scan_rate.getRatePerSecond()/1e6;
      double copy = copy_rate.getRatePerSecond()/1e6;

      System.out.println(
        String.format("Rate: scan_src %s MB/s, scan_dst %s MB/s, copy %s MB/s",
        df.format(scan_src),
        df.format(scan_dst),
        df.format(copy)));



    }

  }

}
