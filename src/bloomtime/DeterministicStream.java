
package duckutil.bloomtime;

import java.util.Random;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import duckutil.TimeRecord;


  /**
   * Use the given data to make a deterministic random stream for use
   * in giving the hash values for the data
   */
  public class DeterministicStream extends Random
  {
    private byte[] hash_data;
    private ByteBuffer hash_buffer;


    public DeterministicStream(byte[] data)
    { 
      hash_data = hash(data);
      hash_buffer = ByteBuffer.wrap(hash_data);

    }

    private byte[] hash(byte[] in)
    {
      try
      {
        long t1 = System.nanoTime();
        // Doesn't need to be cryptographically secure, just deterministic
        // and psuedo-random
        MessageDigest sig=MessageDigest.getInstance("SHA-256");
        TimeRecord.record(t1, "hash_instance");

        long t2 = System.nanoTime();
        sig.update(in);
        byte[] d = sig.digest();
        TimeRecord.record(t2, "hash_digest");

        return d;
      }
      catch (java.security.NoSuchAlgorithmException e)
      {
        throw new RuntimeException(e);
      }
    }

    @Override
    protected int next(int bits) {
      
      int mask = 0xffffffff;
      int shift = 32 - bits;
      mask = mask >> shift;

      if (hash_buffer.remaining() < 8)
      {
        hash_data = hash(hash_data);
        hash_buffer = ByteBuffer.wrap(hash_data);
      }
      int d = hash_buffer.getInt();
      d = d & mask;
      return d;
    }

  }

