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
    
    TaskMaster<Long> copy_task = new TaskMaster(copy_exec);

    {
      long pos = 0;
      while (pos < src_f.size())
      {
        copy_task.addTask(new SyncBlockTask(src_f, dst_f, pos));
        pos += BLOCK_SIZE;
      }
    }

    long copy_sz = 0;
    long dirty_count=0;
    long total_blocks = 0;
    for(long r : copy_task.getResults())
    {
      total_blocks++;
      if (r > 0)
      {
        dirty_count++;
        copy_sz+=r;
      }

    }
    double dirty_ratio = (double) dirty_count / (double) total_blocks;
    DecimalFormat df = new DecimalFormat("0.000");

    long copy_sz_mb = copy_sz / 1048576L;
    System.out.println(String.format("  dirty count: %d (%s) bytes: %d", dirty_count, df.format(dirty_ratio), copy_sz_mb));

    src_f.close();
    dst_f.close();

    dst.setLastModified( src.lastModified() );

  }

  public class SyncBlockTask implements Callable<Long>
  {
    FileChannel in;
    FileChannel out;
    long pos;

    public SyncBlockTask(FileChannel in, FileChannel out, long pos)
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
      byte[] block_b = new byte[sz];
      ByteBuffer block = ByteBuffer.wrap(block_b);
      {
        int r = in.read( block, pos);
        src_scan_rate.record(sz);
        if (r != sz) throw new Exception("Incomplete read");
      }

      block.rewind();

      MessageDigest md = MessageDigest.getInstance("SHA-256");
      String src_hash = Base64.getEncoder().encodeToString(md.digest(block_b));
      boolean dirty=true;
      if (out.size() >= pos+sz)
      {
        byte[] block_dest_b = new byte[sz];
        ByteBuffer block_dest = ByteBuffer.wrap(block_dest_b);
        int r = out.read( block_dest, pos );

        dst_scan_rate.record(sz);
        if (r != sz) throw new Exception("Incomplete read");
        String dst_hash = Base64.getEncoder().encodeToString(md.digest(block_dest_b));

        if (src_hash.equals(dst_hash))
        {
          dirty=false;

        }
      }
      if (dirty)
      {
        out.write(block, pos);
        copy_rate.record(sz);
        return (long)sz;

      }
      return 0L;

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
