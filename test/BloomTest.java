


import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import duckutil.bloomtime.BloomFilter;
import java.util.ArrayList;

public class BloomTest
{

  @Rule
  public TemporaryFolder test_folder = new TemporaryFolder();

  @Test
  public void testBloomFilterRoundUp()
    throws Exception
  {
    File f = test_folder.newFile();

    BloomFilter bf = new BloomFilter(f, 8000001, 24);

    Assert.assertEquals(1000001L, f.length());
  }


  @Test
  public void testBloomFilterBasic()
    throws Exception
  {
    File f = test_folder.newFile();

    BloomFilter bf = new BloomFilter(f, 8000000, 24);

    Assert.assertEquals(1000000L, f.length());

    ArrayList<String> lst = new ArrayList<>();
    Random rnd = new Random();
    for(int i=0; i<100; i++)
    {
      lst.add("s" + rnd.nextLong());
    }

    for(String s : lst)
    {
      bf.add(s);
      Assert.assertTrue(bf.check(s));
    }
    

    bf.flush();

    for(String s : lst)
    {
      Assert.assertTrue(bf.check(s));
    }

    for(int i=0; i<100; i++)
    {
      String nope = "n" + rnd.nextLong();
      System.out.println(nope);
      Assert.assertFalse(bf.check(nope));
    }
    


  }

  @Test
  public void testBloomFilterReopen()
    throws Exception
  {
    File f = test_folder.newFile();

    BloomFilter bf = new BloomFilter(f, 8000000, 24);

    Assert.assertEquals(1000000L, f.length());

    ArrayList<String> lst = new ArrayList<>();
    Random rnd = new Random();
    for(int i=0; i<100; i++)
    {
      lst.add("s" + rnd.nextLong());
    }

    for(String s : lst)
    {
      bf.add(s);
      Assert.assertTrue(bf.check(s));
    }
    
    bf.flush();
    bf = new BloomFilter(f, 8000000, 24);

    for(String s : lst)
    {
      Assert.assertTrue(bf.check(s));
    }

    for(int i=0; i<100; i++)
    {
      String nope = "n" + rnd.nextLong();
      System.out.println(nope);
      Assert.assertFalse(bf.check(nope));
    }
    


  }



}
