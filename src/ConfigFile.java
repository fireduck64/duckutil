package duckutil;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigFile extends Config
{
    private Properties props;
    private String env_override_prefix;

    public ConfigFile(String file_name)
        throws java.io.IOException
    {
        props = new Properties();

        props.load(new FileInputStream(file_name));

        if (get("env_override_prefix") != null)
        {
          env_override_prefix = get("env_override_prefix");
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
      return props.getProperty(key);
    }
}
