package duckutil.jsonrpc;

import net.minidev.json.JSONObject;
import java.util.Random;
import java.util.Base64;
import java.net.HttpURLConnection;
import java.net.URL;
import net.minidev.json.parser.JSONParser;
import java.io.OutputStream;
import java.util.Scanner;

public class JsonRpcClient
{
  private final String uri;
  private final String username;
  private final String password;
  private Random rnd;

  public JsonRpcClient(String uri, String username, String password)
  {
    rnd = new Random();
    this.uri = uri;
    this.username = username;
    this.password = password;

  }
  public JsonRpcClient(String uri)
  {
    this(uri, null, null);
  }


  private long nextID()
  {
    synchronized(rnd)
    {
      return rnd.nextLong();
    }
  }
  
  /**
   * Do a basic request with no params
   */
  public JSONObject request(String method)
		throws Exception
  {
    return request(method, null);
  }

  public JSONObject request(String method, JSONObject params)
		throws Exception
  {
    JSONObject req = new JSONObject();
    req.put("jsonrpc", "2.0");
    req.put("method", method);
    req.put("id", nextID());

    if (params != null)
    {
      req.put("params", params);
    }
    else
    {
      req.put("params", new JSONObject());
    }

		return request(req);

  }

  /** 
   * Do request with already completed request object
   */
  public JSONObject request(JSONObject req)
		throws Exception
  {
    URL u = new URL(uri);

    HttpURLConnection connection = (HttpURLConnection) u.openConnection();

    if (username != null)
    {
      String basic = username+":"+password;
      String encoded = Base64.getEncoder().encodeToString(basic.getBytes());
      connection.setRequestProperty("Authorization", "Basic "+encoded);
    }

		String postdata = req.toJSONString();

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length", "" + Integer.toString(postdata.getBytes().length));
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);

		OutputStream wr = connection.getOutputStream();
		wr.write(postdata.getBytes());
		wr.flush();
		wr.close();

		Scanner scan;

		scan = new Scanner(connection.getInputStream());

		StringBuilder sb = new StringBuilder();

		while(scan.hasNextLine())
		{
				String line = scan.nextLine();
				sb.append(line);
				sb.append('\n');
		}
		scan.close();
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject json = (JSONObject)parser.parse(sb.toString());
		return json;

  }

}
  
 
