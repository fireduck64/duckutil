package duckutil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Does no syncronization
 */
public class ExpiringLRUCache<K,V>
{
  private LRUCache<K,CacheElement<V> > cache;
  private long expire_time_ms;

	private static final long serialVersionUID=11L;

	public ExpiringLRUCache(int cap, long expire_ms)
	{
    cache = new LRUCache<K, CacheElement<V> >(cap);
    this.expire_time_ms = expire_ms;

	}

  public void put(K k, V v)
  {
    cache.put(k, new CacheElement<V>(v));  
  }

  public V get(K k)
  {
    CacheElement<V> c = cache.get(k);
    if (c != null)
    {
      if (c.getTime() + expire_time_ms < System.currentTimeMillis())
      {
        cache.remove(k);
        return null;
      }
      return c.getValue();

    }
    return null;
  }


  public class CacheElement<V>
  {
    private V value;
    private long time;
    public CacheElement(V v)
    {
      this.value = v;
      this.time = System.currentTimeMillis();
    }

    public V getValue(){return value;}
    public long getTime(){return time;}
  }


}
