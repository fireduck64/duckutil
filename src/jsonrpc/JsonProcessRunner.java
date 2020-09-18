package duckutil.jsonrpc;

import duckutil.ProcessRunner;
import java.util.Collection;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

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
     JSONObject json = (JSONObject)parser.parse(getOutput().trim());
     return json;
  }

  public JSONArray getJsonArray()
    throws Exception
  {
     JSONParser parser = new JSONParser(JSONParser.MODE_STRICTEST);
     JSONArray json = (JSONArray)parser.parse(getOutput().trim());
     return json;
  }
}
