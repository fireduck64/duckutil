package duckutil;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
/**
 * Outputs to a temporary file and then renames
 */
public class AtomicFileOutputStream extends OutputStream
{
  private File out_file;
  private File tmp_file;
  private FileOutputStream tmp_out;

  public AtomicFileOutputStream(String out_file)
    throws java.io.IOException
  {
    this(new File(out_file));
  }



  public AtomicFileOutputStream(File dst_file)
    throws java.io.IOException
  {
    this.out_file = dst_file;

    File parent = out_file.getParentFile();

    tmp_file = File.createTempFile(out_file.getName(), "atomic_tmp", parent);

    tmp_out = new FileOutputStream(tmp_file);

  }

  @Override
  public void close()
    throws java.io.IOException
  {
    tmp_out.close();
    Path dst_file_path = out_file.toPath();
    Path tmp_file_path = tmp_file.toPath();

    Files.move(tmp_file_path, dst_file_path, StandardCopyOption.REPLACE_EXISTING);
  }
  
  @Override
  public void flush()
    throws java.io.IOException
  {
    tmp_out.flush();
  }

  @Override
  public void write(byte[] b)
    throws java.io.IOException
  {
    tmp_out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len)
    throws java.io.IOException
  {
    tmp_out.write(b, off, len);
  }

  @Override
  public void write(int b)
    throws java.io.IOException
  {
    tmp_out.write(b);
  }

}

