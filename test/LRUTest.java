
import duckutil.LRUCache;

import org.junit.Test;

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

}
