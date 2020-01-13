package weka.classifiers.pmml.consumer;

import java.io.Serializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.pmml.MappingInfo;
import weka.core.pmml.MiningSchema;
import weka.core.pmml.PMMLModel;
import weka.gui.Logger;






























public abstract class PMMLClassifier
  extends Classifier
  implements Serializable, PMMLModel
{
  private static final long serialVersionUID = -5371600590320702971L;
  protected String m_pmmlVersion = "?";
  

  protected String m_creatorApplication = "?";
  

  protected Logger m_log = null;
  


  protected Instances m_dataDictionary;
  


  protected MiningSchema m_miningSchema;
  

  protected transient MappingInfo m_fieldsMap;
  

  protected transient boolean m_initialized = false;
  






  PMMLClassifier(Instances dataDictionary, MiningSchema miningSchema)
  {
    m_dataDictionary = dataDictionary;
    m_miningSchema = miningSchema;
  }
  




  public void setPMMLVersion(Document doc)
  {
    NodeList tempL = doc.getElementsByTagName("PMML");
    Node pmml = tempL.item(0);
    if (pmml.getNodeType() == 1) {
      String version = ((Element)pmml).getAttribute("version");
      if (version.length() > 0) {
        m_pmmlVersion = version;
      }
    }
  }
  





  public void setCreatorApplication(Document doc)
  {
    NodeList tempL = doc.getElementsByTagName("Header");
    Node header = tempL.item(0);
    if (header.getNodeType() == 1) {
      NodeList appL = ((Element)header).getElementsByTagName("Application");
      if (appL.getLength() > 0) {
        Node app = appL.item(0);
        if (app.getNodeType() == 1) {
          String appName = ((Element)app).getAttribute("name");
          if ((appName != null) && (appName.length() > 0)) {
            String version = ((Element)app).getAttribute("version");
            if ((version != null) && (version.length() > 0)) {
              appName = appName + " v. " + version;
            }
            m_creatorApplication = appName;
          }
        }
      }
    }
  }
  




  public Instances getDataDictionary()
  {
    return m_dataDictionary;
  }
  




  public MiningSchema getMiningSchema()
  {
    return m_miningSchema;
  }
  




  public String getPMMLVersion()
  {
    return m_pmmlVersion;
  }
  





  public String getCreatorApplication()
  {
    return m_creatorApplication;
  }
  




  public void setLog(Logger log)
  {
    m_log = log;
  }
  




  public Logger getLog()
  {
    return m_log;
  }
  




  public void buildClassifier(Instances data)
    throws Exception
  {
    throw new Exception("[PMMLClassifier] PMML models are pre-built and static!");
  }
  








  public void done()
  {
    m_initialized = false;
    m_fieldsMap = null;
  }
  




  public void mapToMiningSchema(Instances dataSet)
    throws Exception
  {
    if (m_fieldsMap == null)
    {
      m_fieldsMap = new MappingInfo(dataSet, m_miningSchema, m_log);
      m_initialized = true;
    }
  }
  






  public String getFieldsMappingString()
  {
    if (!m_initialized) {
      return null;
    }
    return m_fieldsMap.getFieldsMappingString();
  }
}
