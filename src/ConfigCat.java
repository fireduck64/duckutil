package duckutil;

import java.util.LinkedList;
import java.util.List;

public class ConfigCat extends Config
{
  List<Config> list;

  public ConfigCat(Config ... lst)
  {
    list = new LinkedList<>();
    for(Config c : lst) list.add(c);
  }

  @Override
  public String get(String key)
  {
    for(Config c : list)
    {
      String s = c.get(key);
      if (s != null) return s;
    }
    return null;
  }
}
