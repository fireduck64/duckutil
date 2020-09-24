package duckutil;
/**
 * There are reasons to not use a pair, but sometimes you just need a pair
 */
public class Pair<A extends Comparable, B extends Comparable> implements Comparable<Pair<A, B>>
{
  private final A a;
  private final B b;
  public Pair(A a, B b)
  {
    this.a = a;
    this.b = b;
  }
  public int compareTo(Pair<A,B> other)
  {
    int n = a.compareTo(other.a);
    if (n != 0) return n;
    return b.compareTo(other.b);
  }
  @Override
  public boolean equals(Object O)
  {
    if (O instanceof Pair)
    {
      Pair other = (Pair) O;
      if ((a.equals(other.a)) && (b.equals(other.b)))
      {
        return true;
      }
    }
    return false;
  }
  @Override
  public int hashCode()
  {
    return a.hashCode() * 1134 + b.hashCode();
  }
  public A getA(){return a;}
  public B getB(){return b;}
  @Override
  public String toString()
  {
    return String.format("Pair(%s,%s)", a.toString(), b.toString());
  }
}


