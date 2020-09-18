package lobstack;

import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.junit.Assert;

public class ZUtil
{
  private static ThreadLocal<Deflater> defs=new ThreadLocal<Deflater>();
  private static ThreadLocal<Inflater> infs=new ThreadLocal<Inflater>();

  public static byte[] compress(byte[] in)
  {
    Deflater def = defs.get();
    if (def == null)
    {
      def = new Deflater();
      defs.set(def);
    }

    def.reset();

    def.setInput(in);
    def.finish();
    int sz = in.length + 1000;
    byte[] b = new byte[sz];
    int r = def.deflate(b);
    
    Assert.assertTrue(r < sz);
    Assert.assertTrue(r > 0);
    byte[] buff = new byte[r];
    System.arraycopy(b, 0, buff, 0, r);

    def.reset();

    return buff;
  }

  public static ByteString compress(ByteString in)
  {
    return ByteString.copyFrom( compress(in.toByteArray()));
  }

  public static ByteString decompress(ByteString in)
  {
    return ByteString.copyFrom( decompress(in.toByteArray()));
  }
  public static byte[] decompress(byte[] in)
  {
    try
    {
      Inflater inf = infs.get();
      if (inf == null)
      {
        inf = new Inflater();
        infs.set(inf);
      }

      inf.reset();
      inf.setInput(in);
      inf.finished();

      ByteArrayOutputStream b_out = new ByteArrayOutputStream();
      byte[] buff=new byte[10240];
      while(true)
      {
        int r = inf.inflate(buff);
        if (r ==0) break;
        b_out.write(buff,0,r);
      }
      inf.reset();

      return b_out.toByteArray();
    }
    catch(java.util.zip.DataFormatException e)
    {
      throw new RuntimeException(e);
    }
  }

}
