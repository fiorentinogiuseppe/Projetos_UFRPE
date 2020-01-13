package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;

public abstract interface Saver
  extends Serializable, RevisionHandler
{
  public static final int NONE = 0;
  public static final int BATCH = 1;
  public static final int INCREMENTAL = 2;
  
  public abstract void setDestination(File paramFile)
    throws IOException;
  
  public abstract void setDestination(OutputStream paramOutputStream)
    throws IOException;
  
  public abstract void setRetrieval(int paramInt);
  
  public abstract String getFileExtension()
    throws Exception;
  
  public abstract void setFile(File paramFile)
    throws IOException;
  
  public abstract void setFilePrefix(String paramString)
    throws Exception;
  
  public abstract String filePrefix()
    throws Exception;
  
  public abstract void setDir(String paramString)
    throws IOException;
  
  public abstract void setDirAndPrefix(String paramString1, String paramString2)
    throws IOException;
  
  public abstract String retrieveDir()
    throws IOException;
  
  public abstract void setInstances(Instances paramInstances);
  
  public abstract void writeBatch()
    throws IOException;
  
  public abstract void writeIncremental(Instance paramInstance)
    throws IOException;
  
  public abstract int getWriteMode();
}
