package weka.core;

public abstract interface Drawable
{
  public static final int NOT_DRAWABLE = 0;
  public static final int TREE = 1;
  public static final int BayesNet = 2;
  public static final int Newick = 3;
  
  public abstract int graphType();
  
  public abstract String graph()
    throws Exception;
}
