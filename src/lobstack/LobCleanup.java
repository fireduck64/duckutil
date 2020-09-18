package lobstack;

import java.io.File;

public class LobCleanup
{

  public static void main(String args[]) throws Exception
  {
    String path = args[0];
    String name = args[1];
    boolean comp = false;
    if (args.length > 2)
    {
      if (args[2].equals("true"))
      {
        comp=true;
      }
    }

    

    new LobCleanup(new File(path), name, comp);

  }

  private Lobstack input;



  public LobCleanup(File path, String name, boolean comp)
    throws Exception
  {
    input = new Lobstack(path, name, comp);

    input.cleanup(24,0.90,8L*1024L*1024L*1024L);
    input.printTreeStats();   
    //input.printTreeStats();   


  }

}
