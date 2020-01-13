package weka.classifiers.xml;

import weka.classifiers.Classifier;
import weka.core.RevisionUtils;
import weka.core.xml.PropertyHandler;
import weka.core.xml.XMLBasicSerialization;


































public class XMLClassifier
  extends XMLBasicSerialization
{
  public XMLClassifier()
    throws Exception
  {}
  
  public void clear()
    throws Exception
  {
    super.clear();
    

    m_Properties.addAllowed(Classifier.class, "debug");
    m_Properties.addAllowed(Classifier.class, "options");
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}
