package duckutil.jsonrpc;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.JSONObject;
import duckutil.ProcessRunner;
import java.util.Collection;

public class JsonProcessRunner extends ProcessRunner
{
  public JsonProcessRunner(Collection<String> cmd)
    throws Exception
  {
    super(cmd);
  }

  public JSONObject getJsonOutput()
    throws Exception
  {
     JSONParser parser = new JSONParser(JSONParser.MODE_STRICTEST);
     JSONObject json = (JSONObject)parser.parse(getOutput());
     return json;
  }
}

