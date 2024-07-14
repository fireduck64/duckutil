package duckutil;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;

public class ConfigJson extends Config
{
    private String env_override_prefix;
    private JSONObject props;


    public ConfigJson(String file_name)
        throws java.io.IOException, net.minidev.json.parser.ParseException
    {
      this(file_name, null);
    }

    /** 
     * If set, override_prefix will be used as the env_override_prefix.
     * Example, if override_prefix is set to "a_" and a caller asks for "b"
     * then environment variable "a_b" will be checked before checking the file.
     */
    public ConfigJson(String file_name, String override_prefix)
        throws java.io.IOException, net.minidev.json.parser.ParseException
    {
        JSONParser parser = new JSONParser( JSONParser.MODE_STRICTEST );

        props = (JSONObject) parser.parse(new FileInputStream(file_name));

        if (get("env_override_prefix") != null)
        {
          env_override_prefix = get("env_override_prefix");
        }
        else
        {
          if (override_prefix != null)
          {
            env_override_prefix = override_prefix;
          }
        }

    }

    @Override
    public String get(String key)
    {
      if (env_override_prefix != null)
      {
        String k = env_override_prefix + key;
        if (System.getenv().containsKey(k))
        {
          return System.getenv().get(k);
        }
      }
      if (!props.containsKey(key)) return null;

      return "" + props.get(key);
    }

    @Override
    public List<String> getList(String key)
    {
      if (!props.containsKey(key)) return null;


      if (props.get(key) instanceof JSONArray)
      {
        JSONArray js_arr = (JSONArray) props.get(key);
        List<String> lst = new LinkedList<>();

        for(Object o : js_arr)
        {
          lst.add("" + o);
        }
        return lst;
      }

      throw new RuntimeException("getList for " + key + " and it is " + props.get(key).getClass());
    }

    /** Returns Primitive Type, or String, Or JsonObject or JsonArray */
    public Object getJson(String key)
    {
      return props.get(key);
    }

    public JSONObject getAsObject(String key)
    {
      return (JSONObject) props.get(key);
    }
    public JSONArray getAsArray(String key)
    {
      return (JSONArray) props.get(key);
    }
}
