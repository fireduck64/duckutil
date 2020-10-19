package duckutil.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import duckutil.TaskMaster;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class DuckWebServer
{
  private static final Logger logger = Logger.getLogger("duckutil.webserver");
  private final HttpServer http_server;
  private final WebHandler handler;

  public DuckWebServer(String web_host, int web_port, WebHandler handler, int threads)
    throws IOException
  {
    InetSocketAddress listen = new InetSocketAddress(web_port);
    if (web_host != null)
    {
      listen = new InetSocketAddress(web_host, web_port);
    }

    this.handler = handler;

    http_server = HttpServer.create(listen, 16);
    http_server.createContext("/", new RootHandler());
    http_server.setExecutor(TaskMaster.getBasicExecutor(threads,"web_server"));
    http_server.start();
  }

  public class RootHandler implements HttpHandler
  {
    public void handle(HttpExchange t) throws IOException 
    {
      try(WebContext ctx = new WebContext(t))
      {
        try
        {
          handler.handle(ctx);
        }
        catch(Exception e)
        {
          ctx.setException(e);

        }
      }

    }
  }

}
