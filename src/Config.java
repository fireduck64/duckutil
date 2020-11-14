package duckutil;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class Config
{

    public abstract String get(String key);

    public String getWithDefault(String key, String def)
    {
      if (isSet(key))
      {
        return get(key);
      }
      return def;
    }

    public String require(String key)
    {
      String s = get(key);
      if(s == null)
      {
        throw new RuntimeException("Missing required key: " + key);
      }
      return s;
    }


    public int getInt(String key)
    {
        return Integer.parseInt(get(key));
    }

    public int getIntWithDefault(String key, int def)
    {
      if (isSet(key))
      {
        return getInt(key);
      }
      return def;
    }

    public long getLong(String key)
    {
        return Long.parseLong(get(key));
    }

    public long getLongWithDefault(String key, long def)
    {
      if (isSet(key))
      {
        return getLong(key);
      }
      return def;
    }


    public double getDouble(String key)
    {
        return Double.parseDouble(get(key));
    }

    public double getDoubleWithDefault(String key, double def)
    {
      if (isSet(key))
      {
        return getDouble(key);
      }
      return def;
    }

    public List<String> getList(String key)
    {
        String big_str = get(key);

        StringTokenizer stok = new StringTokenizer(big_str, ",");

        LinkedList<String> lst = new LinkedList<String>();
        while(stok.hasMoreTokens())
        {
            String node = stok.nextToken().trim();
            lst.add(node);
        }
        return lst;
    }

    public boolean getBoolean(String key)
    {
        String v = get(key);
        if (v == null) return false;
        v = v.toLowerCase();
        if (v.equals("1")) return true;
        if (v.equals("true")) return true;
        if (v.equals("yes")) return true;
        if (v.equals("y")) return true;
        if (v.equals("on")) return true;
        if (v.equals("hell yeah")) return true;

        return false;
    }
    
    public boolean isSet(String key)
    {
        return (get(key) != null && !get(key).isEmpty());
    }
}
