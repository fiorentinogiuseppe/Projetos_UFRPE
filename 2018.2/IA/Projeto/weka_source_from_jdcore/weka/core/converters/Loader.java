package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;

public abstract interface Loader
  extends Serializable, RevisionHandler
{
  public static final int NONE = 0;
  public static final int BATCH = 1;
  public static final int INCREMENTAL = 2;
  
  public abstract void setRetrieval(int paramInt);
  
  public abstract void reset()
    throws Exception;
  
  public abstract void setSource(File paramFile)
    throws IOException;
  
  public abstract void setSource(InputStream paramInputStream)
    throws IOException;
  
  public abstract Instances getStructure()
    throws IOException;
  
  public abstract Instances getDataSet()
    throws IOException;
  
  public abstract Instance getNextInstance(Instances paramInstances)
    throws IOException;
}
