package weka.classifiers.bayes.net.search.local;

public abstract interface Scoreable
{
  public static final int BAYES = 0;
  public static final int BDeu = 1;
  public static final int MDL = 2;
  public static final int ENTROPY = 3;
  public static final int AIC = 4;
  
  public abstract double logScore(int paramInt1, int paramInt2);
}
