package duckutil;

import java.text.SimpleDateFormat;
import net.minidev.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Date;
import java.io.OutputStream;


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
      doc.put("timestamp", sdf_iso.format(new Date()));
      doc.put("time_ms", System.currentTimeMillis());
    }

    OutputStream wr = connection.getOutputStream ();
    wr.write(doc.toJSONString().getBytes());
    wr.flush();
    wr.close();

    int code =  connection.getResponseCode();
		return code;

  }


}
