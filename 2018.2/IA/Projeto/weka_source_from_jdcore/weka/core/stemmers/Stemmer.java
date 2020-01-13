package weka.core.stemmers;

import java.io.Serializable;
import weka.core.RevisionHandler;

public abstract interface Stemmer
  extends Serializable, RevisionHandler
{
  public abstract String stem(String paramString);
}
