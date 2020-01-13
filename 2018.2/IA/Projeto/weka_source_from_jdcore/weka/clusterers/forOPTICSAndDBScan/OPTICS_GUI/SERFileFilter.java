package weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

















































public class SERFileFilter
  extends FileFilter
  implements RevisionHandler
{
  private String extension;
  private String description;
  
  public SERFileFilter(String extension, String description)
  {
    this.extension = extension;
    this.description = description;
  }
  






  public boolean accept(File f)
  {
    if (f != null) {
      if (f.isDirectory()) {
        return true;
      }
      
      String filename = f.getName();
      int i = filename.lastIndexOf('.');
      if ((i > 0) && (i < filename.length() - 1)) {
        extension = filename.substring(i + 1).toLowerCase();
      }
      if (extension.equals("ser")) { return true;
      }
    }
    return false;
  }
  



  public String getDescription()
  {
    return description;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}
