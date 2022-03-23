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
      this(file_name, null);
    }

    /** 
     * If set, override_prefix will be used as the env_override_prefix.
     * Example, if override_prefix is set to "a_" and a caller asks for "b"
     * then environment variable "a_b" will be checked before checking the file.
     */
    public ConfigFile(String file_name, String override_prefix)
        throws java.io.IOException
    {
        props = new Properties();

        props.load(new FileInputStream(file_name));

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
      return props.getProperty(key);
    }
}
