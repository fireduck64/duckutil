package duckutil;

import java.util.TreeMap;
import java.util.Random;


/**
 * Allow for randomly selecting an item using a group if items with some weight
 * Probability of selecting an item is proporational to its weight over the total weight
 * of the set.
 */
public class WeightedRandomSelector<X>
{
  private TreeMap<Double, X> weight_map;

  private double total_weight = 0.0;
  private Random rnd;

  
  public WeightedRandomSelector()
  {
    weight_map = new TreeMap<>();

    rnd = new Random();

  }

  public synchronized void addItem(X x, double weight)
  {
    if (weight == 0.0) return;

    weight_map.put( total_weight, x);

    total_weight += weight;

  }


  public synchronized X selectItem()
  {
    double v = total_weight * rnd.nextDouble();
    if (weight_map.size() == 0) return null;

    return weight_map.floorEntry(v).getValue();


  
  }


}

