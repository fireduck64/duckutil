package duckutil.jsonrpc;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;

public abstract class JsonRequestHandler implements RequestHandler
{
  private static final Logger logger = Logger.getLogger("jsonrpc_server");
  public abstract String[] handledRequests();

  public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx)
  {
    try
    {
      JSONObject reply = processRequest(req, ctx);

      return new JSONRPC2Response(reply, req.getID());
    }
    catch(Throwable t)
    {
      logger.info("Error in rpc: " + t);
      return new JSONRPC2Response(new JSONRPC2Error(500, t.toString()), req.getID());

    }

  }

  protected abstract JSONObject processRequest(JSONRPC2Request req, MessageContext ctx) throws Exception;

}
