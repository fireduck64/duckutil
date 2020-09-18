package duckutil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * Runs a process and keeps the standard output and standard error as a string.
 * Process is done when constructor returns. So good for quick things.
 */
public class ProcessRunner
{
  StringBuilder err_out;
  StringBuilder std_out;
  int exit_val;

  public ProcessRunner(Collection<String> cmd)
    throws Exception
  {
    ArrayList<String> lst = new ArrayList<>(cmd);
    String[] args = new String[cmd.size()];
    for(int i=0; i<cmd.size(); i++)
    {
      args[i] = lst.get(i);
    }
    Process proc = Runtime.getRuntime().exec(args);

    err_out = new StringBuilder();
    std_out = new StringBuilder();

    PipeThread pt_std = new PipeThread(std_out, proc.getInputStream());
    pt_std.start();

    PipeThread pt_err = new PipeThread(err_out, proc.getErrorStream());
    pt_err.start();

    exit_val = proc.waitFor();
    pt_std.join();
    pt_err.join();

  }

  public int getReturn(){return exit_val;}

  public String getError(){return err_out.toString();}
  public String getOutput(){return std_out.toString();}

  public class PipeThread extends Thread
  {
    private StringBuilder sb;
    private InputStream in;

    public PipeThread(StringBuilder sb, InputStream in)
    {
      this.sb = sb;
      this.in = in;
      
    }

    public void run()
    {
      Scanner scan = new Scanner(in);
      while(scan.hasNextLine())
      {
        String line = scan.nextLine();
        sb.append(line);
        sb.append('\n');
      }
      scan.close();
    }

  }


}
