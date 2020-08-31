package duckutil.webserver;

import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.logging.Logger;

public class WebContext implements AutoCloseable
{
  private static final Logger logger = Logger.getLogger("duckutil.webserver");

  private final HttpExchange http_exchange;

  private int http_code=500;
  private String content_type="text/plain";
  private long output_size = -1;

  private ByteArrayOutputStream output_buffer;
  private PrintStream print_out;

  private boolean out_written;

  // Things that needs to be set
  //  size
  //  content-type
  //  http code

  public WebContext(HttpExchange t)
  {
    this.http_exchange = t;
    logger.info("Request - " + getURI());
    resetBuffer();

  }

  public URI getURI()
  {
    return http_exchange.getRequestURI();
  }
  public String getHost()
  {
    return http_exchange.getRequestHeaders().get("Host").get(0);
  }

  public InputStream getRequestBody() { return http_exchange.getRequestBody(); }
  public String getRequestMethod() { return http_exchange.getRequestMethod(); }

  public void resetBuffer()
  {
    output_buffer = new ByteArrayOutputStream();
    print_out = new PrintStream(output_buffer);
  }
  public void setHttpCode(int code) { http_code = code; }
  public void setContentType(String type) { content_type = type; }
  public void setOutputSize(long size) { output_size = size; }

  public void setException(Throwable t)
  {
    setHttpCode(500);
    setContentType("text/plain");
    out().println(t);
  }

  public PrintStream out(){return print_out; }

  public void writeHeaders()
    throws java.io.IOException
  {
    if (output_size < 0)
    {
      output_size = output_buffer.toByteArray().length;
    }

    logger.info(String.format("HTTP result: %s code:%d sz:%d", content_type, http_code, output_size));
    http_exchange.getResponseHeaders().add("Content-type",content_type);
    http_exchange.sendResponseHeaders( http_code, output_size);

  }

  /**
   * If this is used, do setOutputSize() then writeHeaders() first
   */
  public OutputStream getOutStream()
  {
    out_written=true;
    return http_exchange.getResponseBody();
  }

  public void close()
  {
    try
    {
      if (!out_written)
      {
        writeHeaders();
        byte[] data = output_buffer.toByteArray();
        OutputStream out = http_exchange.getResponseBody();
        out.write(data);
        out_written = true;
      }

      http_exchange.getResponseBody().flush();
      http_exchange.getResponseBody().close();
    }
    catch(java.io.IOException e)
    {
      logger.info(e.toString());
    }


  }

}
