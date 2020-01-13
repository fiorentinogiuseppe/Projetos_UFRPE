package weka.core.converters;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.RevisionUtils;
















































public class SerializedInstancesSaver
  extends AbstractFileSaver
  implements BatchConverter
{
  static final long serialVersionUID = -7717010648500658872L;
  protected ObjectOutputStream m_objectstream;
  
  public SerializedInstancesSaver()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "Serializes the instances to a file with extension bsi.";
  }
  




  public String getFileDescription()
  {
    return "Binary serialized instances";
  }
  



  public void resetOptions()
  {
    super.resetOptions();
    setFileExtension(".bsi");
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  


  public void resetWriter()
  {
    super.resetWriter();
    
    m_objectstream = null;
  }
  




  public void setDestination(OutputStream output)
    throws IOException
  {
    super.setDestination(output);
    
    m_objectstream = new ObjectOutputStream(output);
  }
  



  public void writeBatch()
    throws IOException
  {
    if (getRetrieval() == 2) {
      throw new IOException("Batch and incremental saving cannot be mixed.");
    }
    if (getInstances() == null) {
      throw new IOException("No instances to save");
    }
    setRetrieval(1);
    
    if (m_objectstream == null) {
      throw new IOException("No output for serialization.");
    }
    setWriteMode(0);
    m_objectstream.writeObject(getInstances());
    m_objectstream.flush();
    m_objectstream.close();
    setWriteMode(1);
    resetWriter();
    setWriteMode(2);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4907 $");
  }
  




  public static void main(String[] args)
  {
    runFileSaver(new SerializedInstancesSaver(), args);
  }
}
