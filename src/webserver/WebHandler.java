package duckutil.webserver;
public interface WebHandler
{
  public void handle(WebContext t) throws Exception;
}


