package weka.core.converters;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;













































public class CSVSaver
  extends AbstractFileSaver
  implements BatchConverter, IncrementalConverter, FileSourcedConverter
{
  static final long serialVersionUID = 476636654410701807L;
  
  public CSVSaver()
  {
    resetOptions();
  }
  




  public String globalInfo()
  {
    return "Writes to a destination that is in csv format";
  }
  





  public String getFileDescription()
  {
    return "CSV file: comma separated files";
  }
  



  public void resetOptions()
  {
    super.resetOptions();
    setFileExtension(".csv");
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.STRING_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  




  public void writeIncremental(Instance inst)
    throws IOException
  {
    int writeMode = getWriteMode();
    Instances structure = getInstances();
    PrintWriter outW = null;
    
    if ((getRetrieval() == 1) || (getRetrieval() == 0))
      throw new IOException("Batch and incremental saving cannot be mixed.");
    if (getWriter() != null) {
      outW = new PrintWriter(getWriter());
    }
    if (writeMode == 1) {
      if (structure == null) {
        setWriteMode(2);
        if (inst != null) {
          System.err.println("Structure(Header Information) has to be set in advance");
        }
      } else {
        setWriteMode(3); }
      writeMode = getWriteMode();
    }
    if (writeMode == 2) {
      if (outW != null)
        outW.close();
      cancel();
    }
    if (writeMode == 3) {
      setWriteMode(0);
      
      if ((retrieveFile() == null) && (outW == null))
      {
        for (int i = 0; i < structure.numAttributes(); i++) {
          System.out.print(structure.attribute(i).name());
          if (i < structure.numAttributes() - 1) {
            System.out.print(",");
          } else {
            System.out.println();
          }
        }
      }
      else {
        for (int i = 0; i < structure.numAttributes(); i++) {
          outW.print(structure.attribute(i).name());
          if (i < structure.numAttributes() - 1) {
            outW.print(",");
          } else {
            outW.println();
          }
        }
        outW.flush();
      }
      writeMode = getWriteMode();
    }
    if (writeMode == 0) {
      if (structure == null)
        throw new IOException("No instances information available.");
      if (inst != null)
      {
        if ((retrieveFile() == null) && (outW == null)) {
          System.out.println(inst);
        } else {
          outW.println(instanceToString(inst));
          
          m_incrementalCounter += 1;
          if (m_incrementalCounter > 100) {
            m_incrementalCounter = 0;
            outW.flush();
          }
        }
      }
      else
      {
        if (outW != null) {
          outW.flush();
          outW.close();
        }
        m_incrementalCounter = 0;
        resetStructure();
        outW = null;
        resetWriter();
      }
    }
  }
  


  public void writeBatch()
    throws IOException
  {
    if (getInstances() == null)
      throw new IOException("No instances to save");
    if (getRetrieval() == 2)
      throw new IOException("Batch and incremental saving cannot be mixed.");
    setRetrieval(1);
    setWriteMode(0);
    if ((retrieveFile() == null) && (getWriter() == null))
    {
      for (int i = 0; i < getInstances().numAttributes(); i++) {
        System.out.print(getInstances().attribute(i).name());
        if (i < getInstances().numAttributes() - 1) {
          System.out.print(",");
        } else {
          System.out.println();
        }
      }
      for (int i = 0; i < getInstances().numInstances(); i++) {
        System.out.println(getInstances().instance(i));
      }
      setWriteMode(1);
      return;
    }
    PrintWriter outW = new PrintWriter(getWriter());
    
    for (int i = 0; i < getInstances().numAttributes(); i++) {
      outW.print(Utils.quote(getInstances().attribute(i).name()));
      if (i < getInstances().numAttributes() - 1) {
        outW.print(",");
      } else {
        outW.println();
      }
    }
    for (int i = 0; i < getInstances().numInstances(); i++) {
      outW.println(instanceToString(getInstances().instance(i)));
    }
    outW.flush();
    outW.close();
    setWriteMode(1);
    outW = null;
    resetWriter();
    setWriteMode(2);
  }
  








  protected String instanceToString(Instance inst)
  {
    StringBuffer result = new StringBuffer();
    Instance outInst;
    if ((inst instanceof SparseInstance)) {
      Instance outInst = new Instance(inst.weight(), inst.toDoubleArray());
      outInst.setDataset(inst.dataset());
    }
    else {
      outInst = inst;
    }
    
    for (int i = 0; i < outInst.numAttributes(); i++) {
      if (i > 0)
        result.append(",");
      if (outInst.isMissing(i)) {
        result.append("?");
      } else {
        result.append(outInst.toString(i));
      }
    }
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7950 $");
  }
  




  public static void main(String[] args)
  {
    runFileSaver(new CSVSaver(), args);
  }
}
