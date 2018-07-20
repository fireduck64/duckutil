package duckutil.jsonrpc;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import duckutil.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;

import duckutil.TaskMaster;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Authenticator;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;

import java.util.Scanner;


public class JsonRpcServer
{
  private final HttpServer http_server;
	private final Config config;

  private final AuthAgent auth;

  private final Dispatcher dispatcher;
  private boolean skipauth=false;

  
  public JsonRpcServer(Config config)
		throws Exception
  {
    this(config, true);
  }

  public JsonRpcServer(Config config, boolean use_auth)
		throws Exception
  {
		this.config = config;

    config.require("rpc_port");

    if (use_auth)
    {
      config.require("rpc_username");
      config.require("rpc_password");

      auth = new AuthAgent();
    }
    else
    {
      skipauth=true;
      auth = null;
    }

    int listen_port = config.getInt("rpc_port");
    String listen_host = config.getWithDefault("rpc_host", "localhost");

    http_server = HttpServer.create(new InetSocketAddress(listen_host, listen_port), 0);
    http_server.createContext("/", new RootHandler());
    http_server.setExecutor(TaskMaster.getBasicExecutor(8,"json_rpc_server"));
		http_server.start();

    dispatcher = new Dispatcher();
    
    register(new EchoHandler());

  }

  public void register(RequestHandler handler)
  {
    dispatcher.register(handler);
  }


  public class RootHandler implements HttpHandler
  {
    @Override
    public void handle(HttpExchange t) throws IOException {
      ByteArrayOutputStream b_out = new ByteArrayOutputStream();
      PrintStream print_out = new PrintStream(b_out);

      int code = 200;

      boolean auth_ok=false;

      if (skipauth)
      {
        auth_ok=true;
      }
      else
      {
        if (auth.authenticate(t) instanceof Authenticator.Success)
        {
          auth_ok=true;
        }
      }

      if (!auth_ok)
      {
        code=401;
        print_out.println("http basic auth required");
      }
      else
      {

        try
        {
          Scanner scan = new Scanner(t.getRequestBody());
          String line = scan.nextLine();
          scan.close();

          JSONRPC2Request req = JSONRPC2Request.parse(line);
          JSONRPC2Response resp = dispatcher.process(req, null);

			    print_out.println(resp.toJSONString());

        }
        catch(Throwable e)
        {
          code=500;
          print_out.println(e);
         
        }

      }

      byte[] data = b_out.toByteArray();
      t.sendResponseHeaders(code, data.length);
      OutputStream out = t.getResponseBody();
      out.write(data);
      out.close();

    }
  }

  public class AuthAgent extends BasicAuthenticator
  {
    public AuthAgent()
    {
      super("SnowblossomClient");
    }

    @Override
    public boolean checkCredentials(String username, String password)
    {
      if (username == null) return false;
      if (password == null) return false;

      if (!username.equals(config.get("rpc_username"))) return false;
      if (!password.equals(config.get("rpc_password"))) return false;

      return true;
    }

  }

  public class EchoHandler implements RequestHandler
  {
    public String[] handledRequests() 
    {
      return new String[]{"echo"};
    }

		public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) 
    {
      return new JSONRPC2Response(req.getID());
    }

 
  }

}
