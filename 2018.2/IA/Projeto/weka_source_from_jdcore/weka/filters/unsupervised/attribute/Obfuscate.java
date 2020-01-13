package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;




































public class Obfuscate
  extends Filter
  implements UnsupervisedFilter, StreamableFilter
{
  static final long serialVersionUID = -343922772462971561L;
  
  public Obfuscate() {}
  
  public String globalInfo()
  {
    return "A simple instance filter that renames the relation, all attribute names and all nominal (and string) attribute values. For exchanging sensitive datasets. Currently doesn't like string or relational attributes.";
  }
  








  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    

    FastVector v = new FastVector();
    for (int i = 0; i < instanceInfo.numAttributes(); i++) {
      Attribute oldAtt = instanceInfo.attribute(i);
      Attribute newAtt = null;
      switch (oldAtt.type()) {
      case 0: 
        newAtt = new Attribute("A" + (i + 1));
        break;
      case 3: 
        String format = oldAtt.getDateFormat();
        newAtt = new Attribute("A" + (i + 1), format);
        break;
      case 1: 
        FastVector vals = new FastVector();
        for (int j = 0; j < oldAtt.numValues(); j++) {
          vals.addElement("V" + (j + 1));
        }
        newAtt = new Attribute("A" + (i + 1), vals);
        break;
      case 2: 
      case 4: 
      default: 
        newAtt = (Attribute)oldAtt.copy();
        System.err.println("Not converting attribute: " + oldAtt.name());
      }
      
      newAtt.setWeight(oldAtt.weight());
      v.addElement(newAtt);
    }
    Instances newHeader = new Instances("R", v, 10);
    newHeader.setClassIndex(instanceInfo.classIndex());
    setOutputFormat(newHeader);
    return true;
  }
  











  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    push((Instance)instance.copy());
    return true;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 8579 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Obfuscate(), argv);
  }
}
