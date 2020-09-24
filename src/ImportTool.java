package duckutil;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeSet;

// Super simple import cleaner
// also changes tabs into two spaces
public class ImportTool
{
  public static void main(String args[])
    throws Exception
  {
    for(String s : args)
    {
      System.out.println("Processing " + s);
      new ImportTool(s);
    }
  }

  public ImportTool(String src_path)
    throws Exception
  {
    File src_file = new File(src_path);
    if (!src_file.isFile())
    {
      throw new Exception("is not regular file");
    }

    File tmp_file = new File( src_file.getParentFile(), src_file.getName() +".import.tmp");

    PrintStream tmp_out = new PrintStream(new FileOutputStream(tmp_file, false));

    Scanner scan = new Scanner(new FileInputStream(src_file));

    // 0 - before imports
    // 1 - in imports
    // 2 - after imports
    int mode = 0;

    TreeSet<String> import_lines=new TreeSet<>();
    LinkedList<String> after_lines = new LinkedList<>();
    TreeSet<String> tokens = new TreeSet<>();

    while(scan.hasNextLine())
    {
      String line = scan.nextLine();
      if (line.startsWith("import"))
      {
        if (mode == 2) throw new RuntimeException("unexpected import in mode 2: " + line);
        mode = 1;
        import_lines.add(line.trim());
      }
      else
      {
        if ((mode ==0) || (mode==1))
        {
          if (line.trim().length() > 0)
          {
            if (mode == 1)
            {
              mode = 2;
            }
          }

          if (mode == 0)
          {
            if (line.trim().length() > 0)
            {
              tmp_out.println(line);
            }
          }
          else if (mode == 1)
          {
            if (line.trim().length() > 0)
            {
              tmp_out.println(line);
            }
          }
          else
          {
            after_lines.add(line);
            extractTokens(line, tokens);
          }
        }
        else
        {
          after_lines.add(line);
          extractTokens(line, tokens);
        }
      }
    }

    //System.out.println(tokens);

    tmp_out.println("");
    for(String im : import_lines)
    {
      if (hasKeyword(im,tokens))
      {
        tmp_out.println(im);
      }
      else
      {
        System.out.println("Skipping: " + im);
      }
    }
    tmp_out.println("");
    // Trim blank at end
    while((after_lines.size() > 0) && (after_lines.peekLast().trim().length()==0))
    {
      after_lines.pollLast();
    }

    for(String line : after_lines)
    {
      line = line.replace("\t","  ");
      tmp_out.println(line);
    }

    tmp_out.close();
    tmp_file.renameTo(src_file);
  }

  private boolean hasKeyword(String line, TreeSet<String> tokens)
  {
    int last_dot = line.lastIndexOf('.');
    if (last_dot < 0) throw new RuntimeException("wtfbbq no dot: " + line);

    String id = line.substring(last_dot+1);
    id = id.replace(';',' ');
    id = id.trim();
    if (id.equals("*")) return true;

    if (tokens.contains(id)) return true;
    System.out.println("No: " + id);
    return false;
  }

  private void extractTokens(String line, TreeSet<String> tokens)
  {
    String accum="";

    for(char c : line.toCharArray())
    {
      if (Character.isJavaIdentifierPart(c))
      {
        accum+=c;
      }
      else
      {
        tokens.add(accum);
        accum="";
      }
    }
    tokens.add(accum);

  }

}
