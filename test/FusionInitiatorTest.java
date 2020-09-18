
import duckutil.FusionInitiator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class FusionInitiatorTest
{

  @Test
  public void basicFusion()
  {
    FusionInitiator fi = new FusionInitiator(10);
    fi.start();

    for(int i=0; i<1000; i++)
    for(int j=0; j<10; j++)
    {
      fi.taskWait(j);
      fi.taskComplete(j);
    }
  }



  @Test
  public void threadFusionLarge()
    throws Exception
  {
    testSet(200, 16);
  }

  @Test
  public void threadFusionTiny()
    throws Exception
  {
    testSet(200, 1);
  }

  @Test
  public void threadFusionSmall()
    throws Exception
  {
    testSet(200, 2);
  }



  private void testSet(int passes, int tc)
    throws Exception
  {
    FusionInitiator fi = new FusionInitiator(tc);
    fi.start();

    LinkedList<Integer> lst = new LinkedList<>();

    LinkedList<FusionThread> threads = new LinkedList<>();

    for(int i=0; i<tc; i++)
    {
      FusionThread ft = new FusionThread(fi, i, lst, passes);
      ft.start();
      threads.add(ft);
    }

    for(FusionThread ft : threads)
    {
      ft.join();
    }

    Assert.assertEquals(passes*tc*2, lst.size());

    //System.out.println(lst);

    ArrayList<Integer> lst2 = new ArrayList<Integer>();
    lst2.addAll(lst);

    for(int i=0; i<lst2.size() -1; i++)
    {
      int a = lst.get(i);
      int b = lst.get(i+1);
      //System.out.println("" + a + " " + b);
      Assert.assertTrue( Math.abs(lst.get(i) - lst.get(i+1)) <= 2);
    }

  }


  public class FusionThread extends Thread
  {
    FusionInitiator fi;
    int task;
    LinkedList<Integer> lst;
    int passes;

    public FusionThread(FusionInitiator fi, int task, LinkedList<Integer> lst, int passes)
    {
      this.fi = fi;
      this.task = task;
      this.lst = lst;
      this.passes = passes;
    }

    public void run()
    {
      Random rnd = new Random();
      for(int pass=0; pass<passes; pass++)
      {
        fi.taskWait(task);
        synchronized(lst)
        {
          lst.add(pass);
        }
        try
        {
          sleep(rnd.nextInt(50)+1);
        }
        catch(Exception e){throw new RuntimeException(e);}
        synchronized(lst)
        {
          lst.add(pass);
        }
        fi.taskComplete(task);

      }
    }

  }

}
