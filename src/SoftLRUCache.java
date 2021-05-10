package duckutil;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Map;

public class SoftLRUCache<K,V>
{
  private LRUCache<K, SoftReference<V> > cache;

  public SoftLRUCache(int cap)
  {
    cache = new LRUCache<>(cap);
  }


  public void put(K k, V v)
  {
    cache.put(k,new SoftReference(v));
  }

  public V get(K k)
  {
    SoftReference<V> ref = cache.get(k);
    if (ref != null) return ref.get();

    return null;
  }

  public boolean containsKey(K k)
  {
    SoftReference<V> ref = cache.get(k);
    if (ref != null)
    if (ref.get() != null) 
    {
      return true;
    }
    return false;
  }

  /**
   * Clean out references that are no longer there
   * @return the number of removed mappings
   */
  public int prune()
  {
    HashSet<K> to_remove = new HashSet<>();
    for(Map.Entry<K, SoftReference<V>> me : cache.entrySet())
    {
      if (me.getValue().get() == null)
      {
        to_remove.add(me.getKey());
      }
    }

    for(K k : to_remove)
    {
      cache.remove(k);
    }
    return to_remove.size();

  }

  public int size()
  {
    return cache.size();
  }


}
