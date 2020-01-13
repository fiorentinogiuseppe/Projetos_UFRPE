package weka.core.converters;

import java.io.File;
import java.io.IOException;

public abstract interface FileSourcedConverter
{
  public abstract String getFileExtension();
  
  public abstract String[] getFileExtensions();
  
  public abstract String getFileDescription();
  
  public abstract void setFile(File paramFile)
    throws IOException;
  
  public abstract File retrieveFile();
  
  public abstract void setUseRelativePath(boolean paramBoolean);
  
  public abstract boolean getUseRelativePath();
}
