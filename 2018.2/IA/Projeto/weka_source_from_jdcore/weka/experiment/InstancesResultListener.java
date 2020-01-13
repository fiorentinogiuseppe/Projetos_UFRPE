package weka.experiment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;





















































public class InstancesResultListener
  extends CSVResultListener
{
  static final long serialVersionUID = -2203808461809311178L;
  protected transient FastVector m_Instances;
  protected transient int[] m_AttributeTypes;
  protected transient Hashtable[] m_NominalIndexes;
  protected transient FastVector[] m_NominalStrings;
  
  public InstancesResultListener()
  {
    File resultsFile;
    try
    {
      resultsFile = File.createTempFile("weka_experiment", ".arff");
      resultsFile.deleteOnExit();
    } catch (Exception e) {
      System.err.println("Cannot create temp file, writing to standard out.");
      resultsFile = new File("-");
    }
    setOutputFile(resultsFile);
    setOutputFileName("");
  }
  




  public String globalInfo()
  {
    return "Outputs the received results in arff format to a Writer. All results must be received before the instances can be written out.";
  }
  








  public void preProcess(ResultProducer rp)
    throws Exception
  {
    m_RP = rp;
    if ((m_OutputFile == null) || (m_OutputFile.getName().equals("-"))) {
      m_Out = new PrintWriter(System.out, true);
    } else {
      m_Out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(m_OutputFile)), true);
    }
    


    Object[] keyTypes = m_RP.getKeyTypes();
    Object[] resultTypes = m_RP.getResultTypes();
    
    m_AttributeTypes = new int[keyTypes.length + resultTypes.length];
    m_NominalIndexes = new Hashtable[m_AttributeTypes.length];
    m_NominalStrings = new FastVector[m_AttributeTypes.length];
    m_Instances = new FastVector();
    
    for (int i = 0; i < m_AttributeTypes.length; i++) {
      Object attribute = null;
      if (i < keyTypes.length) {
        attribute = keyTypes[i];
      } else {
        attribute = resultTypes[(i - keyTypes.length)];
      }
      if ((attribute instanceof String)) {
        m_AttributeTypes[i] = 1;
        m_NominalIndexes[i] = new Hashtable();
        m_NominalStrings[i] = new FastVector();
      } else if ((attribute instanceof Double)) {
        m_AttributeTypes[i] = 0;
      } else {
        throw new Exception("Unknown attribute type in column " + (i + 1));
      }
    }
  }
  







  public void postProcess(ResultProducer rp)
    throws Exception
  {
    if (m_RP != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    String[] keyNames = m_RP.getKeyNames();
    String[] resultNames = m_RP.getResultNames();
    FastVector attribInfo = new FastVector();
    for (int i = 0; i < m_AttributeTypes.length; i++) {
      String attribName = "Unknown";
      if (i < keyNames.length) {
        attribName = "Key_" + keyNames[i];
      } else {
        attribName = resultNames[(i - keyNames.length)];
      }
      
      switch (m_AttributeTypes[i]) {
      case 1: 
        if (m_NominalStrings[i].size() > 0) {
          attribInfo.addElement(new Attribute(attribName, m_NominalStrings[i]));
        }
        else {
          attribInfo.addElement(new Attribute(attribName, (FastVector)null));
        }
        break;
      case 0: 
        attribInfo.addElement(new Attribute(attribName));
        break;
      case 2: 
        attribInfo.addElement(new Attribute(attribName, (FastVector)null));
        break;
      default: 
        throw new Exception("Unknown attribute type");
      }
      
    }
    Instances result = new Instances("InstanceResultListener", attribInfo, m_Instances.size());
    
    for (int i = 0; i < m_Instances.size(); i++) {
      result.add((Instance)m_Instances.elementAt(i));
    }
    
    m_Out.println(new Instances(result, 0));
    for (int i = 0; i < result.numInstances(); i++) {
      m_Out.println(result.instance(i));
    }
    
    if ((m_OutputFile != null) && (!m_OutputFile.getName().equals("-"))) {
      m_Out.close();
    }
  }
  








  public void acceptResult(ResultProducer rp, Object[] key, Object[] result)
    throws Exception
  {
    if (m_RP != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    
    Instance newInst = new Instance(m_AttributeTypes.length);
    for (int i = 0; i < m_AttributeTypes.length; i++) {
      Object val = null;
      if (i < key.length) {
        val = key[i];
      } else {
        val = result[(i - key.length)];
      }
      if (val == null) {
        newInst.setValue(i, Instance.missingValue());
      } else {
        switch (m_AttributeTypes[i]) {
        case 1: 
          String str = (String)val;
          Double index = (Double)m_NominalIndexes[i].get(str);
          if (index == null) {
            index = new Double(m_NominalStrings[i].size());
            m_NominalIndexes[i].put(str, index);
            m_NominalStrings[i].addElement(str);
          }
          newInst.setValue(i, index.doubleValue());
          break;
        case 0: 
          double dou = ((Double)val).doubleValue();
          newInst.setValue(i, dou);
          break;
        default: 
          newInst.setValue(i, Instance.missingValue());
        }
      }
    }
    m_Instances.addElement(newInst);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
}
