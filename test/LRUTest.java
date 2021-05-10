
import duckutil.LRUCache;
import duckutil.SoftLRUCache;

import org.junit.Test;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.util.Random;

public class LRUTest
{

  @Test
  public void testLRUPerf()
  {
    LRUCache<Long, Long> cache = new LRUCache<>(25000);

    for(long i=0; i<5000000L; i++)
    {
      cache.put(i,i);
    }
    

  }

  @Test
  public void testSoftLRU()
  {
    SoftLRUCache<Long, ByteBuffer> cache = new SoftLRUCache<>(100000);

    Random rnd = new Random();

    // This bad boy will allocate 10g of random crap
    // so without soft references this isn't going to go well
    for(long i=0; i<10000L; i++)
    {
      byte[] b = new byte[1048576];
      rnd.nextBytes(b);
      cache.put(i, ByteBuffer.wrap(b));
    }

    int removed = cache.prune();
    Assert.assertTrue(removed > 500);

    System.out.println("Removed1: " + removed);

    System.gc();
    removed = cache.prune();
    System.out.println("Removed2: " + removed);
    Assert.assertEquals(0, removed);

  }

}
