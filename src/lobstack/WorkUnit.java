package lobstack;

import duckutil.SimpleFuture;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Assert;

public class WorkUnit
{
  public String mode;
  public WorkUnit(Lobstack stack, NodeEntry ne, TreeMap<Long, ByteBuffer> save_entries)
  {
    this.mode = "PUT";
    this.stack = stack;
    this.ne = ne;
    this.save_entries = save_entries;
  }
  public WorkUnit(Lobstack stack, String prefix, TreeMap<Long, ByteBuffer> save_entries)
  {
    this.mode = "PUT";
    this.stack = stack;
    this.ne = new NodeEntry();
    this.node = new LobstackNode(prefix);
    this.ne.node = true;
    this.save_entries = save_entries;
  }

  public WorkUnit(Lobstack stack, LobstackNode node, int min_file, TreeMap<Long, ByteBuffer> save_entries)
  {
    this.mode = "REPOSITION";
    this.stack = stack;
    this.node = node;
    this.min_file = min_file;
    this.save_entries = save_entries;
  }
  public WorkUnit(Lobstack stack, LobstackNode node, int max_file)
  {
    this.mode = "ESTIMATE_REPOSITION";
    this.stack = stack;
    this.node = node;
    this.max_file = max_file;
  }



  public Lobstack stack;
  public LobstackNode node;
  public TreeMap<Long, ByteBuffer> save_entries;
  public Map<String, NodeEntry> put_map=new TreeMap<String, NodeEntry>();
  public SimpleFuture<NodeEntry> return_entry=new SimpleFuture<NodeEntry>();
  public NodeEntry ne;

  //For reposition
  public int min_file;
  public int max_file;

  public SimpleFuture<TreeMap<Integer, Long>> estimate=new SimpleFuture<>();


  public void assertConsistentForPut()
  {
    if (!ne.node) Assert.assertEquals("Non node, put map should be empty",0,put_map.size());
    if (node==null) Assert.assertTrue("Don't have a node, we must have a loaction", ne.location >= 0);
    for(NodeEntry ne : put_map.values())
    {
      if (ne.node)
      {
        Assert.assertTrue("Only existing nodes added to a node", ne.location>=0);
      }
      //Assert.assertFalse("Should not add nodes to node", ne.node);
    }

  }
  
}
