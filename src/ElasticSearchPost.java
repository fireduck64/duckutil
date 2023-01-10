package duckutil;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import net.minidev.json.JSONObject;

public class ElasticSearchPost
{


  public static int saveDoc(String elasticsearch_url, String index_base, JSONObject values)
    throws Exception
  {
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy.MM.dd");
    String url = String.format("%s%s-%s/_doc",elasticsearch_url,index_base,sdf.format(new Date()));
    URL u = new URL(url);

    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setInstanceFollowRedirects(false);

    connection.setRequestProperty("Content-Type", "application/json");

    JSONObject doc = new JSONObject();

    doc.putAll(values);
    if (!doc.containsKey("timestamp"))
    {
      SimpleDateFormat sdf_iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      sdf_iso.setTimeZone( TimeZone.getTimeZone("GMT") );
      doc.put("timestamp", sdf_iso.format(new Date()));
      doc.put("time_ms", System.currentTimeMillis());
    }

    OutputStream wr = connection.getOutputStream ();
    wr.write(doc.toJSONString().getBytes());
    wr.flush();
    wr.close();

    int code =  connection.getResponseCode();
    if ((code != 200) && (code != 201))
    {
      System.out.println("Elastic search doc code: " + code);
      System.out.println("Input: " + doc.toJSONString());

      Scanner scan = new Scanner(connection.getErrorStream());
      while(scan.hasNextLine())
      {
        System.out.println(scan.nextLine());
      }
    }
    return code;

  }


}
